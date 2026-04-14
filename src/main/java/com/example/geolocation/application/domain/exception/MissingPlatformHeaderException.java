package com.example.geolocation.application.domain.exception;

/**
 * Exceção lançada quando o header x-device-platform está ausente.
 */
public class MissingPlatformHeaderException extends GeolocationException {

    public MissingPlatformHeaderException() {
        super(ErrorCode.MISSING_PLATFORM_HEADER.getMessage());
    }

    @Override
    public String getErrorCode() {
        return ErrorCode.MISSING_PLATFORM_HEADER.getCode();
    }
}
