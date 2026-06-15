# datasync-yt-downloader

A local YouTube music downloader and Apple Music importer for macOS and Windows. 
This application provides a local web UI, browser extension, and optional Telegram bot to queue YouTube videos or playlists for downloading. It leverages `yt-dlp` for downloading, audio extraction, metadata embedding, and cover art embedding before moving the final file to your music library.

> **DISCLAIMER:** This application is for personal/private use only. The tool must not be used to violate copyright or platform terms. It is designed solely for music/audio that the user has explicit rights or permission to download.

## Features
- Downloads high-quality M4A audio via `yt-dlp`.
- Metadata and Cover Art embedding directly via `yt-dlp` and `ffmpeg`.
- Local Web UI.
- Chrome Browser Extension.
- Optional Telegram Bot for remote queuing.
- Smart duplicate skipping via `yt-dlp` download archive.

## Requirements
- Java 21
- `yt-dlp`
- `ffmpeg`

*(Note: Maven is not required globally as this project uses the Maven Wrapper)*

## Setup Instructions

### 1. Configure Environment Variables
1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Edit `.env` with your paths and optional settings:
   - **TELEGRAM_BOT_TOKEN** (optional): Talk to BotFather on Telegram to create a bot. Set `TELEGRAM_ENABLED=true` to use it.
   - **WORK_DIR**: Temporary processing folder where `yt-dlp` runs and stores the duplicate archive.
   - **MUSIC_IMPORT_DIR**: Final destination folder for M4A files.

### 2. macOS Setup
Run the macOS setup script to install dependencies via Homebrew and build the app:
```bash
./scripts/setup-macos.sh
```

**macOS Apple Music Sync:**
You can set `IMPORT_MODE=AUTO_FOLDER` and `MUSIC_IMPORT_DIR` to your `~/Music/Music/Media/Automatically Add to Music.localized` folder. Apple Music will automatically import any M4A file placed there.

### 3. Windows Setup
Run the Windows setup script (uses `winget` to install dependencies):
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\scripts\setup-windows.ps1
```

**Windows Apple Music Sync:**
- Default `IMPORT_MODE` for Windows is `READY_FOLDER`.
- The app will automatically configure your `MUSIC_IMPORT_DIR` to use your OneDrive Music folder if it exists (`C:\Users\<user>\OneDrive\Music\DataSyncYTDownloader\Ready`), otherwise it will fall back to your local Music folder.
- Final tagged M4A files are saved to this Ready folder.
- You can manually import this folder or files into the Apple Music for Windows application.
- To sync to an iPhone, use the Apple Devices app from the Microsoft Store.

## Running the Application
**macOS:**
```bash
./scripts/run-macos.sh
```

**Windows:**
```powershell
.\scripts\run-windows.ps1
```

## Using the Application
1. **Local Web UI**: Open [http://localhost:8765](http://localhost:8765)
2. **Browser Extension**:
   - Go to `chrome://extensions` in your Chromium-based browser.
   - Enable **Developer mode**.
   - Click **Load unpacked** and select the `browser-extension` folder.
   - Click the extension on any YouTube video page to send it to the downloader.
3. **Telegram Bot**: If enabled, message your bot with YouTube links.

## Security Warning
- **Never commit `.env`!** Keep your API keys and tokens private.

## Troubleshooting
- **yt-dlp not found / ffmpeg not found:** Ensure they are in your system PATH. If the app fails to process audio, close and reopen PowerShell/Terminal, then verify by running `yt-dlp --version` and `ffmpeg -version`.
- **Browser extension cannot connect:** Ensure the local Spring Boot app is running on port 8765.
- **Telegram bot not responding:** Ensure `TELEGRAM_ENABLED=true` and your User ID is in `TELEGRAM_ALLOWED_USER_IDS`.
