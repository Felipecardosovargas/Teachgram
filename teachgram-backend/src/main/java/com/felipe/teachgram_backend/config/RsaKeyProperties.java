package com.felipe.teachgram_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "application.security.jwt")
public class RsaKeyProperties {
    private String privateKey;
    private String publicKey;
    private Integer expirationMinutes;
}
