package com.example.geolocation.application.port.out;

import java.util.Optional;
import com.example.geolocation.application.domain.model.GeolocationInfo;

/**
 * Port de saída para cache de geolocalização. Implementado por adaptadores de cache (Caffeine,
 * Redis, etc).
 */
public interface GeolocationCache {

    /**
     * Busca informações de geolocalização no cache.
     *
     * @param ip endereço IP como chave
     * @return Optional com as informações se existir no cache
     */
    Optional<GeolocationInfo> get(String ip);

    /**
     * Armazena informações de geolocalização no cache.
     *
     * @param ip endereço IP como chave
     * @param info informações a serem cacheadas
     */
    void put(String ip, GeolocationInfo info);

    /**
     * Remove uma entrada do cache.
     *
     * @param ip endereço IP como chave
     */
    void evict(String ip);

    /**
     * Limpa o cache.
     */
    void clear();
}
