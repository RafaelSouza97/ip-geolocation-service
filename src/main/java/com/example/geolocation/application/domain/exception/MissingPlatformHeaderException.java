package com.example.geolocation.application.domain.exception;

public class MissingPlatformHeaderException extends GeolocationException {

    public MissingPlatformHeaderException() {
        super(ErrorCode.MISSING_PLATFORM_HEADER.getMessage());
    }

    @Override
    public String getErrorCode() {
        return ErrorCode.MISSING_PLATFORM_HEADER.getCode();
    }
}

