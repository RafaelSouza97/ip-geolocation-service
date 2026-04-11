package com.example.geolocation.application.port.out;

import com.example.geolocation.application.domain.model.GeolocationInfo;

/**
 * Port de saída para consulta de geolocalização em API externa.
 * Implementado por clients HTTP que consomem APIs de geolocalização.
 */
public interface GeolocationProvider {

    /**
     * Consulta informações de geolocalização para um IP.
     *
     * @param ip endereço IP público
     * @return informações de geolocalização da API
     * @throws com.example.geolocation.application.domain.exception.ExternalApiException em caso de erro na API
     */
    GeolocationInfo lookup(String ip);
}
