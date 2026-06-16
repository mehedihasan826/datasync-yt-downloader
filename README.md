# 🎵 DataSync YT Downloader

<div align="right">
  <a href="README.md"><img src="https://img.shields.io/badge/Language-English-blue?style=for-the-badge" alt="English"></a>
  <a href="README.ja.md"><img src="https://img.shields.io/badge/Language-日本語-lightgrey?style=for-the-badge" alt="日本語"></a>
</div>

A local YouTube music downloader and Apple Music importer for macOS and Windows. 🎧

## 🌟 What This App Does
This application provides a local web UI, a browser extension, and an optional Telegram bot to queue YouTube videos or playlists for downloading. It leverages `yt-dlp` for downloading high-quality M4A audio, embedding metadata and cover art before moving the final file to your music library. It is built to seamlessly sync music downloaded from secondary machines (e.g., Windows) to a primary master machine (Mac) via Google Drive, and then into Apple Music.

> ⚠️ **DISCLAIMER:** This application is for personal/private use only. The tool must not be used to violate copyright or platform terms. It is designed solely for music/audio that the user has explicit rights or permission to download.

---

## 🏗️ Recommended Architecture

- 🍎 **Mac** = Master Music Machine (Automatically imports to Apple Music & handles iPhone sync)
- 🪟 **Windows** = Secondary downloader (Downloads to shared Google Drive only)
- ☁️ **Google Drive Desktop** = The shared staging/backup area for both machines
- 📱 **iPhone sync** = Happens directly from Mac Finder/Music

### 📂 Folder Lifecycle

- **`Ready`**: 📥 Downloaded audio, pending import by the master machine.
- **`Imported`**: 📦 Temporary backup copy. Kept briefly after Apple Music import, then safely deleted.
- **`Failed`**: ❌ Failed downloads.
- **`Apple Music`**: 🎵 The actual Apple Music library import path on the Master Mac. **Files here are NEVER automatically deleted.**
- **`archive.txt`**: 📜 The permanent source of truth for "already downloaded" videos to prevent duplicates.

---

## ⚙️ Installation & Setup

### 📦 Standalone Graphical Installers (Recommended)

DataSync YT Downloader can be installed as a standalone desktop application. No manual Java or dependencies setup is required.

