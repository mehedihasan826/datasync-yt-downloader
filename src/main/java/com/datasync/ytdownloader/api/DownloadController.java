package com.datasync.ytdownloader.api;

import com.datasync.ytdownloader.queue.DownloadQueueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // For browser extension or simple local testing
public class DownloadController {

    private final DownloadQueueService queueService;

    public DownloadController(DownloadQueueService queueService) {
        this.queueService = queueService;
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
}
