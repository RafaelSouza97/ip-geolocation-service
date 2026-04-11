package com.example.geolocation.application.domain.exception;

/**
 * Exceção lançada quando o IP é privado ou reservado.
 * IPs privados retornam fallback, não erro, mas esta exceção
 * pode ser usada em contextos que exigem IP público.
 */
public class PrivateIpAddressException extends GeolocationException {

    private static final String ERROR_CODE = "PRIVATE_IP_ADDRESS";
    private final String ip;

    public PrivateIpAddressException(String ip) {
        super("Private or reserved IP address: " + ip);
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
