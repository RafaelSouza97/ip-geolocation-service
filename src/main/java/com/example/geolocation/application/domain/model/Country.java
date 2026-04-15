package com.example.geolocation.application.domain.model;

import com.example.geolocation.application.domain.exception.ErrorCode;

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

