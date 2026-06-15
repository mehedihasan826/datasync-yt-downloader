$ErrorActionPreference = "Stop"

Write-Host "Starting DataSync YT Downloader..." -ForegroundColor Cyan

if (Test-Path .env) {
    Get-Content .env | Where-Object { $_ -match '=' -and $_ -notmatch '^#' } | ForEach-Object {
        $name, $value = $_.Split('=', 2)
        [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim())
    }
}

java -jar target\datasync-yt-downloader-0.0.1-SNAPSHOT.jar
