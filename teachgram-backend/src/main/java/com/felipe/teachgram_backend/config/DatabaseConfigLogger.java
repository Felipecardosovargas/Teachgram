package com.felipe.teachgram_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class DatabaseConfigLogger {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @PostConstruct
    public void log() {
        System.out.println("Datasource URL: " + datasourceUrl);
    }
}
