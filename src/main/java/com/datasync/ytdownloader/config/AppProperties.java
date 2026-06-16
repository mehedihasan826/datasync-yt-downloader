package com.datasync.ytdownloader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "")
public class AppProperties {
    
    private String machineName = "mac-main";
    private boolean isMasterMusicMachine = false;
    private String workDir;
    
    private String googleDriveRoot;
    private String sharedReadyDir;
    private String sharedImportedDir;
    private String sharedFailedDir;
    private String sharedQueueDir;
    
    private String appleMusicImportDir;
    
    private int masterScanIntervalSeconds = 60;
    private int cleanupRetentionDays = 30;
    private String cleanupMode = "manual";
    
    private boolean autoCleanImportedAfterMasterImport = true;
    private int keepImportedBackupDays = 0;
    
    private boolean telegramEnabled = false;
    private String telegramBotToken;
    private String telegramBotUsername;
    private String telegramAllowedUserIds;
    private boolean telegramPollingEnabled = true;
    private String telegramStatusUpdateMode = "edit_message";

    private int maxPlaylistItems = 50;
    private String importMode = "SHARED_DRIVE_AND_MASTER_IMPORT";

    private String ytdlpBinary = "yt-dlp";
    private String ffmpegBinary = "ffmpeg";
    private boolean ytdlpEmbedMetadata = true;
    private boolean ytdlpEmbedThumbnail = true;
    private boolean keepInfoJson = true;
    private String ytDlpArchiveFile;

    // Getters and Setters

    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }

    public boolean isMasterMusicMachine() { return isMasterMusicMachine; }
    public void setIsMasterMusicMachine(boolean masterMusicMachine) { this.isMasterMusicMachine = masterMusicMachine; }
    // Add alias for Spring binding if needed
    public boolean getIsMasterMusicMachine() { return isMasterMusicMachine; }
    public void setMasterMusicMachine(boolean masterMusicMachine) { this.isMasterMusicMachine = masterMusicMachine; }

    public String getWorkDir() { return workDir; }
    public void setWorkDir(String workDir) { this.workDir = workDir; }

    public String getGoogleDriveRoot() { return googleDriveRoot; }
    public void setGoogleDriveRoot(String googleDriveRoot) { this.googleDriveRoot = googleDriveRoot; }

    public String getSharedReadyDir() { return sharedReadyDir; }
    public void setSharedReadyDir(String sharedReadyDir) { this.sharedReadyDir = sharedReadyDir; }

    public String getSharedImportedDir() { return sharedImportedDir; }
    public void setSharedImportedDir(String sharedImportedDir) { this.sharedImportedDir = sharedImportedDir; }

    public String getSharedFailedDir() { return sharedFailedDir; }
    public void setSharedFailedDir(String sharedFailedDir) { this.sharedFailedDir = sharedFailedDir; }

    public String getSharedQueueDir() { return sharedQueueDir; }
    public void setSharedQueueDir(String sharedQueueDir) { this.sharedQueueDir = sharedQueueDir; }

    public String getAppleMusicImportDir() { return appleMusicImportDir; }
    public void setAppleMusicImportDir(String appleMusicImportDir) { this.appleMusicImportDir = appleMusicImportDir; }

    public int getMasterScanIntervalSeconds() { return masterScanIntervalSeconds; }
    public void setMasterScanIntervalSeconds(int masterScanIntervalSeconds) { this.masterScanIntervalSeconds = masterScanIntervalSeconds; }

    public int getCleanupRetentionDays() { return cleanupRetentionDays; }
    public void setCleanupRetentionDays(int cleanupRetentionDays) { this.cleanupRetentionDays = cleanupRetentionDays; }

    public String getCleanupMode() { return cleanupMode; }
    public void setCleanupMode(String cleanupMode) { this.cleanupMode = cleanupMode; }

    public boolean isAutoCleanImportedAfterMasterImport() { return autoCleanImportedAfterMasterImport; }
    public void setAutoCleanImportedAfterMasterImport(boolean autoCleanImportedAfterMasterImport) { this.autoCleanImportedAfterMasterImport = autoCleanImportedAfterMasterImport; }

    public int getKeepImportedBackupDays() { return keepImportedBackupDays; }
    public void setKeepImportedBackupDays(int keepImportedBackupDays) { this.keepImportedBackupDays = keepImportedBackupDays; }

    public boolean isTelegramEnabled() { return telegramEnabled; }
    public void setTelegramEnabled(boolean telegramEnabled) { this.telegramEnabled = telegramEnabled; }

    public String getTelegramBotToken() { return telegramBotToken; }
    public void setTelegramBotToken(String telegramBotToken) { this.telegramBotToken = telegramBotToken; }

    public String getTelegramBotUsername() { return telegramBotUsername; }
    public void setTelegramBotUsername(String telegramBotUsername) { this.telegramBotUsername = telegramBotUsername; }

    public String getTelegramAllowedUserIds() { return telegramAllowedUserIds; }
    public void setTelegramAllowedUserIds(String telegramAllowedUserIds) { this.telegramAllowedUserIds = telegramAllowedUserIds; }

    public boolean isTelegramPollingEnabled() { return telegramPollingEnabled; }
    public void setTelegramPollingEnabled(boolean telegramPollingEnabled) { this.telegramPollingEnabled = telegramPollingEnabled; }

    public String getTelegramStatusUpdateMode() { return telegramStatusUpdateMode; }
    public void setTelegramStatusUpdateMode(String telegramStatusUpdateMode) { this.telegramStatusUpdateMode = telegramStatusUpdateMode; }

    public int getMaxPlaylistItems() { return maxPlaylistItems; }
    public void setMaxPlaylistItems(int maxPlaylistItems) { this.maxPlaylistItems = maxPlaylistItems; }

    public String getImportMode() { return importMode; }
    public void setImportMode(String importMode) { this.importMode = importMode; }

    public String getYtdlpBinary() { return ytdlpBinary; }
    public void setYtdlpBinary(String ytdlpBinary) { this.ytdlpBinary = ytdlpBinary; }

    public String getFfmpegBinary() { return ffmpegBinary; }
    public void setFfmpegBinary(String ffmpegBinary) { this.ffmpegBinary = ffmpegBinary; }

    public boolean isYtdlpEmbedMetadata() { return ytdlpEmbedMetadata; }
    public void setYtdlpEmbedMetadata(boolean ytdlpEmbedMetadata) { this.ytdlpEmbedMetadata = ytdlpEmbedMetadata; }

    public boolean isYtdlpEmbedThumbnail() { return ytdlpEmbedThumbnail; }
    public void setYtdlpEmbedThumbnail(boolean ytdlpEmbedThumbnail) { this.ytdlpEmbedThumbnail = ytdlpEmbedThumbnail; }

    public boolean isKeepInfoJson() { return keepInfoJson; }
    public void setKeepInfoJson(boolean keepInfoJson) { this.keepInfoJson = keepInfoJson; }

    public String getYtDlpArchiveFile() { return ytDlpArchiveFile; }
    public void setYtDlpArchiveFile(String ytDlpArchiveFile) { this.ytDlpArchiveFile = ytDlpArchiveFile; }
}
