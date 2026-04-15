package com.example.geolocation.infrastructure.adapter.out.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Component;

import com.example.geolocation.application.domain.constants.ApiConstants;
import com.example.geolocation.application.domain.exception.ErrorCode;
import com.example.geolocation.application.domain.exception.ExternalApiException;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.port.out.GeolocationProvider;
import com.example.geolocation.infrastructure.config.GeolocationProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("ipApiClient")
@RequiredArgsConstructor
public class IpApiClient implements GeolocationProvider {

    private static final String API_NAME = ApiConstants.PRIMARY_PROVIDER_NAME;

    private final GeolocationProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public String getProviderName() {
        return API_NAME;
    }

    @Override
    public GeolocationInfo lookup(String ip) {
        var url = properties.api().url() + "/" + ip;
        log.debug("Calling external API: {}", url);

        try {
            var request = HttpRequest.newBuilder().uri(URI.create(url))
                    .timeout(properties.api().timeout()).GET().build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("API returned status {}: {}", response.statusCode(), response.body());
                throw new ExternalApiException(API_NAME,
                        ErrorCode.HTTP_ERROR.format(response.statusCode()));
            }

            var apiResponse = objectMapper.readValue(response.body(), IpApiResponse.class);

            if (!ApiConstants.IP_API_SUCCESS_STATUS.equals(apiResponse.status())) {
                log.warn("API returned failure status for IP {}: {}", ip, apiResponse.message());
                throw new ExternalApiException(API_NAME, apiResponse.message());
            }

            return mapToGeolocationInfo(apiResponse);

        } catch (ExternalApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException(API_NAME, "Request interrupted", e);
        } catch (Exception e) {
            log.error("Error calling external API for IP {}: {}", ip, e.getMessage());
            throw new ExternalApiException(API_NAME, e.getMessage(), e);
        }
    }

    private GeolocationInfo mapToGeolocationInfo(IpApiResponse response) {
        return GeolocationInfoMapper.fromApiResponse(new GeolocationInfoMapper.ApiResponseData(
                response.query(), response.countryCode(), response.country(), response.region(),
                response.regionName(), response.city(), response.lat(), response.lon(),
                response.timezone(), response.isp()));
    }

    
    @JsonIgnoreProperties(ignoreUnknown = true)
    record IpApiResponse(String status, String message, String country, String countryCode,
            String region, String regionName, String city, double lat, double lon, String timezone,
            String isp, @JsonProperty("query") String query) {
    }
}

