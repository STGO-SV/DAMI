param([switch]$VerificarEjecucion)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$proyecto = Split-Path -Parent $PSScriptRoot
$raiz = Split-Path -Leaf $proyecto
$nombreZip = 'PMY2201_Exp1_S3_FormaB_pizzeria-kotlin.zip'
$destino = Join-Path (Split-Path -Parent $proyecto) $nombreZip
$identificador = [Guid]::NewGuid().ToString('N')
$temporal = Join-Path ([IO.Path]::GetTempPath()) "DAMI-S3-academico-$identificador"
$null = New-Item -ItemType Directory -Path $temporal
$candidato = Join-Path $temporal $nombreZip

# Cada ruta nueva requiere una decisión explícita de inclusión.
$rutas = @(
    '.gitignore'
    'build.gradle.kts'
    'settings.gradle.kts'
    'gradlew'
    'gradlew.bat'
    'gradle/wrapper/gradle-wrapper.jar'
    'gradle/wrapper/gradle-wrapper.properties'
    'src/main/kotlin/org/example/Configuracion.kt'
    'src/main/kotlin/org/example/Demostracion.kt'
    'src/main/kotlin/org/example/Entrada.kt'
    'src/main/kotlin/org/example/Inspeccion.kt'
    'src/main/kotlin/org/example/Inventario.kt'
    'src/main/kotlin/org/example/Jornada.kt'
    'src/main/kotlin/org/example/Main.kt'
    'src/main/kotlin/org/example/Pedido.kt'
    'src/main/kotlin/org/example/Producto.kt'
    'src/main/kotlin/org/example/Promocion.kt'
    'src/main/kotlin/org/example/Registro.kt'
    'src/main/kotlin/org/example/Usuario.kt'
    'src/test/kotlin/org/example/ConfirmacionSNTest.kt'
    'src/test/kotlin/org/example/IntegracionSemana3Test.kt'
    'src/test/kotlin/org/example/OperadoresIgualdadTest.kt'
    'src/test/kotlin/org/example/PedidoTest.kt'
    'src/test/kotlin/org/example/PromocionTest.kt'
    'src/test/kotlin/org/example/RecuperacionEntradaTest.kt'
    'src/test/kotlin/org/example/RetroalimentacionSemana1Test.kt'
    'src/test/kotlin/org/example/UsabilidadSemana3Test.kt'
    'src/test/kotlin/org/example/ValidacionSemana3Test.kt'
)
$origenes = @{}
foreach ($ruta in $rutas) { $origenes["$raiz/$ruta"] = Join-Path $proyecto $ruta }
$origenes["$raiz/README.md"] = Join-Path $PSScriptRoot 'entrega-semana-3/README.md'
$origenes["$raiz/docs/informe-semana-3.md"] = Join-Path $PSScriptRoot 'entrega-semana-3/informe-semana-3.md'
$nombres = @($origenes.Keys | Sort-Object)
foreach ($nombre in $nombres) {
    if (-not (Test-Path -LiteralPath $origenes[$nombre] -PathType Leaf)) {
        throw "Falta el archivo académico $nombre ($($origenes[$nombre]))."
    }
}

$zip = [IO.Compression.ZipFile]::Open($candidato, [IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($nombre in $nombres) {
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $origenes[$nombre], $nombre) | Out-Null
    }
} finally { $zip.Dispose() }

$zip = [IO.Compression.ZipFile]::OpenRead($candidato)
try {
    $encontradas = @($zip.Entries | ForEach-Object { $_.FullName })
    if ($encontradas.Count -ne $nombres.Count -or @(Compare-Object $nombres $encontradas -CaseSensitive).Count) {
        throw 'Las rutas del ZIP difieren de la lista de inclusión.'
    }
    $manifiesto = @(foreach ($entrada in $zip.Entries) {
        $flujo = $entrada.Open()
        try { $hashEntrada = (Get-FileHash -InputStream $flujo -Algorithm SHA256).Hash } finally { $flujo.Dispose() }
        $hashOrigen = (Get-FileHash -LiteralPath $origenes[$entrada.FullName] -Algorithm SHA256).Hash
        if ($hashEntrada -ne $hashOrigen) { throw "Contenido diferente: $($entrada.FullName)" }
        "$hashEntrada  $($entrada.FullName)"
    })
} finally { $zip.Dispose() }

$hashZip = (Get-FileHash -LiteralPath $candidato -Algorithm SHA256).Hash
$datosZip = Get-Item -LiteralPath $candidato
"$hashZip  $nombreZip" | Set-Content -LiteralPath "$candidato.sha256" -Encoding ASCII
@(
    'VALIDACIÓN DE CONTENIDO CORRECTA'
    "Archivo: $nombreZip"
    "Generación: $($datosZip.LastWriteTime.ToString('yyyy-MM-dd HH:mm:ss zzz'))"
    "Entradas verificadas por SHA-256: $($nombres.Count)"
    "Tamaño en bytes: $($datosZip.Length)"
    "SHA-256 del ZIP: $hashZip"
    ''
    'SHA-256 por entrada y ruta'
    $manifiesto
) | Set-Content -LiteralPath "$candidato.validacion.txt" -Encoding UTF8

if (-not $VerificarEjecucion) {
    "Candidato con integridad verificada: $candidato"
    'Use -VerificarEjecucion para validar la ejecución y sustituir el ZIP de entrega.'
    return
}

