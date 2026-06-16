package com.datasync.ytdownloader;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class YtDownloaderApplication {

    public static void main(String[] args) {
        String configDir = getAppConfigDirectory();
        File appConfigEnv = new File(configDir, ".env");
        Dotenv dotenv;
        String activeEnvPath;

        if (appConfigEnv.exists()) {
            dotenv = Dotenv.configure()
                    .directory(configDir)
                    .filename(".env")
                    .ignoreIfMissing()
                    .load();
            activeEnvPath = appConfigEnv.getAbsolutePath();
        } else {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            activeEnvPath = new File(".env").getAbsolutePath();
        }

        System.setProperty("ACTIVE_ENV_PATH", activeEnvPath);
        System.setProperty("APP_CONFIG_DIR", configDir);

        Map<String, Object> envMap = new HashMap<>();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            envMap.put(entry.getKey(), entry.getValue());
        });

        SpringApplication app = new SpringApplication(YtDownloaderApplication.class);
        app.setDefaultProperties(envMap);
        app.run(args);
    }

    public static String getAppConfigDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            return appData != null ? appData + "\\DataSyncYTDownloader" : System.getProperty("user.home") + "\\AppData\\Roaming\\DataSyncYTDownloader";
        } else if (os.contains("mac")) {
            return System.getProperty("user.home") + "/Library/Application Support/DataSyncYTDownloader";
        } else {
            return System.getProperty("user.home") + "/.datasync-yt-downloader";
        }
    }

    @Component
    public static class BrowserOpener {
        @EventListener(ApplicationReadyEvent.class)
        public void openBrowserAfterStartup() {
            String setupCompletedStr = System.getProperty("SETUP_COMPLETED");
            boolean setupCompleted = "true".equalsIgnoreCase(setupCompletedStr);
            String openBrowserStr = System.getProperty("OPEN_BROWSER_ON_STARTUP");
            boolean openBrowserOnStartup = openBrowserStr == null || "true".equalsIgnoreCase(openBrowserStr);
            String serverPort = System.getProperty("SERVER_PORT");
            if (serverPort == null || serverPort.isBlank()) {
                serverPort = "8765";
            }

            if (!setupCompleted || openBrowserOnStartup) {
                String url = "http://localhost:" + serverPort + (setupCompleted ? "" : "/setup");
                System.out.println("DataSync: Opening browser to " + url);
                try {
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) {
                        new ProcessBuilder("cmd", "/c", "start", url).start();
                    } else if (os.contains("mac")) {
                        new ProcessBuilder("open", url).start();
                    } else {
                        new ProcessBuilder("xdg-open", url).start();
                    }
                } catch (Exception e) {
                    System.err.println("DataSync: Failed to open browser: " + e.getMessage());
                }
            }
        }
    }
}
