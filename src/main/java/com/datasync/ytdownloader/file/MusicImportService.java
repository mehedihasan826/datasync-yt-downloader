package com.datasync.ytdownloader.file;

import com.datasync.ytdownloader.config.AppProperties;

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

    public File importFile(File sourceFile) throws Exception {
        String originalName = sourceFile.getName();
        String baseName = originalName.endsWith(".m4a") ? originalName.substring(0, originalName.length() - 4) : originalName;

        File importDir = new File(properties.getMusicImportDir());
        if (!importDir.exists()) {
            importDir.mkdirs();
        }

        File targetFile = new File(importDir, originalName);
        int counter = 1;
        while (targetFile.exists()) {
            targetFile = new File(importDir, baseName + " (" + counter + ").m4a");
            counter++;
        }

        log.info("Moving file to {}", targetFile.getAbsolutePath());
        Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return targetFile;
    }
}
