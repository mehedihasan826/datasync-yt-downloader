$ErrorActionPreference = "Stop"

# Get installation path or default path
$DefaultPath = "$env:LOCALAPPDATA\DataSyncYTDownloader\DataSyncYTDownloader.exe"
if (-not (Test-Path $DefaultPath)) {
    $DefaultPath = "$env:ProgramFiles\DataSyncYTDownloader\DataSyncYTDownloader.exe"
}

$ExePath = $args[0]
if (-not $ExePath) {
    $ExePath = $DefaultPath
}

if (-not (Test-Path $ExePath)) {
    Write-Host "Warning: Executable not found at $ExePath. Creating shortcut anyway." -ForegroundColor Yellow
}

$StartupFolder = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Startup"
$ShortcutPath = Join-Path $StartupFolder "DataSyncYTDownloader.lnk"

Write-Host "Installing autostart shortcut targeting $ExePath to: $ShortcutPath" -ForegroundColor Cyan

$WshShell = New-Object -ComObject WScript.Shell
$Shortcut = $WshShell.CreateShortcut($ShortcutPath)
$Shortcut.TargetPath = $ExePath
$Shortcut.WorkingDirectory = Split-Path $ExePath
$Shortcut.Description = "Start DataSync YT Downloader at login"
$Shortcut.Save()

Write-Host "Autostart shortcut created successfully." -ForegroundColor Green
