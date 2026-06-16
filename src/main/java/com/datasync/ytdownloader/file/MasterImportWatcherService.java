package com.datasync.ytdownloader.file;

import com.datasync.ytdownloader.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
@EnableScheduling
@ConditionalOnProperty(name = "isMasterMusicMachine", havingValue = "true")
public class MasterImportWatcherService {

    private static final Logger log = LoggerFactory.getLogger(MasterImportWatcherService.class);

    private final AppProperties properties;

    public MasterImportWatcherService(AppProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void onStartup() {
        log.info("MasterImportWatcherService initialized. Scanning for new files in Ready dir...");
        scanAndImport();
    }

    @Scheduled(fixedDelayString = "${masterScanIntervalSeconds:60}000")
    public void scanAndImport() {
        com.datasync.ytdownloader.config.SetupMode mode = properties.getResolvedSetupMode();
        boolean isSharedMaster = mode == com.datasync.ytdownloader.config.SetupMode.MAC_MASTER_WITH_SHARED_DRIVE || 
                                 mode == com.datasync.ytdownloader.config.SetupMode.WINDOWS_MASTER_WITH_SHARED_DRIVE;

        if (!isSharedMaster) {
            return;
        }

        String readyDirPath = properties.getSharedReadyDir();
        String importedDirPath = properties.getSharedImportedDir();
        String failedDirPath = properties.getSharedFailedDir();
        String appleMusicImportDir = properties.getAppleMusicImportDir();

        if (readyDirPath == null || readyDirPath.isBlank() || appleMusicImportDir == null || appleMusicImportDir.isBlank()) {
            return;
        }

        File readyDir = new File(readyDirPath);
        if (!readyDir.exists() || !readyDir.isDirectory()) {
            return;
        }

        File[] files = readyDir.listFiles((dir, name) -> name.endsWith(".m4a"));
        if (files == null || files.length == 0) {
            return;
        }

        File importedDir = new File(importedDirPath);
        if (!importedDir.exists()) {
            importedDir.mkdirs();
        }

        File failedDir = new File(failedDirPath);
        if (!failedDir.exists()) {
            failedDir.mkdirs();
        }
        
        File appleDir = new File(appleMusicImportDir);
        if (!appleDir.exists()) {
            appleDir.mkdirs();
        }

        for (File file : files) {
            log.info("Found file in Ready dir: {}", file.getName());
            try {
                // 1. Copy to Apple Music import folder
                File targetAppleFile = new File(appleDir, file.getName());
                targetAppleFile = getUniqueFile(targetAppleFile);
                Files.copy(file.toPath(), targetAppleFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // 2. Verify copied file exists and size > 0
                if (!targetAppleFile.exists() || targetAppleFile.length() == 0) {
                    throw new Exception("File copy to Apple Music failed or size is 0");
                }

                log.info("Successfully copied {} to Apple Music import folder.", file.getName());

                // 3. Handle backup/cleanup
                if (properties.isAutoCleanImportedAfterMasterImport() && properties.getKeepImportedBackupDays() == 0) {
                    Files.delete(file.toPath());
                    log.info("Imported to Apple Music and removed shared backup because archive.txt keeps download history: {}", file.getName());
                } else {
                    File targetImportedFile = new File(importedDir, file.getName());
                    targetImportedFile = getUniqueFile(targetImportedFile);
                    Files.move(file.toPath(), targetImportedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    log.info("Successfully moved {} to Imported folder.", file.getName());
                }

            } catch (Exception e) {
                log.error("Failed to import file: {}", file.getName(), e);
                try {
                    File targetFailedFile = new File(failedDir, file.getName());
                    targetFailedFile = getUniqueFile(targetFailedFile);
                    Files.move(file.toPath(), targetFailedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ex) {
                    log.error("Failed to move file to Failed dir. Leaving in Ready dir.", ex);
                }
            }
        }
    }

    private File getUniqueFile(File targetFile) {
        String originalName = targetFile.getName();
        String baseName = originalName.endsWith(".m4a") ? originalName.substring(0, originalName.length() - 4) : originalName;
        File dir = targetFile.getParentFile();
        
        int counter = 1;
        while (targetFile.exists()) {
            targetFile = new File(dir, baseName + " (" + counter + ").m4a");
            counter++;
        }
        return targetFile;
    }
}
