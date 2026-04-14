package com.example.geolocation.application.domain.exception;

/**
 * Enum com códigos de erro padronizados da aplicação.
 */
public enum ErrorCode {
    
    // Erros de validação de entrada
    INVALID_IP_FORMAT("INVALID_IP_FORMAT"),
    INVALID_PLATFORM("INVALID_PLATFORM"),
    MISSING_PLATFORM_HEADER("MISSING_PLATFORM_HEADER"),
    MISSING_PARAMETER("MISSING_PARAMETER"),
    VALIDATION_ERROR("VALIDATION_ERROR"),
    
    // Erros de regra de negócio
    PRIVATE_IP_ADDRESS("PRIVATE_IP_ADDRESS"),
    
    // Erros de infraestrutura
    EXTERNAL_API_ERROR("EXTERNAL_API_ERROR"),
    INTERNAL_ERROR("INTERNAL_ERROR");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }
}
