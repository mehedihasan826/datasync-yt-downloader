package com.datasync.ytdownloader;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class YtDownloaderApplication {

    public static void main(String[] args) {
        // Load .env explicitly into system properties so Spring can resolve them
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        Map<String, Object> envMap = new HashMap<>();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            envMap.put(entry.getKey(), entry.getValue());
        });

        SpringApplication app = new SpringApplication(YtDownloaderApplication.class);
        app.setDefaultProperties(envMap);
        app.run(args);
    }
}
