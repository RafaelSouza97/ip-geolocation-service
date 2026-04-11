package com.example.geolocation.application.service;

import com.example.geolocation.application.domain.exception.ExternalApiException;
import com.example.geolocation.application.domain.exception.InvalidIpAddressException;
import com.example.geolocation.application.domain.model.Coordinates;
import com.example.geolocation.application.domain.model.Country;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.domain.model.Region;
import com.example.geolocation.application.port.in.GeolocationUseCase;
import com.example.geolocation.application.port.out.GeolocationCache;
import com.example.geolocation.application.port.out.GeolocationProvider;
import com.example.geolocation.infrastructure.config.GeolocationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

/**
 * Serviço de geolocalização que implementa a lógica de negócio.
 * 
 * Fluxo:
 * 1. Valida o IP
 * 2. Se IP privado/localhost -> retorna fallback
 * 3. Verifica cache -> se hit, retorna do cache
 * 4. Consulta API externa -> armazena no cache e retorna
 * 5. Em caso de erro na API -> retorna fallback
 */
@Slf4j
@Service
public class GeolocationService implements GeolocationUseCase {

    private final GeolocationCache cache;
    private final GeolocationProvider provider;
    private final GeolocationProperties properties;

    public GeolocationService(
            GeolocationCache cache,
            GeolocationProvider provider,
            GeolocationProperties properties) {
        this.cache = Objects.requireNonNull(cache, "cache cannot be null");
        this.provider = Objects.requireNonNull(provider, "provider cannot be null");
        this.properties = Objects.requireNonNull(properties, "properties cannot be null");
    }

    @Override
    public GeolocationInfo locate(String ip) {
        log.debug("Looking up geolocation for IP: {}", ip);

        // 1. Validar IP
        if (!IpValidator.isValid(ip)) {
            log.warn("Invalid IP address: {}", ip);
            throw new InvalidIpAddressException(ip);
        }

        String normalizedIp = IpValidator.normalize(ip);

        // 2. Se IP privado/localhost, retornar fallback
        if (IpValidator.isPrivateOrReserved(normalizedIp)) {
            log.info("Private/reserved IP detected, returning fallback: {}", normalizedIp);
            return createFallback(normalizedIp);
        }

        // 3. Verificar cache
        var cached = cache.get(normalizedIp);
        if (cached.isPresent()) {
            log.debug("Cache hit for IP: {}", normalizedIp);
            return cached.get().withCacheSource();
        }

        // 4. Consultar API externa
        try {
            log.debug("Cache miss, calling external API for IP: {}", normalizedIp);
            var result = provider.lookup(normalizedIp);
            
            // 5. Armazenar no cache (apenas resultados da API, não fallback)
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

    /**
     * Cria resposta de fallback com país padrão (configurável).
     */
    private GeolocationInfo createFallback(String ip) {
        var fallbackProps = properties.fallback();
        return new GeolocationInfo(
            ip,
            new Country(fallbackProps.countryCode(), fallbackProps.countryName()),
            new Region("", ""),
            "",
            Coordinates.zero(),
            "",
            "",
            "fallback",
            Instant.now()
        );
    }
}
