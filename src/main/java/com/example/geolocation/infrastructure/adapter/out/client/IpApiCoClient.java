package com.example.geolocation.infrastructure.adapter.out.client;

import com.example.geolocation.application.domain.constants.ApiConstants;
import com.example.geolocation.application.domain.constants.ErrorMessages;
import com.example.geolocation.application.domain.constants.HttpHeaders;
import com.example.geolocation.application.domain.exception.ExternalApiException;
import com.example.geolocation.application.domain.model.Coordinates;
import com.example.geolocation.application.domain.model.Country;
import com.example.geolocation.application.domain.model.DataSource;
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
 * Cliente HTTP para a API ipapi.co.
 * API secundária de fallback para geolocalização.
 */
@Slf4j
@Component("ipApiCoClient")
public class IpApiCoClient implements GeolocationProvider {

    private static final String API_NAME = ApiConstants.SECONDARY_PROVIDER_NAME;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final GeolocationProperties properties;

    public IpApiCoClient(GeolocationProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(properties.providers().secondary().timeout())
            .build();
    }

    @Override
    public GeolocationInfo lookup(String ip) {
        var baseUrl = properties.providers().secondary().url();
        var url = baseUrl + "/" + ip + "/json/";
        log.debug("Calling secondary API: {}", url);

        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(properties.providers().secondary().timeout())
                .header(HttpHeaders.USER_AGENT, ApiConstants.DEFAULT_USER_AGENT)
                .GET()
                .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("API {} returned status {}: {}", API_NAME, response.statusCode(), response.body());
                throw new ExternalApiException(API_NAME, ErrorMessages.httpError(response.statusCode()));
            }

            var apiResponse = objectMapper.readValue(response.body(), IpApiCoResponse.class);

            // ipapi.co retorna error=true quando há falha
            if (apiResponse.error() != null && apiResponse.error()) {
                log.warn("API {} returned error for IP {}: {}", API_NAME, ip, apiResponse.reason());
                throw new ExternalApiException(API_NAME, apiResponse.reason());
            }

            return mapToGeolocationInfo(apiResponse);

        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling {} API for IP {}: {}", API_NAME, ip, e.getMessage());
            throw new ExternalApiException(API_NAME, e.getMessage(), e);
        }
    }

    public String getProviderName() {
        return API_NAME;
    }

    private GeolocationInfo mapToGeolocationInfo(IpApiCoResponse response) {
        return new GeolocationInfo(
            response.ip(),
            new Country(response.countryCode(), response.countryName()),
            new Region(response.regionCode() != null ? response.regionCode() : "", 
                       response.region() != null ? response.region() : ""),
            response.city() != null ? response.city() : "",
            new Coordinates(
                response.latitude() != null ? response.latitude() : 0.0,
                response.longitude() != null ? response.longitude() : 0.0
            ),
            response.timezone() != null ? response.timezone() : "",
            response.org() != null ? response.org() : "",
            DataSource.API,
            Instant.now()
        );
    }

    /**
     * DTO para resposta da API ipapi.co.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record IpApiCoResponse(
        String ip,
        String city,
        String region,
        @JsonProperty("region_code") String regionCode,
        @JsonProperty("country_name") String countryName,
        @JsonProperty("country_code") String countryCode,
        Double latitude,
        Double longitude,
        String timezone,
        String org,
        Boolean error,
        String reason
    ) {}
}