#### 🍏 macOS Setup
1. Download **`DataSync-YT-Downloader.dmg`** from the [Releases](https://github.com/mehedihasan826/datasync-yt-downloader/releases) page.
2. Double-click the `.dmg` and drag **DataSync YT Downloader.app** to your **Applications** folder.
3. Launch the app from your Applications.

#### 🪟 Windows Setup
1. Download **`DataSyncYTDownloaderSetup.exe`** from the [Releases](https://github.com/mehedihasan826/datasync-yt-downloader/releases) page.
2. Double-click the `.exe` to run the installer and follow the prompt options.
3. Launch the app from your Start Menu.

---

### 🧙 Onboarding Setup Wizard

On first launch, the app starts a local background server (port `8765`) and automatically opens your default web browser to the **Onboarding Setup Wizard** at:
`http://localhost:8765/setup`

The step-by-step wizard will guide you to:
1. **Choose Setup Mode**: Select your role (Simple Local Mac, Simple Local Windows, Mac/Win Master with Shared Google Drive, Secondary Downloader, or Custom).
2. **Auto-Detect Folders**: The app automatically locates your Apple Music Automatically Add folder, Google Drive root, and workspace directories, and validates them.
3. **Configure Telegram Bot (Optional)**: Provide your Bot Token and Allowed User IDs, and test the connection securely in-app.
4. **Setup Browser Extension**: Explains how to load the unpacked extension in developer mode, copies the extension path, and tests connection.
5. **Autostart Preference**: Option to start the app automatically when you log in (configured as a LaunchAgent on macOS, and Startup shortcut on Windows).

*Note: You can rerun the wizard or modify your settings at any time by navigating to `⚙ Settings` in the top right of the dashboard (`http://localhost:8765/settings`).*

---

### 💻 Developer Setup (Running from Source)

### 1️⃣ Prerequisites (Both Machines)
1. Install **[Google Drive Desktop](https://www.google.com/drive/download/)** and sign in. Ensure it's running and your "My Drive" or shared drive is accessible.
2. Install **Git**.

### 2️⃣ Clone the Repository
Open your terminal or command prompt and clone the project:
```bash
git clone https://github.com/mehedihasan826/datasync-yt-downloader.git
cd datasync-yt-downloader
```

### 3️⃣ Windows Setup (Secondary 🪟)
1. Open **PowerShell** as Administrator (or ensure execution policies allow scripts) and run:
   ```powershell
   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
   .\scripts\setup-windows.ps1
   ```
2. The script will install `yt-dlp`, `ffmpeg`, and Java 21 via `winget`.
3. It detects your Google Drive path and creates a `.env` file configured for the secondary role.
4. **Check your `.env`**: Open the `.env` file and ensure paths look correct.
5. Run the app:
   ```powershell
   .\scripts\run-windows.ps1
   ```

### 4️⃣ Mac Setup (Master 🍎)
1. Open **Terminal** and run:
   ```bash
   ./scripts/setup-macos.sh
   ```
2. The script installs `yt-dlp`, `ffmpeg`, and Java 21 via `Homebrew`.
3. It detects your Google Drive path and configures the `.env` file for the Master role.
4. **Check your `.env`**: Open the `.env` file and ensure paths look correct.
5. Run the app:
   ```bash
   ./scripts/run-macos.sh
   ```

---

## 🧩 Browser Extension & Features

The custom browser extension is the primary way to interact with the downloader. It seamlessly integrates into your YouTube experience. 🌐

1. Go to `chrome://extensions` in your Chromium-based browser (Chrome, Edge, Brave, etc).
2. Enable **Developer mode** in the top right.
3. Click **Load unpacked** and select the `browser-extension` folder.

### Key Extension Features & Buttons:
- 🖱️ **Native YouTube Buttons:** The extension injects sleek buttons directly into the native YouTube interface next to the Like and Share buttons:
  - **`⬇ Download`**: Downloads just the current individual video/song.
  - **`Download mix/playlist`**: Automatically appears if you are viewing a YouTube Mix or Playlist. Queues the entire playlist/mix.
- 🔄 **SPA Navigation Support:** Actively tracks your URL and updates buttons dynamically as you click between videos without reloading the page.
- 📊 **Live Status Polling:** See real-time feedback directly on the page!
  - ⏳ **`Queueing...` / `Queued`**: Placed in the processing queue.
  - ⬇️ **`Downloading (XX%)`**: Actively downloading, extracting, and post-processing.
  - ✅ **`Downloaded`**: Successfully downloaded and processed.
  - ✅ **`Already Downloaded`**: Duplicate skipped because the video ID is in `archive.txt`.
  - ❌ **`Failed`**: Download or import failed.
- 🪟 **Popup Interface:** Click the extension icon in your browser toolbar to open a clean, dark-themed popup to easily jump to the Local Web App console.

---

## 📱 Telegram Bot (Optional)

Securely queue downloads from your phone by sending YouTube links to a local Telegram bot running inside your DataSync app on the Master Mac. 🤖

**Setup Steps:**
1. Open Telegram, search for `BotFather`, and send `/newbot`.
2. Follow prompts to get your **Bot Token**.
3. Get your Telegram User ID (e.g., using `@userinfobot`).
4. Update your `.env` file on the Mac:
   ```env
   TELEGRAM_ENABLED=true
   TELEGRAM_BOT_TOKEN=your_token
   TELEGRAM_ALLOWED_USER_IDS=your_user_id
   ```
5. Restart the DataSync app. Send a link or `/start` to your bot!

> 💡 **Note:** The bot runs strictly locally. If your computer is off, it will process queued messages once turned back on.

---

## 🔄 iPhone Sync

To sync your downloaded music to your iPhone:
1. Connect your iPhone to the Mac using a cable.
2. Choose "Trust this computer" on your iPhone if prompted.
3. Open Finder on your Mac, select your iPhone in the sidebar, and enable music sync.
4. Enable "Show this iPhone when on Wi-Fi" to sync wirelessly in the future.

---

## 🛡️ Duplicate Prevention & Cleanup

### 📜 Shared Archive (`archive.txt`)
`YTDLP_ARCHIVE_FILE=<Google Drive Root>/Music/DataSyncYTDownloader/archive.txt`

This file is shared by both Mac and Windows. It is the **permanent duplicate-prevention record**. `yt-dlp` automatically skips videos already listed here.

### 🧹 Cleanup Rules
Over time, your `Imported` backup folder may grow large. 

- **Auto-Cleanup (Mac Master):** By default, `AUTO_CLEAN_IMPORTED_AFTER_MASTER_IMPORT=true` and `KEEP_IMPORTED_BACKUP_DAYS=0`. This means once the Mac successfully imports a song to Apple Music, the Google Drive `Ready` copy is completely deleted instead of being moved to `Imported`. This prevents Windows from needlessly syncing old backup files!
- **Manual Cleanup:** If you have old backups in `Imported`, use the **Cleanup** buttons in the Local Web UI.
- **Safety First:** Cleanup **ONLY** deletes temporary `.m4a` backups in Google Drive. It **NEVER** deletes Apple Music files, `Ready` files, or your `archive.txt`. Duplicate prevention remains 100% intact!
