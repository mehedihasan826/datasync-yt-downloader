package com.datasync.ytdownloader.download;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class YoutubeUrlDetector {
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile(
            "(?:v=|/v/|youtu\\.be/|/embed/|/shorts/)([^&\\n?#]+)");

    public String extractVideoId(String url) {
        if (url == null) return null;
        Matcher matcher = VIDEO_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
