package com.example.geolocation.application.domain.exception;

import lombok.Getter;

/**
 * Exceção lançada quando o IP é privado ou reservado. IPs privados retornam fallback, não erro, mas
 * esta exceção pode ser usada em contextos que exigem IP público.
 */
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
