package com.example.geolocation.application.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GeolocationInfo")
class GeolocationInfoTest {

    @Nested
    @DisplayName("when created with valid data")
    class ValidCreation {

        @Test
        @DisplayName("should create with all fields")
        void shouldCreateWithAllFields() {
            var country = new Country("BR", "Brazil");
            var region = new Region("SP", "São Paulo");
            var coords = new Coordinates(-23.5505, -46.6333);
            var timestamp = Instant.now();
            var info = new GeolocationInfo("8.8.8.8", country, region, "São Paulo", coords,
                    "America/Sao_Paulo", "Google LLC", DataSource.API, timestamp);
            assertEquals("8.8.8.8", info.ip());
            assertEquals(country, info.country());
            assertEquals(region, info.region());
            assertEquals("São Paulo", info.city());
            assertEquals(coords, info.coordinates());
            assertEquals("America/Sao_Paulo", info.timezone());
            assertEquals("Google LLC", info.isp());
            assertEquals(DataSource.API, info.source());
            assertEquals("api", info.sourceValue());
            assertEquals(timestamp, info.timestamp());
        }

        @Test
        @DisplayName("should use defaults for null optional fields")
        void shouldUseDefaultsForNullOptionalFields() {
            var country = new Country("BR", "Brazil");
            var info = new GeolocationInfo("8.8.8.8", country, null,
                    null, null, null, null, DataSource.API, null);

            assertEquals("8.8.8.8", info.ip());
            assertEquals(country, info.country());
            assertEquals(new Region("", ""), info.region());
            assertEquals("", info.city());
            assertEquals(Coordinates.zero(), info.coordinates());
            assertEquals("", info.timezone());
            assertEquals("", info.isp());
            assertEquals(DataSource.API, info.source());
            assertNotNull(info.timestamp());
        }
    }

    @Nested
    @DisplayName("when created with invalid data")
    class InvalidCreation {

        @Test
        @DisplayName("should throw exception for null IP")
        void shouldThrowExceptionForNullIp() {
            var country = new Country("BR", "Brazil");
            assertThrows(NullPointerException.class, () -> new GeolocationInfo(null, country, null,
                    null, null, null, null, DataSource.API, null));
        }

        @Test
        @DisplayName("should throw exception for null country")
        void shouldThrowExceptionForNullCountry() {
            assertThrows(NullPointerException.class, () -> new GeolocationInfo("8.8.8.8", null,
                    null, null, null, null, null, DataSource.API, null));
        }

        @Test
        @DisplayName("should throw exception for null source")
        void shouldThrowExceptionForNullSource() {
            var country = new Country("BR", "Brazil");
            assertThrows(NullPointerException.class, () -> new GeolocationInfo("8.8.8.8", country,
                    null, null, null, null, null, null, null));
        }
    }

    @Nested
    @DisplayName("source operations")
    class SourceOperations {

        @Test
        @DisplayName("should create copy with cache source")
        void shouldCreateCopyWithCacheSource() {
            var country = new Country("BR", "Brazil");
            var info = new GeolocationInfo("8.8.8.8", country, null, null, null, null, null,
                    DataSource.API, null);
            var cached = info.withCacheSource();
            assertEquals(DataSource.CACHE, cached.source());
            assertEquals("cache", cached.sourceValue());
            assertEquals(info.ip(), cached.ip());
            assertEquals(info.country(), cached.country());
        }

        @Test
        @DisplayName("should identify fallback source")
        void shouldIdentifyFallbackSource() {
            var country = new Country("BR", "Brazil");
            var fallbackInfo = new GeolocationInfo("8.8.8.8", country, null, null, null, null, null,
                    DataSource.FALLBACK, null);
            var apiInfo = new GeolocationInfo("8.8.8.8", country, null, null, null, null, null,
                    DataSource.API, null);
            assertTrue(fallbackInfo.isFallback());
            assertFalse(apiInfo.isFallback());
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("should be equal for same data")
        void shouldBeEqualForSameData() {
            var country = new Country("BR", "Brazil");
            var timestamp = Instant.now();
            var info1 = new GeolocationInfo("8.8.8.8", country, null, null, null, null, null,
                    DataSource.API, timestamp);
            var info2 = new GeolocationInfo("8.8.8.8", country, null, null, null, null, null,
                    DataSource.API, timestamp);
            assertEquals(info1, info2);
            assertEquals(info1.hashCode(), info2.hashCode());
        }
    }
}
