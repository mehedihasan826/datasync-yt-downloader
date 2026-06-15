package com.datasync.ytdownloader.queue;

import java.time.Instant;

public class DownloadJob {
    private final String id;
    private final String url;
    private final boolean playlist;
    private final String source;
    private final Instant createdAt;

    private DownloadJobStatus status;
    private DownloadJobStatus phase;
    private String message;
    
    private Integer playlistIndex;
    private Integer playlistTotal;
    private String currentTitle;
    private Double currentPercent;
    private Double overallPercent;
    private String speed;
    private String eta;
    
    private int downloadedFileCount;
    private int importedFileCount;
    private int failedFileCount;
    
    private String lastLogLine;
    private Instant updatedAt;
    
    private String providerName;

    public DownloadJob(String id, String url, boolean playlist, String source) {
        this.id = id;
        this.url = url;
        this.playlist = playlist;
        this.source = source;
        this.status = DownloadJobStatus.QUEUED;
        this.phase = DownloadJobStatus.QUEUED;
        this.message = "Queued";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getUrl() { return url; }
    public boolean isPlaylist() { return playlist; }
    public String getSource() { return source; }
    public Instant getCreatedAt() { return createdAt; }

    public synchronized DownloadJobStatus getStatus() { return status; }
    public synchronized void setStatus(DownloadJobStatus status) { this.status = status; touch(); }
    
    public synchronized DownloadJobStatus getPhase() { return phase; }
    public synchronized void setPhase(DownloadJobStatus phase) { this.phase = phase; touch(); }

    public synchronized String getMessage() { return message; }
    public synchronized void setMessage(String message) { this.message = message; touch(); }

    public synchronized Integer getPlaylistIndex() { return playlistIndex; }
    public synchronized void setPlaylistIndex(Integer playlistIndex) { this.playlistIndex = playlistIndex; touch(); }

    public synchronized Integer getPlaylistTotal() { return playlistTotal; }
    public synchronized void setPlaylistTotal(Integer playlistTotal) { this.playlistTotal = playlistTotal; touch(); }

    public synchronized String getCurrentTitle() { return currentTitle; }
    public synchronized void setCurrentTitle(String currentTitle) { this.currentTitle = currentTitle; touch(); }

    public synchronized Double getCurrentPercent() { return currentPercent; }
    public synchronized void setCurrentPercent(Double currentPercent) { this.currentPercent = currentPercent; touch(); }

    public synchronized Double getOverallPercent() { return overallPercent; }
    public synchronized void setOverallPercent(Double overallPercent) { this.overallPercent = overallPercent; touch(); }

    public synchronized String getSpeed() { return speed; }
    public synchronized void setSpeed(String speed) { this.speed = speed; touch(); }

    public synchronized String getEta() { return eta; }
    public synchronized void setEta(String eta) { this.eta = eta; touch(); }

    public synchronized int getDownloadedFileCount() { return downloadedFileCount; }
    public synchronized void incrementDownloadedFileCount() { this.downloadedFileCount++; touch(); }

    public synchronized int getImportedFileCount() { return importedFileCount; }
    public synchronized void incrementImportedFileCount() { this.importedFileCount++; touch(); }

    public synchronized int getFailedFileCount() { return failedFileCount; }
    public synchronized void incrementFailedFileCount() { this.failedFileCount++; touch(); }

    public synchronized String getLastLogLine() { return lastLogLine; }
    public synchronized void setLastLogLine(String lastLogLine) { this.lastLogLine = lastLogLine; touch(); }

    public synchronized Instant getUpdatedAt() { return updatedAt; }
    private void touch() { this.updatedAt = Instant.now(); }

    public synchronized String getProviderName() { return providerName; }
    public synchronized void setProviderName(String providerName) { this.providerName = providerName; touch(); }
}
