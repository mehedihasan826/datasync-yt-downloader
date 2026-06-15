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

    private final Map<String, DownloadJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2); // Process 2 jobs at a time

    public DownloadQueueService(YtDlpService ytDlpService, MusicImportService musicImportService) {
        this.ytDlpService = ytDlpService;
        this.musicImportService = musicImportService;
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
        return res;
    }

    private void updateStatus(DownloadJob job, DownloadJobStatus status, String message) {
        job.setStatus(status);
        job.setMessage(message);
        log.info("Job {} [{}]: {}", job.getId(), status, message);
    }

    private void processJob(DownloadJob job) {
        try {
            updateStatus(job, DownloadJobStatus.DOWNLOADING, "Downloading with yt-dlp...");
            List<DownloadResult> downloadedFiles = ytDlpService.download(job.getUrl(), job.isPlaylist(), job.getId());
            
            if (downloadedFiles.isEmpty()) {
                updateStatus(job, DownloadJobStatus.COMPLETED, "No new files were downloaded (possibly all duplicates skipped).");
                return;
            }

            job.setProviderName("yt-dlp built-in metadata");

            int successCount = 0;
            for (DownloadResult result : downloadedFiles) {
                updateStatus(job, DownloadJobStatus.TAGGING, "Moving to import folder...");
                musicImportService.importFile(result.getFile());
                successCount++;
            }

            if (successCount > 0) {
                updateStatus(job, DownloadJobStatus.COMPLETED, "Completed " + successCount + " tracks.");
            } else {
                updateStatus(job, DownloadJobStatus.FAILED, "Tracks downloaded but failed to import.");
            }
        } catch (Exception e) {
            log.error("Job {} failed", job.getId(), e);
            updateStatus(job, DownloadJobStatus.FAILED, "Error: " + e.getMessage());
        }
    }
}
