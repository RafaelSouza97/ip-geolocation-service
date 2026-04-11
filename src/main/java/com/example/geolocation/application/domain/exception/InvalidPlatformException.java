package com.example.geolocation.application.domain.exception;

/**
 * Exceção lançada quando a plataforma informada não é válida.
 * Plataformas válidas: iOS, Android, Web
 */
public class InvalidPlatformException extends GeolocationException {

    private static final String ERROR_CODE = "INVALID_PLATFORM";
    private final String platform;

    public InvalidPlatformException(String platform) {
        super("Invalid platform: " + platform + ". Valid values: iOS, Android, Web");
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
