package com.datasync.ytdownloader.queue;

public enum DownloadJobStatus {
    QUEUED,
    DOWNLOADING,
    RESOLVING_METADATA,
    TAGGING,
    COMPLETED,
    FAILED,
    DUPLICATE_SKIPPED
}
