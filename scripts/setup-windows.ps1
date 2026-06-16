$ErrorActionPreference = "Stop"

Write-Host "=== DataSync YT Downloader: Windows Setup ===" -ForegroundColor Cyan

# Check winget
if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
    Write-Host "winget not found. Please install App Installer from the Microsoft Store." -ForegroundColor Red
    exit 1
}

Write-Host "Installing/checking dependencies via winget..."
winget install --id=yt-dlp.yt-dlp -e --accept-source-agreements --accept-package-agreements
winget install --id=Gyan.FFmpeg -e --accept-source-agreements --accept-package-agreements
winget install --id=EclipseAdoptium.Temurin.21.JDK -e --accept-source-agreements --accept-package-agreements

Write-Host "Refreshing PATH in current session..."
$machinePath = [Environment]::GetEnvironmentVariable("Path", "Machine")
$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
$env:Path = "$machinePath;$userPath"

$UserName = [Environment]::UserName
$WorkDir = "C:\Users\$UserName\Music\DataSyncYTDownloaderWork"

# Detect Google Drive
$GDriveRoot = $null
$searchDirs = @(
    "G:\My Drive",
    "C:\Users\$UserName\My Drive",
    "C:\Users\$UserName\Google Drive",
    "C:\Users\$UserName\Google Drive\My Drive"
)

foreach ($dir in $searchDirs) {
    if (Test-Path $dir) {
        $GDriveRoot = $dir
        break
    }
}

Write-Host ""
Write-Host "Choose setup mode:"
Write-Host "1) Secondary downloader with Google Drive sync [recommended for current user]"
Write-Host "2) Simple local Windows only"
Write-Host "3) Windows master with Google Drive sync"
Write-Host "4) Custom"
$choice = Read-Host "Enter choice [default based on detection]"

$SetupMode = ""
if ($choice -eq "1") {
    $SetupMode = "SECONDARY_DOWNLOADER"
} elseif ($choice -eq "2") {
    $SetupMode = "SIMPLE_LOCAL_WINDOWS"
} elseif ($choice -eq "3") {
    $SetupMode = "WINDOWS_MASTER_WITH_SHARED_DRIVE"
} elseif ($choice -eq "4") {
    $SetupMode = "CUSTOM"
} else {
    if ($GDriveRoot) {
        $SetupMode = "SECONDARY_DOWNLOADER"
    } else {
        $SetupMode = "SIMPLE_LOCAL_WINDOWS"
    }
}

