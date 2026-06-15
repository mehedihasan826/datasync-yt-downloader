package com.datasync.ytdownloader.queue;

public enum DownloadJobStatus {
    QUEUED,
    EXTRACTING,
    DOWNLOADING,
    POST_PROCESSING,
    COPYING_TO_DRIVE,
    IMPORTING_TO_APPLE_MUSIC,
    COMPLETED,
    FAILED,
    CANCELLED
}
