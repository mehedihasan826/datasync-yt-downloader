package com.datasync.ytdownloader.config;

import com.datasync.ytdownloader.util.CommandRunner;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class StartupValidator {

    private static final Logger log = LoggerFactory.getLogger(StartupValidator.class);
    
    private final AppProperties properties;
    private final CommandRunner commandRunner;

    public StartupValidator(AppProperties properties, CommandRunner commandRunner) {
        this.properties = properties;
        this.commandRunner = commandRunner;
    }

    @PostConstruct
    public void validate() {
        log.info("Validating startup dependencies and configuration...");

        // Ensure directories exist
        ensureDir(properties.getWorkDir(), "Work Directory");
        
        if (properties.getGoogleDriveRoot() != null && !properties.getGoogleDriveRoot().isBlank()) {
            ensureDir(properties.getSharedReadyDir(), "Shared Ready Directory");
            ensureDir(properties.getSharedImportedDir(), "Shared Imported Directory");
            ensureDir(properties.getSharedFailedDir(), "Shared Failed Directory");
            ensureDir(properties.getSharedQueueDir(), "Shared Queue Directory");
        }
        
        if (properties.isMasterMusicMachine() && properties.getAppleMusicImportDir() != null && !properties.getAppleMusicImportDir().isBlank()) {
            ensureDir(properties.getAppleMusicImportDir(), "Apple Music Import Directory");
        }

        // Validate dependencies
        if (!isCommandAvailable(properties.getYtdlpBinary(), "--version")) {
            log.error("yt-dlp not found! Please install it and ensure it is in the system PATH.");
        }
        if (!isCommandAvailable(properties.getFfmpegBinary(), "-version")) {
            log.error("ffmpeg not found! Please install it and ensure it is in the system PATH.");
        }
        
        log.info("Startup validation complete.");
    }

    private void ensureDir(String path, String name) {
        if (path == null || path.isBlank()) {
            return;
        }
        File dir = new File(path);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.info("Created {}: {}", name, path);
            } else {
                throw new RuntimeException("Failed to create " + name + " at " + path + ". Check permissions.");
            }
        }
    }

    private boolean isCommandAvailable(String command, String versionArg) {
        try {
            if (command == null || command.isBlank()) return false;
            int exitCode = commandRunner.runCommandAndWait(command, versionArg);
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
