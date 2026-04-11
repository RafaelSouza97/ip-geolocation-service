package com.example.geolocation.application.domain.exception;

/**
 * Exceção base para erros de domínio da geolocalização.
 */
public abstract class GeolocationException extends RuntimeException {

    protected GeolocationException(String message) {
        super(message);
    }

    protected GeolocationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Retorna o código de erro para respostas HTTP.
     */
    public abstract String getErrorCode();
}
