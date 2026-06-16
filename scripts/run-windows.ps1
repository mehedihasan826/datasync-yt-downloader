$ErrorActionPreference = "Stop"

Write-Host "Starting DataSync YT Downloader..." -ForegroundColor Cyan

if (Test-Path .env) {
    Get-Content .env | Where-Object { $_ -match '=' -and $_ -notmatch '^#' } | ForEach-Object {
        $name, $value = $_.Split('=', 2)
        $cleanName = $name.Trim()
        $cleanValue = $value.Trim().Trim('"', "'")
        [Environment]::SetEnvironmentVariable($cleanName, $cleanValue)
    }
}

.\mvnw.cmd spring-boot:run
