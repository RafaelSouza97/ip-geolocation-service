package com.example.geolocation.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de segurança JWT.
 */
@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
    JwtProperties jwt,
    UserProperties user
) {
    public record JwtProperties(
        String secretKey,
        long expiration
    ) {
        public JwtProperties {
            if (secretKey == null || secretKey.length() < 32) {
                throw new IllegalArgumentException("JWT secret key must be at least 32 characters");
            }
            if (expiration <= 0) expiration = 86400000L; // 24h default
        }
    }

    public record UserProperties(
        String username,
        String password
    ) {
        public UserProperties {
            if (username == null || username.isBlank()) username = "admin";
            if (password == null || password.isBlank()) password = "Admin123@";
        }
    }
}
