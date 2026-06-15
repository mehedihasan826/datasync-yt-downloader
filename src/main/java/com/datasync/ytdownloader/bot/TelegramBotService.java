package com.datasync.ytdownloader.bot;

import com.datasync.ytdownloader.config.AppProperties;
import com.datasync.ytdownloader.queue.DownloadJob;
import com.datasync.ytdownloader.queue.DownloadJobStatus;
import com.datasync.ytdownloader.queue.DownloadQueueService;
import com.datasync.ytdownloader.queue.JobProgressListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramBotService implements LongPollingSingleThreadUpdateConsumer, JobProgressListener {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);

    private final AppProperties properties;
    private final DownloadQueueService queueService;
    private TelegramClient telegramClient;
    private TelegramBotsLongPollingApplication botsApplication;

    private List<String> allowedUserIds;

    private File lastUpdateIdFile;
    private File processedMessagesFile;
    private long lastUpdateId = 0;
    private Set<String> processedMessageIds = ConcurrentHashMap.newKeySet();
    
    private final ConcurrentHashMap<Integer, Instant> lastEditTimes = new ConcurrentHashMap<>();

    public TelegramBotService(AppProperties properties, @Lazy DownloadQueueService queueService) {
        this.properties = properties;
        this.queueService = queueService;
    }

    @PostConstruct
    public void start() {
        if (!properties.isTelegramEnabled()) {
            log.info("Telegram bot is disabled in config.");
            return;
        }

        if (properties.getTelegramAllowedUserIds() != null && !properties.getTelegramAllowedUserIds().isBlank()) {
            allowedUserIds = Arrays.asList(properties.getTelegramAllowedUserIds().split(","));
        } else {
            allowedUserIds = List.of();
        }

        lastUpdateIdFile = new File(properties.getWorkDir(), "telegram-last-update-id.txt");
        processedMessagesFile = new File(properties.getWorkDir(), "telegram-processed-messages.txt");

        loadState();

        if (properties.isTelegramPollingEnabled()) {
            try {
                log.info("Starting Telegram Bot...");
                telegramClient = new OkHttpTelegramClient(properties.getTelegramBotToken());
                botsApplication = new TelegramBotsLongPollingApplication();
                botsApplication.registerBot(properties.getTelegramBotToken(), this);
                log.info("Telegram Bot started successfully. Listening for updates...");
                
                queueService.addListener(this);
            } catch (Exception e) {
                log.error("Failed to start Telegram bot", e);
            }
        } else {
            log.info("Telegram polling is disabled in config. Bot will not receive messages.");
        }
    }

    @PreDestroy
    public void stop() {
        if (botsApplication != null) {
            try {
                botsApplication.stop();
            } catch (Exception e) {
                log.error("Error stopping Telegram bot", e);
            }
        }
    }

    private void loadState() {
        try {
            if (lastUpdateIdFile.exists()) {
                String val = Files.readString(lastUpdateIdFile.toPath()).trim();
                if (!val.isEmpty()) {
                    lastUpdateId = Long.parseLong(val);
                }
            }
            if (processedMessagesFile.exists()) {
                List<String> lines = Files.readAllLines(processedMessagesFile.toPath());
                processedMessageIds.addAll(lines);
            }
        } catch (Exception e) {
            log.warn("Could not load Telegram bot state", e);
        }
    }

    private void saveLastUpdateId(long updateId) {
        if (updateId <= lastUpdateId) return;
        lastUpdateId = updateId;
        try {
            Files.writeString(lastUpdateIdFile.toPath(), String.valueOf(lastUpdateId));
        } catch (Exception e) {
            log.error("Failed to save telegram last update id", e);
        }
    }

    private void saveProcessedMessage(String msgId) {
        processedMessageIds.add(msgId);
        try {
            Files.writeString(processedMessagesFile.toPath(), msgId + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            log.error("Failed to save telegram processed message", e);
        }
    }

    @Override
    public void consume(Update update) {
        if (update.getUpdateId() <= lastUpdateId) {
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String text = message.getText().trim();
            Long chatId = message.getChatId();
            String userId = String.valueOf(message.getFrom().getId());
            String msgIdKey = chatId + ":" + message.getMessageId();

            if (processedMessageIds.contains(msgIdKey)) {
                saveLastUpdateId(update.getUpdateId());
                return;
            }

            if (!allowedUserIds.contains(userId)) {
                sendMessage(chatId, "Unauthorized.");
                saveProcessedMessage(msgIdKey);
                saveLastUpdateId(update.getUpdateId());
                return;
            }

            handleCommandOrUrl(chatId, userId, message.getMessageId(), text);
            saveProcessedMessage(msgIdKey);
        }

        saveLastUpdateId(update.getUpdateId());
    }

    private void handleCommandOrUrl(Long chatId, String userId, Integer messageId, String text) {
        if (text.startsWith("/start") || text.startsWith("/help")) {
            sendMessage(chatId, "DataSync YT Downloader is ready.\nSend a YouTube link to download.\nUse /video <url> for single video.\nUse /mix <url> for playlist/mix.");
            return;
        }

        if (text.startsWith("/status")) {
            long queueCount = queueService.getAllJobs().stream().filter(j -> j.getStatus() == DownloadJobStatus.QUEUED).count();
            sendMessage(chatId, String.format("Machine: %s\nMaster: %s\nQueue: %d", 
                properties.getMachineName(), properties.isMasterMusicMachine(), queueCount));
            return;
        }

        boolean isPlaylist = false;
        String url = text;

        if (text.startsWith("/video ")) {
            url = text.substring(7).trim();
            isPlaylist = false;
        } else if (text.startsWith("/mix ") || text.startsWith("/playlist ")) {
            url = text.substring(text.indexOf(" ") + 1).trim();
            isPlaylist = true;
        } else {
            if (url.contains("list=") || url.contains("start_radio=1") || url.contains("/playlist?list=")) {
                isPlaylist = true;
            }
        }

        if (url.startsWith("http") && (url.contains("youtube.com") || url.contains("youtu.be"))) {
            Message reply = sendMessage(chatId, "Queued ✅\nURL: " + url + "\nStatus: Waiting");
            
            String jobId = queueService.queueJob(url, isPlaylist, "telegram:" + chatId);
            DownloadJob job = queueService.getJob(jobId);
            
            if (reply != null) {
                // If the job wasn't created synchronously, wait a beat and fetch it
                int retries = 0;
                while (job == null && retries < 5) {
                    try { Thread.sleep(200); } catch (Exception ignored) {}
                    job = queueService.getJob(jobId);
                    retries++;
                }

                if (job != null) {
                    job.setTelegramChatId(String.valueOf(chatId));
                    job.setTelegramUserId(userId);
                    job.setTelegramMessageId(messageId);
                    job.setTelegramStatusMessageId(reply.getMessageId());
                }
            }
        } else {
            sendMessage(chatId, "Please send a valid YouTube video or playlist URL.");
        }
    }

    private Message sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        try {
            if (telegramClient != null) {
                return telegramClient.execute(message);
            }
        } catch (Exception e) {
            log.error("Failed to send Telegram message", e);
        }
        return null;
    }

    @Override
    public void onProgressUpdate(DownloadJob job) {
        if (!"edit_message".equals(properties.getTelegramStatusUpdateMode())) {
            return;
        }

        String chatId = job.getTelegramChatId();
        Integer statusMsgId = job.getTelegramStatusMessageId();

        if (chatId == null || statusMsgId == null || telegramClient == null) {
            return;
        }

        DownloadJobStatus status = job.getStatus();
        boolean isTerminal = (status == DownloadJobStatus.COMPLETED || status == DownloadJobStatus.FAILED || status == DownloadJobStatus.CANCELLED);

        Instant lastEdit = lastEditTimes.get(statusMsgId);
        if (!isTerminal && lastEdit != null && Duration.between(lastEdit, Instant.now()).getSeconds() < 5) {
            return; // Throttle to 5 seconds
        }

        String text = buildStatusMessage(job);

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(statusMsgId)
                .text(text)
                .build();

        try {
            telegramClient.execute(editMessage);
            lastEditTimes.put(statusMsgId, Instant.now());
        } catch (Exception e) {
            String err = e.getMessage();
            if (err != null && !err.contains("message is not modified")) {
                log.warn("Failed to edit Telegram status message", e);
            }
        }
    }

    private String buildStatusMessage(DownloadJob job) {
        StringBuilder sb = new StringBuilder();
        
        switch (job.getStatus()) {
            case QUEUED:
                sb.append("Queued ✅\n");
                sb.append("Status: Waiting");
                break;
            case DOWNLOADING:
            case EXTRACTING:
                sb.append("Downloading ⬇️\n");
                if (job.getPlaylistIndex() != null && job.getPlaylistTotal() != null) {
                    sb.append("Item: ").append(job.getPlaylistIndex()).append("/").append(job.getPlaylistTotal()).append("\n");
                }
                if (job.getCurrentPercent() != null) {
                    sb.append(String.format("Current: %.1f%%\n", job.getCurrentPercent()));
                }
                if (job.getOverallPercent() != null) {
                    sb.append(String.format("Overall: %.1f%%\n", job.getOverallPercent()));
                }
                if (job.getSpeed() != null) sb.append("Speed: ").append(job.getSpeed()).append("\n");
                if (job.getEta() != null) sb.append("ETA: ").append(job.getEta()).append("\n");
                break;
            case POST_PROCESSING:
                sb.append("Importing to Apple Music 🎵\n");
                sb.append("Downloaded: ").append(job.getDownloadedFileCount()).append("\n");
                sb.append("Imported: ").append(job.getImportedFileCount()).append("\n");
                break;
            case COMPLETED:
                sb.append("Completed ✅\n");
                sb.append("Downloaded: ").append(job.getDownloadedFileCount()).append("\n");
                sb.append("Imported: ").append(job.getImportedFileCount()).append("\n");
                if (job.getFailedFileCount() > 0) {
                    sb.append("Failed: ").append(job.getFailedFileCount()).append("\n");
                }
                if (job.getDownloadedFileCount() == 0 && job.getMessage().contains("No new files")) {
                    sb.append("\nAlready Downloaded/Skipped.\n");
                }
                break;
            case FAILED:
                sb.append("Failed ❌\n");
                sb.append("Reason: ").append(job.getMessage());
                break;
            case CANCELLED:
                sb.append("Cancelled ⏹\n");
                break;
        }

        return sb.toString().trim();
    }
}
