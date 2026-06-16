package com.datasync.ytdownloader.api;

import com.datasync.ytdownloader.config.AppProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final AppProperties properties;

    public ViewController(AppProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/setup")
    public String setup() {
        return "forward:/setup.html";
    }

    @GetMapping("/settings")
    public String settings() {
        return "forward:/settings.html";
    }

    @GetMapping("/")
    public String index() {
        if (!properties.isSetupCompleted()) {
            return "redirect:/setup";
        }
        return "forward:/index.html";
    }
}
