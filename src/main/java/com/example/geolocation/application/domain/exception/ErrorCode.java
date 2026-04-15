package com.example.geolocation.application.domain.exception;

import com.example.geolocation.application.domain.constants.HttpHeaders;

public enum ErrorCode {

    INVALID_IP_FORMAT("Invalid IP address format: %s"), PRIVATE_IP_ADDRESS(
            "Private or reserved IP address: %s"),

    INVALID_PLATFORM("Invalid platform: %s. Valid values: %s"), INVALID_PLATFORM_SIMPLE(
            "Invalid platform: %s"), MISSING_PLATFORM_HEADER(
                    "Missing required header: " + HttpHeaders.DEVICE_PLATFORM),

    MISSING_PARAMETER("Missing required parameter: %s"), VALIDATION_ERROR("Validation error"),

    INTERNAL_ERROR("An unexpected error occurred"), EXTERNAL_API_ERROR(
            "External API error [%s]: %s"), BOTH_PROVIDERS_FAILED(
                    "Both providers failed. Primary: %s, Secondary: %s"),

    COUNTRY_CODE_NULL("Country code cannot be null or blank"), COUNTRY_NAME_NULL(
            "Country name cannot be null or blank"),

    LATITUDE_OUT_OF_RANGE("Latitude must be between -90 and 90"), LONGITUDE_OUT_OF_RANGE(
            "Longitude must be between -180 and 180"),

    IP_NULL("IP cannot be null"), COUNTRY_NULL("Country cannot be null"), SOURCE_NULL(
            "Source cannot be null"),

    CACHE_NULL("cache cannot be null"), PROVIDER_NULL("provider cannot be null"), PROPERTIES_NULL(
            "properties cannot be null"),

    JWT_SECRET_TOO_SHORT("JWT secret key must be at least 32 characters"),

    HTTP_ERROR("HTTP %d");

    private final String messageTemplate;

    ErrorCode(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getCode() {
        return name();
    }

    public String getMessage() {
        return messageTemplate;
    }

    public String format(Object... args) {
        return args.length > 0 ? String.format(messageTemplate, args) : messageTemplate;
    }

    @Override
    public String toString() {
        return messageTemplate;
    }

    public static final class Validation {
        public static final String USERNAME_REQUIRED = "Username is required";
        public static final String PASSWORD_REQUIRED = "Password is required";

        private Validation() {}
    }
}

