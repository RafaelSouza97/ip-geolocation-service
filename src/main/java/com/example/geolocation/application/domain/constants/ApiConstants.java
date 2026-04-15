package com.example.geolocation.application.domain.constants;

import lombok.experimental.UtilityClass;

/**
 * Constantes relacionadas a APIs externas de geolocalização.
 */
@UtilityClass
public class ApiConstants {

    /** Nome do provider primário (ip-api.com). */
    public final String PRIMARY_PROVIDER_NAME = "ip-api.com";

    /** Nome do provider secundário (ipapi.co). */
    public final String SECONDARY_PROVIDER_NAME = "ipapi.co";

    /** URL padrão do provider primário. */
    public final String PRIMARY_PROVIDER_URL = "http://ip-api.com/json";

    /** URL padrão do provider secundário. */
    public final String SECONDARY_PROVIDER_URL = "https://ipapi.co";

    /** Status de sucesso retornado pela API ip-api.com. */
    public final String IP_API_SUCCESS_STATUS = "success";

    /** User-Agent padrão para requisições HTTP. */
    public final String DEFAULT_USER_AGENT = "ip-geolocation-service/1.0";
}
