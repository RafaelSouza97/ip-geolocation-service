package com.example.geolocation.application.port.out;

import com.example.geolocation.application.domain.model.GeolocationInfo;

public interface GeolocationProvider {

    GeolocationInfo lookup(String ip);
}

