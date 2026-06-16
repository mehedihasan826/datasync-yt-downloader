# 🎵 DataSync YT Downloader

<div align="right">
  <a href="#en">🇺🇸 English</a> | <a href="#ja">🇯🇵 日本語</a>
</div>

---

<span id="en"></span>
# 🇺🇸 English Version

A local YouTube music downloader and Apple Music importer for macOS and Windows. 🎧

## 🌟 What This App Does
This application provides a local web UI, a browser extension, and an optional Telegram bot to queue YouTube videos or playlists for downloading. It leverages `yt-dlp` for downloading high-quality M4A audio, embedding metadata and cover art before moving the final file to your music library. It is built to seamlessly sync music downloaded from secondary machines (e.g., Windows) to a primary master machine (Mac) via Google Drive, and then into Apple Music.

> ⚠️ **DISCLAIMER:** This application is for personal/private use only. The tool must not be used to violate copyright or platform terms. It is designed solely for music/audio that the user has explicit rights or permission to download.

---

## 🏗️ Recommended Architecture

- 🍎 **Mac** = Master Music Machine (Automatically imports to Apple Music & handles iPhone sync)
- 🪟 **Windows** = Secondary downloader (Downloads to shared Google Drive only)
- ☁️ **Google Drive Desktop** = The shared staging/backup area for both machines
- 📱 **iPhone sync** = Happens directly from Mac Finder/Music

### 📂 Folder Lifecycle

- **`Ready`**: 📥 Downloaded audio, pending import by the master machine.
- **`Imported`**: 📦 Temporary backup copy. Kept briefly after Apple Music import, then safely deleted.
- **`Failed`**: ❌ Failed downloads.
- **`Apple Music`**: 🎵 The actual Apple Music library import path on the Master Mac. **Files here are NEVER automatically deleted.**
- **`archive.txt`**: 📜 The permanent source of truth for "already downloaded" videos to prevent duplicates.

---

## ⚙️ Installation & Setup

### 🎛️ Choose Your Setup Mode

This application adapts to your specific hardware and cloud storage preferences. When running the setup script, you will be prompted to choose a **Setup Mode**:

1. **`MAC_MASTER_WITH_SHARED_DRIVE`** *(Recommended for Mac + Windows)*
   - **Use Case:** You have a Mac as your primary Apple Music library, but also want to queue downloads from a Windows PC.
   - **Behavior:** Requires Google Drive. The Mac monitors Google Drive, imports songs directly into Apple Music, and syncs your iPhone. Windows acts as a secondary downloader.

2. **`SECONDARY_DOWNLOADER`** *(Recommended for Windows secondary)*
   - **Use Case:** A Windows PC (or second Mac) that only downloads songs to Google Drive for the Master Mac to import.
   - **Behavior:** Requires Google Drive. Does NOT import into local Apple Music.

3. **`SIMPLE_LOCAL_MAC`**
   - **Use Case:** You only have one Mac and don't want to use Google Drive.
   - **Behavior:** No Google Drive needed. Downloads go straight into your Mac's Apple Music library.

4. **`SIMPLE_LOCAL_WINDOWS`**
   - **Use Case:** You only have one Windows PC and don't want to use Google Drive.
   - **Behavior:** No Google Drive needed. Downloads go straight into your Windows Apple Music library. *(Note: You may need to manually drag the folder into Apple Music on Windows if auto-import is unreliable).*

5. **`MULTI_MAC_SHARED_DRIVE` / `WINDOWS_MASTER_WITH_SHARED_DRIVE`**
   - Alternative advanced setups for multiple Macs or using Windows as the primary Apple Music library.

6. **`CUSTOM`**
   - Provide your exact paths in the `.env` manually.

---

