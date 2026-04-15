package com.example.geolocation.application.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Platform")
@SuppressWarnings("java:S2187") // False positive: @Nested classes contain the tests
class PlatformTest {

    @Nested
    @DisplayName("getValue")
    class GetValue {

        @Test
        @DisplayName("should return 'iOS' for IOS")
        void shouldReturnIosForIos() {
            assertEquals("iOS", Platform.IOS.getValue());
        }

        @Test
        @DisplayName("should return 'Android' for ANDROID")
        void shouldReturnAndroidForAndroid() {
            assertEquals("Android", Platform.ANDROID.getValue());
        }

        @Test
        @DisplayName("should return 'Web' for WEB")
        void shouldReturnWebForWeb() {
            assertEquals("Web", Platform.WEB.getValue());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringMethod {

        @ParameterizedTest
        @EnumSource(Platform.class)
        @DisplayName("should return non-empty string matching getValue")
        void shouldReturnNonEmptyStringMatchingGetValue(Platform platform) {
            String result = platform.toString();
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(platform.getValue(), result);
        }
    }

    @Nested
    @DisplayName("validValues")
    class ValidValues {

        @Test
        @DisplayName("should return set containing all platform values")
        void shouldReturnSetContainingAllPlatformValues() {
            Set<String> validValues = Platform.validValues();

            assertNotNull(validValues);
            assertEquals(3, validValues.size());
            assertTrue(validValues.contains("iOS"));
            assertTrue(validValues.contains("Android"));
            assertTrue(validValues.contains("Web"));
        }

        @Test
        @DisplayName("should return immutable set")
        void shouldReturnImmutableSet() {
            Set<String> validValues = Platform.validValues();

            assertThrows(UnsupportedOperationException.class, () -> validValues.add("NewPlatform"));
        }
    }

    @Nested
    @DisplayName("isValid")
    class IsValid {

        @ParameterizedTest
        @ValueSource(strings = {"iOS", "Android", "Web"})
        @DisplayName("should return true for valid platforms")
        void shouldReturnTrueForValidPlatforms(String platform) {
            assertTrue(Platform.isValid(platform));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ios", "android", "web", "INVALID", "", "null"})
        @DisplayName("should return false for invalid platforms")
        void shouldReturnFalseForInvalidPlatforms(String platform) {
            assertFalse(Platform.isValid(platform));
        }
    }

    @Nested
    @DisplayName("fromValue")
    class FromValue {

        @Test
        @DisplayName("should return IOS for 'iOS'")
        void shouldReturnIosForIos() {
            assertEquals(Platform.IOS, Platform.fromValue("iOS"));
        }

        @Test
        @DisplayName("should return ANDROID for 'Android'")
        void shouldReturnAndroidForAndroid() {
            assertEquals(Platform.ANDROID, Platform.fromValue("Android"));
        }

        @Test
        @DisplayName("should return WEB for 'Web'")
        void shouldReturnWebForWeb() {
            assertEquals(Platform.WEB, Platform.fromValue("Web"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ios", "ANDROID", "invalid", ""})
        @DisplayName("should throw exception for invalid values")
        void shouldThrowExceptionForInvalidValues(String value) {
            assertThrows(IllegalArgumentException.class, () -> Platform.fromValue(value));
        }
    }

    @Nested
    @DisplayName("validValuesAsString")
    class ValidValuesAsString {

        @Test
        @DisplayName("should return comma-separated string of valid values")
        void shouldReturnCommaSeparatedString() {
            String result = Platform.validValuesAsString();

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.contains("iOS"));
            assertTrue(result.contains("Android"));
            assertTrue(result.contains("Web"));
        }
    }
}
