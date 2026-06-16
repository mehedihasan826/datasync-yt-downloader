# Privacy Policy

DataSync YT Downloader is designed with user privacy as its top priority.

## Key Principles
1. **100% Local Execution**: The application runs entirely on your local machine. There is no central server, database, or external control plane.
2. **No Data Collection**: We do not collect, track, sell, or share any of your personal data, YouTube links, or download histories.
3. **No Database**: All application state (job listings and duplicate prevention logs) is kept in memory or written directly to flat text files (like `archive.txt`) on your local drive.

## Third-Party Integrations
- **YouTube Downloads**: YouTube audio streams are fetched locally by the `yt-dlp` binary on your computer. Your IP address and request headers are sent directly to YouTube's servers to fetch the media stream, exactly as if you visited the site in a browser.
- **Google Drive Sync**: Shared-drive directory synchronization is handled entirely by the official Google Drive Desktop application. DataSync YT Downloader simply reads and writes to the local folders mounted by Google Drive on your drive.
- **Telegram Bot**: If you enable the Telegram Bot feature, the bot client runs inside the local app process and uses Telegram's long-polling API to fetch messages. The bot token is stored locally in your `.env` file and is never sent to any server other than the official Telegram Bot API endpoints.
- **Browser Extension**: The companion extension only reads the current tab's active YouTube URL when you click the download button, sending it directly to `http://localhost:8765`. It collects no telemetry or analytics.

## Log and Cache Storage
- Local logs are stored in the work directory (e.g. `%APPDATA%\DataSyncYTDownloader\logs` or `~/Library/Application Support/DataSyncYTDownloader/logs`). Logs contain processed YouTube URLs and local file system paths for debugging. You can delete these files manually at any time.
