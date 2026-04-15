package com.example.geolocation.application.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("DataSource")
@SuppressWarnings("java:S2187") // False positive: @Nested classes contain the tests
class DataSourceTest {

    @Nested
    @DisplayName("getValue")
    class GetValue {

        @Test
        @DisplayName("should return 'cache' for CACHE")
        void shouldReturnCacheForCache() {
            assertEquals("cache", DataSource.CACHE.getValue());
        }

        @Test
        @DisplayName("should return 'api' for API")
        void shouldReturnApiForApi() {
            assertEquals("api", DataSource.API.getValue());
        }

        @Test
        @DisplayName("should return 'fallback' for FALLBACK")
        void shouldReturnFallbackForFallback() {
            assertEquals("fallback", DataSource.FALLBACK.getValue());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringMethod {

        @ParameterizedTest
        @EnumSource(DataSource.class)
        @DisplayName("should return non-empty string for all enum values")
        void shouldReturnNonEmptyStringForAllValues(DataSource source) {
            String result = source.toString();
            assertNotNull(result);
            assertEquals(source.getValue(), result);
        }

        @Test
        @DisplayName("should return same value as getValue")
        void shouldReturnSameValueAsGetValue() {
            assertEquals(DataSource.CACHE.getValue(), DataSource.CACHE.toString());
            assertEquals(DataSource.API.getValue(), DataSource.API.toString());
            assertEquals(DataSource.FALLBACK.getValue(), DataSource.FALLBACK.toString());
        }
    }
}
