package com.example.geolocation.application.domain.constants;

/**
 * Constantes de nomes de headers HTTP utilizados na aplicação.
 */
public final class HttpHeaders {
    
    private HttpHeaders() {
        // Utility class
    }
    
    /**
     * Header para identificar a plataforma do dispositivo.
     */
    public static final String DEVICE_PLATFORM = "x-device-platform";
    
    /**
     * Header de autorização.
     */
    public static final String AUTHORIZATION = "Authorization";
    
    /**
     * Prefixo do token Bearer.
     */
    public static final String BEARER_PREFIX = "Bearer ";
    
    /**
     * Header User-Agent.
     */
    public static final String USER_AGENT = "User-Agent";
}
