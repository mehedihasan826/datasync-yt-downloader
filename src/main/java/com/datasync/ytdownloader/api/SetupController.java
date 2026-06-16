package com.datasync.ytdownloader.api;

import com.datasync.ytdownloader.YtDownloaderApplication;
import com.datasync.ytdownloader.config.AppProperties;
import com.datasync.ytdownloader.queue.DownloadQueueService;
import com.datasync.ytdownloader.service.AutostartService;
import com.datasync.ytdownloader.util.CommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private static final Logger log = LoggerFactory.getLogger(SetupController.class);

    private final AppProperties properties;
    private final DownloadQueueService queueService;
    private final AutostartService autostartService;
    private final CommandRunner commandRunner;

    public SetupController(AppProperties properties, DownloadQueueService queueService,
                           AutostartService autostartService, CommandRunner commandRunner) {
        this.properties = properties;
        this.queueService = queueService;
        this.autostartService = autostartService;
        this.commandRunner = commandRunner;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new HashMap<>();
        String os = System.getProperty("os.name").toLowerCase();
        String osName = os.contains("win") ? "windows" : (os.contains("mac") ? "macos" : "linux");

        String activeEnvPath = System.getProperty("ACTIVE_ENV_PATH", ".env");
        String appConfigDir = System.getProperty("APP_CONFIG_DIR", YtDownloaderApplication.getAppConfigDirectory());

        File localEnv = new File(".env");
        File appDataEnv = new File(appConfigDir, ".env");

        boolean localExists = localEnv.exists();
        boolean appDataExists = appDataEnv.exists();

        response.put("setupCompleted", properties.isSetupCompleted());
        response.put("existingConfigDetected", localExists && !appDataExists);
        response.put("projectLocalConfigPath", localEnv.getAbsolutePath());
        response.put("appConfigPath", appDataEnv.getAbsolutePath());
        response.put("activeEnvPath", activeEnvPath);

        // Detected paths
        Map<String, Object> detected = new HashMap<>();
        detected.put("os", osName);
        
        String gdriveRoot = detectGoogleDriveRoot(os);
        detected.put("googleDriveRoot", gdriveRoot);

        String appleMusicDir = detectAppleMusicDir(os);
        detected.put("appleMusicImportDir", appleMusicDir);

        String defaultWorkDir = os.contains("win") 
            ? System.getenv("USERPROFILE") + "\\Music\\DataSyncYTDownloaderWork"
            : System.getProperty("user.home") + "/Music/DataSyncYTDownloaderWork";
        detected.put("workDir", defaultWorkDir);

        if (!gdriveRoot.isEmpty()) {
            detected.put("sharedReadyDir", gdriveRoot + (os.contains("win") ? "\\Music\\DataSyncYTDownloader\\Ready" : "/Music/DataSyncYTDownloader/Ready"));
            detected.put("sharedImportedDir", gdriveRoot + (os.contains("win") ? "\\Music\\DataSyncYTDownloader\\Imported" : "/Music/DataSyncYTDownloader/Imported"));
            detected.put("sharedFailedDir", gdriveRoot + (os.contains("win") ? "\\Music\\DataSyncYTDownloader\\Failed" : "/Music/DataSyncYTDownloader/Failed"));
            detected.put("sharedQueueDir", gdriveRoot + (os.contains("win") ? "\\Music\\DataSyncYTDownloader\\Queue" : "/Music/DataSyncYTDownloader/Queue"));
            detected.put("ytDlpArchiveFile", gdriveRoot + (os.contains("win") ? "\\Music\\DataSyncYTDownloader\\archive.txt" : "/Music/DataSyncYTDownloader/archive.txt"));
        } else {
            detected.put("sharedReadyDir", "");
            detected.put("sharedImportedDir", "");
            detected.put("sharedFailedDir", "");
            detected.put("sharedQueueDir", "");
            detected.put("ytDlpArchiveFile", defaultWorkDir + (os.contains("win") ? "\\archive.txt" : "/archive.txt"));
        }

        response.put("detectedPaths", detected);

        // Dependency checks
        response.put("ytdlpAvailable", isCommandAvailable(properties.getYtdlpBinary(), "--version"));
        response.put("ffmpegAvailable", isCommandAvailable(properties.getFfmpegBinary(), "-version"));

        // Current config (with masked Telegram token)
        Map<String, Object> currentConfig = new HashMap<>();
        currentConfig.put("setupMode", properties.getSetupMode());
        currentConfig.put("machineName", properties.getMachineName());
        currentConfig.put("isMasterMusicMachine", properties.isMasterMusicMachine());
        currentConfig.put("workDir", properties.getWorkDir());
        currentConfig.put("googleDriveRoot", properties.getGoogleDriveRoot());
        currentConfig.put("sharedReadyDir", properties.getSharedReadyDir());
        currentConfig.put("sharedImportedDir", properties.getSharedImportedDir());
        currentConfig.put("sharedFailedDir", properties.getSharedFailedDir());
        currentConfig.put("sharedQueueDir", properties.getSharedQueueDir());
        currentConfig.put("appleMusicImportDir", properties.getAppleMusicImportDir());
        currentConfig.put("ytDlpArchiveFile", properties.getYtDlpArchiveFile());
        currentConfig.put("telegramEnabled", properties.isTelegramEnabled());
        
        String botToken = properties.getTelegramBotToken();
        currentConfig.put("telegramBotToken", (botToken != null && !botToken.isBlank()) ? "[Configured]" : "");
        currentConfig.put("telegramBotUsername", properties.getTelegramBotUsername());
        currentConfig.put("telegramAllowedUserIds", properties.getTelegramAllowedUserIds());
        
        currentConfig.put("autoStartEnabled", properties.isAutoStartEnabled());
        currentConfig.put("openBrowserOnStartup", properties.isOpenBrowserOnStartup());
        currentConfig.put("runInBackground", properties.isRunInBackground());
        currentConfig.put("appLanguage", properties.getAppLanguage());

        response.put("currentConfig", currentConfig);

        return response;
    }

    @PostMapping("/test-telegram")
    public Map<String, Object> testTelegram(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String token = request.get("token");
        
        if (token == null || token.isBlank()) {
            response.put("success", false);
            response.put("message", "Token is empty");
            return response;
        }

        // If the token is masked, retrieve it from loaded properties
        if ("[Configured]".equalsIgnoreCase(token)) {
            token = properties.getTelegramBotToken();
        }

        if (token == null || token.isBlank()) {
            response.put("success", false);
            response.put("message", "No configured token found");
            return response;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.telegram.org/bot" + token + "/getMe"))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() == 200) {
                response.put("success", true);
                // Extract username from JSON
                String body = httpResponse.body();
                String username = "";
                int userIndex = body.indexOf("\"username\":\"");
                if (userIndex != -1) {
                    int start = userIndex + 12;
                    int end = body.indexOf("\"", start);
                    if (end != -1) {
                        username = body.substring(start, end);
                    }
                }
                response.put("username", username);
            } else {
                response.put("success", false);
                response.put("message", "Telegram API returned status: " + httpResponse.statusCode());
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveConfig(@RequestBody Map<String, Object> config) {
        // Block changes if download jobs are active
        if (queueService.hasActiveJobs()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Cannot modify configuration while downloads are running. Please wait for downloads to complete.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        try {
            String appConfigDir = System.getProperty("APP_CONFIG_DIR", YtDownloaderApplication.getAppConfigDirectory());
            File configDir = new File(appConfigDir);
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            File envFile = new File(configDir, ".env");
            
            // Backup old config
            if (envFile.exists()) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File backupFile = new File(configDir, ".env.backup." + timestamp);
                Files.copy(envFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.info("Backed up configuration to: {}", backupFile.getName());
            }

            // Read existing properties to resolve masked Telegram tokens
            Properties currentProps = new Properties();
            if (envFile.exists()) {
                try (InputStream is = new FileInputStream(envFile)) {
                    currentProps.load(is);
                }
            }

            // Write new configuration
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(envFile), StandardCharsets.UTF_8))) {
                writer.println("# DataSync YT Downloader Configuration");
                writer.println("# Generated at: " + new Date().toString());
                writer.println("SERVER_PORT=" + properties.getWorkDir()); // wait, read from config or keep default
                writer.println("SERVER_PORT=" + (config.get("serverPort") != null ? config.get("serverPort") : "8765"));

                for (Map.Entry<String, Object> entry : config.entrySet()) {
                    String key = entry.getKey();
                    Object valObj = entry.getValue();
                    if (valObj == null) continue;

                    String value = valObj.toString();
                    
                    // Do not overwrite masked Telegram token with the mask
                    if ("TELEGRAM_BOT_TOKEN".equals(key) && "[Configured]".equalsIgnoreCase(value)) {
                        value = currentProps.getProperty("TELEGRAM_BOT_TOKEN", properties.getTelegramBotToken());
                        if (value == null) value = "";
                    }

                    // Format values safely
                    String keyUpper = convertCamelToSnake(key);
                    if ("SERVER_PORT".equals(keyUpper)) continue; // Already written above
                    
                    writer.println(keyUpper + "=" + value);
                }
            }

            // Set dynamic language configuration immediately
            String appLang = (String) config.get("appLanguage");
            if (appLang != null) {
                properties.setAppLanguage(appLang);
            }

            // Install/uninstall autostart
            boolean autoStart = Boolean.parseBoolean(config.get("autoStartEnabled") != null ? config.get("autoStartEnabled").toString() : "false");
            properties.setAutoStartEnabled(autoStart);
            if (autoStart) {
                autostartService.installAutostart();
            } else {
                autostartService.uninstallAutostart();
            }

            // Notify UI if restart is required (almost always true for backend configuration except language)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("restartRequired", true);
            response.put("message", "Configuration saved successfully.");

            // Update loaded app properties
            properties.setSetupCompleted(true);
            System.setProperty("SETUP_COMPLETED", "true");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to save configuration", e);
            Map<String, String> err = new HashMap<>();
            err.put("error", "Failed to save configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @PostMapping("/restart")
    public Map<String, Object> restartApp() {
        log.info("Application restart requested by user.");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Application is shutting down. It will restart automatically if autostart/service is configured.");
        
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
            System.exit(0);
        }).start();

        return response;
    }

    @GetMapping("/translations")
    public Map<String, String> getTranslations(@RequestParam(value = "lang", required = false) String lang) {
        if (lang == null || lang.isBlank()) {
            lang = properties.getAppLanguage();
        }
        if (lang == null || lang.isBlank()) {
            lang = "en-US";
        }
        if (!lang.equals("en-US") && !lang.equals("ja-JP") && !lang.equals("bn-BD")) {
            lang = "en-US";
        }

        Map<String, String> translations = new HashMap<>();
        String resourcePath = "/i18n/messages_" + lang + ".properties";
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                for (String key : props.stringPropertyNames()) {
                    translations.put(key, props.getProperty(key));
                }
            } else {
                log.warn("Translation file not found: {}", resourcePath);
            }
        } catch (IOException e) {
            log.error("Failed to load translations for: " + lang, e);
        }
        return translations;
    }

    private String convertCamelToSnake(String camelCase) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char ch = camelCase.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_').append(ch);
            } else {
                result.append(Character.toUpperCase(ch));
            }
        }
        return result.toString();
    }

    private String detectAppleMusicDir(String os) {
        String userHome = System.getProperty("user.home");
        if (os.contains("mac")) {
            String[] paths = {
                userHome + "/Music/Music/Media.localized/Automatically Add to Music.localized",
                userHome + "/Music/Music/Media/Automatically Add to Music.localized",
                userHome + "/Music/Music/Media/Automatically Add to Music"
            };
            for (String p : paths) {
                File f = new File(p);
                if (f.exists() && f.isDirectory()) {
                    return f.getAbsolutePath();
                }
            }
        } else if (os.contains("win")) {
            String[] paths = {
                System.getenv("USERPROFILE") + "\\Music\\Apple Music\\Media\\Automatically Add to Music",
                System.getenv("USERPROFILE") + "\\Music\\Apple Music\\Media",
                System.getenv("USERPROFILE") + "\\Music\\Apple Music"
            };
            for (String p : paths) {
                File f = new File(p);
                if (f.exists() && f.isDirectory()) {
                    return f.getAbsolutePath();
                }
            }
        }
        return "";
    }

    private String detectGoogleDriveRoot(String os) {
        String userHome = System.getProperty("user.home");
        if (os.contains("mac")) {
            File cloudStorage = new File(userHome, "Library/CloudStorage");
            if (cloudStorage.exists() && cloudStorage.isDirectory()) {
                File[] dirs = cloudStorage.listFiles();
                if (dirs != null) {
                    for (File d : dirs) {
                        if (d.isDirectory() && (d.getName().startsWith("GoogleDrive-") || d.getName().equals("Google Drive"))) {
                            File myDrive = new File(d, "My Drive");
                            if (myDrive.exists() && myDrive.isDirectory()) {
                                return myDrive.getAbsolutePath();
                            }
                        }
                    }
                }
            }
        } else if (os.contains("win")) {
            File gDrive = new File("G:\\My Drive");
            if (gDrive.exists() && gDrive.isDirectory()) {
                return gDrive.getAbsolutePath();
            }
            for (char letter = 'D'; letter <= 'Z'; letter++) {
                File drive = new File(letter + ":\\My Drive");
                if (drive.exists() && drive.isDirectory()) {
                    return drive.getAbsolutePath();
                }
            }
        }
        return "";
    }

    private boolean isCommandAvailable(String command, String versionArg) {
        try {
            if (command == null || command.isBlank()) return false;
            return commandRunner.runCommandAndWait(command, versionArg) == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
