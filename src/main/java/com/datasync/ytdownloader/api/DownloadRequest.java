package com.datasync.ytdownloader.api;

public class DownloadRequest {
    private String url;
    private boolean playlist;
    private String source;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isPlaylist() { return playlist; }
    public void setPlaylist(boolean playlist) { this.playlist = playlist; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
