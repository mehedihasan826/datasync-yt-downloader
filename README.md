# DataSync YT Downloader

A local YouTube music downloader and Apple Music importer for macOS and Windows. 

## What This App Does
This application provides a local web UI, a browser extension, and an optional Telegram bot to queue YouTube videos or playlists for downloading. It leverages `yt-dlp` for downloading high-quality M4A audio, embedding metadata and cover art before moving the final file to your music library. It is built to seamlessly sync music downloaded from secondary machines (e.g. Windows) to a primary master machine (Mac) via Google Drive, and then into Apple Music.

> **DISCLAIMER:** This application is for personal/private use only. The tool must not be used to violate copyright or platform terms. It is designed solely for music/audio that the user has explicit rights or permission to download.

## Recommended Setup

- **Mac** = Master Music Machine
- **Windows** = Secondary downloader
- **Google Drive Desktop** installed on both machines
- **Google Drive** is the shared staging/backup area
- **Mac** automatically imports into Apple Music
- **iPhone sync** happens from Mac Finder/Music

## Folder Lifecycle

- **`Ready`**: Downloaded audio, but not imported by the master machine yet.
- **`Imported`**: Already imported into Apple Music. This serves as a backup and can be safely deleted later.
- **`Failed`**: Failed cases.
- **`Apple Music import folder`**: The actual Apple Music library import path. **Files here are NEVER automatically deleted.**

## Windows Setup (Secondary)

1. Open PowerShell and run the setup script:
   ```powershell
   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
   .\scripts\setup-windows.ps1
   ```
2. The script will install `yt-dlp`, `ffmpeg`, and Java 21 via winget.
3. It will automatically detect your Google Drive path and configure the `.env` file.
4. Run the app:
   ```powershell
   .\scripts\run-windows.ps1
   ```

## Mac Setup (Master)

1. Open Terminal and run the setup script:
   ```bash
   ./scripts/setup-macos.sh
   ```
2. The script will install `yt-dlp`, `ffmpeg`, and Java 21 via Homebrew.
3. It will automatically detect your Google Drive path and configure the `.env` file for the Master role.
4. Run the app:
   ```bash
   ./scripts/run-macos.sh
   ```

## Browser Extension & Features

The custom browser extension is the primary way to interact with the downloader. It seamlessly integrates into your YouTube experience.

1. Go to `chrome://extensions` in your Chromium-based browser (Chrome, Edge, Brave, etc).
2. Enable **Developer mode** in the top right.
3. Click **Load unpacked** and select the `browser-extension` folder.

### Key Extension Features & Buttons:
- **Native YouTube Buttons:** The extension injects two sleek buttons directly into the native YouTube interface next to the Like and Share buttons:
  - **`⬇ DataSync Video`**: Clicking this downloads just the current individual video/song you are watching.
  - **`Mix/Playlist`**: This secondary button automatically appears if you are viewing a YouTube Mix (`start_radio=1`) or a standard Playlist (`list=`). Clicking it queues up the entire playlist/mix for batch processing.
- **SPA Navigation Support:** Because YouTube is a Single Page Application (SPA), the extension actively tracks your URL and updates both buttons dynamically as you click between videos without reloading the page.
- **Live Status Polling:** Clicking either download button will give you real-time feedback directly on the page. The button text actively updates by polling your local backend. You will see these statuses:
  - ⏳ **`Queueing...` / `Queued`**: The backend has received the request and placed it in the processing queue.
  - ⬇️ **`Downloading (XX%)`**: The audio is actively being downloaded, extracted, and post-processed. The percentage reflects `yt-dlp`'s exact progress.
  - ✅ **`Downloaded`**: The download and Apple Music import processes have completely finished.
  - ✅ **`Already Downloaded`**: The duplicate prevention system recognized the video ID inside your `archive.txt`, so `yt-dlp` intentionally skipped downloading it to prevent clutter.
  - ❌ **`Failed`**: The download or import failed (e.g. video unavailable, network crash).
- **Popup Interface:** If you prefer, you can click the extension icon in your browser toolbar to open a clean, dark-themed popup. It intelligently detects your current page (identifying if it's a single video or a playlist) and allows you to easily jump to the Local Web App console.
- **Global CORS Routing:** The extension safely routes all network requests to your local backend (`localhost:8765`) via a background Service Worker, completely bypassing normal browser cross-origin constraints.

## Telegram Setup and Limitation

If `TELEGRAM_ENABLED=true` is set in your `.env`, you can queue downloads by sending YouTube links directly to your Telegram bot. You must specify your Telegram User ID in `TELEGRAM_ALLOWED_USER_IDS` to restrict access.

**Important Limitation:**
The Telegram bot runs locally on your machine alongside the app. **If all your computers are off, this local Telegram bot cannot receive or process messages.** There is no VPS, server, or offline cloud bot implemented.

## iPhone Sync

To sync your downloaded music to your iPhone:
1. First connect your iPhone to the Mac using a cable.
2. Choose "Trust this computer" on your iPhone if prompted.
3. Open Finder on your Mac, select your iPhone in the sidebar, and enable music sync.
4. Enable "Show this iPhone when on Wi-Fi" if desired.
5. In the future, your Mac must be awake and on the same Wi-Fi network to sync wirelessly.

## Duplicate Prevention and Shared Archive

If Google Drive is detected, your setup will automatically set:
`YTDLP_ARCHIVE_FILE=<Google Drive Root>/Music/DataSyncYTDownloader/archive.txt`

- **Shared Across Machines:** This archive is shared by both Mac and Windows.
- **Deduplication:** `yt-dlp` automatically skips videos already listed in this archive. Duplicate skipping is based purely on the YouTube video ID, not the song title.
- **Important Limitations:** 
  - The exact same song uploaded as a different YouTube video ID may still download again.
  - Avoid running the exact same playlist on Windows and Mac at exactly the same time. Google Drive sync is not a real-time database or lock system, and concurrent changes might conflict.

## Cleanup Rules

Over time, your `Imported` backup folder may grow large. You can use the Cleanup button in the Local Web UI to delete old files safely.

- **Imported is just a backup:** The `Imported` folder is only a backup created after Apple Music import. It is perfectly safe to delete old Imported backups after your configured retention days.
- **Safety First:** Cleanup **ONLY** deletes `.m4a` files in the `Imported` backups folder. It **NEVER** deletes Apple Music files, `Ready` files, or `Failed` files.
- **Duplicate Prevention is unaffected:** Deleting Imported backups does not affect your Apple Music library, and it does not affect duplicate prevention.
- **Preserve archive.txt:** The `archive.txt` file should be kept permanently because it is the only thing preventing re-downloading the same YouTube video IDs over and over. Cleanup will never touch your `archive.txt`.
