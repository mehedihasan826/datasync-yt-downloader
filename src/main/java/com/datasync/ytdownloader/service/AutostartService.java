package com.datasync.ytdownloader.service;

import com.datasync.ytdownloader.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;

@Service
public class AutostartService {

    private static final Logger log = LoggerFactory.getLogger(AutostartService.class);
    private final AppProperties properties;

    public AutostartService(AppProperties properties) {
        this.properties = properties;
    }

    public static String getLauncherPath() {
        String javaHome = System.getProperty("java.home");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            if (javaHome.contains(".app/Contents/runtime/Contents/Home")) {
                int index = javaHome.indexOf(".app/Contents/runtime/Contents/Home");
                return javaHome.substring(0, index + 4); // Target the .app folder
            }
        } else if (os.contains("win")) {
            File runtimeDir = new File(javaHome);
            File installDir = runtimeDir.getParentFile();
            if (installDir != null) {
                File exeFile = new File(installDir, "DataSyncYTDownloader.exe");
                if (exeFile.exists()) {
                    return exeFile.getAbsolutePath();
                }
            }
        }
        return null;
    }

    public boolean installAutostart() {
        String os = System.getProperty("os.name").toLowerCase();
        String launcherPath = getLauncherPath();
        
        if (launcherPath == null) {
            log.info("Launcher path not detected (dev mode). Creating autostart config with fallback paths for verification.");
            if (os.contains("win")) {
                launcherPath = new File("scripts/run-windows.ps1").getAbsolutePath();
            } else if (os.contains("mac")) {
                launcherPath = "/Applications/DataSync YT Downloader.app";
            }
        }

        try {
            if (os.contains("win")) {
                String startupFolder = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
                File shortcutFile = new File(startupFolder, "DataSyncYTDownloader.lnk");
                
                String command = String.format(
                    "$WshShell = New-Object -ComObject WScript.Shell; " +
                    "$Shortcut = $WshShell.CreateShortcut('%s'); " +
                    "$Shortcut.TargetPath = '%s'; " +
                    "$Shortcut.WorkingDirectory = '%s'; " +
                    "$Shortcut.Description = 'Start DataSync YT Downloader at login'; " +
                    "$Shortcut.Save()",
                    shortcutFile.getAbsolutePath().replace("'", "''"),
                    launcherPath.replace("'", "''"),
                    new File(launcherPath).getParent().replace("'", "''")
                );

                ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-Command", command);
                Process p = pb.start();
                int exitCode = p.waitFor();
                if (exitCode == 0) {
                    log.info("Successfully created Windows Startup shortcut targeting: {}", launcherPath);
                    return true;
                } else {
                    log.error("PowerShell failed to create Startup shortcut with exit code {}", exitCode);
                    return false;
                }
            } else if (os.contains("mac")) {
                String home = System.getProperty("user.home");
                File launchAgentsDir = new File(home, "Library/LaunchAgents");
                if (!launchAgentsDir.exists()) {
                    launchAgentsDir.mkdirs();
                }
                
                File plistFile = new File(launchAgentsDir, "com.datasync.ytdownloader.plist");
                
                String execPath = launcherPath;
                if (launcherPath.endsWith(".app")) {
                    execPath = launcherPath + "/Contents/MacOS/DataSync YT Downloader";
                }

                String plistContent = String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                    "<plist version=\"1.0\">\n" +
                    "<dict>\n" +
                    "    <key>Label</key>\n" +
                    "    <string>com.datasync.ytdownloader</string>\n" +
                    "    <key>ProgramArguments</key>\n" +
                    "    <array>\n" +
                    "        <string>%s</string>\n" +
                    "    </array>\n" +
                    "    <key>RunAtLoad</key>\n" +
                    "    <true/>\n" +
                    "    <key>KeepAlive</key>\n" +
                    "    <false/>\n" +
                    "</dict>\n" +
                    "</plist>\n",
                    execPath
                );

                try (FileWriter writer = new FileWriter(plistFile)) {
                    writer.write(plistContent);
                }

                ProcessBuilder pb = new ProcessBuilder("launchctl", "load", plistFile.getAbsolutePath());
                Process p = pb.start();
                p.waitFor();
                
                log.info("Successfully created macOS LaunchAgent plist targeting: {}", execPath);
                return true;
            }
        } catch (Exception e) {
            log.error("Failed to install autostart config", e);
        }
        return false;
    }

    public boolean uninstallAutostart() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                String startupFolder = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
                File shortcutFile = new File(startupFolder, "DataSyncYTDownloader.lnk");
                if (shortcutFile.exists()) {
                    boolean deleted = shortcutFile.delete();
                    log.info("Uninstalled Windows Startup shortcut: {}", deleted);
                    return deleted;
                }
            } else if (os.contains("mac")) {
                String home = System.getProperty("user.home");
                File plistFile = new File(home, "Library/LaunchAgents/com.datasync.ytdownloader.plist");
                if (plistFile.exists()) {
                    ProcessBuilder pb = new ProcessBuilder("launchctl", "unload", plistFile.getAbsolutePath());
                    Process p = pb.start();
                    p.waitFor();
                    
                    boolean deleted = plistFile.delete();
                    log.info("Uninstalled macOS LaunchAgent plist: {}", deleted);
                    return deleted;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to uninstall autostart config", e);
        }
        return false;
    }
}
