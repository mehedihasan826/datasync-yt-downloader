package com.datasync.ytdownloader.download;

import com.datasync.ytdownloader.config.AppProperties;
import com.datasync.ytdownloader.queue.DownloadJob;
import com.datasync.ytdownloader.queue.DownloadJobStatus;
import com.datasync.ytdownloader.util.CommandRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YtDlpService {

    private static final Logger log = LoggerFactory.getLogger(YtDlpService.class);

    private final AppProperties properties;
    private final CommandRunner commandRunner;
    private final ObjectMapper objectMapper;

    // Regex patterns for parsing yt-dlp output
    private static final Pattern PLAYLIST_PROGRESS_PATTERN = Pattern.compile("\\[download\\] Downloading (?:item|video) (\\d+) of (\\d+)");
    private static final Pattern PERCENT_PATTERN = Pattern.compile("\\[download\\]\\s+(\\d+\\.\\d+)%\\s+.*?(?:at\\s+([^\\s]+))?\\s+(?:ETA\\s+([^\\s]+))?");
    private static final Pattern DESTINATION_PATTERN = Pattern.compile("\\[download\\] Destination: (.*)");
    private static final Pattern EXTRACT_URL_PATTERN = Pattern.compile("\\[youtube\\] Extracting URL: .*");
    private static final Pattern WEBPAGE_PATTERN = Pattern.compile("\\[youtube\\] .*: Downloading webpage");

    public YtDlpService(AppProperties properties, CommandRunner commandRunner, ObjectMapper objectMapper) {
        this.properties = properties;
        this.commandRunner = commandRunner;
        this.objectMapper = objectMapper;
    }

    public List<DownloadResult> download(DownloadJob job, Runnable progressCallback) throws Exception {
        File jobDir = new File(properties.getWorkDir(), "jobs/" + job.getId());
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

        File archiveFile = resolveAndMergeArchiveFile();
        command.add("--download-archive"); command.add(archiveFile.getAbsolutePath());
        
        File downloadedFilesLog = new File(jobDir, "downloaded-files.txt");
        command.add("--print-to-file"); command.add("after_move:filepath"); command.add(downloadedFilesLog.getAbsolutePath());

        String url = job.getUrl();
        if (job.isPlaylist()) {
            command.add("--yes-playlist");
            command.add("--playlist-end"); command.add(String.valueOf(properties.getMaxPlaylistItems()));
            command.add("--output"); command.add(jobDir.toPath().resolve("%(playlist_index)03d - %(title).200U [%(id)s].%(ext)s").toString());
        } else {
            command.add("--no-playlist");
            command.add("--output"); command.add(jobDir.toPath().resolve("%(title).200U [%(id)s].%(ext)s").toString());

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

        CommandRunner.CommandResult result = commandRunner.runCommandAndWaitWithOutput(line -> {
            parseOutputLine(job, line);
            if (progressCallback != null) {
                progressCallback.run();
            }
        }, command.toArray(new String[0]));
        if (result.exitCode != 0) {
            throw new RuntimeException("yt-dlp exited with code " + result.exitCode + "\nLogs:\n" + result.output);
        }

        List<DownloadResult> results = parseDownloadedFilesLog(downloadedFilesLog);
        if (results.isEmpty()) {
            throw new RuntimeException("yt-dlp completed, but no .m4a files were found in output log.");
        }
        return results;
    }

    private void parseOutputLine(DownloadJob job, String line) {
        job.setLastLogLine(line);

        if (line.contains("[ExtractAudio]") || line.contains("[Metadata]") || line.contains("[EmbedThumbnail]")) {
            job.setPhase(DownloadJobStatus.POST_PROCESSING);
            return;
        }

        Matcher playlistMatcher = PLAYLIST_PROGRESS_PATTERN.matcher(line);
        if (playlistMatcher.find()) {
            job.setPhase(DownloadJobStatus.EXTRACTING);
            int index = Integer.parseInt(playlistMatcher.group(1));
            int total = Integer.parseInt(playlistMatcher.group(2));
            job.setPlaylistIndex(index);
            job.setPlaylistTotal(total);
            job.setMessage("Downloading item " + index + " of " + total);
            return;
        }

        Matcher destMatcher = DESTINATION_PATTERN.matcher(line);
        if (destMatcher.find()) {
            String dest = destMatcher.group(1);
            File destFile = new File(dest);
            job.setCurrentTitle(destFile.getName());
            return;
        }

        if (EXTRACT_URL_PATTERN.matcher(line).matches() || WEBPAGE_PATTERN.matcher(line).matches()) {
            job.setPhase(DownloadJobStatus.EXTRACTING);
            return;
        }

        Matcher percentMatcher = PERCENT_PATTERN.matcher(line);
        if (percentMatcher.find()) {
            job.setPhase(DownloadJobStatus.DOWNLOADING);
            try {
                double percent = Double.parseDouble(percentMatcher.group(1));
                job.setCurrentPercent(percent);
                
                String speed = percentMatcher.group(2);
                if (speed != null) job.setSpeed(speed);
                
                String eta = percentMatcher.group(3);
                if (eta != null) job.setEta(eta);

                if (job.getPlaylistTotal() != null && job.getPlaylistIndex() != null) {
                    double overall = ((job.getPlaylistIndex() - 1) + (percent / 100.0)) / job.getPlaylistTotal() * 100.0;
                    job.setOverallPercent(overall);
                } else if (!job.isPlaylist()) {
                    job.setOverallPercent(percent);
                }
            } catch (Exception ignored) {}
        }
    }

    private File resolveAndMergeArchiveFile() {
        File localArchive = new File(properties.getWorkDir(), "archive.txt");
        File sharedArchive;

        String configuredShared = properties.getYtDlpArchiveFile();
        if (configuredShared != null && !configuredShared.isBlank()) {
            sharedArchive = new File(configuredShared);
        } else if (properties.getGoogleDriveRoot() != null && !properties.getGoogleDriveRoot().isBlank()) {
            sharedArchive = new File(properties.getGoogleDriveRoot(), "Music/DataSyncYTDownloader/archive.txt");
        } else {
            return localArchive;
        }

        if (localArchive.exists()) {
            try {
                if (!sharedArchive.getParentFile().exists()) {
                    sharedArchive.getParentFile().mkdirs();
                }
                
                java.util.Set<String> lines = new java.util.LinkedHashSet<>();
                if (sharedArchive.exists()) {
                    lines.addAll(java.nio.file.Files.readAllLines(sharedArchive.toPath(), java.nio.charset.StandardCharsets.UTF_8));
                }
                
                List<String> localLines = java.nio.file.Files.readAllLines(localArchive.toPath(), java.nio.charset.StandardCharsets.UTF_8);
                boolean addedNew = false;
                for (String line : localLines) {
                    line = line.trim();
                    if (!line.isEmpty() && lines.add(line)) {
                        addedNew = true;
                    }
                }
                
                if (addedNew || !sharedArchive.exists()) {
                    java.nio.file.Files.write(sharedArchive.toPath(), lines, java.nio.charset.StandardCharsets.UTF_8);
                    log.info("Merged local yt-dlp archive into shared archive.");
                }
            } catch (Exception e) {
                log.warn("Failed to merge local archive into shared archive", e);
            }
        } else if (!sharedArchive.exists()) {
            try {
                if (!sharedArchive.getParentFile().exists()) {
                    sharedArchive.getParentFile().mkdirs();
                }
                sharedArchive.createNewFile();
            } catch (Exception e) {
                log.warn("Failed to create shared archive", e);
            }
        }

        return sharedArchive;
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
