package com.example.geolocation.application.service;

import java.time.Instant;
import org.springframework.stereotype.Service;
import com.example.geolocation.application.domain.exception.ExternalApiException;
import com.example.geolocation.application.domain.exception.InvalidIpAddressException;
import com.example.geolocation.application.domain.model.Coordinates;
import com.example.geolocation.application.domain.model.Country;
import com.example.geolocation.application.domain.model.DataSource;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.domain.model.Region;
import com.example.geolocation.application.port.in.GeolocationUseCase;
import com.example.geolocation.application.port.out.GeolocationCache;
import com.example.geolocation.application.port.out.GeolocationProvider;
import com.example.geolocation.infrastructure.config.GeolocationProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeolocationService implements GeolocationUseCase {

    @NonNull
    private final GeolocationCache cache;
    @NonNull
    private final GeolocationProvider provider;
    @NonNull
    private final GeolocationProperties properties;

    @Override
    public GeolocationInfo locate(String ip) {
        log.debug("Looking up geolocation for IP: {}", ip);

        if (!IpValidator.isValid(ip)) {
            log.warn("Invalid IP address: {}", ip);
            throw new InvalidIpAddressException(ip);
        }

        String normalizedIp = IpValidator.normalize(ip);

        if (IpValidator.isPrivateOrReserved(normalizedIp)) {
            log.info("Private/reserved IP detected, returning fallback: {}", normalizedIp);
            return createFallback(normalizedIp);
        }

        var cached = cache.get(normalizedIp);
        if (cached.isPresent()) {
            log.debug("Cache hit for IP: {}", normalizedIp);
            return cached.get().withCacheSource();
        }

        try {
            log.debug("Cache miss, calling external API for IP: {}", normalizedIp);
            var result = provider.lookup(normalizedIp);

            cache.put(normalizedIp, result);
            log.info("Geolocation found for IP {} from {}", normalizedIp, result.source());

            return result;
        } catch (ExternalApiException e) {
            log.error("External API failed for IP {}: {}", normalizedIp, e.getMessage());
            return createFallback(normalizedIp);
        } catch (Exception e) {
            log.error("Unexpected error looking up IP {}: {}", normalizedIp, e.getMessage(), e);
            return createFallback(normalizedIp);
        }
    }

    private GeolocationInfo createFallback(String ip) {
        var fallbackProps = properties.fallback();
        return new GeolocationInfo(ip,
                new Country(fallbackProps.countryCode(), fallbackProps.countryName()),
                new Region("", ""), "", Coordinates.zero(), "", "", DataSource.FALLBACK,
                Instant.now());
    }
}

