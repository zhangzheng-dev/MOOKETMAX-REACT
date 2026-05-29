param(
    [Parameter(Mandatory = $true)]
    [string]$Version,

    [Parameter(Mandatory = $true)]
    [int]$VersionCode,

    [string]$UpdateContent = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = "E:\mooketmax"
$apkSource = Join-Path $repoRoot "mobile\android\app\build\outputs\apk\release\app-release.apk"
$fileName = "mooket-max-$Version-v$VersionCode.apk"
$localNamedApk = Join-Path $env:TEMP $fileName
$remoteDir = "/tmp/mooket-hot-updates"
$remoteApk = "$remoteDir/$fileName"
$sshScript = "C:/Users/zhangzheng/.claude/skills/ssh-skill/scripts/ssh_upload.py"
$sshExecScript = "C:/Users/zhangzheng/.claude/skills/ssh-skill/scripts/ssh_execute.py"

if (!(Test-Path $apkSource)) {
    throw "APK not found: $apkSource"
}

Copy-Item $apkSource $localNamedApk -Force

python $sshExecScript ai-pg-43.139.56.124 "mkdir -p $remoteDir" --timeout 30 --no-daemon
if ($LASTEXITCODE -ne 0) {
    throw "Failed to create remote hot update directory."
}

$env:MSYS_NO_PATHCONV = "1"
python $sshScript ai-pg-43.139.56.124 $localNamedApk $remoteApk
if ($LASTEXITCODE -ne 0) {
    throw "Failed to upload APK."
}

if (![string]::IsNullOrWhiteSpace($UpdateContent)) {
    $localTxt = Join-Path $env:TEMP ("mooket-max-$Version-v$VersionCode.txt")
    $remoteTxt = "$remoteDir/mooket-max-$Version-v$VersionCode.txt"
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($localTxt, $UpdateContent, $utf8NoBom)
    python $sshScript ai-pg-43.139.56.124 $localTxt $remoteTxt
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to upload update content."
    }
}

Write-Host "Hot update uploaded:"
Write-Host "  APK: $remoteApk"
if (![string]::IsNullOrWhiteSpace($UpdateContent)) {
    Write-Host "  Notes: $remoteDir/mooket-max-$Version-v$VersionCode.txt"
}
