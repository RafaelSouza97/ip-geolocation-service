package com.example.geolocation.application.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Region")
@SuppressWarnings("java:S2187")
class RegionTest {

    @Nested
    @DisplayName("when created with valid data")
    class ValidCreation {

        @Test
        @DisplayName("should create region with code and name")
        void shouldCreateRegionWithCodeAndName() {
            // Arrange & Act
            var region = new Region("SP", "São Paulo");

            // Assert
            assertEquals("SP", region.code());
            assertEquals("São Paulo", region.name());
        }

        @Test
        @DisplayName("should handle null code as empty string")
        void shouldHandleNullCodeAsEmptyString() {
            // Arrange & Act
            var region = new Region(null, "São Paulo");

            // Assert
            assertEquals("", region.code());
            assertEquals("São Paulo", region.name());
        }

        @Test
        @DisplayName("should handle null name as empty string")
        void shouldHandleNullNameAsEmptyString() {
            // Arrange & Act
            var region = new Region("SP", null);

            // Assert
            assertEquals("SP", region.code());
            assertEquals("", region.name());
        }

        @Test
        @DisplayName("should handle both null values")
        void shouldHandleBothNullValues() {
            // Arrange & Act
            var region = new Region(null, null);

            // Assert
            assertEquals("", region.code());
            assertEquals("", region.name());
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("should be equal for same code and name")
        void shouldBeEqualForSameCodeAndName() {
            // Arrange
            var region1 = new Region("SP", "São Paulo");
            var region2 = new Region("SP", "São Paulo");

            // Assert
            assertEquals(region1, region2);
            assertEquals(region1.hashCode(), region2.hashCode());
        }
    }
}
