package com.example.geolocation.application.port.in;

import com.example.geolocation.application.domain.model.GeolocationInfo;

public interface GeolocationUseCase {

    GeolocationInfo locate(String ip);
}

