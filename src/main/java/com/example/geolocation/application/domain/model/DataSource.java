package com.example.geolocation.application.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DataSource {

    
    CACHE("cache"),

    
    API("api"),

    
    FALLBACK("fallback");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}

