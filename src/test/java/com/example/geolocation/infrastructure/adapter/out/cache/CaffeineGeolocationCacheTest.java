package com.example.geolocation.infrastructure.adapter.out.cache;

import com.example.geolocation.application.domain.model.Coordinates;
import com.example.geolocation.application.domain.model.Country;
import com.example.geolocation.application.domain.model.DataSource;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.domain.model.Region;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CaffeineGeolocationCache")
class CaffeineGeolocationCacheTest {

    private CaffeineGeolocationCache cache;
    private Cache<String, GeolocationInfo> caffeineCache;

    @BeforeEach
    void setUp() {
        caffeineCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(1))
            .maximumSize(100)
            .build();
        cache = new CaffeineGeolocationCache(caffeineCache);
    }

    private GeolocationInfo createGeolocationInfo(String ip) {
        return new GeolocationInfo(
            ip,
            new Country("US", "United States"),
            new Region("CA", "California"),
            "Mountain View",
            new Coordinates(37.4056, -122.0775),
            "America/Los_Angeles",
            "Google LLC",
            DataSource.API,
            Instant.now()
        );
    }

    @Test
    @DisplayName("should return empty optional when key not found")
    void shouldReturnEmptyWhenKeyNotFound() {
        var result = cache.get("8.8.8.8");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return value when key exists")
    void shouldReturnValueWhenKeyExists() {
        var ip = "8.8.8.8";
        var info = createGeolocationInfo(ip);
        cache.put(ip, info);
        var result = cache.get(ip);
        assertTrue(result.isPresent());
        assertEquals(ip, result.get().ip());
    }

    @Test
    @DisplayName("should store value correctly")
    void shouldStoreValueCorrectly() {
        var ip = "8.8.8.8";
        var info = createGeolocationInfo(ip);
        cache.put(ip, info);
        assertNotNull(caffeineCache.getIfPresent(ip));
    }

    @Test
    @DisplayName("should evict value")
    void shouldEvictValue() {
        var ip = "8.8.8.8";
        var info = createGeolocationInfo(ip);
        cache.put(ip, info);
        cache.evict(ip);
        assertTrue(cache.get(ip).isEmpty());
    }

    @Test
    @DisplayName("should clear all values")
    void shouldClearAllValues() {
        cache.put("8.8.8.8", createGeolocationInfo("8.8.8.8"));
        cache.put("1.1.1.1", createGeolocationInfo("1.1.1.1"));
        cache.clear();
        assertTrue(cache.get("8.8.8.8").isEmpty());
        assertTrue(cache.get("1.1.1.1").isEmpty());
    }
}
