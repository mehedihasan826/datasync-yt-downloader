$ErrorActionPreference = "Stop"

# Compile project using Maven Wrapper only
Write-Host "Compiling project with Maven Wrapper..." -ForegroundColor Cyan
.\mvnw.cmd clean package -DskipTests

# Clean dist directory
if (Test-Path dist/windows) {
    Remove-Item -Recurse -Force dist/windows
}
New-Item -ItemType Directory -Force -Path dist/windows | Out-Null

$Version = "0.0.1"

# Run jpackage to create Windows EXE Installer
Write-Host "Creating Windows EXE Installer..." -ForegroundColor Cyan
jpackage `
  --type exe `
  --dest dist/windows `
  --name "DataSyncYTDownloader" `
  --input target `
  --main-jar "datasync-yt-downloader-$Version-SNAPSHOT.jar" `
  --main-class com.datasync.ytdownloader.YtDownloaderApplication `
  --vendor "DataSync" `
  --app-version "$Version" `
  --win-menu `
  --win-shortcut `
  --win-dir-chooser

# Rename output to DataSyncYTDownloaderSetup.exe
Get-ChildItem dist/windows/*.exe | Rename-Item -NewName "DataSyncYTDownloaderSetup.exe"

# If WiX toolset (light.exe) is installed, let's also create an MSI
if (Get-Command light -ErrorAction SilentlyContinue) {
    Write-Host "WiX Toolset detected. Creating Windows MSI Installer..." -ForegroundColor Cyan
    jpackage `
      --type msi `
      --dest dist/windows `
      --name "DataSyncYTDownloader" `
      --input target `
      --main-jar "datasync-yt-downloader-$Version-SNAPSHOT.jar" `
      --main-class com.datasync.ytdownloader.YtDownloaderApplication `
      --vendor "DataSync" `
      --app-version "$Version"
} else {
    Write-Host "WiX Toolset not found (light.exe). Skipping MSI installer generation." -ForegroundColor Yellow
}

Write-Host "Windows Packaging complete! Outputs generated in dist/windows/" -ForegroundColor Green
