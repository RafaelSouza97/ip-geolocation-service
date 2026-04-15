package com.example.geolocation.application.domain.exception;

import com.example.geolocation.application.domain.model.Platform;
import lombok.Getter;

/**
 * Exceção lançada quando a plataforma informada não é válida. Plataformas válidas: iOS, Android,
 * Web
 */
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
