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
     * Dados de resposta de API de geolocalização.
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
     */
    public record ApiResponseData(
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
    }

    /**
     * Cria um GeolocationInfo a partir de dados de resposta da API externa.
     * Trata valores nulos de forma segura.
     *
     * @param data dados da resposta da API
     * @return GeolocationInfo preenchido
     */
    public static GeolocationInfo fromApiResponse(ApiResponseData data) {
        return new GeolocationInfo(
            data.ip(),
            new Country(data.countryCode(), data.countryName()),
            new Region(
                data.regionCode() != null ? data.regionCode() : "",
                data.regionName() != null ? data.regionName() : ""
            ),
            data.city() != null ? data.city() : "",
            new Coordinates(
                data.latitude() != null ? data.latitude() : 0.0,
                data.longitude() != null ? data.longitude() : 0.0
            ),
            data.timezone() != null ? data.timezone() : "",
            data.isp() != null ? data.isp() : "",
            DataSource.API,
            Instant.now()
        );
    }
}
