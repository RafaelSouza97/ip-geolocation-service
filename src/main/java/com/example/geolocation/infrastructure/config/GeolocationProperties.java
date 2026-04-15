package com.example.geolocation.infrastructure.config;

import com.example.geolocation.application.domain.constants.ApiConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "geolocation")
public record GeolocationProperties(
    ProviderProperties providers,
    CacheProperties cache,
    FallbackProperties fallback
) {

    public record ProviderProperties(
        ApiProperties primary,
        ApiProperties secondary,
        Duration failoverDuration
    ) {
        public ProviderProperties {
            if (primary == null) {
                primary = new ApiProperties(
                    ApiConstants.PRIMARY_PROVIDER_NAME,
                    ApiConstants.PRIMARY_PROVIDER_URL,
                    Duration.ofSeconds(5)
                );
            }
            if (secondary == null) {
                secondary = new ApiProperties(
                    ApiConstants.SECONDARY_PROVIDER_NAME,
                    ApiConstants.SECONDARY_PROVIDER_URL,
                    Duration.ofSeconds(5)
                );
            }
            if (failoverDuration == null) {
                failoverDuration = Duration.ofMinutes(5);
            }
        }
    }

    public record ApiProperties(
        String name,
        String url,
        Duration timeout
    ) {
        public ApiProperties {
            if (name == null) {
                name = "unknown";
            }
            if (url == null) {
                url = ApiConstants.PRIMARY_PROVIDER_URL;
            }
            if (timeout == null) {
                timeout = Duration.ofSeconds(5);
            }
        }
    }

    public record CacheProperties(
        Duration ttl,
        int maxSize
    ) {
        public CacheProperties {
            if (ttl == null) {
                ttl = Duration.ofHours(24);
            }
            if (maxSize <= 0) {
                maxSize = 10000;
            }
        }
    }

    public record FallbackProperties(
        String countryCode,
        String countryName
    ) {
        public FallbackProperties {
            if (countryCode == null) {
                countryCode = "BR";
            }
            if (countryName == null) {
                countryName = "Brazil";
            }
        }
    }

    public ApiProperties api() {
        return providers != null ? providers.primary() : new ApiProperties(
            ApiConstants.PRIMARY_PROVIDER_NAME,
            ApiConstants.PRIMARY_PROVIDER_URL,
            Duration.ofSeconds(5)
        );
    }
}

