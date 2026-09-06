$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$adbPath = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$validationPackage = 'com.ainalluna.michimusica.validation'
$entry = "$validationPackage/com.ainalluna.michimusica.validation.PodcastRegressionActivity"
$reportFolder = Join-Path $repoRoot 'tmp/youtube-podcasts'
New-Item -ItemType Directory -Force -Path $reportFolder | Out-Null
if ((& $adbPath get-state 2>$null) -ne 'device') { throw 'Connect and authorize an Android device first.' }
if ((& $adbPath shell dumpsys window policy) -match 'mIsShowing=true') { throw 'Unlock the device before running native validation.' }
& $adbPath install -r (Join-Path $repoRoot 'app/build/outputs/apk/validation/app-validation.apk')
if ($LASTEXITCODE -ne 0) { throw 'Validation APK installation failed.' }
# Reset only our separate test app and its private audio. Never clear the personal app.
if ($validationPackage -ne 'com.ainalluna.michimusica.validation') { throw 'Unexpected target package.' }
& $adbPath shell pm clear $validationPackage
& $adbPath shell pm grant $validationPackage android.permission.POST_NOTIFICATIONS
& $adbPath shell am start --activity-clear-top -n $entry --es phase youtube
$deadline = [DateTime]::UtcNow.AddMinutes(20)
$lastReport = ''
while ([DateTime]::UtcNow -lt $deadline) {
    $savedPreference = $ErrorActionPreference
    $ErrorActionPreference = 'SilentlyContinue'
    $report = (& $adbPath shell run-as $validationPackage cat files/regression-report.txt 2>$null) -join "`n"
    $ErrorActionPreference = $savedPreference
    $report | Set-Content (Join-Path $reportFolder 'native-report.txt') -Encoding UTF8
    if ($report -ne $lastReport) { Write-Output $report; $lastReport = $report }
    if ($report.Contains('FAIL ')) { throw $report }
    if ($report.Contains('YOUTUBE COMPLETE')) { exit 0 }
    Start-Sleep -Seconds 2
}
throw 'Timed out waiting for real YouTube download validation.'
