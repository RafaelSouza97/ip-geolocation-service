package com.example.geolocation.application.domain.exception;

/**
 * Exceção lançada quando o formato do endereço IP é inválido.
 */
public class InvalidIpAddressException extends GeolocationException {

    private static final String ERROR_CODE = "INVALID_IP_FORMAT";
    private final String ip;

    public InvalidIpAddressException(String ip) {
        super("Invalid IP address format: " + ip);
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
