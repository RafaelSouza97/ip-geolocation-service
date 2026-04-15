package com.example.geolocation.application.domain.model;

import com.example.geolocation.application.domain.exception.ErrorCode;

public record Coordinates(double latitude, double longitude) {
    public Coordinates {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException(ErrorCode.LATITUDE_OUT_OF_RANGE.getMessage());
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(ErrorCode.LONGITUDE_OUT_OF_RANGE.getMessage());
        }
    }

    public static Coordinates zero() {
        return new Coordinates(0, 0);
    }
}

