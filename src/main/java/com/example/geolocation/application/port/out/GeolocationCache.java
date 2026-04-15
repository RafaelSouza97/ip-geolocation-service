package com.example.geolocation.application.port.out;

import java.util.Optional;
import com.example.geolocation.application.domain.model.GeolocationInfo;

public interface GeolocationCache {

    Optional<GeolocationInfo> get(String ip);

    void put(String ip, GeolocationInfo info);

    void evict(String ip);

    void clear();
}

