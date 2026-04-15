package com.example.geolocation.application.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Coordinates")
class CoordinatesTest {

    @Nested
    @DisplayName("when created with valid data")
    class ValidCreation {

        @Test
        @DisplayName("should create coordinates with latitude and longitude")
        void shouldCreateCoordinatesWithLatitudeAndLongitude() {
            // Arrange & Act
            var coords = new Coordinates(-23.5505, -46.6333);

            // Assert
            assertEquals(-23.5505, coords.latitude());
            assertEquals(-46.6333, coords.longitude());
        }

        @ParameterizedTest
        @CsvSource({"0, 0", "90, 180", "-90, -180", "45.5, -122.6", "-33.8688, 151.2093"})
        @DisplayName("should create coordinates for various valid values")
        void shouldCreateCoordinatesForVariousValidValues(double lat, double lon) {
            // Arrange & Act
            var coords = new Coordinates(lat, lon);

            // Assert
            assertEquals(lat, coords.latitude());
            assertEquals(lon, coords.longitude());
        }

        @Test
        @DisplayName("should create zero coordinates with factory method")
        void shouldCreateZeroCoordinatesWithFactoryMethod() {
            // Arrange & Act
            var coords = Coordinates.zero();

            // Assert
            assertEquals(0, coords.latitude());
            assertEquals(0, coords.longitude());
        }
    }

    @Nested
    @DisplayName("when created with invalid data")
    class InvalidCreation {

        @Test
        @DisplayName("should throw exception for latitude below -90")
        void shouldThrowExceptionForLatitudeBelowMinus90() {
            // Arrange & Act & Assert
            var exception =
                    assertThrows(IllegalArgumentException.class, () -> new Coordinates(-91, 0));
            assertTrue(exception.getMessage().contains("Latitude"));
        }

        @Test
        @DisplayName("should throw exception for latitude above 90")
        void shouldThrowExceptionForLatitudeAbove90() {
            // Arrange & Act & Assert
            var exception =
                    assertThrows(IllegalArgumentException.class, () -> new Coordinates(91, 0));
            assertTrue(exception.getMessage().contains("Latitude"));
        }

        @Test
        @DisplayName("should throw exception for longitude below -180")
        void shouldThrowExceptionForLongitudeBelowMinus180() {
            // Arrange & Act & Assert
            var exception =
                    assertThrows(IllegalArgumentException.class, () -> new Coordinates(0, -181));
            assertTrue(exception.getMessage().contains("Longitude"));
        }

        @Test
        @DisplayName("should throw exception for longitude above 180")
        void shouldThrowExceptionForLongitudeAbove180() {
            // Arrange & Act & Assert
            var exception =
                    assertThrows(IllegalArgumentException.class, () -> new Coordinates(0, 181));
            assertTrue(exception.getMessage().contains("Longitude"));
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("should be equal for same latitude and longitude")
        void shouldBeEqualForSameLatitudeAndLongitude() {
            // Arrange
            var coords1 = new Coordinates(-23.5505, -46.6333);
            var coords2 = new Coordinates(-23.5505, -46.6333);

            // Assert
            assertEquals(coords1, coords2);
            assertEquals(coords1.hashCode(), coords2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different coordinates")
        void shouldNotBeEqualForDifferentCoordinates() {
            // Arrange
            var coords1 = new Coordinates(-23.5505, -46.6333);
            var coords2 = new Coordinates(40.7128, -74.0060);

            // Assert
            assertNotEquals(coords1, coords2);
        }
    }
}
