# DataSync YT Downloader - Browser Extension

This is a Chrome (Manifest V3) extension that allows you to quickly queue YouTube videos or playlists to your local DataSync app.

## How to Install (Manually)

1. Open your Chromium-based browser (Chrome, Edge, Brave, etc.).
2. Navigate to `chrome://extensions/` (or `edge://extensions/`).
3. Turn on **Developer mode** (usually a toggle in the top right corner).
4. Click on the **Load unpacked** button.
5. Select this `browser-extension` folder.
6. The extension is now installed! You can pin it to your toolbar for easy access.

## How it works

When you click on the extension popup while watching a YouTube video, it reads the current active tab URL and sends a POST request to your locally running DataSync app (`http://localhost:8765/api/download`).

If the local app is not running, it will show an error asking you to start the app first.
