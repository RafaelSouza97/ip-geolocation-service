package com.example.geolocation.application.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GeolocationException")
class GeolocationExceptionTest {

    private static class TestGeolocationException extends GeolocationException {
        
        TestGeolocationException(String message) {
            super(message);
        }

        TestGeolocationException(String message, Throwable cause) {
            super(message, cause);
        }

        @Override
        public String getErrorCode() {
            return "TEST_ERROR";
        }
    }

    @Test
    @DisplayName("should create exception with message only")
    void shouldCreateExceptionWithMessageOnly() {
        var exception = new TestGeolocationException("Test message");
        assertEquals("Test message", exception.getMessage());
        assertNull(exception.getCause());
        assertEquals("TEST_ERROR", exception.getErrorCode());
    }

    @Test
    @DisplayName("should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        var cause = new RuntimeException("Root cause");
        var exception = new TestGeolocationException("Test message", cause);
        assertEquals("Test message", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("Root cause", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("should preserve cause chain")
    void shouldPreserveCauseChain() {
        var rootCause = new IllegalArgumentException("Root");
        var intermediateCause = new RuntimeException("Intermediate", rootCause);
        var exception = new TestGeolocationException("Top level", intermediateCause);
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    @Test
    @DisplayName("should be instance of RuntimeException")
    void shouldBeInstanceOfRuntimeException() {
        var exception = new TestGeolocationException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
