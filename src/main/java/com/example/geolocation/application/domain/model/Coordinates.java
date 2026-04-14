package com.example.geolocation.application.domain.model;

import com.example.geolocation.application.domain.exception.ErrorCode;

/**
 * Representa coordenadas geográficas.
 *
 * @param latitude latitude em graus decimais (-90 a 90)
 * @param longitude longitude em graus decimais (-180 a 180)
 */
public record Coordinates(double latitude, double longitude) {
    public Coordinates {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException(ErrorCode.LATITUDE_OUT_OF_RANGE.getMessage());
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(ErrorCode.LONGITUDE_OUT_OF_RANGE.getMessage());
        }
    }

    /**
     * Cria coordenadas zeradas (origem).
     */
    public static Coordinates zero() {
        return new Coordinates(0, 0);
    }
}
