package com.example.geolocation.application.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@DisplayName("Country")
class CountryTest {

    @Nested
    @DisplayName("when created with valid data")
    class ValidCreation {

        @Test
        @DisplayName("should create country with code and name")
        void shouldCreateCountryWithCodeAndName() {
            var country = new Country("BR", "Brazil");
            assertEquals("BR", country.code());
            assertEquals("Brazil", country.name());
        }

        @Test
        @DisplayName("should uppercase country code")
        void shouldUppercaseCountryCode() {
            var country = new Country("br", "Brazil");
            assertEquals("BR", country.code());
        }

        @ParameterizedTest
        @CsvSource({"US, United States", "BR, Brazil", "JP, Japan", "DE, Germany"})
        @DisplayName("should create country for various codes")
        void shouldCreateCountryForVariousCodes(String code, String name) {
            var country = new Country(code, name);
            assertEquals(code.toUpperCase(), country.code());
            assertEquals(name, country.name());
        }
    }

    @Nested
    @DisplayName("when created with invalid data")
    class InvalidCreation {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should throw exception for null or empty code")
        void shouldThrowExceptionForNullOrEmptyCode(String code) {
            assertThrows(IllegalArgumentException.class, () -> new Country(code, "Brazil"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should throw exception for null or empty name")
        void shouldThrowExceptionForNullOrEmptyName(String name) {
            assertThrows(IllegalArgumentException.class, () -> new Country("BR", name));
        }

        @Test
        @DisplayName("should throw exception for blank code")
        void shouldThrowExceptionForBlankCode() {
            assertThrows(IllegalArgumentException.class, () -> new Country("   ", "Brazil"));
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("should be equal for same code and name")
        void shouldBeEqualForSameCodeAndName() {
            var country1 = new Country("BR", "Brazil");
            var country2 = new Country("BR", "Brazil");
            assertEquals(country1, country2);
            assertEquals(country1.hashCode(), country2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different codes")
        void shouldNotBeEqualForDifferentCodes() {
            var country1 = new Country("BR", "Brazil");
            var country2 = new Country("US", "Brazil");
            assertNotEquals(country1, country2);
        }
    }
}
