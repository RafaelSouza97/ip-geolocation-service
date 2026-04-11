package com.example.geolocation.application.port.in;

import com.example.geolocation.application.domain.model.GeolocationInfo;

/**
 * Port de entrada para o caso de uso de geolocalização.
 * Define o contrato que o serviço de geolocalização deve implementar.
 */
public interface GeolocationUseCase {

    /**
     * Localiza informações geográficas a partir de um endereço IP.
     *
     * @param ip endereço IP (IPv4 ou IPv6)
     * @return informações de geolocalização
     * @throws com.example.geolocation.application.domain.exception.InvalidIpAddressException se o IP for inválido
     */
    GeolocationInfo locate(String ip);
}
