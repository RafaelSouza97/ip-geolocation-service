package com.example.geolocation.application.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Agregado contendo todas as informações de geolocalização de um IP.
 *
 * @param ip          endereço IP consultado
 * @param country     país identificado
 * @param region      região/estado (pode ser vazio)
 * @param city        cidade (pode ser vazio)
 * @param coordinates coordenadas geográficas
 * @param timezone    fuso horário (ex: "America/Sao_Paulo")
 * @param isp         provedor de internet
 * @param source      origem dos dados: "api", "cache" ou "fallback"
 * @param timestamp   momento da consulta
 */
public record GeolocationInfo(
    String ip,
    Country country,
    Region region,
    String city,
    Coordinates coordinates,
    String timezone,
    String isp,
    String source,
    Instant timestamp
) {
    public GeolocationInfo {
        Objects.requireNonNull(ip, "IP cannot be null");
        Objects.requireNonNull(country, "Country cannot be null");
        Objects.requireNonNull(source, "Source cannot be null");
        
        if (region == null) region = new Region("", "");
        if (city == null) city = "";
        if (coordinates == null) coordinates = Coordinates.zero();
        if (timezone == null) timezone = "";
        if (isp == null) isp = "";
        if (timestamp == null) timestamp = Instant.now();
    }

    /**
     * Cria uma cópia com source alterado para "cache".
     */
    public GeolocationInfo withCacheSource() {
        return new GeolocationInfo(
            ip, country, region, city, coordinates, 
            timezone, isp, "cache", timestamp
        );
    }

    /**
     * Verifica se esta informação veio do fallback.
     */
    public boolean isFallback() {
        return "fallback".equals(source);
    }
}
