package com.datasync.ytdownloader.api;

import com.datasync.ytdownloader.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cleanup")
public class CleanupController {

    private static final Logger log = LoggerFactory.getLogger(CleanupController.class);

    private final AppProperties properties;

    public CleanupController(AppProperties properties) {
        this.properties = properties;
    }

    @PostMapping("/imported")
    public ResponseEntity<Map<String, Object>> cleanupImported() {
        Map<String, Object> response = new HashMap<>();
        String importedDirPath = properties.getSharedImportedDir();
        int retentionDays = properties.getKeepImportedBackupDays() > 0 
            ? properties.getKeepImportedBackupDays() 
            : properties.getCleanupRetentionDays();

        if (importedDirPath == null || importedDirPath.isBlank()) {
            response.put("status", "error");
            response.put("message", "Shared Imported Dir is not configured.");
            return ResponseEntity.badRequest().body(response);
        }

        File importedDir = new File(importedDirPath);
        if (!importedDir.exists() || !importedDir.isDirectory()) {
            response.put("status", "error");
            response.put("message", "Shared Imported Dir does not exist.");
            return ResponseEntity.badRequest().body(response);
        }

        File[] files = importedDir.listFiles((dir, name) -> name.endsWith(".m4a"));
        if (files == null || files.length == 0) {
            response.put("status", "ok");
            response.put("deletedCount", 0);
            response.put("skippedCount", 0);
            response.put("message", "Deleted old Imported backup files only. Apple Music files and archive.txt were not touched.");
            return ResponseEntity.ok(response);
        }

        long cutoffTime = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L);
        int deletedCount = 0;
        int skippedCount = 0;

        for (File file : files) {
            if (file.lastModified() < cutoffTime) {
                try {
                    boolean deleted = file.delete();
                    if (deleted) {
                        deletedCount++;
                        log.info("Cleaned up old imported file: {}", file.getName());
                    } else {
                        skippedCount++;
                        log.warn("Failed to delete file: {}", file.getName());
                    }
                } catch (Exception e) {
                    skippedCount++;
                    log.error("Error deleting file: {}", file.getName(), e);
                }
            } else {
                skippedCount++;
            }
        }

        response.put("status", "ok");
        response.put("deletedCount", deletedCount);
        response.put("skippedCount", skippedCount);
        response.put("message", "Deleted old Imported backup files only. Apple Music files and archive.txt were not touched.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/imported/all")
    public ResponseEntity<Map<String, Object>> cleanupImportedAll() {
        Map<String, Object> response = new HashMap<>();
        String importedDirPath = properties.getSharedImportedDir();

        if (importedDirPath == null || importedDirPath.isBlank()) {
            response.put("status", "error");
            response.put("message", "Shared Imported Dir is not configured.");
            return ResponseEntity.badRequest().body(response);
        }

        File importedDir = new File(importedDirPath);
        if (!importedDir.exists() || !importedDir.isDirectory()) {
            response.put("status", "error");
            response.put("message", "Shared Imported Dir does not exist.");
            return ResponseEntity.badRequest().body(response);
        }

        File[] files = importedDir.listFiles((dir, name) -> name.endsWith(".m4a"));
        if (files == null || files.length == 0) {
            response.put("status", "ok");
            response.put("deletedCount", 0);
            response.put("skippedCount", 0);
            response.put("message", "Deleted Imported backup files only. Apple Music files and archive.txt were not touched.");
            return ResponseEntity.ok(response);
        }

        int deletedCount = 0;
        int skippedCount = 0;

        for (File file : files) {
            try {
                boolean deleted = file.delete();
                if (deleted) {
                    deletedCount++;
                    log.info("Cleaned up imported file: {}", file.getName());
                } else {
                    skippedCount++;
                    log.warn("Failed to delete file: {}", file.getName());
                }
            } catch (Exception e) {
                skippedCount++;
                log.error("Error deleting file: {}", file.getName(), e);
            }
        }

        response.put("status", "ok");
        response.put("deletedCount", deletedCount);
        response.put("skippedCount", skippedCount);
        response.put("message", "Deleted Imported backup files only. Apple Music files and archive.txt were not touched.");
        return ResponseEntity.ok(response);
    }
}
