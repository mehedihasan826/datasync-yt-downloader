# 🎵 DataSync YT Downloader

<div align="right">
  <a href="README.md">🇺🇸 English</a> | <strong>🇯🇵 日本語</strong>
</div>

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

## 🚀 インストールとセットアップ

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
