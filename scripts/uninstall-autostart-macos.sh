#!/bin/bash
PLIST_PATH="$HOME/Library/LaunchAgents/com.datasync.ytdownloader.plist"

if [ -f "$PLIST_PATH" ]; then
    launchctl unload "$PLIST_PATH" 2>/dev/null || true
    rm -f "$PLIST_PATH"
    echo "DataSync YT Downloader autostart uninstalled successfully."
else
    echo "Autostart configuration not found."
fi
