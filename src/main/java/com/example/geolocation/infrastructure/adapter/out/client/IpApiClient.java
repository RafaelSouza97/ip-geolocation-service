package com.example.geolocation.infrastructure.adapter.out.client;

import com.example.geolocation.application.domain.exception.ExternalApiException;
import com.example.geolocation.application.domain.model.Coordinates;
import com.example.geolocation.application.domain.model.Country;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.domain.model.Region;
import com.example.geolocation.application.port.out.GeolocationProvider;
import com.example.geolocation.infrastructure.config.GeolocationProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

/**
 * Cliente HTTP para a API ip-api.com.
 * Provedor primário de geolocalização.
 */
@Slf4j
@Component("ipApiClient")
public class IpApiClient implements GeolocationProvider {

    private static final String API_NAME = "ip-api.com";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final GeolocationProperties properties;

    public IpApiClient(GeolocationProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(properties.api().timeout())
            .build();
    }

    public String getProviderName() {
        return API_NAME;
    }

    @Override
    public GeolocationInfo lookup(String ip) {
        var url = properties.api().url() + "/" + ip;
        log.debug("Calling external API: {}", url);

        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(properties.api().timeout())
                .GET()
                .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("API returned status {}: {}", response.statusCode(), response.body());
                throw new ExternalApiException(API_NAME, "HTTP " + response.statusCode());
            }

            var apiResponse = objectMapper.readValue(response.body(), IpApiResponse.class);

            if (!"success".equals(apiResponse.status())) {
                log.warn("API returned failure status for IP {}: {}", ip, apiResponse.message());
                throw new ExternalApiException(API_NAME, apiResponse.message());
            }

            return mapToGeolocationInfo(apiResponse);

        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling external API for IP {}: {}", ip, e.getMessage());
            throw new ExternalApiException(API_NAME, e.getMessage(), e);
        }
    }

    private GeolocationInfo mapToGeolocationInfo(IpApiResponse response) {
        return new GeolocationInfo(
            response.query(),
            new Country(response.countryCode(), response.country()),
            new Region(response.region(), response.regionName()),
            response.city(),
            new Coordinates(response.lat(), response.lon()),
            response.timezone(),
            response.isp(),
            "api",
            Instant.now()
        );
    }

    /**
     * DTO para resposta da API ip-api.com.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record IpApiResponse(
        String status,
        String message,
        String country,
        String countryCode,
        String region,
        String regionName,
        String city,
        double lat,
        double lon,
        String timezone,
        String isp,
        @JsonProperty("query") String query
    ) {}
}
