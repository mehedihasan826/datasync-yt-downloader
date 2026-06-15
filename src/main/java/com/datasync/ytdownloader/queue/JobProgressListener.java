package com.datasync.ytdownloader.queue;

public interface JobProgressListener {
    void onProgressUpdate(DownloadJob job);
}
