package com.datasync.ytdownloader.api;

import com.datasync.ytdownloader.queue.DownloadQueueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DownloadController {

    private final DownloadQueueService queueService;
    private final com.datasync.ytdownloader.config.AppProperties properties;
    private final com.datasync.ytdownloader.download.YoutubeUrlDetector urlDetector;

    public DownloadController(DownloadQueueService queueService, 
                              com.datasync.ytdownloader.config.AppProperties properties, 
                              com.datasync.ytdownloader.download.YoutubeUrlDetector urlDetector) {
        this.queueService = queueService;
        this.properties = properties;
        this.urlDetector = urlDetector;
    }

    @PostMapping("/download")
    public DownloadResponse download(@RequestBody DownloadRequest request) {
        String jobId = queueService.queueJob(request.getUrl(), request.isPlaylist(), request.getSource());
        return new DownloadResponse("queued", jobId);
    }

    @GetMapping("/jobs")
    public List<JobStatusResponse> getJobs() {
        return queueService.getAllJobs();
    }

    @GetMapping("/jobs/{jobId}")
    public JobStatusResponse getJobStatus(@PathVariable String jobId) {
        return queueService.getJobStatus(jobId);
    }

    @GetMapping("/archive/status")
    public java.util.Map<String, Object> checkArchiveStatus(@RequestParam String url) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (url == null || url.isBlank()) {
            response.put("error", "URL is required");
            return response;
        }

        String videoId = urlDetector.extractVideoId(url);
        response.put("videoId", videoId);
        response.put("archiveFile", properties.getYtDlpArchiveFile());
        response.put("sharedArchiveEnabled", properties.getYtDlpArchiveFile() != null && !properties.getYtDlpArchiveFile().isBlank());
        
        if (videoId == null) {
            response.put("downloaded", false);
            return response;
        }

        boolean downloaded = false;
        String archivePath = properties.getYtDlpArchiveFile();
        if (archivePath != null && !archivePath.isBlank()) {
            java.io.File archiveFile = new java.io.File(archivePath);
            if (archiveFile.exists()) {
                try {
                    String searchStr = "youtube " + videoId;
                    java.util.List<String> lines = java.nio.file.Files.readAllLines(archiveFile.toPath());
                    for (String line : lines) {
                        if (line.trim().equals(searchStr)) {
                            downloaded = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Ignore read errors
                }
            }
        }
        
        response.put("downloaded", downloaded);
        return response;
    }
}
