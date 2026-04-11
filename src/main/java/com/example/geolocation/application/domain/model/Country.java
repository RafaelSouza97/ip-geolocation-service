package com.example.geolocation.application.domain.model;

/**
 * Representa um país com código ISO e nome.
 *
 * @param code código ISO 3166-1 alpha-2 (ex: "BR", "US")
 * @param name nome completo do país (ex: "Brazil", "United States")
 */
public record Country(
    String code,
    String name
) {
    public Country {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Country code cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Country name cannot be null or blank");
        }
        code = code.toUpperCase();
    }
}
