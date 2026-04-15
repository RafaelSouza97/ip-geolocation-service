package com.example.geolocation.infrastructure.adapter.out.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.stereotype.Component;
import com.example.geolocation.application.domain.constants.ApiConstants;
import com.example.geolocation.application.domain.constants.HttpHeaders;
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
@Component("ipApiCoClient")
@RequiredArgsConstructor
public class IpApiCoClient implements GeolocationProvider {

    private static final String API_NAME = ApiConstants.SECONDARY_PROVIDER_NAME;

    private final GeolocationProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Override
    public GeolocationInfo lookup(String ip) {
        var baseUrl = properties.providers().secondary().url();
        var url = baseUrl + "/" + ip + "/json/";
        log.debug("Calling secondary API: {}", url);

        try {
            var request = HttpRequest.newBuilder().uri(URI.create(url))
                    .timeout(properties.providers().secondary().timeout())
                    .header(HttpHeaders.USER_AGENT, ApiConstants.DEFAULT_USER_AGENT).GET().build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("API {} returned status {}: {}", API_NAME, response.statusCode(),
                        response.body());
                throw new ExternalApiException(API_NAME,
                        ErrorCode.HTTP_ERROR.format(response.statusCode()));
            }

            var apiResponse = objectMapper.readValue(response.body(), IpApiCoResponse.class);
            if (apiResponse.error() != null && apiResponse.error()) {
                log.warn("API {} returned error for IP {}: {}", API_NAME, ip, apiResponse.reason());
                throw new ExternalApiException(API_NAME, apiResponse.reason());
            }

            return mapToGeolocationInfo(apiResponse);

        } catch (ExternalApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException(API_NAME, "Request interrupted", e);
        } catch (Exception e) {
            log.error("Error calling {} API for IP {}: {}", API_NAME, ip, e.getMessage());
            throw new ExternalApiException(API_NAME, e.getMessage(), e);
        }
    }

    public String getProviderName() {
        return API_NAME;
    }

    private GeolocationInfo mapToGeolocationInfo(IpApiCoResponse response) {
        return GeolocationInfoMapper.fromApiResponse(new GeolocationInfoMapper.ApiResponseData(
                response.ip(), response.countryCode(), response.countryName(),
                response.regionCode(), response.region(), response.city(), response.latitude(),
                response.longitude(), response.timezone(), response.org()));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IpApiCoResponse(String ip, String city, String region,
            @JsonProperty("region_code") String regionCode,
            @JsonProperty("country_name") String countryName,
            @JsonProperty("country_code") String countryCode, Double latitude, Double longitude,
            String timezone, String org, Boolean error, String reason) {
    }
}

