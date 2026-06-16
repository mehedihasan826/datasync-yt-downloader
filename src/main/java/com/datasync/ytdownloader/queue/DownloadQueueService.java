package com.datasync.ytdownloader.queue;

import com.datasync.ytdownloader.api.JobStatusResponse;
import com.datasync.ytdownloader.download.DownloadResult;
import com.datasync.ytdownloader.download.YtDlpService;
import com.datasync.ytdownloader.file.MusicImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class DownloadQueueService {

    private static final Logger log = LoggerFactory.getLogger(DownloadQueueService.class);

    private final YtDlpService ytDlpService;
    private final MusicImportService musicImportService;
    private final com.datasync.ytdownloader.config.AppProperties properties;

    private final Map<String, DownloadJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2); // Process 2 jobs at a time
    
    private final List<JobProgressListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    public DownloadQueueService(YtDlpService ytDlpService, MusicImportService musicImportService, com.datasync.ytdownloader.config.AppProperties properties) {
        this.ytDlpService = ytDlpService;
        this.musicImportService = musicImportService;
        this.properties = properties;
    }
    
    public void addListener(JobProgressListener listener) {
        listeners.add(listener);
    }
    
    public DownloadJob getJob(String jobId) {
        return jobs.get(jobId);
    }
    
    public void notifyProgress(DownloadJob job) {
        for (JobProgressListener listener : listeners) {
            try {
                listener.onProgressUpdate(job);
            } catch (Exception e) {
                log.error("Error notifying listener", e);
            }
        }
    }

    public String queueJob(String url, boolean playlist, String source) {
        String jobId = UUID.randomUUID().toString();
        DownloadJob job = new DownloadJob(jobId, url, playlist, source);
        jobs.put(jobId, job);

        executor.submit(() -> processJob(job));
        return jobId;
    }

    public List<JobStatusResponse> getAllJobs() {
        return jobs.values().stream()
                .sorted(Comparator.comparing(DownloadJob::getCreatedAt).reversed())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public JobStatusResponse getJobStatus(String jobId) {
        DownloadJob job = jobs.get(jobId);
        if (job == null) return null;
        return toResponse(job);
    }

    private JobStatusResponse toResponse(DownloadJob job) {
        JobStatusResponse res = new JobStatusResponse(job.getId(), job.getUrl(), job.getStatus(), job.getMessage(), job.isPlaylist());
        res.setProviderName(job.getProviderName());
        res.setPhase(job.getPhase());
        res.setPlaylistIndex(job.getPlaylistIndex());
        res.setPlaylistTotal(job.getPlaylistTotal());
        res.setCurrentTitle(job.getCurrentTitle());
        res.setCurrentPercent(job.getCurrentPercent());
        res.setOverallPercent(job.getOverallPercent());
        res.setSpeed(job.getSpeed());
        res.setEta(job.getEta());
        res.setDownloadedFileCount(job.getDownloadedFileCount());
        res.setImportedFileCount(job.getImportedFileCount());
        res.setFailedFileCount(job.getFailedFileCount());
        res.setLastLogLine(job.getLastLogLine());
        res.setStartedAt(job.getCreatedAt());
        res.setUpdatedAt(job.getUpdatedAt());
        return res;
    }

    private void updateStatus(DownloadJob job, DownloadJobStatus status, String message) {
        job.setStatus(status);
        job.setMessage(message);
        if (status == DownloadJobStatus.COMPLETED || status == DownloadJobStatus.FAILED || status == DownloadJobStatus.CANCELLED) {
            job.setPhase(status);
        }
        log.info("Job {} [{}]: {}", job.getId(), status, message);
        notifyProgress(job);
    }

    private void processJob(DownloadJob job) {
        try {
            updateStatus(job, DownloadJobStatus.DOWNLOADING, "Downloading with yt-dlp...");
            List<DownloadResult> downloadedFiles = ytDlpService.download(job, () -> notifyProgress(job));
            
            if (downloadedFiles.isEmpty()) {
                updateStatus(job, DownloadJobStatus.COMPLETED, "No new files were downloaded (possibly all duplicates skipped).");
                return;
            }

            job.setProviderName("yt-dlp built-in metadata");

            int successCount = 0;
            for (DownloadResult result : downloadedFiles) {
                job.setPhase(DownloadJobStatus.POST_PROCESSING);
                job.setMessage("Importing " + result.getFile().getName());
                try {
                    musicImportService.importFile(result.getFile(), job);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to import file {}", result.getFile().getName(), e);
                    job.incrementFailedFileCount();
                }
            }

            if (successCount > 0) {
                com.datasync.ytdownloader.config.SetupMode mode = properties.getResolvedSetupMode();
                boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
                boolean isAppleMusicImportMode = mode == com.datasync.ytdownloader.config.SetupMode.SIMPLE_LOCAL_MAC ||
                                                 mode == com.datasync.ytdownloader.config.SetupMode.SIMPLE_LOCAL_WINDOWS ||
                                                 mode == com.datasync.ytdownloader.config.SetupMode.MAC_MASTER_WITH_SHARED_DRIVE ||
                                                 mode == com.datasync.ytdownloader.config.SetupMode.WINDOWS_MASTER_WITH_SHARED_DRIVE ||
                                                 mode == com.datasync.ytdownloader.config.SetupMode.MULTI_MAC_SHARED_DRIVE;

                String successMsg;
                if (isAppleMusicImportMode) {
                    if (isMac) {
                        successMsg = "Completed " + successCount + " tracks. Apple Music import completed (iPhone sync depends on Finder).";
                    } else {
                        successMsg = "Completed " + successCount + " tracks. Copied to Apple Music import folder; please verify in Apple Music (iPhone sync depends on Apple Devices).";
                    }
                } else if (mode == com.datasync.ytdownloader.config.SetupMode.SECONDARY_DOWNLOADER) {
                    successMsg = "Completed " + successCount + " tracks. Saved to Google Drive Ready folder.";
                } else {
                    successMsg = "Completed " + successCount + " tracks.";
                }
                updateStatus(job, DownloadJobStatus.COMPLETED, successMsg);
            } else {
                updateStatus(job, DownloadJobStatus.FAILED, "Tracks downloaded but failed to import.");
            }
        } catch (Exception e) {
            log.error("Job {} failed", job.getId(), e);
            updateStatus(job, DownloadJobStatus.FAILED, "Error: " + e.getMessage());
        }
    }

    public boolean hasActiveJobs() {
        return jobs.values().stream().anyMatch(j -> 
            j.getStatus() == DownloadJobStatus.QUEUED ||
            j.getStatus() == DownloadJobStatus.DOWNLOADING ||
            j.getStatus() == DownloadJobStatus.EXTRACTING ||
            j.getStatus() == DownloadJobStatus.POST_PROCESSING
        );
    }
}
