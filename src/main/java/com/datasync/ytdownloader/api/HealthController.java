package com.datasync.ytdownloader.api;

import com.datasync.ytdownloader.config.AppProperties;
import com.datasync.ytdownloader.util.CommandRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final AppProperties properties;
    private final CommandRunner commandRunner;

    private volatile boolean extensionContacted = false;

    public HealthController(AppProperties properties, CommandRunner commandRunner) {
        this.properties = properties;
        this.commandRunner = commandRunner;
    }

    @GetMapping
    public Map<String, Object> health(@org.springframework.web.bind.annotation.RequestParam(value = "source", required = false) String source) {
        if ("extension".equalsIgnoreCase(source)) {
            extensionContacted = true;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        
        com.datasync.ytdownloader.config.SetupMode mode = properties.getResolvedSetupMode();
        response.put("setupMode", mode.name());
        
        boolean localOnlyMode = mode == com.datasync.ytdownloader.config.SetupMode.SIMPLE_LOCAL_MAC || mode == com.datasync.ytdownloader.config.SetupMode.SIMPLE_LOCAL_WINDOWS;
        boolean sharedDriveMode = mode == com.datasync.ytdownloader.config.SetupMode.MAC_MASTER_WITH_SHARED_DRIVE || 
                                  mode == com.datasync.ytdownloader.config.SetupMode.WINDOWS_MASTER_WITH_SHARED_DRIVE || 
                                  mode == com.datasync.ytdownloader.config.SetupMode.MULTI_MAC_SHARED_DRIVE || 
                                  mode == com.datasync.ytdownloader.config.SetupMode.SECONDARY_DOWNLOADER;
        boolean masterMode = mode == com.datasync.ytdownloader.config.SetupMode.MAC_MASTER_WITH_SHARED_DRIVE || 
                             mode == com.datasync.ytdownloader.config.SetupMode.WINDOWS_MASTER_WITH_SHARED_DRIVE || 
                             mode == com.datasync.ytdownloader.config.SetupMode.SIMPLE_LOCAL_MAC || 
                             mode == com.datasync.ytdownloader.config.SetupMode.SIMPLE_LOCAL_WINDOWS;
        boolean secondaryMode = mode == com.datasync.ytdownloader.config.SetupMode.SECONDARY_DOWNLOADER;

        response.put("localOnlyMode", localOnlyMode);
        response.put("sharedDriveMode", sharedDriveMode);
        response.put("masterMusicMachine", masterMode);
        response.put("appleMusicImportEnabled", masterMode);
        response.put("secondaryDownloader", secondaryMode);

        response.put("machineName", properties.getMachineName());
        
        response.put("ytDlpAvailable", isAvailable(properties.getYtdlpBinary(), "--version"));
        response.put("ffmpegAvailable", isAvailable(properties.getFfmpegBinary(), "-version"));
        
        response.put("workDirExists", checkExists(properties.getWorkDir()));
        response.put("googleDriveRootDetected", checkExists(properties.getGoogleDriveRoot()));
        response.put("sharedReadyDirExists", checkExists(properties.getSharedReadyDir()));
        response.put("sharedImportedDirExists", checkExists(properties.getSharedImportedDir()));
        response.put("sharedFailedDirExists", checkExists(properties.getSharedFailedDir()));
        response.put("appleMusicImportDirExists", checkExists(properties.getAppleMusicImportDir()));

        response.put("readyCount", countM4aFiles(properties.getSharedReadyDir()));
        response.put("importedCount", countM4aFiles(properties.getSharedImportedDir()));
        response.put("failedCount", countM4aFiles(properties.getSharedFailedDir()));

        File archiveFile = resolveArchiveFile();
        response.put("ytDlpArchiveFile", archiveFile.getAbsolutePath());
        response.put("ytDlpArchiveFileParentExists", archiveFile.getParentFile() != null && archiveFile.getParentFile().exists());
        
        boolean sharedEnabled = false;
        String gDriveRoot = properties.getGoogleDriveRoot();
        if (gDriveRoot != null && !gDriveRoot.isBlank()) {
            sharedEnabled = archiveFile.getAbsolutePath().startsWith(new File(gDriveRoot).getAbsolutePath());
        }
        response.put("sharedArchiveEnabled", sharedEnabled);

        response.put("telegramEnabled", properties.isTelegramEnabled());
        response.put("telegramPollingEnabled", properties.isTelegramPollingEnabled());
        response.put("telegramConfigured", properties.getTelegramBotToken() != null && !properties.getTelegramBotToken().isBlank());
        response.put("telegramAllowedUsersConfigured", properties.getTelegramAllowedUserIds() != null && !properties.getTelegramAllowedUserIds().isBlank());

        // Installer/config fields
        response.put("setupCompleted", properties.isSetupCompleted());
        response.put("configPath", System.getProperty("ACTIVE_ENV_PATH", ".env"));
        response.put("autoStartEnabled", properties.isAutoStartEnabled());
        response.put("installedApp", com.datasync.ytdownloader.service.AutostartService.getLauncherPath() != null);
        response.put("browserExtensionInstalled", extensionContacted);

        return response;
    }

    private File resolveArchiveFile() {
        File localArchive = new File(properties.getWorkDir(), "archive.txt");
        String configuredShared = properties.getYtDlpArchiveFile();
        if (configuredShared != null && !configuredShared.isBlank()) {
            return new File(configuredShared);
        } else if (properties.getGoogleDriveRoot() != null && !properties.getGoogleDriveRoot().isBlank()) {
            return new File(properties.getGoogleDriveRoot(), "Music/DataSyncYTDownloader/archive.txt");
        } else {
            return localArchive;
        }
    }

    private int countM4aFiles(String path) {
        if (path == null || path.isBlank()) return 0;
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) return 0;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".m4a"));
        return files != null ? files.length : 0;
    }

    private boolean checkExists(String path) {
        if (path == null || path.isBlank()) return false;
        return new File(path).exists();
    }

    private boolean isAvailable(String command, String versionArg) {
        try {
            return commandRunner.runCommandAndWait(command, versionArg) == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
