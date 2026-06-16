# Architectural Topologies

DataSync YT Downloader supports multiple installation topologies depending on whether you use multiple machines, different operating systems, or local storage.

## 1. Simple Local Mode (`SIMPLE_LOCAL_MAC` / `SIMPLE_LOCAL_WINDOWS`)
- **Use Case**: You only download and listen to music on one computer.
- **Workflow**:
  - Downloads are stored locally on the machine.
  - Files are imported directly into the local Apple Music automatically add folder.
  - **No Google Drive required**.
  - Duplicate prevention archive file is saved in the local work directory (`WORK_DIR/archive.txt`).

## 2. Sync Mode: Mac Master + Shared Drive (`MAC_MASTER_WITH_SHARED_DRIVE`)
- **Use Case**: You have a Mac (which manages your Apple Music library and syncs with your iPhone) and other machines (like Windows PCs) that you use to download music.
- **Workflow**:
  - The Mac runs as the **Master Machine**, watching the Google Drive shared `Ready` folder.
  - When new tracks appear in Google Drive `Ready` (downloaded by this Mac or secondary Windows machines), the Mac copies them into its local Apple Music library and moves the files in Google Drive to the `Imported` backup folder.
  - iPhone sync is completed using macOS Finder.

## 3. Sync Mode: Windows Master + Shared Drive (`WINDOWS_MASTER_WITH_SHARED_DRIVE`)
- **Use Case**: You use a Windows PC to manage your main Apple Music library and sync with your iPhone or devices.
- **Workflow**:
  - Similar to Mac Master, but Windows watches the shared `Ready` directory and copies files into the Windows Apple Music import folder.

## 4. Secondary Downloader (`SECONDARY_DOWNLOADER`)
- **Use Case**: You have a secondary computer (like a Windows gaming PC) and want to queue downloads from it, but your main library is managed by a Master Mac.
- **Workflow**:
  - Downloads go directly to the shared Google Drive `Ready` folder.
  - The local machine **does not import** to Apple Music.
  - The Master Mac imports the files later when it runs.
