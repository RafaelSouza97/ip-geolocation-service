package com.example.geolocation.application.domain.constants;

import lombok.experimental.UtilityClass;

/**
 * Constantes de nomes de headers HTTP utilizados na aplicação.
 */
@UtilityClass
public class HttpHeaders {

    /** Header para identificar a plataforma do dispositivo. */
    public final String DEVICE_PLATFORM = "x-device-platform";

    /** Header de autorização. */
    public final String AUTHORIZATION = "Authorization";

    /** Prefixo do token Bearer. */
    public final String BEARER_PREFIX = "Bearer ";

    /** Header User-Agent. */
    public final String USER_AGENT = "User-Agent";
}
