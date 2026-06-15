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

ONEDRIVE_DIR=""
if [ -d "$HOME/OneDrive" ]; then
    ONEDRIVE_DIR="$HOME/OneDrive"
else
    for dir in "$HOME"/Library/CloudStorage/OneDrive-*; do
        if [ -d "$dir" ]; then
            ONEDRIVE_DIR="$dir"
            break
        fi
    done
fi

if [ -n "$ONEDRIVE_DIR" ]; then
    MUSIC_IMPORT_DIR="$ONEDRIVE_DIR/Music/DataSyncYTDownloader/Ready"
else
    MUSIC_IMPORT_DIR="$HOME/Music/DataSyncYTDownloader/Ready"
fi

echo "Creating .env if missing..."
if [ ! -f .env ]; then
    sed -e "s|WORK_DIR=.*|WORK_DIR=$WORK_DIR|" \
        -e "s|MUSIC_IMPORT_DIR=.*|MUSIC_IMPORT_DIR=$MUSIC_IMPORT_DIR|" \
        -e "s|IMPORT_MODE=.*|IMPORT_MODE=READY_FOLDER|" \
        .env.example > .env
    echo ".env created with detected paths."
else
    echo ".env already exists."
    echo -e "\033[0;36mRecommended paths:\033[0m"
    echo "WORK_DIR=$WORK_DIR"
    echo "MUSIC_IMPORT_DIR=$MUSIC_IMPORT_DIR"
fi

echo "Ensuring WORK_DIR and READY_FOLDER exist..."
mkdir -p "$WORK_DIR"
mkdir -p "$MUSIC_IMPORT_DIR"

echo "Building application with Maven Wrapper..."
if [ ! -f ./mvnw ]; then
    echo "Maven Wrapper is missing. Please regenerate the project with Maven Wrapper."
    exit 1
fi
chmod +x ./mvnw
./mvnw clean package -DskipTests

echo "=== Setup Complete ==="
echo "Next steps:"
echo "1. Review .env."
echo "2. Run the application: ./scripts/run-macos.sh"
