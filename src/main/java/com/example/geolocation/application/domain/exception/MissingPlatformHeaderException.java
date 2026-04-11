package com.example.geolocation.application.domain.exception;

/**
 * Exceção lançada quando o header x-device-platform está ausente.
 */
public class MissingPlatformHeaderException extends GeolocationException {

    private static final String ERROR_CODE = "MISSING_PLATFORM_HEADER";

    public MissingPlatformHeaderException() {
        super("Missing required header: x-device-platform");
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
