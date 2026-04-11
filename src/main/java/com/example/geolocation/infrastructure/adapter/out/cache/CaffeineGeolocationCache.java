package com.example.geolocation.infrastructure.adapter.out.cache;

import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.port.out.GeolocationCache;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementação de cache usando Caffeine.
 */
@Slf4j
@Component
public class CaffeineGeolocationCache implements GeolocationCache {

    private final Cache<String, GeolocationInfo> cache;

    public CaffeineGeolocationCache(Cache<String, GeolocationInfo> cache) {
        this.cache = cache;
    }

    @Override
    public Optional<GeolocationInfo> get(String ip) {
        var result = cache.getIfPresent(ip);
        if (result != null) {
            log.debug("Cache hit for IP: {}", ip);
        } else {
            log.debug("Cache miss for IP: {}", ip);
        }
        return Optional.ofNullable(result);
    }

    @Override
    public void put(String ip, GeolocationInfo info) {
        log.debug("Caching geolocation for IP: {}", ip);
        cache.put(ip, info);
    }

    @Override
    public void evict(String ip) {
        log.debug("Evicting cache for IP: {}", ip);
        cache.invalidate(ip);
    }

    @Override
    public void clear() {
        log.info("Clearing geolocation cache");
        cache.invalidateAll();
    }
}
