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

Write-Host "Verifying installed tools..."
$missingTools = $false
try {
    yt-dlp --version | Out-Null
} catch {
    Write-Host "yt-dlp not found in PATH. Searching for it..." -ForegroundColor Yellow
    
    $searchPaths = @(
        "$env:LOCALAPPDATA\Microsoft\WinGet\Packages",
        "C:\Program Files",
        "C:\Program Files (x86)"
    )
    
    $ytdlpExe = $null
    
    foreach ($path in $searchPaths) {
        if (Test-Path $path) {
            $foundYtdlp = Get-ChildItem -Path $path -Filter "yt-dlp.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
            if ($foundYtdlp) {
                $ytdlpExe = $foundYtdlp.FullName
                break
            }
        }
    }
    
    if ($ytdlpExe) {
        Write-Host "Found yt-dlp at: $ytdlpExe" -ForegroundColor Green
        
        $ytdlpBin = (Get-Item $ytdlpExe).Directory.FullName
        $env:Path = "$env:Path;$ytdlpBin"
    } else {
        Write-Host "yt-dlp not found after installation. Please manually install yt-dlp and add it to PATH." -ForegroundColor Red
        $missingTools = $true
    }
}

try {
    ffmpeg -version | Out-Null
} catch {
    Write-Host "ffmpeg not found in PATH. Searching for it..." -ForegroundColor Yellow
    
    $searchPaths = @(
        "$env:LOCALAPPDATA\Microsoft\WinGet\Packages",
        "C:\Program Files",
        "C:\Program Files (x86)"
    )
    
    $ffmpegExe = $null
    $ffprobeExe = $null
    
    foreach ($path in $searchPaths) {
        if (Test-Path $path) {
            $foundFfmpeg = Get-ChildItem -Path $path -Filter "ffmpeg.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
            if ($foundFfmpeg) {
                $ffmpegExe = $foundFfmpeg.FullName
                $ffprobeExe = Join-Path -Path $foundFfmpeg.Directory.FullName -ChildPath "ffprobe.exe"
                if (-not (Test-Path $ffprobeExe)) { $ffprobeExe = $null }
                break
            }
        }
    }
    
    if ($ffmpegExe) {
        Write-Host "Found ffmpeg at: $ffmpegExe" -ForegroundColor Green
        if ($ffprobeExe) { Write-Host "Found ffprobe at: $ffprobeExe" -ForegroundColor Green }
        
        $ffmpegBin = (Get-Item $ffmpegExe).Directory.FullName
        $env:Path = "$env:Path;$ffmpegBin"
    } else {
        Write-Host "ffmpeg not found after installation. Please manually install ffmpeg and add it to PATH." -ForegroundColor Red
        $missingTools = $true
    }
}

if ($missingTools) {
    Write-Host "Please close and reopen PowerShell, then run .\scripts\run-windows.ps1 again." -ForegroundColor Yellow
}

$UserName = [Environment]::UserName
$WorkDir = "C:\Users\$UserName\Music\DataSyncYTDownloaderWork"

if (Test-Path "C:\Users\$UserName\OneDrive") {
    $MusicImportDir = "C:\Users\$UserName\OneDrive\Music\DataSyncYTDownloader\Ready"
} else {
    $MusicImportDir = "C:\Users\$UserName\Music\Apple Music\Media\Automatically Add to Apple Music"
}

Write-Host "Creating .env if missing..."
if (-not (Test-Path .env)) {
    $envContent = Get-Content .env.example -Raw
    $envContent = $envContent -replace 'WORK_DIR=.*', "WORK_DIR=$WorkDir"
    $envContent = $envContent -replace 'MUSIC_IMPORT_DIR=.*', "MUSIC_IMPORT_DIR=$MusicImportDir"
    $envContent = $envContent -replace 'IMPORT_MODE=.*', "IMPORT_MODE=READY_FOLDER"
    
    if ($ytdlpExe) {
        $envContent = $envContent -replace 'YTDLP_BINARY=.*', "YTDLP_BINARY=$ytdlpExe"
    }
    if ($ffmpegExe) {
        $envContent = $envContent -replace 'FFMPEG_BINARY=.*', "FFMPEG_BINARY=$ffmpegExe"
    }
    if ($ffprobeExe) {
        $envContent = $envContent -replace 'FFPROBE_BINARY=.*', "FFPROBE_BINARY=$ffprobeExe"
    }
    
    Set-Content -Path .env -Value $envContent
    Write-Host ".env created with detected paths." -ForegroundColor Yellow
} else {
    Write-Host ".env already exists."
    
    $existingEnv = Get-Content .env -Raw
    $envUpdated = $false
    
    if ($ytdlpExe) {
        $existingEnv = $existingEnv -replace 'YTDLP_BINARY=.*', "YTDLP_BINARY=$ytdlpExe"
        $envUpdated = $true
    }
    if ($ffmpegExe) {
        $existingEnv = $existingEnv -replace 'FFMPEG_BINARY=.*', "FFMPEG_BINARY=$ffmpegExe"
        $envUpdated = $true
    }
    if ($ffprobeExe) {
        $existingEnv = $existingEnv -replace 'FFPROBE_BINARY=.*', "FFPROBE_BINARY=$ffprobeExe"
        $envUpdated = $true
    }
    
    if ($existingEnv -match '/Users/yourname') {
        $existingEnv = $existingEnv -replace 'WORK_DIR=.*', "WORK_DIR=$WorkDir"
        $existingEnv = $existingEnv -replace 'MUSIC_IMPORT_DIR=.*', "MUSIC_IMPORT_DIR=$MusicImportDir"
        $existingEnv = $existingEnv -replace 'IMPORT_MODE=.*', "IMPORT_MODE=READY_FOLDER"
        $envUpdated = $true
    }
    
    if ($envUpdated) {
        Set-Content -Path .env -Value $existingEnv
        Write-Host ".env updated with detected paths." -ForegroundColor Yellow
    }
    
    Write-Host "Recommended paths:" -ForegroundColor Cyan
    Write-Host "WORK_DIR=$WorkDir"
    Write-Host "MUSIC_IMPORT_DIR=$MusicImportDir"
    if ($ytdlpExe) { Write-Host "YTDLP_BINARY=$ytdlpExe" }
    if ($ffmpegExe) { Write-Host "FFMPEG_BINARY=$ffmpegExe" }
    if ($ffprobeExe) { Write-Host "FFPROBE_BINARY=$ffprobeExe" }
}

Write-Host "Ensuring WORK_DIR and READY_FOLDER exist..."
if (-not (Test-Path $WorkDir)) { New-Item -ItemType Directory -Force -Path $WorkDir | Out-Null }
if (-not (Test-Path $MusicImportDir)) { New-Item -ItemType Directory -Force -Path $MusicImportDir | Out-Null }

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
