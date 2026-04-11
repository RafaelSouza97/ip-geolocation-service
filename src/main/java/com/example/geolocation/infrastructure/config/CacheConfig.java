package com.example.geolocation.infrastructure.config;

import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do cache Caffeine.
 */
@Slf4j
@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, GeolocationInfo> geolocationCache(GeolocationProperties properties) {
        var cacheProps = properties.cache();
        log.info("Configuring Caffeine cache: TTL={}, maxSize={}", 
            cacheProps.ttl(), cacheProps.maxSize());
        
        return Caffeine.newBuilder()
            .expireAfterWrite(cacheProps.ttl())
            .maximumSize(cacheProps.maxSize())
            .recordStats()
            .build();
    }
}
