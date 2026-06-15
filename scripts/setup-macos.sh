#!/bin/bash
set -e

echo "=== DataSync YT Downloader: macOS Setup ==="

# Check Homebrew
if ! command -v brew &> /dev/null; then
    echo "Homebrew not found. Please install Homebrew first (https://brew.sh/)."
    exit 1
fi

echo "Installing/checking dependencies..."
brew install yt-dlp ffmpeg openjdk@21

WORK_DIR="$HOME/Music/DataSyncYTDownloaderWork"
APPLE_MUSIC_IMPORT_DIR="$HOME/Music/Music/Media/Automatically Add to Music.localized"

# Detect Google Drive
GDRIVE_ROOT=""
for dir in "$HOME/Library/CloudStorage/GoogleDrive-"*"/My Drive" "$HOME/Google Drive" "$HOME/My Drive"; do
    if [ -d "$dir" ]; then
        GDRIVE_ROOT="$dir"
        break
    fi
done

echo "Creating .env if missing..."
if [ ! -f .env ]; then
    cat <<EOF > .env
SERVER_PORT=8765

MACHINE_NAME=mac-main
IS_MASTER_MUSIC_MACHINE=true

WORK_DIR=$WORK_DIR

EOF

    if [ -n "$GDRIVE_ROOT" ]; then
        echo "GOOGLE_DRIVE_ROOT=$GDRIVE_ROOT" >> .env
        echo "SHARED_READY_DIR=$GDRIVE_ROOT/Music/DataSyncYTDownloader/Ready" >> .env
        echo "SHARED_IMPORTED_DIR=$GDRIVE_ROOT/Music/DataSyncYTDownloader/Imported" >> .env
        echo "SHARED_FAILED_DIR=$GDRIVE_ROOT/Music/DataSyncYTDownloader/Failed" >> .env
        echo "SHARED_QUEUE_DIR=$GDRIVE_ROOT/Music/DataSyncYTDownloader/Queue" >> .env
        echo "YTDLP_ARCHIVE_FILE=$GDRIVE_ROOT/Music/DataSyncYTDownloader/archive.txt" >> .env
    else
        echo "GOOGLE_DRIVE_ROOT=" >> .env
        echo "SHARED_READY_DIR=" >> .env
        echo "SHARED_IMPORTED_DIR=" >> .env
        echo "SHARED_FAILED_DIR=" >> .env
        echo "SHARED_QUEUE_DIR=" >> .env
        echo "YTDLP_ARCHIVE_FILE=" >> .env
    fi

    cat <<EOF >> .env

APPLE_MUSIC_IMPORT_DIR=$APPLE_MUSIC_IMPORT_DIR

MASTER_SCAN_INTERVAL_SECONDS=60
CLEANUP_RETENTION_DAYS=30
CLEANUP_MODE=manual

TELEGRAM_ENABLED=false
TELEGRAM_BOT_TOKEN=
TELEGRAM_BOT_USERNAME=
TELEGRAM_ALLOWED_USER_IDS=

MAX_PLAYLIST_ITEMS=50
IMPORT_MODE=SHARED_DRIVE_AND_MASTER_IMPORT

YTDLP_BINARY=yt-dlp
FFMPEG_BINARY=ffmpeg
YTDLP_EMBED_METADATA=true
YTDLP_EMBED_THUMBNAIL=true
KEEP_INFO_JSON=true
EOF
    echo ".env created with detected paths."
else
    echo ".env already exists. Not overwriting."
    echo -e "\033[0;36mRecommended Mac defaults:\033[0m"
    echo "MACHINE_NAME=mac-main"
    echo "IS_MASTER_MUSIC_MACHINE=true"
    echo "WORK_DIR=$WORK_DIR"
    echo "APPLE_MUSIC_IMPORT_DIR=$APPLE_MUSIC_IMPORT_DIR"
    if [ -n "$GDRIVE_ROOT" ]; then
        echo "SHARED_READY_DIR=$GDRIVE_ROOT/Music/DataSyncYTDownloader/Ready"
        echo "YTDLP_ARCHIVE_FILE=$GDRIVE_ROOT/Music/DataSyncYTDownloader/archive.txt"
    fi
fi

echo "Ensuring WORK_DIR exists..."
mkdir -p "$WORK_DIR"

if [ -n "$GDRIVE_ROOT" ]; then
    echo "Ensuring Google Drive shared folders exist..."
    mkdir -p "$GDRIVE_ROOT/Music/DataSyncYTDownloader/Ready"
    mkdir -p "$GDRIVE_ROOT/Music/DataSyncYTDownloader/Imported"
    mkdir -p "$GDRIVE_ROOT/Music/DataSyncYTDownloader/Failed"
    mkdir -p "$GDRIVE_ROOT/Music/DataSyncYTDownloader/Queue"
else
    echo "Google Drive not found. You will be using local-output."
fi

if [ -d "$APPLE_MUSIC_IMPORT_DIR" ] || [ -d "$HOME/Music/Music/Media" ]; then
    mkdir -p "$APPLE_MUSIC_IMPORT_DIR"
fi

echo "Building application with Maven Wrapper..."
if [ ! -f ./mvnw ]; then
    echo "Maven Wrapper is missing."
    exit 1
fi
chmod +x ./mvnw
./mvnw clean package -DskipTests

echo "=== Setup Complete ==="
echo "Next steps:"
echo "1. Review .env."
echo "2. Run the application: ./scripts/run-macos.sh"
