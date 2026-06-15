package com.datasync.ytdownloader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "")
public class AppProperties {
    
    private String workDir;
    private String musicImportDir;
    private int maxPlaylistItems = 50;
    private boolean duplicateSkip = true;
    private ImportMode importMode = ImportMode.READY_FOLDER;
    
    private String ytdlpBinary = "yt-dlp";
    private String ffmpegBinary = "ffmpeg";
    private String ffprobeBinary = "ffprobe";
    
    private boolean telegramEnabled = false;
    private String telegramBotToken;
    private String telegramBotUsername;
    private String telegramAllowedUserIds;

    private boolean ytdlpEmbedMetadata = true;
    private boolean ytdlpEmbedThumbnail = true;
    private boolean keepInfoJson = true;

    public enum ImportMode {
        AUTO_FOLDER, READY_FOLDER
    }

    // Getters and Setters

    public String getWorkDir() { return workDir; }
    public void setWorkDir(String workDir) { this.workDir = workDir; }

    public String getMusicImportDir() { return musicImportDir; }
    public void setMusicImportDir(String musicImportDir) { this.musicImportDir = musicImportDir; }

    public int getMaxPlaylistItems() { return maxPlaylistItems; }
    public void setMaxPlaylistItems(int maxPlaylistItems) { this.maxPlaylistItems = maxPlaylistItems; }

    public boolean isDuplicateSkip() { return duplicateSkip; }
    public void setDuplicateSkip(boolean duplicateSkip) { this.duplicateSkip = duplicateSkip; }

    public ImportMode getImportMode() { return importMode; }
    public void setImportMode(ImportMode importMode) { this.importMode = importMode; }

    public String getYtdlpBinary() { return ytdlpBinary; }
    public void setYtdlpBinary(String ytdlpBinary) { this.ytdlpBinary = ytdlpBinary; }

    public String getFfmpegBinary() { return ffmpegBinary; }
    public void setFfmpegBinary(String ffmpegBinary) { this.ffmpegBinary = ffmpegBinary; }

    public String getFfprobeBinary() { return ffprobeBinary; }
    public void setFfprobeBinary(String ffprobeBinary) { this.ffprobeBinary = ffprobeBinary; }

    public boolean isTelegramEnabled() { return telegramEnabled; }
    public void setTelegramEnabled(boolean telegramEnabled) { this.telegramEnabled = telegramEnabled; }

    public String getTelegramBotToken() { return telegramBotToken; }
    public void setTelegramBotToken(String telegramBotToken) { this.telegramBotToken = telegramBotToken; }

    public String getTelegramBotUsername() { return telegramBotUsername; }
    public void setTelegramBotUsername(String telegramBotUsername) { this.telegramBotUsername = telegramBotUsername; }

    public String getTelegramAllowedUserIds() { return telegramAllowedUserIds; }
    public void setTelegramAllowedUserIds(String telegramAllowedUserIds) { this.telegramAllowedUserIds = telegramAllowedUserIds; }

    public boolean isYtdlpEmbedMetadata() { return ytdlpEmbedMetadata; }
    public void setYtdlpEmbedMetadata(boolean ytdlpEmbedMetadata) { this.ytdlpEmbedMetadata = ytdlpEmbedMetadata; }

    public boolean isYtdlpEmbedThumbnail() { return ytdlpEmbedThumbnail; }
    public void setYtdlpEmbedThumbnail(boolean ytdlpEmbedThumbnail) { this.ytdlpEmbedThumbnail = ytdlpEmbedThumbnail; }

    public boolean isKeepInfoJson() { return keepInfoJson; }
    public void setKeepInfoJson(boolean keepInfoJson) { this.keepInfoJson = keepInfoJson; }
}
