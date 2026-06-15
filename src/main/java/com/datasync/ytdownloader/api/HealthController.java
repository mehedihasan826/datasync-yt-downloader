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

    public HealthController(AppProperties properties, CommandRunner commandRunner) {
        this.properties = properties;
        this.commandRunner = commandRunner;
    }

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        
        response.put("ytDlpAvailable", isAvailable(properties.getYtdlpBinary(), "--version"));
        response.put("ffmpegAvailable", isAvailable(properties.getFfmpegBinary(), "-version"));
        response.put("ffprobeAvailable", isAvailable(properties.getFfprobeBinary(), "-version"));
        
        response.put("workDirExists", new File(properties.getWorkDir()).exists());
        response.put("musicImportDirExists", new File(properties.getMusicImportDir()).exists());

        return response;
    }

    private boolean isAvailable(String command, String versionArg) {
        try {
            return commandRunner.runCommandAndWait(command, versionArg) == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
