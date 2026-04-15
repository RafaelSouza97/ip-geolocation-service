package com.example.geolocation.application.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExternalApiException")
class ExternalApiExceptionTest {

    @Test
    @DisplayName("should contain API name in message")
    void shouldContainApiNameInMessage() {
        var apiName = "ip-api.com";
        var message = "Connection timeout";
        var exception = new ExternalApiException(apiName, message);
        assertTrue(exception.getMessage().contains(apiName));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    @DisplayName("should return API name via getter")
    void shouldReturnApiNameViaGetter() {
        var apiName = "ip-api.com";
        var exception = new ExternalApiException(apiName, "Error");
        assertEquals(apiName, exception.getApiName());
    }

    @Test
    @DisplayName("should preserve cause")
    void shouldPreserveCause() {
        var cause = new RuntimeException("Original error");
        var apiName = "ip-api.com";
        var exception = new ExternalApiException(apiName, "Wrapper error", cause);
        assertEquals(cause, exception.getCause());
        assertEquals(apiName, exception.getApiName());
    }
}
