package com.example.geolocation.application.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvalidPlatformException")
class InvalidPlatformExceptionTest {

    @Test
    @DisplayName("should contain platform in message")
    void shouldContainPlatformInMessage() {
        // Arrange
        var platform = "InvalidPlatform";

        // Act
        var exception = new InvalidPlatformException(platform);

        // Assert
        assertTrue(exception.getMessage().contains(platform));
        assertTrue(exception.getMessage().contains("iOS"));
        assertTrue(exception.getMessage().contains("Android"));
        assertTrue(exception.getMessage().contains("Web"));
    }

    @Test
    @DisplayName("should return platform via getter")
    void shouldReturnPlatformViaGetter() {
        // Arrange
        var platform = "Desktop";

        // Act
        var exception = new InvalidPlatformException(platform);

        // Assert
        assertEquals(platform, exception.getPlatform());
    }

    @Test
    @DisplayName("should return correct error code")
    void shouldReturnCorrectErrorCode() {
        // Arrange & Act
        var exception = new InvalidPlatformException("Invalid");

        // Assert
        assertEquals("INVALID_PLATFORM", exception.getErrorCode());
    }
}
