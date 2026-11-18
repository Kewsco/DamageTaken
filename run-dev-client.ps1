$ErrorActionPreference = 'Stop'

if (-not $env:JAVA_HOME) {
    $javaHome = 'C:\Program Files\BlueJ\jdk'
    if (Test-Path (Join-Path $javaHome 'bin\java.exe')) {
        $env:JAVA_HOME = $javaHome
    }
}

$gradle = Get-Command gradle.bat -ErrorAction SilentlyContinue
if ($gradle) {
    & $gradle.Source run
    exit $LASTEXITCODE
}

$gradle = Get-ChildItem "$HOME\.gradle\wrapper\dists" -Recurse -Filter gradle.bat -ErrorAction SilentlyContinue |
    Sort-Object FullName -Descending |
    Select-Object -First 1

if (-not $gradle) {
    throw 'Gradle was not found. Install Gradle or add gradle.bat to PATH.'
}

& $gradle.FullName run
exit $LASTEXITCODE
