package com.example.geolocation.application.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MissingPlatformHeaderException")
class MissingPlatformHeaderExceptionTest {

    @Test
    @DisplayName("should have descriptive message")
    void shouldHaveDescriptiveMessage() {
        var exception = new MissingPlatformHeaderException();
        assertTrue(exception.getMessage().contains("x-device-platform"));
    }

    @Test
    @DisplayName("should return correct error code")
    void shouldReturnCorrectErrorCode() {
        var exception = new MissingPlatformHeaderException();
        assertEquals("MISSING_PLATFORM_HEADER", exception.getErrorCode());
    }
}
