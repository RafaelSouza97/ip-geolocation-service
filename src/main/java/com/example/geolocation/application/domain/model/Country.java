package com.example.geolocation.application.domain.model;

import com.example.geolocation.application.domain.exception.ErrorCode;

/**
 * Representa um país com código ISO e nome.
 *
 * @param code código ISO 3166-1 alpha-2 (ex: "BR", "US")
 * @param name nome completo do país (ex: "Brazil", "United States")
 */
public record Country(String code, String name) {
    public Country {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.COUNTRY_CODE_NULL.getMessage());
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.COUNTRY_NAME_NULL.getMessage());
        }
        code = code.toUpperCase();
    }
}
