package com.datasync.ytdownloader.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/download")
                .allowedOriginPatterns("chrome-extension://*", "http://localhost:8765")
                .allowedMethods("POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
                
        registry.addMapping("/api/health")
                .allowedOriginPatterns("chrome-extension://*", "http://localhost:8765")
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
                
        registry.addMapping("/api/jobs")
                .allowedOriginPatterns("chrome-extension://*", "http://localhost:8765")
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
