package com.example.geolocation.application.domain.exception;

import lombok.Getter;

@Getter
public class PrivateIpAddressException extends GeolocationException {

    private final String ip;

    public PrivateIpAddressException(String ip) {
        super(ErrorCode.PRIVATE_IP_ADDRESS.format(ip));
        this.ip = ip;
    }

    @Override
    public String getErrorCode() {
        return ErrorCode.PRIVATE_IP_ADDRESS.getCode();
    }
}