$extraccion = Join-Path $temporal 'extraccion'
[IO.Compression.ZipFile]::ExtractToDirectory($candidato, $extraccion)
$extraido = Join-Path $extraccion $raiz
Push-Location -LiteralPath $extraido
try {
    & .\gradlew.bat clean build --console=plain 2>&1 | Tee-Object -FilePath "$candidato.pruebas.txt"
    if ($LASTEXITCODE -ne 0) { throw 'Falló clean build desde la extracción.' }
    $totalPruebas = 0
    foreach ($reporte in Get-ChildItem -LiteralPath 'build/test-results/test' -Filter 'TEST-*.xml') {
        $suite = ([xml](Get-Content -Raw -LiteralPath $reporte.FullName)).testsuite
        if ([int]$suite.failures -or [int]$suite.errors -or [int]$suite.skipped) {
            throw "Pruebas fallidas u omitidas en $($reporte.Name)."
        }
        $totalPruebas += [int]$suite.tests
    }
    if ($totalPruebas -ne 124) { throw "Se esperaban 124 pruebas y se encontraron $totalPruebas." }

    & .\gradlew.bat installDist --console=plain 2>&1 | Tee-Object -FilePath "$candidato.pruebas.txt" -Append
    if ($LASTEXITCODE -ne 0) { throw 'Falló installDist desde la extracción.' }

    & .\gradlew.bat run --console=plain --args=--demo 2>&1 | Tee-Object -FilePath "$candidato.demostracion.txt"
    if ($LASTEXITCODE -ne 0) { throw 'Falló --demo desde la extracción.' }
    $demostracion = Get-Content -Raw -LiteralPath "$candidato.demostracion.txt"
    if (-not $demostracion.Contains('SIM(DEMO-A+DEMO-B)') -or -not $demostracion.Contains('Resumen final:')) {
        throw 'La salida de --demo está incompleta.'
    }

    $verificador = Join-Path $PSScriptRoot 'Verificar-Consola-Semana3.ps1'
    $evidencias = Join-Path $temporal 'consola'
    & $verificador -DirectorioProyecto $extraido -DirectorioEvidencias $evidencias |
        Tee-Object -FilePath "$candidato.consola.txt"
    $escenarios = @(Get-Content -LiteralPath (Join-Path $evidencias 'verificacion-consola.txt') |
        Where-Object { $_ -match '^CORRECTO:' })
    $comprobaciones = ($escenarios | ForEach-Object {
        [int]([regex]::Match($_, '(\d+) comprobaciones').Groups[1].Value)
    } | Measure-Object -Sum).Sum
    if ($escenarios.Count -ne 23 -or $comprobaciones -ne 185) {
        throw "Resultados inesperados: $($escenarios.Count) escenarios y $comprobaciones comprobaciones."
    }
} finally { Pop-Location }

@(
    'VALIDACIÓN DE EJECUCIÓN CORRECTA'
    "SHA-256 del ZIP: $hashZip"
    "Extracción independiente: $extraido"
    'clean build e installDist: BUILD SUCCESSFUL'
    "Pruebas aprobadas: $totalPruebas. Fallos, errores y omisiones: 0."
    'Demostración integrada: aprobada.'
    "Consola con verificador externo: $($escenarios.Count) escenarios y $comprobaciones comprobaciones."
    "Transcripciones de la ejecución: $evidencias"
) | Set-Content -LiteralPath "$candidato.ejecucion.txt" -Encoding UTF8

# Las copias de entrega se preparan en el mismo volumen que sus destinos.
$archivosEntrega = foreach ($sufijo in @('', '.sha256', '.validacion.txt', '.pruebas.txt',
    '.demostracion.txt', '.consola.txt', '.ejecucion.txt')) {
    [pscustomobject]@{
        Origen = "$candidato$sufijo"
        Destino = "$destino$sufijo"
        Temporal = "$destino$sufijo.$identificador.tmp"
        Respaldo = "$destino$sufijo.$identificador.bak"
        Existia = Test-Path -LiteralPath "$destino$sufijo"
        Sustituido = $false
    }
}
try {
    foreach ($archivo in $archivosEntrega) {
        [IO.File]::Copy($archivo.Origen, $archivo.Temporal)
        if ((Get-FileHash -LiteralPath $archivo.Temporal).Hash -ne (Get-FileHash -LiteralPath $archivo.Origen).Hash) {
            throw "Copia de entrega diferente: $($archivo.Destino)"
        }
    }
    if ((Get-FileHash -LiteralPath $archivosEntrega[0].Temporal).Hash -ne $hashZip) {
        throw 'Cambió el candidato después de validarse.'
    }
    foreach ($archivo in $archivosEntrega) {
        if ($archivo.Existia) {
            [IO.File]::Replace($archivo.Temporal, $archivo.Destino, $archivo.Respaldo)
        } else {
            [IO.File]::Move($archivo.Temporal, $archivo.Destino)
        }
        $archivo.Sustituido = $true
    }
} catch {
    foreach ($archivo in $archivosEntrega | Where-Object Sustituido) {
        if ($archivo.Existia) {
            [IO.File]::Replace($archivo.Respaldo, $archivo.Destino, [NullString]::Value)
        } else {
            Remove-Item -LiteralPath $archivo.Destino
        }
    }
    throw
} finally {
    foreach ($archivo in $archivosEntrega) {
        if (Test-Path -LiteralPath $archivo.Temporal) { Remove-Item -LiteralPath $archivo.Temporal }
    }
}
foreach ($archivo in $archivosEntrega) {
    if (Test-Path -LiteralPath $archivo.Respaldo) { Remove-Item -LiteralPath $archivo.Respaldo }
}

"ZIP académico verificado: $destino"
"Entradas: $($nombres.Count) | Tamaño: $($datosZip.Length) bytes"
"SHA-256: $hashZip"
