# Telegram Bot Security Guide

The Telegram bot inside DataSync YT Downloader runs entirely within your local application instance. There is no external server or intermediary service.

## Token Security

> [!CAUTION]
> Your Telegram Bot Token (`TELEGRAM_BOT_TOKEN`) grants full control over your bot.
> - **Never share the token** with anyone.
> - **Never commit it to Git**.
> - Do not include it in bug reports or log files.
> - If you suspect your token has been leaked, open Telegram, message `@BotFather`, and revoke it to generate a new one immediately.

## Access Control

By default, anyone who knows your bot's username can send messages to it. To prevent unauthorized access and stop strangers from initiating downloads on your machine, you **MUST** configure the allowed user IDs list:

- **Allowed User IDs (`TELEGRAM_ALLOWED_USER_IDS`)**: A comma-separated list of numeric Telegram User IDs (e.g. `987654321,123456789`).
- When the bot receives a message, it checks the sender's ID. If it is not in the allowed list, the request is instantly rejected with an "Unauthorized" response and ignored.

To find your Telegram User ID:
1. Search for `@userinfobot` or `@raw_data_bot` in Telegram.
2. Send any message to it.
3. It will reply with your numeric User ID.

## Message Polling
The bot uses Long Polling (`getUpdates`) to receive commands. This means it establishes a secure HTTPS connection to `https://api.telegram.org` and waits for updates.
- The bot **only receives messages** when the DataSync YT Downloader application is running.
- If the application is offline, messages sent to the bot will be queued by Telegram's servers and processed automatically as soon as you start the application again.
- The app stores the last processed message ID in `telegram-last-update-id.txt` in the work directory to ensure messages are not processed multiple times.
