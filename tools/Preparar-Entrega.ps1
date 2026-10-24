$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$proyecto = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$destino = Join-Path (Split-Path -Parent $proyecto) 'PMY2201_Exp1_S1_FormaB_pizzeria-kotlin.zip'
$raiz = Split-Path -Leaf $proyecto
$archivos = @(
    Get-Item -LiteralPath (Join-Path $proyecto '.gitignore')
    Get-Item -LiteralPath (Join-Path $proyecto 'README.md')
    Get-Item -LiteralPath (Join-Path $proyecto 'build.gradle.kts')
    Get-Item -LiteralPath (Join-Path $proyecto 'settings.gradle.kts')
    Get-Item -LiteralPath (Join-Path $proyecto 'gradlew')
    Get-Item -LiteralPath (Join-Path $proyecto 'gradlew.bat')
    foreach ($carpeta in @('src', 'gradle', 'docs', 'tools')) {
        Get-ChildItem -LiteralPath (Join-Path $proyecto $carpeta) -File -Recurse
    }
) | Sort-Object FullName
if (Test-Path -LiteralPath $destino) { Remove-Item -LiteralPath $destino }
$zip = [IO.Compression.ZipFile]::Open($destino, [IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($archivo in $archivos) {
        $relativa = $archivo.FullName.Substring($proyecto.Length + 1).Replace('\', '/')
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $archivo.FullName, "$raiz/$relativa") | Out-Null
    }
} finally { $zip.Dispose() }

$zip = [IO.Compression.ZipFile]::OpenRead($destino)
try {
    if ($zip.Entries.Count -ne $archivos.Count) { throw 'Cantidad de entradas inesperada.' }
    $nombres = @($zip.Entries | ForEach-Object { $_.FullName })
    foreach ($requerido in @('gradlew', 'gradlew.bat', 'gradle/wrapper/gradle-wrapper.jar',
        'gradle/wrapper/gradle-wrapper.properties', 'build.gradle.kts', 'settings.gradle.kts',
        'src/main/kotlin/org/example/Main.kt', 'src/test/kotlin/org/example/PedidoTest.kt',
        'README.md', 'docs/informe-semana-1.md', 'docs/estado-final-semana-1.md')) {
        if ($nombres -notcontains "$raiz/$requerido") { throw "Falta: $requerido" }
    }
    foreach ($entrada in $zip.Entries) {
        if ($entrada.FullName -match '/(\.gradle|\.idea|\.kotlin|build|out|\.git)/') {
            throw "Entrada excluida encontrada: $($entrada.FullName)"
        }
        $relativa = $entrada.FullName.Substring($raiz.Length + 1)
        $original = Join-Path $proyecto $relativa
        $flujo = $entrada.Open()
        try {
            $algoritmo = [Security.Cryptography.SHA256]::Create()
            try { $hashEntrada = [BitConverter]::ToString($algoritmo.ComputeHash($flujo)).Replace('-', '') }
            finally { $algoritmo.Dispose() }
        } finally { $flujo.Dispose() }
        if ($hashEntrada -ne (Get-FileHash -LiteralPath $original -Algorithm SHA256).Hash) {
            throw "El contenido difiere: $relativa"
        }
    }
} finally { $zip.Dispose() }
$hashZip = (Get-FileHash -LiteralPath $destino -Algorithm SHA256).Hash
$informe = @(
    'VALIDACIÓN CORRECTA'
    "Fecha local: $([DateTimeOffset]::Now.ToString('o'))"
    "Archivo: $destino"
    "Entradas verificadas byte a byte mediante SHA-256: $($archivos.Count)"
    'Wrapper completo; sin .gradle, .idea, .kotlin, build, out ni .git.'
    "SHA-256 del ZIP: $hashZip"
    ''
    $nombres
)
$informe | Set-Content -LiteralPath "$destino.validacion.txt" -Encoding UTF8
"$hashZip  $([IO.Path]::GetFileName($destino))" | Set-Content -LiteralPath "$destino.sha256" -Encoding ASCII
$informe
