$ErrorActionPreference = 'Stop'
$signingDirectory = Join-Path $env:USERPROFILE '.config/credentials/michi-android'
$keyStorePath = Join-Path $signingDirectory 'release.keystore'
$propertiesPath = Join-Path $signingDirectory 'release.properties'
if (Test-Path -LiteralPath $keyStorePath) {
    if (-not (Test-Path -LiteralPath $propertiesPath)) { throw 'Existing key has no signing configuration; do not replace it.' }
    Write-Output 'Existing release signing configuration preserved.'
    exit 0
}
New-Item -ItemType Directory -Force -Path $signingDirectory | Out-Null
$identity = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
& icacls.exe $signingDirectory /inheritance:r /grant:r "${identity}:(OI)(CI)F" | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Could not restrict access to the signing directory.' }
$randomBytes = New-Object byte[] 32
$random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$random.GetBytes($randomBytes)
$random.Dispose()
$password = -join ($randomBytes | ForEach-Object { $_.ToString('x2') })
$passwordPath = Join-Path $signingDirectory 'store-password.txt'
[IO.File]::WriteAllText($passwordPath, $password, [Text.Encoding]::ASCII)
$keytool = Join-Path $env:JAVA_HOME 'bin/keytool.exe'
$ErrorActionPreference = 'Continue' # keytool writes normal progress to stderr on Windows PowerShell.
& $keytool -genkeypair -keystore $keyStorePath -storetype PKCS12 -alias michi-release -keyalg RSA -keysize 3072 -validity 10000 -storepass:file $passwordPath -keypass:file $passwordPath -dname 'CN=Michi Musica, OU=Open Source, O=Michi Musica'
$ErrorActionPreference = 'Stop'
if ($LASTEXITCODE -ne 0) { throw 'Release key generation failed.' }
$config = "storeFile=release.keystore`nstorePassword=$password`nkeyAlias=michi-release`nkeyPassword=$password`n"
[IO.File]::WriteAllText($propertiesPath, $config, [Text.Encoding]::ASCII)
$index = Join-Path $env:USERPROFILE '.config/credentials/INDEX.md'
$entry = "`n## Michi Música Android`n`n- ``michi-android/release.keystore``: firma privada de APK públicas de Michi Música y Michi Iconos.`n- ``michi-android/release.properties`` y ``store-password.txt``: configuración privada de firma. No copiar al repositorio ni a GitHub.`n"
Add-Content -LiteralPath $index -Value $entry -Encoding utf8
Write-Output 'Release key created outside the repository with access restricted to the current Windows user.'
