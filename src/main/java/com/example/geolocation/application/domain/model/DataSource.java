package com.example.geolocation.application.domain.model;

/**
 * Enum que representa a origem dos dados de geolocalização.
 */
public enum DataSource {
    
    /**
     * Dados obtidos do cache local.
     */
    CACHE("cache"),
    
    /**
     * Dados obtidos da API externa.
     */
    API("api"),
    
    /**
     * Dados de fallback (padrão quando a API falha).
     */
    FALLBACK("fallback");

    private final String value;

    DataSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
