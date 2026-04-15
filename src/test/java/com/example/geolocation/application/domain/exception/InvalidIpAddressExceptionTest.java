package com.example.geolocation.application.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvalidIpAddressException")
class InvalidIpAddressExceptionTest {

    @Test
    @DisplayName("should contain IP in message")
    void shouldContainIpInMessage() {
        var ip = "invalid-ip";
        var exception = new InvalidIpAddressException(ip);
        assertTrue(exception.getMessage().contains(ip));
    }

    @Test
    @DisplayName("should return IP via getter")
    void shouldReturnIpViaGetter() {
        var ip = "999.999.999.999";
        var exception = new InvalidIpAddressException(ip);
        assertEquals(ip, exception.getIp());
    }

    @Test
    @DisplayName("should return correct error code")
    void shouldReturnCorrectErrorCode() {
        var exception = new InvalidIpAddressException("invalid");
        assertEquals("INVALID_IP_FORMAT", exception.getErrorCode());
    }
}
