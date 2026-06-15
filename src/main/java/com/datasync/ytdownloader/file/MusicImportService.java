package com.datasync.ytdownloader.file;

import com.datasync.ytdownloader.config.AppProperties;
import com.datasync.ytdownloader.queue.DownloadJob;
import com.datasync.ytdownloader.queue.DownloadJobStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class MusicImportService {

    private static final Logger log = LoggerFactory.getLogger(MusicImportService.class);

    private final AppProperties properties;
    private final FileNameSanitizer sanitizer;

    public MusicImportService(AppProperties properties, FileNameSanitizer sanitizer) {
        this.properties = properties;
        this.sanitizer = sanitizer;
    }

    public File importFile(File sourceFile, DownloadJob job) throws Exception {
        String originalName = sourceFile.getName();

        File readyDir = new File(properties.getSharedReadyDir() != null ? properties.getSharedReadyDir() : "");
        File targetDir;

        boolean gDriveExists = readyDir.exists() && readyDir.isDirectory();
        if (gDriveExists) {
            targetDir = readyDir;
        } else {
            log.warn("Google Drive not detected. Saving to local-output fallback.");
            targetDir = new File(properties.getWorkDir(), "local-output");
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
        }

        if (job != null) job.setPhase(DownloadJobStatus.COPYING_TO_DRIVE);
        
        File targetReadyFile = getUniqueFile(targetDir, originalName);
        log.info("Copying file to {}", targetReadyFile.getAbsolutePath());
        Files.copy(sourceFile.toPath(), targetReadyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        if (job != null) job.incrementDownloadedFileCount();

        if (properties.isMasterMusicMachine() && gDriveExists) {
            String appleDirStr = properties.getAppleMusicImportDir();
            if (appleDirStr != null && !appleDirStr.isBlank()) {
                File appleDir = new File(appleDirStr);
                if (appleDir.exists()) {
                    if (job != null) job.setPhase(DownloadJobStatus.IMPORTING_TO_APPLE_MUSIC);
                    
                    File targetAppleFile = getUniqueFile(appleDir, originalName);
                    log.info("Master machine: Copying to Apple Music import dir {}", targetAppleFile.getAbsolutePath());
                    Files.copy(targetReadyFile.toPath(), targetAppleFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    if (!targetAppleFile.exists() || targetAppleFile.length() == 0) {
                        throw new Exception("File copy to Apple Music failed or size is 0");
                    }

                    File importedDir = new File(properties.getSharedImportedDir());
                    if (!importedDir.exists()) {
                        importedDir.mkdirs();
                    }

                    File targetImportedFile = getUniqueFile(importedDir, originalName);
                    log.info("Master machine: Moving from Ready to Imported dir {}", targetImportedFile.getAbsolutePath());
                    Files.move(targetReadyFile.toPath(), targetImportedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    if (job != null) job.incrementImportedFileCount();
                } else {
                    log.warn("Apple Music import dir does not exist. Leaving in Ready dir.");
                }
            }
        }

        return targetReadyFile;
    }

    private File getUniqueFile(File dir, String originalName) {
        String baseName = originalName.endsWith(".m4a") ? originalName.substring(0, originalName.length() - 4) : originalName;
        File targetFile = new File(dir, originalName);
        int counter = 1;
        while (targetFile.exists()) {
            targetFile = new File(dir, baseName + " (" + counter + ").m4a");
            counter++;
        }
        return targetFile;
    }
}