### 1️⃣ Prerequisites (Both Machines)
1. Install **[Google Drive Desktop](https://www.google.com/drive/download/)** and sign in. Ensure it's running and your "My Drive" or shared drive is accessible.
2. Install **Git**.

### 2️⃣ Clone the Repository
Open your terminal or command prompt and clone the project:
```bash
git clone https://github.com/mehedihasan826/datasync-yt-downloader.git
cd datasync-yt-downloader
```

### 3️⃣ Windows Setup (Secondary 🪟)
1. Open **PowerShell** as Administrator (or ensure execution policies allow scripts) and run:
   ```powershell
   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
   .\scripts\setup-windows.ps1
   ```
2. The script will install `yt-dlp`, `ffmpeg`, and Java 21 via `winget`.
3. It detects your Google Drive path and creates a `.env` file configured for the secondary role.
4. **Check your `.env`**: Open the `.env` file and ensure paths look correct.
5. Run the app:
   ```powershell
   .\scripts\run-windows.ps1
   ```

### 4️⃣ Mac Setup (Master 🍎)
1. Open **Terminal** and run:
   ```bash
   ./scripts/setup-macos.sh
   ```
2. The script installs `yt-dlp`, `ffmpeg`, and Java 21 via `Homebrew`.
3. It detects your Google Drive path and configures the `.env` file for the Master role.
4. **Check your `.env`**: Open the `.env` file and ensure paths look correct.
5. Run the app:
   ```bash
   ./scripts/run-macos.sh
   ```

---

## 🧩 Browser Extension & Features

The custom browser extension is the primary way to interact with the downloader. It seamlessly integrates into your YouTube experience. 🌐

1. Go to `chrome://extensions` in your Chromium-based browser (Chrome, Edge, Brave, etc).
2. Enable **Developer mode** in the top right.
3. Click **Load unpacked** and select the `browser-extension` folder.

### Key Extension Features & Buttons:
- 🖱️ **Native YouTube Buttons:** The extension injects sleek buttons directly into the native YouTube interface next to the Like and Share buttons:
  - **`⬇ Download`**: Downloads just the current individual video/song.
  - **`Download mix/playlist`**: Automatically appears if you are viewing a YouTube Mix or Playlist. Queues the entire playlist/mix.
- 🔄 **SPA Navigation Support:** Actively tracks your URL and updates buttons dynamically as you click between videos without reloading the page.
- 📊 **Live Status Polling:** See real-time feedback directly on the page!
  - ⏳ **`Queueing...` / `Queued`**: Placed in the processing queue.
  - ⬇️ **`Downloading (XX%)`**: Actively downloading, extracting, and post-processing.
  - ✅ **`Downloaded`**: Successfully downloaded and processed.
  - ✅ **`Already Downloaded`**: Duplicate skipped because the video ID is in `archive.txt`.
  - ❌ **`Failed`**: Download or import failed.
- 🪟 **Popup Interface:** Click the extension icon in your browser toolbar to open a clean, dark-themed popup to easily jump to the Local Web App console.

---

## 📱 Telegram Bot (Optional)

Securely queue downloads from your phone by sending YouTube links to a local Telegram bot running inside your DataSync app on the Master Mac. 🤖

**Setup Steps:**
1. Open Telegram, search for `BotFather`, and send `/newbot`.
2. Follow prompts to get your **Bot Token**.
3. Get your Telegram User ID (e.g., using `@userinfobot`).
4. Update your `.env` file on the Mac:
   ```env
   TELEGRAM_ENABLED=true
   TELEGRAM_BOT_TOKEN=your_token
   TELEGRAM_ALLOWED_USER_IDS=your_user_id
   ```
5. Restart the DataSync app. Send a link or `/start` to your bot!

> 💡 **Note:** The bot runs strictly locally. If your computer is off, it will process queued messages once turned back on.

---

## 🔄 iPhone Sync

To sync your downloaded music to your iPhone:
1. Connect your iPhone to the Mac using a cable.
2. Choose "Trust this computer" on your iPhone if prompted.
3. Open Finder on your Mac, select your iPhone in the sidebar, and enable music sync.
4. Enable "Show this iPhone when on Wi-Fi" to sync wirelessly in the future.

---

## 🛡️ Duplicate Prevention & Cleanup

### 📜 Shared Archive (`archive.txt`)
`YTDLP_ARCHIVE_FILE=<Google Drive Root>/Music/DataSyncYTDownloader/archive.txt`

This file is shared by both Mac and Windows. It is the **permanent duplicate-prevention record**. `yt-dlp` automatically skips videos already listed here.

### 🧹 Cleanup Rules
Over time, your `Imported` backup folder may grow large. 

- **Auto-Cleanup (Mac Master):** By default, `AUTO_CLEAN_IMPORTED_AFTER_MASTER_IMPORT=true` and `KEEP_IMPORTED_BACKUP_DAYS=0`. This means once the Mac successfully imports a song to Apple Music, the Google Drive `Ready` copy is completely deleted instead of being moved to `Imported`. This prevents Windows from needlessly syncing old backup files!
- **Manual Cleanup:** If you have old backups in `Imported`, use the **Cleanup** buttons in the Local Web UI.
- **Safety First:** Cleanup **ONLY** deletes temporary `.m4a` backups in Google Drive. It **NEVER** deletes Apple Music files, `Ready` files, or your `archive.txt`. Duplicate prevention remains 100% intact!

---

<span id="ja"></span>
# 🇯🇵 日本語版

macOSおよびWindows向けのローカルYouTube音楽ダウンローダーおよびApple Musicインポーター。🎧

## 🌟 主な機能
このアプリケーションは、ローカルWeb UI、ブラウザ拡張機能、およびオプションのTelegramボットを提供し、YouTubeの動画やプレイリストをダウンロードキューに追加できます。`yt-dlp`を使用して高品質のM4Aオーディオをダウンロードし、メタデータとカバーアートを埋め込んでから、最終的なファイルをミュージックライブラリに移動します。Google Driveを経由して、セカンダリマシン（例：Windows）からプライマリマスターマシン（Mac）へダウンロードした音楽をシームレスに同期し、Apple Musicに取り込めるように設計されています。

> ⚠️ **免責事項:** 本アプリケーションは個人利用・私的使用のみを目的としています。著作権やプラットフォームの利用規約に違反する用途で使用しないでください。ユーザーがダウンロードする明示的な権利または許可を持っている音楽/オーディオのみを対象として設計されています。

---

## 🏗️ 推奨アーキテクチャ

- 🍎 **Mac** = マスターミュージックマシン（Apple Musicへの自動インポートおよびiPhone同期を担当）
- 🪟 **Windows** = セカンダリダウンローダー（共有Google Driveへのダウンロードのみ）
- ☁️ **Google Drive Desktop** = 両方のマシンで共有する一時保存/バックアップ領域
- 📱 **iPhone同期** = MacのFinderまたはミュージックアプリから直接行います

### 📂 フォルダのライフサイクル

- **`Ready`**: 📥 ダウンロード済みのオーディオ。マスターマシンによるインポート待ち状態。
- **`Imported`**: 📦 一時的なバックアップコピー。Apple Musicへのインポート後に一時的に保持され、その後安全に削除されます。
- **`Failed`**: ❌ ダウンロードに失敗したファイル。
- **`Apple Music`**: 🎵 マスターMac上の実際のApple Musicライブラリのインポートパス。**ここにあるファイルが自動的に削除されることはありません。**
- **`archive.txt`**: 📜 重複ダウンロードを防ぐための「ダウンロード済み」動画の永続的なソースオブトゥルース（信頼できる情報源）。

---

## ⚙️ インストールとセットアップ

### 🎛️ セットアップモードの選択

このアプリケーションは、お使いのハードウェアやクラウドストレージの好みに適応します。セットアップスクリプトを実行すると、以下の**セットアップモード**を選択するよう求められます：

1. **`MAC_MASTER_WITH_SHARED_DRIVE`** *(Mac + Windows構成での推奨設定)*
   - **ユースケース:** MacをプライマリのApple Musicライブラリとして使用しているが、Windows PCからもダウンロードキューに追加したい場合。
   - **挙動:** Google Driveが必要です。MacがGoogle Driveを監視し、曲をApple Musicに直接インポートし、iPhoneと同期します。Windowsはセカンダリダウンローダーとして動作します。

2. **`SECONDARY_DOWNLOADER`** *(Windowsセカンダリでの推奨設定)*
   - **ユースケース:** マスターMacがインポートできるように、Google Driveへの曲のダウンロードのみを行うWindows PC（あるいは2台目のMac）。
   - **挙動:** Google Driveが必要です。ローカルのApple Musicへのインポートは行いません。

3. **`SIMPLE_LOCAL_MAC`**
   - **ユースケース:** Macを1台だけ所有しており、Google Driveを使用したくない場合。
   - **挙動:** Google Driveは不要です。ダウンロードされた曲はMacのApple Musicライブラリに直接保存されます。

4. **`SIMPLE_LOCAL_WINDOWS`**
   - **ユースケース:** Windows PCを1台だけ所有しており、Google Driveを使用したくない場合。
   - **挙動:** Google Driveは不要です。ダウンロードされた曲はWindowsのApple Musicライブラリに直接保存されます。*(注意: 自動インポートが不安定な場合は、Windows上のApple Musicにフォルダを手動でドラッグ＆ドロップする必要がある場合があります)。*

5. **`MULTI_MAC_SHARED_DRIVE` / `WINDOWS_MASTER_WITH_SHARED_DRIVE`**
   - 複数のMacを使用する場合、またはWindowsをプライマリのApple Musicライブラリとして使用する場合などの高度な代替設定。

6. **`CUSTOM`**
   - `.env`ファイルに独自のパスを手動で指定します。

---

### 1️⃣ 前提条件（両方のマシン）
1. **[Google Drive デスクトップ](https://www.google.com/drive/download/)**をインストールし、サインインします。起動していること、および「マイドライブ」または共有ドライブにアクセスできることを確認してください。
2. **Git**をインストールします。

### 2️⃣ リポジトリのクローン
ターミナルまたはコマンドプロンプトを開き、プロジェクトをクローンします：
```bash
git clone https://github.com/mehedihasan826/datasync-yt-downloader.git
cd datasync-yt-downloader
```

### 3️⃣ Windowsのセットアップ（セカンダリ 🪟）
1. **PowerShell**を管理者として実行（またはスクリプトの実行許可ポリシーを確認）し、以下を実行します：
   ```powershell
   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
   .\scripts\setup-windows.ps1
   ```
2. スクリプトにより、`winget`経由で`yt-dlp`、`ffmpeg`、およびJava 21がインストールされます。
3. Google Driveのパスが自動検出され、セカンダリの役割に構成された`.env`ファイルが作成されます。
4. **`.env`の確認**: `.env`ファイルを開き、パスが正しいことを確認します。
5. アプリケーションの実行：
   ```powershell
   .\scripts\run-windows.ps1
   ```

### 4️⃣ Macのセットアップ（マスター 🍎）
1. **ターミナル**を開き、以下を実行します：
   ```bash
   ./scripts/setup-macos.sh
   ```
2. スクリプトにより、`Homebrew`経由で`yt-dlp`、`ffmpeg`、およびJava 21がインストールされます。
3. Google Driveのパスが自動検出され、マスターの役割に構成された`.env`ファイルが作成されます。
4. **`.env`の確認**: `.env`ファイルを開き、パスが正しいことを確認します。
5. アプリケーションの実行：
   ```bash
   ./scripts/run-macos.sh
   ```

---

## 🧩 ブラウザ拡張機能と機能

カスタムブラウザ拡張機能は、ダウンローダーと対話する主な方法です。YouTubeのブラウジング体験にシームレスに統合されます。🌐

1. Chromiumベースのブラウザ（Chrome、Edge、Braveなど）で `chrome://extensions` を開きます。
2. 右上の**デベロッパーモード**を有効にします。
3. **パッケージ化されていない拡張機能を読み込む**をクリックし、`browser-extension` フォルダを選択します。

### 拡張機能の主な機能とボタン：
- 🖱️ **YouTubeネイティブボタン:** 拡張機能は、高評価ボタンや共有ボタンの隣に、YouTubeのネイティブインターフェースにスリークなボタンを挿入します：
  - **`⬇ Download`**: 現在の動画/楽曲のみをダウンロードします。
  - **`Download mix/playlist`**: YouTube Mixリストまたはプレイリストを表示している場合に自動的に表示され、プレイリスト全体をキューに追加します。
- 🔄 **SPAナビゲーションのサポート:** ページをリロードすることなく、動画間の遷移時にURLをアクティブに追跡し、ボタンを動的に更新します。
- 📊 **リアルタイムのステータス取得:** ページ上で直接リアルタイムのフィードバックを確認できます！
  - ⏳ **`Queueing...` / `Queued`**: 処理キューに追加されました。
  - ⬇️ **`Downloading (XX%)`**: ダウンロード、展開、後処理がアクティブに実行されています。
  - ✅ **`Downloaded`**: ダウンロードと処理が正常に完了しました。
  - ✅ **`Already Downloaded`**: 動画IDが`archive.txt`にあるため、重複としてスキップされました。
  - ❌ **`Failed`**: ダウンロードまたはインポートに失敗しました。
- 🪟 **ポップアップインターフェース:** ブラウザのツールバーにある拡張機能のアイコンをクリックすると、クリーンでダークテーマのポップアップが開き、ローカルWebアプリのコンソールへ簡単にジャンプできます。

---

## 📱 Telegramボット（オプション）

マスターMac上のDataSyncアプリ内でローカルに実行されているTelegramボットにYouTubeのリンクを送信することで、スマートフォンから安全にダウンロードをキューに追加できます。🤖

**セットアップ手順:**
1. Telegramを開き、`BotFather`を検索して `/newbot` を送信します。
2. 指示に従って **ボットトークン（Bot Token）** を取得します。
3. あなたのTelegramユーザーIDを取得します（例: `@userinfobot` を使用）。
4. Mac上の `.env` ファイルを更新します：
   ```env
   TELEGRAM_ENABLED=true
   TELEGRAM_BOT_TOKEN=your_token
   TELEGRAM_ALLOWED_USER_IDS=your_user_id
   ```
5. DataSyncアプリを再起動します。ボットにリンクまたは `/start` を送信してください！

> 💡 **注意:** ボットは完全にローカルで実行されます。コンピューターの電源がオフの場合、電源を入れた後にキューに入れられたメッセージが処理されます。

---

## 🔄 iPhone同期

ダウンロードした音楽をiPhoneに同期するには：
1. ケーブルを使用してiPhoneをMacに接続します。
2. 求められた場合は、iPhoneで「このコンピュータを信頼する」を選択します。
3. MacのFinderを開き、サイドバーでiPhoneを選択し、ミュージックの同期を有効にします。
4. 「Wi-Fiオン時にこのiPhoneを表示」を有効にすると、今後はワイヤレスで同期できます。

---

## 🛡️ 重複防止とクリーンアップ

### 📜 共有アーカイブ (`archive.txt`)
`YTDLP_ARCHIVE_FILE=<Google Drive Root>/Music/DataSyncYTDownloader/archive.txt`

このファイルはMacとWindowsの両方で共有されます。これは**永続的な重複防止レコード**です。`yt-dlp`は、このリストにある動画を自動的にスキップします。

### 🧹 クリーンアップルール
時間の経過とともに、`Imported`バックアップフォルダのサイズが大きくなる場合があります。

- **自動クリーンアップ（Macマスター）:** デフォルトでは、`AUTO_CLEAN_IMPORTED_AFTER_MASTER_IMPORT=true` および `KEEP_IMPORTED_BACKUP_DAYS=0` に設定されています。これは、MacがApple Musicへのインポートに成功すると、Google Driveの`Ready`コピーが`Imported`へ移動されることなく完全に削除されることを意味します。これにより、Windowsが古いバックアップファイルを不必要に同期するのを防ぎます！
- **手動クリーンアップ:** `Imported`に古いバックアップがある場合は、ローカルWeb UIの**Cleanup**ボタンを使用します。
- **安全第一:** クリーンアップはGoogle Drive内の一時的な`.m4a`バックアップ**のみ**を削除します。Apple Musicのファイル、`Ready`ファイル、または`archive.txt`を削除することは**絶対にありません**。重複防止機能は100%維持されます！
