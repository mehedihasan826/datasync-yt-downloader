# Security Guidelines

This document outlines the security architecture of DataSync YT Downloader and provides checklists to ensure secure public distribution.

## Security Architecture

1. **Local Ports**: By default, the application binds to `localhost` (port `8765`). It is not exposed to the public internet unless you manually configure reverse proxies.
2. **CORS Policy**: Cross-Origin Resource Sharing (CORS) is explicitly configured in `WebConfig.java` to allow requests only from `http://localhost:8765` and `chrome-extension://*` (to support the companion browser extension).
3. **Secret Storage**: Secret tokens (like `TELEGRAM_BOT_TOKEN`) are stored in the local `.env` configuration file in the user's home folder. The `.env` file should **never** be committed to Git.
4. **Duplicate Prevention**: The `archive.txt` file prevents downloading duplicate tracks. It should be kept safe, as losing it will cause yt-dlp to re-download previously imported tracks if they are queued again.

---

## Production Packaging & Distribution Checklist

To distribute this application publicly without triggering security alerts (like Windows SmartScreen or macOS Gatekeeper), follow this checklist:

### macOS Distribution
- [ ] **Code Signing**: Sign the generated `DataSync YT Downloader.app` bundle using an Apple Developer Certificate (Developer ID Application).
- [ ] **Notarization**: Submit the signed `.app` bundle or `.dmg` installer to Apple's notarization service:
  ```bash
  xcrun altool --notarize-app --primary-bundle-id "com.datasync.ytdownloader" --username "your-apple-id" --password "app-specific-password" --file dist/macos/DataSync-YT-Downloader.dmg
  ```
- [ ] **Stapling**: Once notarized, staple the ticket to the DMG:
  ```bash
  xcrun stapler staple dist/macos/DataSync-YT-Downloader.dmg
  ```
- [ ] **No Sudo Requirement**: Ensure no installation step requests administrator (`sudo`) privileges.

### Windows Distribution
- [ ] **Code Signing**: Sign the generated `DataSyncYTDownloaderSetup.exe` and `.msi` installers using a valid code signing certificate (EV Certificate is preferred to bypass SmartScreen instantly).
  ```powershell
  signtool sign /tr http://timestamp.digicert.com /td sha256 /fd sha256 /a dist/windows/DataSyncYTDownloaderSetup.exe
  ```
- [ ] **Standard Installer Location**: Place the app files in user-space directory `%LocalAppData%\DataSyncYTDownloader` by default, avoiding protected system folders (like `Program Files`) if installed without admin rights.
- [ ] **User-level Autostart**: Prefer writing a Startup shortcut to `%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup` instead of modifying registry keys under HKLM.

### Release Validation
- [ ] **Secrets Audit**: Verify that logs do not print private keys, Telegram tokens, or user paths.
- [ ] **VirusTotal Validation**: Scan the compiled `.exe` or `.dmg` on VirusTotal (https://www.virustotal.com) to verify there are no false-positive flag triggers before public release.
