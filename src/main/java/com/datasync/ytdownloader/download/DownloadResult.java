package com.datasync.ytdownloader.download;

import java.io.File;

public class DownloadResult {
    private File file;
    private String videoId;
    private String rawTitle;

    public DownloadResult(File file, String videoId, String rawTitle) {
        this.file = file;
        this.videoId = videoId;
        this.rawTitle = rawTitle;
    }

    public File getFile() { return file; }
    public String getVideoId() { return videoId; }
    public String getRawTitle() { return rawTitle; }
}
