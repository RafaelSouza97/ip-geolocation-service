package com.example.geolocation.application.domain.exception;

/**
 * Exceção lançada quando o formato do endereço IP é inválido.
 */
public class InvalidIpAddressException extends GeolocationException {

    private final String ip;

    public InvalidIpAddressException(String ip) {
        super(ErrorCode.INVALID_IP_FORMAT.format(ip));
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    @Override
    public String getErrorCode() {
        return ErrorCode.INVALID_IP_FORMAT.getCode();
    }
}
