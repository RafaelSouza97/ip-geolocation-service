package com.example.geolocation.application.domain.exception;

import com.example.geolocation.application.domain.model.Platform;
import lombok.Getter;

@Getter
public class InvalidPlatformException extends GeolocationException {

    private final String platform;

    public InvalidPlatformException(String platform) {
        super(ErrorCode.INVALID_PLATFORM.format(platform, Platform.validValuesAsString()));
        this.platform = platform;
    }

    @Override
    public String getErrorCode() {
        return ErrorCode.INVALID_PLATFORM.getCode();
    }
}

