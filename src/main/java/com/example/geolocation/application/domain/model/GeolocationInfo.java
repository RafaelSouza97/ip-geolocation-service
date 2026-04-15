package com.example.geolocation.application.domain.model;

import java.time.Instant;
import java.util.Objects;
import com.example.geolocation.application.domain.exception.ErrorCode;

public record GeolocationInfo(String ip, Country country, Region region, String city,
        Coordinates coordinates, String timezone, String isp, DataSource source,
        Instant timestamp) {
    public GeolocationInfo {
        Objects.requireNonNull(ip, ErrorCode.IP_NULL.getMessage());
        Objects.requireNonNull(country, ErrorCode.COUNTRY_NULL.getMessage());
        Objects.requireNonNull(source, ErrorCode.SOURCE_NULL.getMessage());

        if (region == null) {
            region = new Region("", "");
        }
        if (city == null) {
            city = "";
        }
        if (coordinates == null) {
            coordinates = Coordinates.zero();
        }
        if (timezone == null) {
            timezone = "";
        }
        if (isp == null) {
            isp = "";
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public GeolocationInfo withCacheSource() {
        return new GeolocationInfo(ip, country, region, city, coordinates, timezone, isp,
                DataSource.CACHE, timestamp);
    }

    public boolean isFallback() {
        return DataSource.FALLBACK.equals(source);
    }

    public String sourceValue() {
        return source.getValue();
    }
}

