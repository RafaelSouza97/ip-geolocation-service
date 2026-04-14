package com.example.geolocation.application.domain.exception;

import com.example.geolocation.application.domain.constants.ErrorMessages;

/**
 * Exceção lançada quando a plataforma informada não é válida.
 * Plataformas válidas: iOS, Android, Web
 */
public class InvalidPlatformException extends GeolocationException {

    private final String platform;

    public InvalidPlatformException(String platform) {
        super(ErrorMessages.invalidPlatform(platform));
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
