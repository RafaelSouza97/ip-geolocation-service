package com.example.geolocation.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "geolocation")
public record GeolocationProperties(
    ApiProperties api,
    CacheProperties cache,
    FallbackProperties fallback
) {
    public record ApiProperties(
        String url,
        Duration timeout
    ) {
        public ApiProperties {
            if (url == null) url = "http://ip-api.com/json";
            if (timeout == null) timeout = Duration.ofSeconds(5);
        }
    }

    public record CacheProperties(
        Duration ttl,
        int maxSize
    ) {
        public CacheProperties {
            if (ttl == null) ttl = Duration.ofHours(24);
            if (maxSize <= 0) maxSize = 10000;
        }
    }

    public record FallbackProperties(
        String countryCode,
        String countryName
    ) {
        public FallbackProperties {
            if (countryCode == null) countryCode = "BR";
            if (countryName == null) countryName = "Brazil";
        }
    }
}
