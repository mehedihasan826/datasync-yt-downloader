package com.datasync.ytdownloader.file;

import org.springframework.stereotype.Component;

@Component
public class FileNameSanitizer {

    public String sanitize(String input) {
        if (input == null) return "Unknown";
        
        // Remove characters invalid in Windows/macOS filenames
        String sanitized = input.replaceAll("[\\\\/:*?\"<>|]", "");
        
        // Collapse whitespace
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        
        // Avoid reserved windows names like CON, PRN, AUX, etc.
        if (sanitized.matches("(?i)^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$")) {
            sanitized = sanitized + "_file";
        }
        
        // Limit length to avoid path too long errors
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200).trim();
        }
        
        return sanitized.isEmpty() ? "Unknown" : sanitized;
    }
}
