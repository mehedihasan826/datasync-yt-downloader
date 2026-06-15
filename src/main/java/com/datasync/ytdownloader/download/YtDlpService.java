package com.datasync.ytdownloader.download;

import com.datasync.ytdownloader.config.AppProperties;
import com.datasync.ytdownloader.util.CommandRunner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class YtDlpService {

    private static final Logger log = LoggerFactory.getLogger(YtDlpService.class);

    private final AppProperties properties;
    private final CommandRunner commandRunner;
    private final ObjectMapper objectMapper;

    public YtDlpService(AppProperties properties, CommandRunner commandRunner, ObjectMapper objectMapper) {
        this.properties = properties;
        this.commandRunner = commandRunner;
        this.objectMapper = objectMapper;
    }

    public List<DownloadResult> download(String url, boolean isPlaylist, String jobId) throws Exception {
        File jobDir = new File(properties.getWorkDir(), "jobs/" + jobId);
        if (!jobDir.exists() && !jobDir.mkdirs()) {
            throw new RuntimeException("Failed to create job directory: " + jobDir.getAbsolutePath());
        }

        List<String> command = new ArrayList<>();
        command.add(properties.getYtdlpBinary());
        command.add("-f"); command.add("bestaudio");
        command.add("-x");
        command.add("--audio-format"); command.add("m4a");
        command.add("--audio-quality"); command.add("0");
        command.add("--encoding"); command.add("utf-8");
        
        if (properties.isYtdlpEmbedMetadata()) {
            command.add("--embed-metadata");
            command.add("--parse-metadata"); command.add("%(uploader|)s:%(meta_artist)s");
        }
        if (properties.isYtdlpEmbedThumbnail()) {
            command.add("--embed-thumbnail");
            command.add("--convert-thumbnails"); command.add("jpg");
        }
        if (properties.isKeepInfoJson()) {
            command.add("--write-info-json");
        }

        command.add("--download-archive"); command.add(new File(properties.getWorkDir(), "archive.txt").getAbsolutePath());
        command.add("--no-progress");
        
        File downloadedFilesLog = new File(jobDir, "downloaded-files.txt");
        command.add("--print-to-file"); command.add("after_move:filepath"); command.add(downloadedFilesLog.getAbsolutePath());

        if (isPlaylist) {
            command.add("--yes-playlist");
            command.add("--playlist-end"); command.add(String.valueOf(properties.getMaxPlaylistItems()));
            command.add("--output"); command.add(jobDir.toPath().resolve("%(playlist_index)03d - %(title).200U [%(id)s].%(ext)s").toString());
        } else {
            command.add("--no-playlist");
            command.add("--output"); command.add(jobDir.toPath().resolve("%(title).200U [%(id)s].%(ext)s").toString());

            // Strip playlist parameters for single video mode
            if (url.contains("youtube.com/watch")) {
                url = url.replaceAll("(?i)(?:&|\\?)(?:list|index)=[^&]*", "");
                if (url.contains("&") && !url.contains("?")) {
                    url = url.replaceFirst("&", "?");
                }
            }
        }
        
        String ffmpegBinary = properties.getFfmpegBinary();
        if (ffmpegBinary != null && !ffmpegBinary.equalsIgnoreCase("ffmpeg") && !ffmpegBinary.equalsIgnoreCase("ffmpeg.exe")) {
            command.add("--ffmpeg-location"); command.add(ffmpegBinary);
        }
        command.add(url);

        log.info("Running yt-dlp: {}", String.join(" ", command));

        CommandRunner.CommandResult result = commandRunner.runCommandAndWaitWithOutput(command.toArray(new String[0]));
        if (result.exitCode != 0) {
            throw new RuntimeException("yt-dlp exited with code " + result.exitCode + "\nLogs:\n" + result.output);
        }

        return parseDownloadedFilesLog(downloadedFilesLog);
    }

    private List<DownloadResult> parseDownloadedFilesLog(File downloadedFilesLog) {
        List<DownloadResult> results = new ArrayList<>();
        if (!downloadedFilesLog.exists()) {
            return results;
        }

        try {
            List<String> lines = java.nio.file.Files.readAllLines(downloadedFilesLog.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                File m4aFile = new File(line);
                if (m4aFile.exists() && m4aFile.getName().endsWith(".m4a")) {
                    // Extract ID and Title from filename: %(title)s [%(id)s].m4a
                    String title = m4aFile.getName().replace(".m4a", "");
                    String videoId = null;
                    int start = title.lastIndexOf('[');
                    int end = title.lastIndexOf(']');
                    if (start != -1 && end != -1 && end > start) {
                        videoId = title.substring(start + 1, end);
                        title = title.substring(0, start).trim();
                    }
                    results.add(new DownloadResult(m4aFile, videoId, title));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to read downloaded-files.txt", e);
        }
        return results;
    }
}
