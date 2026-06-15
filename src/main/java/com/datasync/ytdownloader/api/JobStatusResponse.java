package com.datasync.ytdownloader.api;

import com.datasync.ytdownloader.queue.DownloadJobStatus;
import java.time.Instant;

public class JobStatusResponse {
    private String jobId;
    private String url;
    private DownloadJobStatus status;
    private DownloadJobStatus phase;
    private String message;
    private boolean isPlaylist;
    private String providerName;

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
    private Instant startedAt;
    private Instant updatedAt;

    public JobStatusResponse() {}

    public JobStatusResponse(String jobId, String url, DownloadJobStatus status, String message, boolean isPlaylist) {
        this.jobId = jobId;
        this.url = url;
        this.status = status;
        this.message = message;
        this.isPlaylist = isPlaylist;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public DownloadJobStatus getStatus() { return status; }
    public void setStatus(DownloadJobStatus status) { this.status = status; }

    public DownloadJobStatus getPhase() { return phase; }
    public void setPhase(DownloadJobStatus phase) { this.phase = phase; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isPlaylist() { return isPlaylist; }
    public void setPlaylist(boolean isPlaylist) { this.isPlaylist = isPlaylist; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public Integer getPlaylistIndex() { return playlistIndex; }
    public void setPlaylistIndex(Integer playlistIndex) { this.playlistIndex = playlistIndex; }

    public Integer getPlaylistTotal() { return playlistTotal; }
    public void setPlaylistTotal(Integer playlistTotal) { this.playlistTotal = playlistTotal; }

    public String getCurrentTitle() { return currentTitle; }
    public void setCurrentTitle(String currentTitle) { this.currentTitle = currentTitle; }

    public Double getCurrentPercent() { return currentPercent; }
    public void setCurrentPercent(Double currentPercent) { this.currentPercent = currentPercent; }

    public Double getOverallPercent() { return overallPercent; }
    public void setOverallPercent(Double overallPercent) { this.overallPercent = overallPercent; }

    public String getSpeed() { return speed; }
    public void setSpeed(String speed) { this.speed = speed; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }

    public int getDownloadedFileCount() { return downloadedFileCount; }
    public void setDownloadedFileCount(int downloadedFileCount) { this.downloadedFileCount = downloadedFileCount; }

    public int getImportedFileCount() { return importedFileCount; }
    public void setImportedFileCount(int importedFileCount) { this.importedFileCount = importedFileCount; }

    public int getFailedFileCount() { return failedFileCount; }
    public void setFailedFileCount(int failedFileCount) { this.failedFileCount = failedFileCount; }

    public String getLastLogLine() { return lastLogLine; }
    public void setLastLogLine(String lastLogLine) { this.lastLogLine = lastLogLine; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
