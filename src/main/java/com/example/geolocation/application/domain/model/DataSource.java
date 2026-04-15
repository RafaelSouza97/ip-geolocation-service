package com.example.geolocation.application.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum que representa a origem dos dados de geolocalização.
 */
@Getter
@RequiredArgsConstructor
public enum DataSource {

    /** Dados obtidos do cache local. */
    CACHE("cache"),

    /** Dados obtidos da API externa. */
    API("api"),

    /** Dados de fallback (padrão quando a API falha). */
    FALLBACK("fallback");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
