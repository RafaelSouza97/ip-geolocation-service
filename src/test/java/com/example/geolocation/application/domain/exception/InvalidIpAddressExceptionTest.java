package com.example.geolocation.application.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvalidIpAddressException")
class InvalidIpAddressExceptionTest {

    @Test
    @DisplayName("should contain IP in message")
    void shouldContainIpInMessage() {
        // Arrange
        var ip = "invalid-ip";

        // Act
        var exception = new InvalidIpAddressException(ip);

        // Assert
        assertTrue(exception.getMessage().contains(ip));
    }

    @Test
    @DisplayName("should return IP via getter")
    void shouldReturnIpViaGetter() {
        // Arrange
        var ip = "999.999.999.999";

        // Act
        var exception = new InvalidIpAddressException(ip);

        // Assert
        assertEquals(ip, exception.getIp());
    }

    @Test
    @DisplayName("should return correct error code")
    void shouldReturnCorrectErrorCode() {
        // Arrange & Act
        var exception = new InvalidIpAddressException("invalid");

        // Assert
        assertEquals("INVALID_IP_FORMAT", exception.getErrorCode());
    }
}
