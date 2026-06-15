package com.datasync.ytdownloader.bot;

import com.datasync.ytdownloader.config.AppProperties;
import com.datasync.ytdownloader.queue.DownloadQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.Arrays;
import java.util.List;

@Service
public class TelegramBotService implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);

    private final AppProperties properties;
    private final DownloadQueueService queueService;
    private TelegramClient telegramClient;
    private TelegramBotsLongPollingApplication botsApplication;

    private List<String> allowedUserIds;

    public TelegramBotService(AppProperties properties, DownloadQueueService queueService) {
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

        try {
            log.info("Starting Telegram Bot...");
            telegramClient = new OkHttpTelegramClient(properties.getTelegramBotToken());
            botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(properties.getTelegramBotToken(), this);
            log.info("Telegram Bot started successfully.");
        } catch (Exception e) {
            log.error("Failed to start Telegram bot", e);
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

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String userId = String.valueOf(update.getMessage().getFrom().getId());

            if (!allowedUserIds.contains(userId)) {
                sendMessage(chatId, "You are not allowed to use this bot.");
                return;
            }

            if (text.startsWith("http") && text.contains("youtube.com") || text.contains("youtu.be")) {
                boolean isPlaylist = text.contains("list=");
                String jobId = queueService.queueJob(text, isPlaylist, "telegram");
                sendMessage(chatId, "Queued job: " + jobId + "\nYou can check status on the local web UI.");
            } else {
                sendMessage(chatId, "Please send a valid YouTube video or playlist URL.");
            }
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        try {
            if (telegramClient != null) {
                telegramClient.execute(message);
            }
        } catch (Exception e) {
            log.error("Failed to send Telegram message", e);
        }
    }
}
