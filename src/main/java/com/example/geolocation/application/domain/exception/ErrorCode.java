package com.example.geolocation.application.domain.exception;

import com.example.geolocation.application.domain.constants.HttpHeaders;

/**
 * Enum unificado de códigos e mensagens de erro da aplicação.
 * 
 * <p>
 * Cada constante representa um tipo de erro com seu código técnico e mensagem para o usuário. Use
 * {@link #getCode()} para obter o identificador técnico e {@link #getMessage()} ou
 * {@link #format(Object...)} para a mensagem.
 * </p>
 * 
 * <p>
 * Para constantes de validação em anotações, use {@link Validation}.
 * </p>
 */
public enum ErrorCode {

    // ==================== IP Validation ====================
    INVALID_IP_FORMAT("Invalid IP address format: %s"), PRIVATE_IP_ADDRESS(
            "Private or reserved IP address: %s"),

    // ==================== Platform Validation ====================
    INVALID_PLATFORM("Invalid platform: %s. Valid values: %s"), INVALID_PLATFORM_SIMPLE(
            "Invalid platform: %s"), MISSING_PLATFORM_HEADER(
                    "Missing required header: " + HttpHeaders.DEVICE_PLATFORM),

    // ==================== Parameters ====================
    MISSING_PARAMETER("Missing required parameter: %s"), VALIDATION_ERROR("Validation error"),

    // ==================== Generic Errors ====================
    INTERNAL_ERROR("An unexpected error occurred"), EXTERNAL_API_ERROR(
            "External API error [%s]: %s"), BOTH_PROVIDERS_FAILED(
                    "Both providers failed. Primary: %s, Secondary: %s"),

    // ==================== Domain - Country ====================
    COUNTRY_CODE_NULL("Country code cannot be null or blank"), COUNTRY_NAME_NULL(
            "Country name cannot be null or blank"),

    // ==================== Domain - Coordinates ====================
    LATITUDE_OUT_OF_RANGE("Latitude must be between -90 and 90"), LONGITUDE_OUT_OF_RANGE(
            "Longitude must be between -180 and 180"),

    // ==================== Domain - GeolocationInfo ====================
    IP_NULL("IP cannot be null"), COUNTRY_NULL("Country cannot be null"), SOURCE_NULL(
            "Source cannot be null"),

    // ==================== Dependencies ====================
    CACHE_NULL("cache cannot be null"), PROVIDER_NULL("provider cannot be null"), PROPERTIES_NULL(
            "properties cannot be null"),

    // ==================== Security ====================
    JWT_SECRET_TOO_SHORT("JWT secret key must be at least 32 characters"),

    // ==================== HTTP ====================
    HTTP_ERROR("HTTP %d");

    private final String messageTemplate;

    ErrorCode(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    /**
     * Retorna o código técnico do erro (equivalente ao name() do enum).
     */
    public String getCode() {
        return name();
    }

    /**
     * Retorna a mensagem de erro sem formatação.
     */
    public String getMessage() {
        return messageTemplate;
    }

    /**
     * Formata a mensagem substituindo os placeholders pelos argumentos.
     * 
     * @param args argumentos para substituir os placeholders (%s, %d, etc.)
     * @return a mensagem formatada
     */
    public String format(Object... args) {
        return args.length > 0 ? String.format(messageTemplate, args) : messageTemplate;
    }

    @Override
    public String toString() {
        return messageTemplate;
    }

    /**
     * Constantes para uso em anotações de validação.
     * 
     * <p>
     * Anotações como {@code @NotBlank} exigem valores constantes em tempo de compilação:
     * </p>
     * 
     * <pre>{@code
     * @NotBlank(message = ErrorCode.Validation.USERNAME_REQUIRED)
     * String username;
     * }</pre>
     */
    public static final class Validation {
        public static final String USERNAME_REQUIRED = "Username is required";
        public static final String PASSWORD_REQUIRED = "Password is required";

        private Validation() {}
    }
}
