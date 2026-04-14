package com.example.geolocation.application.domain.exception;

import com.example.geolocation.application.domain.constants.ErrorMessages;

/**
 * Exceção lançada quando o header x-device-platform está ausente.
 */
public class MissingPlatformHeaderException extends GeolocationException {

    public MissingPlatformHeaderException() {
        super(ErrorMessages.MISSING_PLATFORM_HEADER);
    }

    @Override
    public String getErrorCode() {
        return ErrorCode.MISSING_PLATFORM_HEADER.getCode();
    }
}
