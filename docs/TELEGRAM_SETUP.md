# Telegram Bot Setup Guide

The built-in Telegram Bot lets you download tracks by sending YouTube links in chat from your phone, even when you are away from your computer.

## Step-by-Step Registration

1. Open Telegram and search for `@BotFather` (ensure it has a blue verification tick).
2. Send the command `/newbot` to start the bot creation process.
3. Choose a display name for your bot (e.g. `My Music Sync Downloader`).
4. Choose a unique username for your bot ending in `bot` (e.g. `MyDataSyncDownloaderBot`).
5. BotFather will reply with a success message containing your **Bot Token** (e.g. `123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ`). Copy this token.

---

## Configuring DataSync

1. Open the DataSync setup wizard or settings page (`http://localhost:8765/settings`).
2. Toggle the **Enable Telegram Bot** checkbox.
3. Paste the **Bot Token** into the Token field.
4. Input your bot's username (without `@`) in the **Bot Username** field.
5. Retrieve your Telegram User ID (see below) and paste it into the **Allowed Telegram User IDs** field.
6. Click **Test Telegram Bot** to verify connectivity.
7. Click **Save Settings** and restart the app.

---

## Getting Your Telegram User ID
For security, your bot will only process links sent by authorized users.
1. Search for `@userinfobot` in Telegram.
2. Send `/start` or any text.
3. It will reply with your numeric ID (e.g. `987654321`). Copy and paste this ID into the allowed list in DataSync settings.
