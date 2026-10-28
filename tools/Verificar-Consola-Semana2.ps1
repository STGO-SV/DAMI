$ErrorActionPreference = 'Stop'
$proyecto = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$lib = Join-Path $proyecto 'build/install/pizzeria-kotlin/lib/*'
if (-not (Test-Path -LiteralPath (Split-Path -Parent $lib))) {
    throw 'Ejecute primero .\gradlew.bat installDist.'
}
$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { (Get-Command java).Source }
$evidencias = Join-Path $proyecto 'docs/evidencias/semana-2/recuperacion-entradas'
New-Item -ItemType Directory -Path $evidencias -Force | Out-Null

function Confirmar-Escenario($nombre, [string[]]$entradas, [string[]]$esperados, [string]$tasa = '0.19') {
    $configuracion = New-Object System.Diagnostics.ProcessStartInfo
    $configuracion.FileName = $java
    $configuracion.Arguments = "-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp `"$lib`" org.example.MainKt $tasa"
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
    $texto = if ($entradas.Count -gt 0) { ($entradas -join "`n") + "`n" } else { '' }
    $bytes = [Text.Encoding]::UTF8.GetBytes($texto)
    $proceso.StandardInput.BaseStream.Write($bytes, 0, $bytes.Length)
    $proceso.StandardInput.Close()
    if (-not $proceso.WaitForExit(30000)) { $proceso.Kill(); throw "Tiempo agotado: $nombre" }
    $resultado = $salida.GetAwaiter().GetResult()
    $errorTexto = $errores.GetAwaiter().GetResult()
    $codigo = $proceso.ExitCode
    $proceso.Dispose()
    $resultado | Set-Content -LiteralPath (Join-Path $evidencias "$nombre.txt") -Encoding UTF8
    if ($codigo -ne 0 -or $errorTexto) { throw "Falló ${nombre}: $errorTexto (código $codigo)" }
    foreach ($esperado in $esperados) {
        if (-not $resultado.Contains($esperado)) { throw "${nombre}: falta '$esperado'" }
    }
    "CORRECTO: $nombre | código $codigo | $($esperados.Count) comprobaciones"
}

Confirmar-Escenario 'consola-lote-mixto' @(
    '1', 'José', 'Ñuñoa 123', '+56901234567', 'jose@example.com',
    '1', '2', 's', '2', '2', 'n', 'sin sal', '2', '6', '0'
) @(
    'Cliente: José', 'Dirección: Ñuñoa 123', 'Subtotal solicitado: $34.000 CLP',
    'Descuento aplicado: $3.400 CLP', 'Impuestos (19 %): $5.814 CLP', 'Total a pagar: $36.414 CLP',
    'Margarita x2, Pepperoni x2', 'Masa=6, Tomate=12, Queso=12, Pepperoni=3'
)
Confirmar-Escenario 'consola-reabastecimiento' @(
    '1', 'Ana', 'Centro', '12345678', 'mal', '1', '11', 'n', '',
    '7', 'Masa', '-1', '7', 'Masa', '1', '7', 'Tomate', '2', '7', 'Queso', '2',
    '1', 'Ana', 'Centro', '12345678', 'ana@example.cl', '1', '11', 'n', '', '2', '6', '0'
) @(
    'Datos inválidos: Use un ingrediente y una cantidad positiva.', 'Estado: Rechazado por falta de inventario',
    'Estado: En preparación', 'Total a pagar: $94.248 CLP', 'Masa=0, Tomate=0, Queso=0, Pepperoni=5',
    'Rechazados: 1'
)
Confirmar-Escenario 'consola-fin-entrada' @('1') @(
    'Registro del pedido cancelado', 'Completados (Entregado): 0', 'Rechazados: 0'
)
Confirmar-Escenario 'consola-tasa-cero' @(
    '1', 'Ana', 'Centro', '12345678', 'ana@example.cl', '1', '4', 'n', '', '0'
) @('Impuestos (0 %): $0 CLP', 'Total a pagar: $28.800 CLP') '0'
Confirmar-Escenario 'consola-tasa-invalida' @() @('Tasa inválida.') '-1'
Confirmar-Escenario 'consola-recuperacion' @(
    '1', 'José', 'Ñuñoa 123', '+56901234567', 'jose@example.com',
    'sdfjn', '-1', '1', '2', 's', 'abc', '2147483648', '2',
    '0', '-2', 'texto', '2147483648', '2', 'n', 'sin sal', '2', '6', '0'
) @(
    'Pizza inexistente. Seleccione una opción válida.', 'Cantidad inválida.',
    'Cliente: José', 'Dirección: Ñuñoa 123', 'Correo: jose@example.com',
    'Margarita x2, Pepperoni x2', 'Total a pagar: $36.414 CLP', 'Masa=6, Tomate=12, Queso=12, Pepperoni=3'
)
