#!/bin/bash
set -e

# Target plist location
PLIST_DIR="$HOME/Library/LaunchAgents"
PLIST_PATH="$PLIST_DIR/com.datasync.ytdownloader.plist"
mkdir -p "$PLIST_DIR"

# Determine app executable path.
# Default to /Applications/DataSync YT Downloader.app/Contents/MacOS/DataSync YT Downloader
# If custom path is provided as argument, use that.
APP_PATH="${1:-/Applications/DataSync YT Downloader.app/Contents/MacOS/DataSync YT Downloader}"

cat <<EOF > "$PLIST_PATH"
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.datasync.ytdownloader</string>
    <key>ProgramArguments</key>
    <array>
        <string>$APP_PATH</string>
    </array>
    <key>RunAtLoad</key>
    <true/>
    <key>KeepAlive</key>
    <false/>
</dict>
</plist>
EOF

# Load the agent
launchctl unload "$PLIST_PATH" 2>/dev/null || true
launchctl load "$PLIST_PATH"

echo "DataSync YT Downloader autostart installed successfully targeting: $APP_PATH"
