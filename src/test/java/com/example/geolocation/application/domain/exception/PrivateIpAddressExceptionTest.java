package com.example.geolocation.application.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PrivateIpAddressException")
class PrivateIpAddressExceptionTest {

    @Test
    @DisplayName("should contain IP in message")
    void shouldContainIpInMessage() {
        // Arrange
        var ip = "192.168.1.1";

        // Act
        var exception = new PrivateIpAddressException(ip);

        // Assert
        assertTrue(exception.getMessage().contains(ip));
    }

    @Test
    @DisplayName("should return IP via getter")
    void shouldReturnIpViaGetter() {
        // Arrange
        var ip = "10.0.0.1";

        // Act
        var exception = new PrivateIpAddressException(ip);

        // Assert
        assertEquals(ip, exception.getIp());
    }

    @Test
    @DisplayName("should return correct error code")
    void shouldReturnCorrectErrorCode() {
        // Arrange & Act
        var exception = new PrivateIpAddressException("192.168.1.1");

        // Assert
        assertEquals("PRIVATE_IP_ADDRESS", exception.getErrorCode());
    }
}
