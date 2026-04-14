package com.example.geolocation.application.domain.exception;

/**
 * Exceção lançada quando a API externa falha.
 */
public class ExternalApiException extends RuntimeException {

    private final String apiName;

    public ExternalApiException(String apiName, String message) {
        super(ErrorCode.EXTERNAL_API_ERROR.format(apiName, message));
        this.apiName = apiName;
    }

    public ExternalApiException(String apiName, String message, Throwable cause) {
        super(ErrorCode.EXTERNAL_API_ERROR.format(apiName, message), cause);
        this.apiName = apiName;
    }

    public String getApiName() {
        return apiName;
    }
}
