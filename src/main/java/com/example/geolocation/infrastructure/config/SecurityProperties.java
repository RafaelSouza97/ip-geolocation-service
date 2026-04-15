package com.example.geolocation.infrastructure.config;

import com.example.geolocation.application.domain.exception.ErrorCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(JwtProperties jwt, UserProperties user) {
    public record JwtProperties(String secretKey, long expiration) {
        public JwtProperties {
            if (secretKey == null || secretKey.length() < 32) {
                throw new IllegalArgumentException(ErrorCode.JWT_SECRET_TOO_SHORT.getMessage());
            }
            if (expiration <= 0) {
                expiration = 86400000L;
            }
        }
    }

    public record UserProperties(String username, String password) {
        public UserProperties {
            if (username == null || username.isBlank()) {
                username = "admin";
            }
            if (password == null || password.isBlank()) {
                password = "Admin123@";
            }
        }
    }
}

