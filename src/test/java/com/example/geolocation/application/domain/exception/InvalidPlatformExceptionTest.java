package com.example.geolocation.application.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvalidPlatformException")
class InvalidPlatformExceptionTest {

    @Test
    @DisplayName("should contain platform in message")
    void shouldContainPlatformInMessage() {
        var platform = "InvalidPlatform";
        var exception = new InvalidPlatformException(platform);
        assertTrue(exception.getMessage().contains(platform));
        assertTrue(exception.getMessage().contains("iOS"));
        assertTrue(exception.getMessage().contains("Android"));
        assertTrue(exception.getMessage().contains("Web"));
    }

    @Test
    @DisplayName("should return platform via getter")
    void shouldReturnPlatformViaGetter() {
        var platform = "Desktop";
        var exception = new InvalidPlatformException(platform);
        assertEquals(platform, exception.getPlatform());
    }

    @Test
    @DisplayName("should return correct error code")
    void shouldReturnCorrectErrorCode() {
        var exception = new InvalidPlatformException("Invalid");
        assertEquals("INVALID_PLATFORM", exception.getErrorCode());
    }
}
