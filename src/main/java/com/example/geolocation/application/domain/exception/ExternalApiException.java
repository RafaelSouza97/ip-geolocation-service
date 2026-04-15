package com.example.geolocation.application.domain.exception;

import lombok.Getter;

@Getter
public class ExternalApiException extends GeolocationException {

    private final String apiName;

    public ExternalApiException(String apiName, String message) {
        super(ErrorCode.EXTERNAL_API_ERROR.format(apiName, message));
        this.apiName = apiName;
    }

    public ExternalApiException(String apiName, String message, Throwable cause) {
        super(ErrorCode.EXTERNAL_API_ERROR.format(apiName, message), cause);
        this.apiName = apiName;
    }

    @Override
    public String getErrorCode() {
        return ErrorCode.EXTERNAL_API_ERROR.getCode();
    }
}

