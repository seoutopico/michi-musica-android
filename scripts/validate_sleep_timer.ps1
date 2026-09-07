$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$adbPath = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$validationPackage = 'com.ainalluna.michimusica.validation'
$out = Join-Path $repoRoot 'tmp/sleep-timer'
New-Item -ItemType Directory -Force -Path $out | Out-Null
if ((& $adbPath get-state 2>$null) -ne 'device') { throw 'Connect and authorize an Android device first.' }
if ((& $adbPath shell dumpsys window policy) -match 'mIsShowing=true') { throw 'Unlock the Pixel for native validation.' }
& $adbPath install -r (Join-Path $repoRoot 'app/build/outputs/apk/validation/app-validation.apk')
if ($LASTEXITCODE -ne 0) { throw 'Validation APK installation failed.' }
# Only this isolated package and its own synthetic audio are reset.
if ($validationPackage -ne 'com.ainalluna.michimusica.validation') { throw 'Unexpected target package.' }
& $adbPath shell pm clear $validationPackage
& $adbPath shell pm grant $validationPackage android.permission.POST_NOTIFICATIONS
& $adbPath shell am start -n "$validationPackage/com.ainalluna.michimusica.validation.SleepTimerRegressionActivity"
$deadline = [DateTime]::UtcNow.AddMinutes(2)
while ([DateTime]::UtcNow -lt $deadline) {
    $previousPreference = $ErrorActionPreference
    $ErrorActionPreference = 'SilentlyContinue'
    $report = (& $adbPath shell run-as $validationPackage cat files/sleep-report.txt 2>$null) -join "`n"
    $ErrorActionPreference = $previousPreference
    $report | Set-Content (Join-Path $out 'report.txt') -Encoding UTF8
    if ($report.Contains('FAIL ')) { throw $report }
    if ($report.Contains('SLEEP SUITE COMPLETE')) { Write-Output $report; exit 0 }
    Start-Sleep -Milliseconds 300
}
throw 'Sleep timer regression timed out.'
