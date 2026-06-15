package com.datasync.ytdownloader.queue;

import java.time.Instant;

public class DownloadJob {
    private String id;
    private String url;
    private boolean playlist;
    private String source;
    private DownloadJobStatus status;
    private String message;
    private Instant createdAt;

    public DownloadJob(String id, String url, boolean playlist, String source) {
        this.id = id;
        this.url = url;
        this.playlist = playlist;
        this.source = source;
        this.status = DownloadJobStatus.QUEUED;
        this.message = "Queued";
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getUrl() { return url; }
    public boolean isPlaylist() { return playlist; }
    public String getSource() { return source; }
    
    public DownloadJobStatus getStatus() { return status; }
    public void setStatus(DownloadJobStatus status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getCreatedAt() { return createdAt; }

    private String providerName;
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
}
