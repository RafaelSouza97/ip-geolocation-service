package com.example.geolocation.application.domain.constants;

/**
 * Constantes relacionadas a APIs externas de geolocalização.
 */
public final class ApiConstants {
    
    private ApiConstants() {
        // Utility class
    }
    
    /**
     * Nome do provider primário (ip-api.com).
     */
    public static final String PRIMARY_PROVIDER_NAME = "ip-api.com";
    
    /**
     * Nome do provider secundário (ipapi.co).
     */
    public static final String SECONDARY_PROVIDER_NAME = "ipapi.co";
    
    /**
     * URL padrão do provider primário.
     */
    public static final String PRIMARY_PROVIDER_URL = "http://ip-api.com/json";
    
    /**
     * URL padrão do provider secundário.
     */
    public static final String SECONDARY_PROVIDER_URL = "https://ipapi.co";
    
    /**
     * Status de sucesso retornado pela API ip-api.com.
     */
    public static final String IP_API_SUCCESS_STATUS = "success";
    
    /**
     * User-Agent padrão para requisições HTTP.
     */
    public static final String DEFAULT_USER_AGENT = "ip-geolocation-service/1.0";
}