Write-Host "Creating .env if missing..."
if (-not (Test-Path .env)) {
    $envContent = @"
SERVER_PORT=8765

SETUP_MODE=$SetupMode
MACHINE_NAME=windows-secondary
IS_MASTER_MUSIC_MACHINE=false

WORK_DIR=$WorkDir

"@
    
    Set-Content -Path .env -Value $envContent

    if ($GDriveRoot) {
        Add-Content -Path .env -Value "GOOGLE_DRIVE_ROOT=$GDriveRoot"
        Add-Content -Path .env -Value "SHARED_READY_DIR=$GDriveRoot\Music\DataSyncYTDownloader\Ready"
        Add-Content -Path .env -Value "SHARED_IMPORTED_DIR=$GDriveRoot\Music\DataSyncYTDownloader\Imported"
        Add-Content -Path .env -Value "SHARED_FAILED_DIR=$GDriveRoot\Music\DataSyncYTDownloader\Failed"
        Add-Content -Path .env -Value "SHARED_QUEUE_DIR=$GDriveRoot\Music\DataSyncYTDownloader\Queue"
        Add-Content -Path .env -Value "YTDLP_ARCHIVE_FILE=$GDriveRoot\Music\DataSyncYTDownloader\archive.txt"
    } else {
        Add-Content -Path .env -Value "GOOGLE_DRIVE_ROOT="
        Add-Content -Path .env -Value "SHARED_READY_DIR="
        Add-Content -Path .env -Value "SHARED_IMPORTED_DIR="
        Add-Content -Path .env -Value "SHARED_FAILED_DIR="
        Add-Content -Path .env -Value "SHARED_QUEUE_DIR="
        Add-Content -Path .env -Value "YTDLP_ARCHIVE_FILE="
    }
    
    $envRest = @"

APPLE_MUSIC_IMPORT_DIR=

MASTER_SCAN_INTERVAL_SECONDS=60
AUTO_CLEAN_IMPORTED_AFTER_MASTER_IMPORT=true
KEEP_IMPORTED_BACKUP_DAYS=0
CLEANUP_RETENTION_DAYS=30
CLEANUP_MODE=manual

TELEGRAM_ENABLED=false
TELEGRAM_BOT_TOKEN=
TELEGRAM_BOT_USERNAME=
TELEGRAM_ALLOWED_USER_IDS=

MAX_PLAYLIST_ITEMS=50
IMPORT_MODE=SHARED_DRIVE_SECONDARY_ONLY

YTDLP_BINARY=yt-dlp
FFMPEG_BINARY=ffmpeg
YTDLP_EMBED_METADATA=true
YTDLP_EMBED_THUMBNAIL=true
KEEP_INFO_JSON=true
"@
    
    Add-Content -Path .env -Value $envRest
    Write-Host ".env created with detected paths." -ForegroundColor Green
} else {
    Write-Host ".env already exists. Not overwriting." -ForegroundColor Yellow
    Write-Host "Recommended Windows defaults:" -ForegroundColor Cyan
    Write-Host "MACHINE_NAME=windows-secondary"
    Write-Host "IS_MASTER_MUSIC_MACHINE=false"
    Write-Host "WORK_DIR=$WorkDir"
    if ($GDriveRoot) {
        Write-Host "SHARED_READY_DIR=$GDriveRoot\Music\DataSyncYTDownloader\Ready"
        Write-Host "YTDLP_ARCHIVE_FILE=$GDriveRoot\Music\DataSyncYTDownloader\archive.txt"
    }
}

Write-Host "Ensuring WORK_DIR exists..."
if (-not (Test-Path $WorkDir)) { New-Item -ItemType Directory -Force -Path $WorkDir | Out-Null }

if ($GDriveRoot) {
    Write-Host "Ensuring Google Drive shared folders exist..."
    $readyDir = "$GDriveRoot\Music\DataSyncYTDownloader\Ready"
    $importedDir = "$GDriveRoot\Music\DataSyncYTDownloader\Imported"
    $failedDir = "$GDriveRoot\Music\DataSyncYTDownloader\Failed"
    $queueDir = "$GDriveRoot\Music\DataSyncYTDownloader\Queue"
    
    if (-not (Test-Path $readyDir)) { New-Item -ItemType Directory -Force -Path $readyDir | Out-Null }
    if (-not (Test-Path $importedDir)) { New-Item -ItemType Directory -Force -Path $importedDir | Out-Null }
    if (-not (Test-Path $failedDir)) { New-Item -ItemType Directory -Force -Path $failedDir | Out-Null }
    if (-not (Test-Path $queueDir)) { New-Item -ItemType Directory -Force -Path $queueDir | Out-Null }
} else {
    Write-Host "Google Drive not found. You will be using local-output." -ForegroundColor Yellow
}

Write-Host "Building application with Maven Wrapper..."
if (-not (Test-Path .\mvnw.cmd)) {
    Write-Host "Maven Wrapper is missing. Please regenerate the project with Maven Wrapper." -ForegroundColor Red
    exit 1
}
.\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Build failed."
    exit $LASTEXITCODE
}

Write-Host "=== Setup Complete ===" -ForegroundColor Green
Write-Host "Next steps:"
Write-Host "1. Review .env."
Write-Host "2. Run the application: .\scripts\run-windows.ps1"
