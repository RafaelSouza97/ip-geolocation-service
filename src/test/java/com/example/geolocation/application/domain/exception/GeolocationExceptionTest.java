package com.example.geolocation.application.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GeolocationException")
class GeolocationExceptionTest {

    /**
     * Classe concreta para testar a classe abstrata GeolocationException.
     */
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
        // Arrange & Act
        var exception = new TestGeolocationException("Test message");

        // Assert
        assertEquals("Test message", exception.getMessage());
        assertNull(exception.getCause());
        assertEquals("TEST_ERROR", exception.getErrorCode());
    }

    @Test
    @DisplayName("should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        // Arrange
        var cause = new RuntimeException("Root cause");

        // Act
        var exception = new TestGeolocationException("Test message", cause);

        // Assert
        assertEquals("Test message", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("Root cause", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("should preserve cause chain")
    void shouldPreserveCauseChain() {
        // Arrange
        var rootCause = new IllegalArgumentException("Root");
        var intermediateCause = new RuntimeException("Intermediate", rootCause);

        // Act
        var exception = new TestGeolocationException("Top level", intermediateCause);

        // Assert
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    @Test
    @DisplayName("should be instance of RuntimeException")
    void shouldBeInstanceOfRuntimeException() {
        // Arrange & Act
        var exception = new TestGeolocationException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
    }
}
