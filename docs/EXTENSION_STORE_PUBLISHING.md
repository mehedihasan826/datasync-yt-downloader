# Browser Extension Store Publishing Plan

Publishing the companion browser extension to the Chrome Web Store and Microsoft Edge Add-ons makes it easier and safer for normal users to install it, avoiding the need for manual "Load unpacked" developer steps.

## Publishing Checklist

### 1. Minimal Permissions
Ensure `manifest.json` requests only the absolute minimum permissions required:
- `activeTab`: To read the URL of the tab currently active when the user clicks the action popup.
- `tabs`: To open new tabs or inspect URLs when checking download state.
- `storage`: To save settings.
- **Host Permissions**:
  - `https://www.youtube.com/*`
  - `https://youtube.com/*`
  - `https://music.youtube.com/*`
  - `http://localhost:8765/*` (for localhost API communication)

### 2. Privacy Policy Declarations
When submitting to the stores, you must provide a Privacy Policy link. Declare that:
- The extension acts as a local companion/controller for the standalone desktop application "DataSync YT Downloader".
- The extension **does not collect, store, or sell** personal data.
- The extension **only transmits** the active YouTube tab URL to the local app running on your computer (`http://localhost:8765`) when you request a download.
- No analytics or third-party tracking scripts are bundled.

### 3. Store Descriptions
- Clearly state that the extension requires the local DataSync desktop application to be running to function.
- Do not claim that the extension bypasses licensing or downloads media itself; describe it as a link-forwarder companion.
- Include step-by-step guides on how the extension talks to the local server.
