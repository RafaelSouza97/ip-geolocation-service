package com.example.geolocation.application.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExternalApiException")
class ExternalApiExceptionTest {

    @Test
    @DisplayName("should contain API name in message")
    void shouldContainApiNameInMessage() {
        // Arrange
        var apiName = "ip-api.com";
        var message = "Connection timeout";

        // Act
        var exception = new ExternalApiException(apiName, message);

        // Assert
        assertTrue(exception.getMessage().contains(apiName));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    @DisplayName("should return API name via getter")
    void shouldReturnApiNameViaGetter() {
        // Arrange
        var apiName = "ip-api.com";

        // Act
        var exception = new ExternalApiException(apiName, "Error");

        // Assert
        assertEquals(apiName, exception.getApiName());
    }

    @Test
    @DisplayName("should preserve cause")
    void shouldPreserveCause() {
        // Arrange
        var cause = new RuntimeException("Original error");
        var apiName = "ip-api.com";

        // Act
        var exception = new ExternalApiException(apiName, "Wrapper error", cause);

        // Assert
        assertEquals(cause, exception.getCause());
        assertEquals(apiName, exception.getApiName());
    }
}
