#!/bin/bash
set -e

# Compile project using Maven Wrapper only
./mvnw clean package -DskipTests

VERSION="0.0.1"

# Target directory
rm -rf dist/macos
mkdir -p dist/macos

echo "Creating macOS App Image..."
jpackage \
  --type app-image \
  --dest dist/macos \
  --name "DataSync YT Downloader" \
  --input target \
  --main-jar datasync-yt-downloader-${VERSION}-SNAPSHOT.jar \
  --main-class com.datasync.ytdownloader.YtDownloaderApplication \
  --mac-package-name "DataSync YT Downloader" \
  --mac-package-identifier com.datasync.ytdownloader \
  --vendor "DataSync" \
  --app-version "${VERSION}"

echo "Creating macOS DMG installer..."
jpackage \
  --type dmg \
  --dest dist/macos \
  --name "DataSync YT Downloader" \
  --app-image "dist/macos/DataSync YT Downloader.app" \
  --mac-package-name "DataSync YT Downloader" \
  --vendor "DataSync" \
  --app-version "${VERSION}"

# Rename dmg to output name specified
mv dist/macos/*.dmg dist/macos/DataSync-YT-Downloader.dmg

echo "macOS Packaging complete! Outputs generated in dist/macos/"
