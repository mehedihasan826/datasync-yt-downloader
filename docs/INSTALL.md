# Installation Guide

This guide explains how to install DataSync YT Downloader on macOS and Windows.

## macOS Installation

### 1. Graphical Installer (.dmg)
1. Download `DataSync-YT-Downloader.dmg` from the releases page.
2. Double-click the `.dmg` file to open it.
3. Drag **DataSync YT Downloader.app** to your **Applications** folder.
4. Open it from your Applications folder.

### 2. Package Manager (Homebrew Cask)
Once the cask is published to your tap:
```bash
brew install --cask datasync-yt-downloader
```

---

## Windows Installation

### 1. Graphical Installer (.exe)
1. Download `DataSyncYTDownloaderSetup.exe` from the releases page.
2. Double-click the `.exe` and follow the onboarding wizard steps (select directory, shortcuts, etc.).
3. Once installation completes, launch it from the Start Menu or desktop shortcut.

### 2. Graphical Installer (.msi)
Double-click `DataSyncYTDownloader.msi` for a silent or corporate installation.

### 3. Package Manager (Winget)
Once submitted to the Winget repository:
```cmd
winget install DataSyncYTDownloader
```

---

## Post-Installation
On the first startup, the application will automatically open your default browser to:
`http://localhost:8765/setup`

Complete the Setup Wizard to prepare your directories and start downloading music!
