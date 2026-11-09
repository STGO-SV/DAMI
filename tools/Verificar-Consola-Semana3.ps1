param([string]$DirectorioEvidencias, [string]$Java, [string]$DirectorioProyecto)
$ErrorActionPreference = 'Stop'
if (-not $DirectorioProyecto) { $DirectorioProyecto = Split-Path -Parent $PSScriptRoot }
$proyecto = (Resolve-Path -LiteralPath $DirectorioProyecto).Path
$lib = Join-Path $proyecto 'build/install/pizzeria-kotlin/lib/*'
if (-not (Test-Path -LiteralPath (Split-Path -Parent $lib))) { throw 'Ejecute primero .\gradlew.bat installDist.' }
if (-not $DirectorioEvidencias) { $DirectorioEvidencias = Join-Path $proyecto 'docs/evidencias/semana-3/usabilidad' }
if (-not $Java) {
    $corretto = Join-Path $env:USERPROFILE '.jdks/corretto-21.0.12.1/bin/java.exe'
    $Java = if (Test-Path -LiteralPath $corretto) { $corretto } elseif ($env:JAVA_HOME) {
        Join-Path $env:JAVA_HOME 'bin/java.exe'
    } else { (Get-Command java).Source }
}
New-Item -ItemType Directory -Path $DirectorioEvidencias -Force | Out-Null
$script:comprobaciones = 0
$script:escenarios = 0

