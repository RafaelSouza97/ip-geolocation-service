package com.example.geolocation.application.domain.exception;

import lombok.Getter;

@Getter
public class InvalidIpAddressException extends GeolocationException {

    private final String ip;

    public InvalidIpAddressException(String ip) {
        super(ErrorCode.INVALID_IP_FORMAT.format(ip));
        this.ip = ip;
    }

    @Override
    public String getErrorCode() {
        return ErrorCode.INVALID_IP_FORMAT.getCode();
    }
}

