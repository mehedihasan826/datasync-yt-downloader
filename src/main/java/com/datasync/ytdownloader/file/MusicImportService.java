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
        com.datasync.ytdownloader.config.SetupMode mode = properties.getResolvedSetupMode();

        boolean isLocalOnly = mode == com.datasync.ytdownloader.config.SetupMode.SIMPLE_LOCAL_MAC || 
                              mode == com.datasync.ytdownloader.config.SetupMode.SIMPLE_LOCAL_WINDOWS;
                              
        boolean isMaster = mode == com.datasync.ytdownloader.config.SetupMode.MAC_MASTER_WITH_SHARED_DRIVE || 
                           mode == com.datasync.ytdownloader.config.SetupMode.WINDOWS_MASTER_WITH_SHARED_DRIVE ||
                           mode == com.datasync.ytdownloader.config.SetupMode.SIMPLE_LOCAL_MAC || 
                           mode == com.datasync.ytdownloader.config.SetupMode.SIMPLE_LOCAL_WINDOWS;

        if (isLocalOnly) {
            // Drop directly into Apple Music Import Dir or fallback
            File targetDir;
            String appleDirStr = properties.getAppleMusicImportDir();
            if (appleDirStr != null && !appleDirStr.isBlank() && new File(appleDirStr).exists()) {
                targetDir = new File(appleDirStr);
            } else {
                log.warn("Apple Music import dir missing for local mode. Saving to local-output fallback.");
                targetDir = new File(properties.getWorkDir(), "local-output");
                if (!targetDir.exists()) targetDir.mkdirs();
            }

            if (job != null) job.setPhase(DownloadJobStatus.IMPORTING_TO_APPLE_MUSIC);
            File finalFile = getUniqueFile(targetDir, originalName);
            if (!copyAndVerify(sourceFile, finalFile)) {
                throw new Exception("File copy/verification failed to Apple Music import folder");
            }
            
            if (job != null) {
                job.incrementDownloadedFileCount();
                job.incrementImportedFileCount();
            }
            return finalFile;
        }

        // Shared drive modes
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
        log.info("Shared mode: Copying file to {}", targetReadyFile.getAbsolutePath());
        Files.copy(sourceFile.toPath(), targetReadyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        if (job != null) job.incrementDownloadedFileCount();

        if (isMaster && gDriveExists) {
            String appleDirStr = properties.getAppleMusicImportDir();
            if (appleDirStr != null && !appleDirStr.isBlank()) {
                File appleDir = new File(appleDirStr);
                if (appleDir.exists()) {
                    if (job != null) job.setPhase(DownloadJobStatus.IMPORTING_TO_APPLE_MUSIC);
                    
                    File targetAppleFile = getUniqueFile(appleDir, originalName);
                    if (!copyAndVerify(targetReadyFile, targetAppleFile)) {
                        throw new Exception("File copy to Apple Music failed verification");
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

    public boolean copyAndVerify(File sourceFile, File targetFile) {
        try {
            log.info("Copying file to: {}", targetFile.getAbsolutePath());
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // Wait up to 2.0 seconds, polling every 100ms
            String os = System.getProperty("os.name").toLowerCase();
            boolean isMac = os.contains("mac");
            boolean hadPositiveVerification = false;

            for (int i = 0; i < 20; i++) {
                Thread.sleep(100);
                if (targetFile.exists() && targetFile.length() > 0) {
                    hadPositiveVerification = true;
                    break;
                }
            }

            if (hadPositiveVerification) {
                log.info("Verification succeeded: Target file exists and has size > 0");
                return true;
            }

            // If it disappeared on macOS after the copy returned, treat it as successful Apple Music consumption.
            if (isMac) {
                log.info("Verification note: File disappeared on macOS, assuming Apple Music consumed it successfully.");
                return true;
            }

            log.error("Verification failed: Target file does not exist or size is 0 after 2 seconds.");
            return false;
        } catch (Exception e) {
            log.error("Error copying/verifying file: " + targetFile.getName(), e);
            return false;
        }
    }
}
