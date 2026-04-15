package com.example.geolocation.infrastructure.adapter.out.client;

import java.time.Instant;
import com.example.geolocation.application.domain.model.Coordinates;
import com.example.geolocation.application.domain.model.Country;
import com.example.geolocation.application.domain.model.DataSource;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.domain.model.Region;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GeolocationInfoMapper {

    public record ApiResponseData(String ip, String countryCode, String countryName,
            String regionCode, String regionName, String city, Double latitude, Double longitude,
            String timezone, String isp) {
    }

    public GeolocationInfo fromApiResponse(ApiResponseData data) {
        return new GeolocationInfo(data.ip(), new Country(data.countryCode(), data.countryName()),
                new Region(data.regionCode() != null ? data.regionCode() : "",
                        data.regionName() != null ? data.regionName() : ""),
                data.city() != null ? data.city() : "",
                new Coordinates(data.latitude() != null ? data.latitude() : 0.0,
                        data.longitude() != null ? data.longitude() : 0.0),
                data.timezone() != null ? data.timezone() : "",
                data.isp() != null ? data.isp() : "", DataSource.API, Instant.now());
    }
}

