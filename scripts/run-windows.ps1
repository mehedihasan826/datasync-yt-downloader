$ErrorActionPreference = "Stop"

Write-Host "Starting DataSync YT Downloader..." -ForegroundColor Cyan


java -jar target\datasync-yt-downloader-0.0.1-SNAPSHOT.jar