function Confirmar-Escenario([string]$Nombre, [string[]]$Entradas, [string[]]$Esperados,
    [string]$Argumentos = '--fecha=2026-11-03', [string[]]$NoEsperados = @()) {
    $configuracion = New-Object System.Diagnostics.ProcessStartInfo
    $configuracion.FileName = $Java
    $configuracion.Arguments = "-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp `"$lib`" org.example.MainKt $Argumentos"
    $configuracion.UseShellExecute = $false
    $configuracion.CreateNoWindow = $true
    $configuracion.RedirectStandardInput = $true
    $configuracion.RedirectStandardOutput = $true
    $configuracion.RedirectStandardError = $true
    $configuracion.StandardOutputEncoding = [Text.Encoding]::UTF8
    $configuracion.StandardErrorEncoding = [Text.Encoding]::UTF8
    $proceso = New-Object System.Diagnostics.Process
    $proceso.StartInfo = $configuracion
    [void]$proceso.Start()
    $salida = $proceso.StandardOutput.ReadToEndAsync()
    $errores = $proceso.StandardError.ReadToEndAsync()
    try {
        $texto = if ($Entradas.Count -gt 0) { ($Entradas -join "`n") + "`n" } else { '' }
        $bytes = [Text.Encoding]::UTF8.GetBytes($texto)
        $proceso.StandardInput.BaseStream.Write($bytes, 0, $bytes.Length)
        $proceso.StandardInput.Close()
        if (-not $proceso.WaitForExit(30000)) { $proceso.Kill(); throw "Tiempo agotado: $Nombre" }
        $resultado = $salida.GetAwaiter().GetResult()
        $errorTexto = $errores.GetAwaiter().GetResult()
        if ($proceso.ExitCode -ne 0 -or $errorTexto) { throw "Falló ${Nombre}: $errorTexto (código $($proceso.ExitCode))" }
        $resultado | Set-Content -LiteralPath (Join-Path $DirectorioEvidencias "$Nombre.txt") -Encoding UTF8
        foreach ($esperado in $Esperados) {
            if (-not $resultado.Contains($esperado)) { throw "${Nombre}: falta '$esperado'" }
        }
        foreach ($noEsperado in $NoEsperados) {
            if ($resultado.Contains($noEsperado)) { throw "${Nombre}: apareció '$noEsperado'" }
        }
        $cantidad = $Esperados.Count + $NoEsperados.Count
        $script:comprobaciones += $cantidad
        $script:escenarios++
        "CORRECTO: $Nombre | código 0 | $cantidad comprobaciones"
    } finally { $proceso.Dispose() }
}

function Confirmar-Promocion([string]$Nombre, [string]$Fecha, [string]$Porcentaje, [bool]$Vigente, [bool]$GanaPromocion) {
    $vigencia = if ($Vigente) { 'vigente' } else { 'no vigente' }
    $porcentajeVisible = if ($Porcentaje -eq '0.15') { 15 } else { 5 }
    $esperados = @(
        "Fecha: $Fecha", "Promoción por fechas: 2026-11-01 a 2026-11-07 (extremos incluidos), $porcentajeVisible %; $vigencia",
        'Teléfono: (+56) 9 1234 5678', 'Subtotal solicitado: $34.000 CLP',
        'Masa=6, Tomate=12, Queso=12, Pepperoni=3', 'Margarita x2, Pepperoni x2'
    )
    if ($GanaPromocion) {
        $esperados += @('Descuento por cantidad: $0 CLP', 'Descuento promocional: $5.100 CLP', 'Base imponible: $28.900 CLP',
            'Impuestos (19 %): $5.491 CLP', 'Total a pagar: $34.391 CLP')
    } else {
        $esperados += @('Descuento por cantidad: $3.400 CLP', 'Descuento promocional: $0 CLP', 'Base imponible: $30.600 CLP',
            'Impuestos (19 %): $5.814 CLP', 'Total a pagar: $36.414 CLP')
    }
    Confirmar-Escenario $Nombre @('1', 'José', 'Centro', '+56912345678', 'cliente@example.com',
        '1', '2', 's', '2', '2', 'n', '', '6', '0') $esperados "--fecha=$Fecha --promocion=2026-11-01,2026-11-07,$Porcentaje"
}

$resultados = @(
    Confirmar-Escenario 'consola-normal-reparto' @(
        '1', 'José', 'Ñuñoa 123', '+56912345678', 'jose@example.com', '1', '4', 'n', 'sin sal',
        '3', 'PED-001', '4', 'PED-001', '5', 'PED-001', '2', '6', '0'
    ) @('Cliente: José', 'Dirección: Ñuñoa 123', 'Teléfono: (+56) 9 1234 5678', 'Fecha: 2026-11-03',
        'Descuento aplicado: $3.200 CLP', 'Impuestos (19 %): $5.472 CLP', 'Total a pagar: $34.272 CLP',
        'Estado: Listo', 'Estado: En camino', 'Estado: Entregado', 'Repartidor asignado: R02 - Diego',
        'Completados (Entregado): 1', 'Masa=6, Tomate=12, Queso=12, Pepperoni=5', 'Notas: sin sal')

    Confirmar-Escenario 'consola-recuperacion' @(
        '1', 'José', 'Ñuñoa 123', '+12 345 678 9012', 'mal', '', 'cliente@example.cl',
        'abc', '1', '101', '0', 'texto', '1', 'n', '', '0'
    ) @('Correo inválido.', 'Cantidad inválida.', 'Pizza inexistente.', 'Correo: cliente@example.cl',
        'Teléfono: +12 (345) 678-9012', 'Cliente: José', 'Dirección: Ñuñoa 123', 'Total a pagar: $9.520 CLP', 'Cantidad: 1')

    Confirmar-Promocion 'consola-promocion-activa' '2026-11-03' '0.15' $true $true
    Confirmar-Promocion 'consola-promocion-inactiva' '2026-11-08' '0.15' $false $false
    Confirmar-Promocion 'consola-promocion-inicio' '2026-11-01' '0.15' $true $true
    Confirmar-Promocion 'consola-promocion-fin' '2026-11-07' '0.15' $true $true
    Confirmar-Promocion 'consola-promocion-dia-anterior' '2026-10-31' '0.15' $false $false
    Confirmar-Promocion 'consola-promocion-menor-que-cantidad' '2026-11-03' '0.05' $true $false

    Confirmar-Escenario 'consola-disponibilidad-mixta' @(
        '1', 'Ana', 'Centro', '223456789', 'ana@example.com', '1', '8', 's', '2', '3', '2', 'n', '', '6', '0'
    ) @('Teléfono: (+56) 2 2345 6789', 'Unidades disponibles para este pedido: 10',
        'Unidades disponibles para este pedido: 2', 'Máximo disponible: 2.', 'Cantidad total de pizzas: 10',
        'Margarita x8, Pepperoni x2', 'Total a pagar: $87.822 CLP', 'Masa=0, Tomate=0, Queso=0, Pepperoni=3')

    Confirmar-Escenario 'consola-agotada-cancelacion' @(
        '1', 'Ana', 'Centro', '956781234', 'ana@example.com', '2', '5', 's', '2', 'C', '6', '0'
    ) @('Pepperoni está agotada para este pedido.', 'Registro del pedido cancelado',
        'Masa=10, Tomate=20, Queso=20, Pepperoni=5', 'Rechazados: 0') '--fecha=2026-11-03' @('Identificador: PED-001')

    Confirmar-Escenario 'consola-eof-correo' @('1', 'Ana', 'Centro', '12345678', 'mal') @(
        'Correo inválido.', 'Registro del pedido cancelado', 'Completados (Entregado): 0',
        'Pendientes (Registrado, En preparación, Listo, En camino): 0', 'Rechazados: 0')

    Confirmar-Escenario 'consola-duplicados' @(
        '1', 'Ana', 'Centro', '12345678', 'ana@example.com', '1', '1', 'n', '',
        '1', 'ANA', 'Otra dirección', '87654321', 'ANA@example.com', '2', '1', 'n', '', '6', '0'
    ) @('Cliente ya registrado: se conserva la primera ficha', 'Dirección: Otra dirección', 'PED-002',
        'Masa=8, Tomate=16, Queso=16, Pepperoni=4', 'Pendientes (Registrado, En preparación, Listo, En camino): 2')

    Confirmar-Escenario 'consola-demostracion' @() @(
        'Correo ''sin-arroba'': inválido', '+12 (345) 678-9012', 'Cantidad 100: válida', 'Cantidad 101: inválida',
        'Margarita: promocionable=true', 'Pepperoni: promocionable=true', 'Producto de referencia: promocionable=false',
        '@Promocionable(Pizza clásica)', '@Promocionable(Pizza con pepperoni)', 'Clase: Producto', 'Clase: Pedido',
        'Propiedades:', 'Métodos:', 'Pedidos combinados mediante +:', 'SIM(DEMO-A+DEMO-B)', 'Total a pagar: $36.414 CLP',
        'Promoción activa: 2026-11-03', 'Promoción inactiva: 2026-11-08', 'Total a pagar: $34.391 CLP',
        'Cliente desestructurado: José | cliente@example.com | +12 (345) 678-9012',
        'Pedido desestructurado: José | Margarita x2, Pepperoni x2 | $36.414 CLP', 'Clientes ==: true',
        'Productos ==: true', 'duplicado agregado=false, fichas=1', 'Resumen final:',
        'Identificador: DEMO-ELEGIBILIDAD', 'Subtotal solicitado: $16.000 CLP', 'Descuento promocional: $1.200 CLP',
        'Base imponible: $14.800 CLP', 'Impuestos (19 %): $2.812 CLP', 'Total a pagar: $17.612 CLP') '--demo'

    Confirmar-Escenario 'consola-menu-demostracion' @('8', '6', '0') @(
        'Demostración integrada', 'Masa=10, Tomate=20, Queso=20, Pepperoni=5', 'Completados (Entregado): 0',
        'Pendientes (Registrado, En preparación, Listo, En camino): 0')

    Confirmar-Escenario 'consola-reabastecer-tasa-cero' @(
        '7', 'Masa', '-1', '7', 'Masa', '1', '7', 'Tomate', '2', '7', 'Queso', '2',
        '1', 'Ana', 'Centro', '12345678', 'ana@example.cl', '1', '11', 'n', '', '6', '0'
    ) @('Datos inválidos: Use un ingrediente y una cantidad positiva.', 'Estado: En preparación',
        'Impuestos (0 %): $0 CLP', 'Total a pagar: $79.200 CLP', 'Masa=0, Tomate=0, Queso=0, Pepperoni=5') '0 --fecha=2026-11-03'

    Confirmar-Escenario 'consola-confirmaciones-reintentos' @(
        '1', 'José', 'Ñuñoa 123', '+56901234567', 'jose@example.com',
        '1', '2', 'quizás', '2', 's', '2', '2', '', 'no', '0', ' N ', 'sin sal', '6', '2', '0'
    ) @('Respuesta inválida. Ingrese S o N.', 'Cliente: José', 'Dirección: Ñuñoa 123',
        'Pizza: Margarita', 'Pizza: Pepperoni', 'Cantidad total de pizzas: 4', 'Notas: sin sal',
        'Total a pagar: $36.414 CLP', 'Masa=6, Tomate=12, Queso=12, Pepperoni=3',
        'PED-001 | José | Margarita x2, Pepperoni x2 | $36.414 CLP | En preparación')

    Confirmar-Escenario 'consola-confirmaciones-negativa' @(
        '1', 'Ana', 'Centro', '12345678', 'ana@example.com', '1', '1', 'abc', 'N', 'sin sal', '6', '0'
    ) @('Respuesta inválida. Ingrese S o N.', 'Cantidad total de pizzas: 1', 'Notas: sin sal',
        'Total a pagar: $9.520 CLP', 'Masa=9, Tomate=18, Queso=18, Pepperoni=5',
        'PED-001 | Ana | Margarita x1 | $9.520 CLP | En preparación')

    Confirmar-Escenario 'consola-confirmaciones-eof' @(
        '1', 'Ana', 'Centro', '12345678', 'ana@example.com', '1', '1', 'n', '',
        '3', 'PED-001', '4', 'PED-001', '6',
        '1', 'José', 'Ñuñoa 123', '+56901234567', 'jose@example.com', '1', '2', 'quizás', 's', '2', '2', 'abc'
    ) @('Respuesta inválida. Ingrese S o N.', 'Registro del pedido cancelado', 'Estado: En camino',
        'Repartidor asignado: R02 - Diego', 'Masa=9, Tomate=18, Queso=18, Pepperoni=5',
        'PED-001 | Ana | Margarita x1 | $9.520 CLP | En camino',
        'Pendientes (Registrado, En preparación, Listo, En camino): 1')

    $sinMenu = @('1 Nuevo pedido')
    Confirmar-Escenario 'consola-configuracion-invalida' @() @('Configuración inválida.') '--promocion=2026-11-07,2026-11-01,0.15' $sinMenu
    Confirmar-Escenario 'consola-fecha-invalida' @() @('Fecha inválida.') '--fecha=2026-02-30' $sinMenu
    Confirmar-Escenario 'consola-promocion-negativa' @() @('Configuración inválida.') '--promocion=2026-11-01,2026-11-07,-0.15' $sinMenu
    Confirmar-Escenario 'consola-promocion-superior-a-uno' @() @('Configuración inválida.') '--promocion=2026-11-01,2026-11-07,1.01' $sinMenu
    Confirmar-Escenario 'consola-promocion-incompleta' @() @('Configuración inválida.') '--promocion=2026-11-01,2026-11-07' $sinMenu
)
$resultados += "TOTAL: $script:escenarios escenarios, $script:comprobaciones comprobaciones de salida, todos con código 0."
$resultados | Set-Content -LiteralPath (Join-Path $DirectorioEvidencias 'verificacion-consola.txt') -Encoding UTF8
$resultados
