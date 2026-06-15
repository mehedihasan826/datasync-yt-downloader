package com.datasync.ytdownloader.api;

import com.datasync.ytdownloader.queue.DownloadJobStatus;

public class JobStatusResponse {
    private String jobId;
    private String url;
    private DownloadJobStatus status;
    private String message;
    private boolean isPlaylist;
    private String providerName;

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

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isPlaylist() { return isPlaylist; }
    public void setPlaylist(boolean isPlaylist) { this.isPlaylist = isPlaylist; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
}
