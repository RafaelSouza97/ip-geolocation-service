package com.example.geolocation.application.domain.constants;

import com.example.geolocation.application.domain.model.Platform;

/**
 * Constantes de mensagens de erro da aplicação.
 */
public final class ErrorMessages {
    
    private ErrorMessages() {
        // Utility class
    }
    
    // Mensagens de validação de IP
    public static final String INVALID_IP_FORMAT = "Invalid IP address format: %s";
    public static final String PRIVATE_IP_ADDRESS = "Private or reserved IP address: %s";
    
    // Mensagens de validação de plataforma
    public static final String INVALID_PLATFORM_TEMPLATE = "Invalid platform: %s. Valid values: " + Platform.validValuesAsString();
    public static final String MISSING_PLATFORM_HEADER = "Missing required header: " + HttpHeaders.DEVICE_PLATFORM;
    
    // Mensagens de parâmetros
    public static final String MISSING_PARAMETER = "Missing required parameter: %s";
    
    // Mensagens de erro genéricas
    public static final String INTERNAL_ERROR = "An unexpected error occurred";
    public static final String BOTH_PROVIDERS_FAILED = "Both providers failed. Primary: %s, Secondary: %s";
    
    // Mensagens de validação de DTOs
    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String PASSWORD_REQUIRED = "Password is required";
    
    // Formato de mensagem para resposta de erro HTTP
    public static final String HTTP_ERROR_FORMAT = "HTTP %d";
    
    /**
     * Formata mensagem de IP inválido.
     */
    public static String invalidIpFormat(String ip) {
        return String.format(INVALID_IP_FORMAT, ip);
    }
    
    /**
     * Formata mensagem de IP privado.
     */
    public static String privateIpAddress(String ip) {
        return String.format(PRIVATE_IP_ADDRESS, ip);
    }
    
    /**
     * Formata mensagem de plataforma inválida.
     */
    public static String invalidPlatform(String platform) {
        return String.format(INVALID_PLATFORM_TEMPLATE, platform);
    }
    
    /**
     * Formata mensagem de parâmetro ausente.
     */
    public static String missingParameter(String paramName) {
        return String.format(MISSING_PARAMETER, paramName);
    }
    
    /**
     * Formata mensagem de erro HTTP.
     */
    public static String httpError(int statusCode) {
        return String.format(HTTP_ERROR_FORMAT, statusCode);
    }
    
    /**
     * Formata mensagem de falha de ambos os providers.
     */
    public static String bothProvidersFailed(String primaryError, String secondaryError) {
        return String.format(BOTH_PROVIDERS_FAILED, primaryError, secondaryError);
    }
}
