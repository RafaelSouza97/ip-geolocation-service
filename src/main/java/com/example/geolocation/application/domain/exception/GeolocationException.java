package com.example.geolocation.application.domain.exception;

public abstract class GeolocationException extends RuntimeException {

    protected GeolocationException(String message) {
        super(message);
    }

    protected GeolocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract String getErrorCode();
}

