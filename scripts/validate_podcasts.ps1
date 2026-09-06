$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$adbPath = Join-Path $env:LOCALAPPDATA 'Android/Sdk/platform-tools/adb.exe'
$validationPackage = 'com.ainalluna.michimusica.validation'
$entry = "$validationPackage/com.ainalluna.michimusica.validation.PodcastRegressionActivity"
$apk = Join-Path $repoRoot 'app/build/outputs/apk/validation/app-validation.apk'
$reportFolder = Join-Path $repoRoot 'tmp/podcast-regression'
New-Item -ItemType Directory -Force -Path $reportFolder | Out-Null
if ((& $adbPath get-state 2>$null) -ne 'device') { throw 'Connect and authorize an Android device first.' }
if ((& $adbPath shell dumpsys window policy) -match 'mIsShowing=true') { throw 'Unlock the device before running native validation.' }
& $adbPath install -r $apk
if ($LASTEXITCODE -ne 0) { throw 'Validation APK installation failed.' }
# This resets ONLY the dedicated validation package, never the personal app or its folder.
if ($validationPackage -ne 'com.ainalluna.michimusica.validation') { throw 'Unexpected target package.' }
& $adbPath shell pm clear $validationPackage
& $adbPath shell pm grant $validationPackage android.permission.POST_NOTIFICATIONS

function Read-Report {
    # The activity creates this file asynchronously after the first launch.
    $readPreference = $ErrorActionPreference
    $ErrorActionPreference = 'SilentlyContinue'
    $content = (& $adbPath shell run-as $validationPackage cat files/regression-report.txt 2>$null) -join "`n"
    $ErrorActionPreference = $readPreference
    $content | Set-Content (Join-Path $reportFolder 'report.txt') -Encoding UTF8
    return $content
}
function Wait-Report([string]$marker) {
    $deadline = [DateTime]::UtcNow.AddSeconds(60)
    while ([DateTime]::UtcNow -lt $deadline) {
        $content = Read-Report
        if ($content.Contains('FAIL ')) { throw $content }
        if ($content.Contains($marker)) { Write-Output $marker; return }
        Start-Sleep -Milliseconds 200
    }
    throw "Timed out waiting for $marker. $(Read-Report)"
}
function Start-Phase([string]$phase) { & $adbPath shell am start -n $entry --es phase $phase | Out-Null }
Start-Phase 'suite'
Wait-Report 'SUITE COMPLETE'
Start-Phase 'interrupt'
Wait-Report 'READY FOR PROCESS KILL'
& $adbPath shell am force-stop $validationPackage
Start-Phase 'recover'
Wait-Report 'RECOVERY COMPLETE'
Start-Phase 'news-seed'
Wait-Report 'READY FOR SCHEDULED REFRESH'
& $adbPath shell cmd jobscheduler run -f $validationPackage 1200
Start-Phase 'news-check'
Wait-Report 'NEWS INTENT SENT'
& $adbPath shell uiautomator dump /sdcard/michi-validation-ui.xml | Out-Null
$ui = (& $adbPath shell cat /sdcard/michi-validation-ui.xml) -join "`n"
$ui | Set-Content (Join-Path $reportFolder 'news-ui.xml') -Encoding UTF8
if (-not $ui.Contains('Novedades') -or -not $ui.Contains('Últimos 3 días')) { throw 'Notification did not open the expected news screen.' }
Start-Phase 'news-repeat'
Wait-Report 'NEWS COMPLETE'
Read-Report
Write-Output 'Native regression complete. Only synthetic audio in the validation package was used.'
