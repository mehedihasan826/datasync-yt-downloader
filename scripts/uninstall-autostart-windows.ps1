$StartupFolder = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Startup"
$ShortcutPath = Join-Path $StartupFolder "DataSyncYTDownloader.lnk"

if (Test-Path $ShortcutPath) {
    Remove-Item $ShortcutPath -Force
    Write-Host "Autostart shortcut removed successfully." -ForegroundColor Green
} else {
    Write-Host "Autostart shortcut not found." -ForegroundColor Yellow
}
