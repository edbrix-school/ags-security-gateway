package com.als.securityserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "security.jwt")
@Component
public class JwtConfig {

    private String secret;
    private String refreshSecret;
    private long expirationInMs;
    private long refreshExpirationInMs;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRefreshSecret() {
        return refreshSecret;
    }

    public void setRefreshSecret(String refreshSecret) {
        this.refreshSecret = refreshSecret;
    }

    public long getExpirationInMs() {
        return expirationInMs;
    }

    public void setExpirationInMs(long expirationInMs) {
        this.expirationInMs = expirationInMs;
    }

    public long getRefreshExpirationInMs() {
        return refreshExpirationInMs;
    }

    public void setRefreshExpirationInMs(long refreshExpirationInMs) {
        this.refreshExpirationInMs = refreshExpirationInMs;
    }
}

