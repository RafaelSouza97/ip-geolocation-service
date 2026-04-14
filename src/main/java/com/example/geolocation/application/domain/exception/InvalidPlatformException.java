package com.example.geolocation.application.domain.exception;

import com.example.geolocation.application.domain.model.Platform;

/**
 * Exceção lançada quando a plataforma informada não é válida. Plataformas válidas: iOS, Android,
 * Web
 */
public class InvalidPlatformException extends GeolocationException {

    private final String platform;

    public InvalidPlatformException(String platform) {
        super(ErrorCode.INVALID_PLATFORM.format(platform, Platform.validValuesAsString()));
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    @Override
    public String getErrorCode() {
        return ErrorCode.INVALID_PLATFORM.getCode();
    }
}
