package com.example.geolocation.infrastructure.adapter.out.client;

import com.example.geolocation.application.domain.model.Coordinates;
import com.example.geolocation.application.domain.model.Country;
import com.example.geolocation.application.domain.model.DataSource;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.domain.model.Region;

import java.time.Instant;

/**
 * Utilitário para criar GeolocationInfo a partir de respostas de APIs externas.
 * Centraliza o tratamento de valores nulos e a criação do objeto de domínio.
 */
public final class GeolocationInfoMapper {

    private GeolocationInfoMapper() {
        // Utility class
    }

    /**
     * Cria um GeolocationInfo a partir de valores da API externa.
     * Trata valores nulos de forma segura.
     *
     * @param ip          endereço IP consultado
     * @param countryCode código ISO do país
     * @param countryName nome do país
     * @param regionCode  código da região (pode ser null)
     * @param regionName  nome da região (pode ser null)
     * @param city        cidade (pode ser null)
     * @param latitude    latitude (pode ser null)
     * @param longitude   longitude (pode ser null)
     * @param timezone    fuso horário (pode ser null)
     * @param isp         provedor de internet (pode ser null)
     * @return GeolocationInfo preenchido
     */
    public static GeolocationInfo fromApiResponse(
            String ip,
            String countryCode,
            String countryName,
            String regionCode,
            String regionName,
            String city,
            Double latitude,
            Double longitude,
            String timezone,
            String isp) {
        
        return new GeolocationInfo(
            ip,
            new Country(countryCode, countryName),
            new Region(
                regionCode != null ? regionCode : "",
                regionName != null ? regionName : ""
            ),
            city != null ? city : "",
            new Coordinates(
                latitude != null ? latitude : 0.0,
                longitude != null ? longitude : 0.0
            ),
            timezone != null ? timezone : "",
            isp != null ? isp : "",
            DataSource.API,
            Instant.now()
        );
    }
}
