package com.example.geolocation.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.geolocation.application.domain.exception.ExternalApiException;
import com.example.geolocation.application.domain.exception.InvalidIpAddressException;
import com.example.geolocation.application.domain.model.Coordinates;
import com.example.geolocation.application.domain.model.Country;
import com.example.geolocation.application.domain.model.DataSource;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.domain.model.Region;
import com.example.geolocation.application.port.out.GeolocationCache;
import com.example.geolocation.application.port.out.GeolocationProvider;
import com.example.geolocation.infrastructure.config.GeolocationProperties;
import com.example.geolocation.infrastructure.config.GeolocationProperties.ApiProperties;
import com.example.geolocation.infrastructure.config.GeolocationProperties.CacheProperties;
import com.example.geolocation.infrastructure.config.GeolocationProperties.FallbackProperties;
import com.example.geolocation.infrastructure.config.GeolocationProperties.ProviderProperties;

@DisplayName("GeolocationService")
@ExtendWith(MockitoExtension.class)
class GeolocationServiceTest {

    @Mock
    private GeolocationCache cache;

    @Mock
    private GeolocationProvider provider;

    private GeolocationService service;

    private static final String PUBLIC_IP = "8.8.8.8";

    @BeforeEach
    void setUp() {
        var primary =
                new ApiProperties("ip-api.com", "http://ip-api.com/json", Duration.ofSeconds(5));
        var secondary = new ApiProperties("ipapi.co", "https://ipapi.co", Duration.ofSeconds(5));
        var providers = new ProviderProperties(primary, secondary, Duration.ofMinutes(5));
        var properties = new GeolocationProperties(providers,
                new CacheProperties(Duration.ofHours(24), 10000),
                new FallbackProperties("BR", "Brazil"));
        service = new GeolocationService(cache, provider, properties);
    }

    private GeolocationInfo createApiResponse(String ip) {
        return new GeolocationInfo(ip, new Country("US", "United States"),
                new Region("CA", "California"), "Mountain View",
                new Coordinates(37.4056, -122.0775), "America/Los_Angeles", "Google LLC",
                DataSource.API, Instant.now());
    }

    @Nested
    @DisplayName("when IP is valid and public")
    class ValidPublicIp {

        @Test
        @DisplayName("should return cached result when cache hit")
        void shouldReturnCachedResultWhenCacheHit() {
            var cachedInfo = createApiResponse(PUBLIC_IP);
            when(cache.get(PUBLIC_IP)).thenReturn(Optional.of(cachedInfo));
            var result = service.locate(PUBLIC_IP);
            assertEquals(DataSource.CACHE, result.source());
            assertEquals(PUBLIC_IP, result.ip());
            verify(cache).get(PUBLIC_IP);
            verify(provider, never()).lookup(any());
        }

        @Test
        @DisplayName("should call provider when cache miss")
        void shouldCallProviderWhenCacheMiss() {
            var apiResponse = createApiResponse(PUBLIC_IP);
            when(cache.get(PUBLIC_IP)).thenReturn(Optional.empty());
            when(provider.lookup(PUBLIC_IP)).thenReturn(apiResponse);
            var result = service.locate(PUBLIC_IP);
            assertEquals(DataSource.API, result.source());
            assertEquals(PUBLIC_IP, result.ip());
            assertEquals("United States", result.country().name());
            verify(cache).get(PUBLIC_IP);
            verify(provider).lookup(PUBLIC_IP);
            verify(cache).put(eq(PUBLIC_IP), any(GeolocationInfo.class));
        }

        @Test
        @DisplayName("should store result in cache after API call")
        void shouldStoreResultInCacheAfterApiCall() {
            var apiResponse = createApiResponse(PUBLIC_IP);
            when(cache.get(PUBLIC_IP)).thenReturn(Optional.empty());
            when(provider.lookup(PUBLIC_IP)).thenReturn(apiResponse);
            service.locate(PUBLIC_IP);
            verify(cache).put(PUBLIC_IP, apiResponse);
        }
    }

    @Nested
    @DisplayName("when IP is private or localhost")
    class PrivateOrLocalhostIp {

        @ParameterizedTest
        @ValueSource(strings = {"192.168.1.1", "10.0.0.1", "172.16.0.1", "127.0.0.1"})
        @DisplayName("should return fallback for private IPs without calling provider")
        void shouldReturnFallbackForPrivateIps(String ip) {
            var result = service.locate(ip);
            assertEquals(DataSource.FALLBACK, result.source());
            assertEquals("BR", result.country().code());
            assertEquals("Brazil", result.country().name());
            verify(cache, never()).get(any());
            verify(provider, never()).lookup(any());
        }

        @Test
        @DisplayName("should return fallback for localhost IPv6")
        void shouldReturnFallbackForLocalhostIpv6() {
            var result = service.locate("::1");
            assertEquals(DataSource.FALLBACK, result.source());
            assertEquals("BR", result.country().code());
            verify(provider, never()).lookup(any());
        }
    }

    @Nested
    @DisplayName("when IP is invalid")
    class InvalidIp {

        @ParameterizedTest
        @ValueSource(strings = {"invalid", "999.999.999.999", "abc.def.ghi.jkl", ""})
        @DisplayName("should throw InvalidIpAddressException for invalid IPs")
        void shouldThrowExceptionForInvalidIps(String ip) {
            var exception = assertThrows(InvalidIpAddressException.class, () -> service.locate(ip));
            assertEquals(ip, exception.getIp());
        }

        @Test
        @DisplayName("should throw InvalidIpAddressException for null IP")
        void shouldThrowExceptionForNullIp() {
            assertThrows(InvalidIpAddressException.class, () -> service.locate(null));
        }
    }

    @Nested
    @DisplayName("when external API fails")
    class ExternalApiFailure {

        @Test
        @DisplayName("should return fallback when API throws ExternalApiException")
        void shouldReturnFallbackWhenApiThrowsException() {
            when(cache.get(PUBLIC_IP)).thenReturn(Optional.empty());
            when(provider.lookup(PUBLIC_IP))
                    .thenThrow(new ExternalApiException("ip-api.com", "Connection timeout"));
            var result = service.locate(PUBLIC_IP);
            assertEquals(DataSource.FALLBACK, result.source());
            assertEquals("BR", result.country().code());
        }

        @Test
        @DisplayName("should return fallback when API throws unexpected exception")
        void shouldReturnFallbackWhenApiThrowsUnexpectedException() {
            when(cache.get(PUBLIC_IP)).thenReturn(Optional.empty());
            when(provider.lookup(PUBLIC_IP)).thenThrow(new RuntimeException("Unexpected"));
            var result = service.locate(PUBLIC_IP);
            assertEquals(DataSource.FALLBACK, result.source());
            assertEquals("BR", result.country().code());
        }

        @Test
        @DisplayName("should not cache fallback responses")
        void shouldNotCacheFallbackResponses() {
            when(cache.get(PUBLIC_IP)).thenReturn(Optional.empty());
            when(provider.lookup(PUBLIC_IP))
                    .thenThrow(new ExternalApiException("ip-api.com", "Error"));
            service.locate(PUBLIC_IP);
            verify(cache, never()).put(any(), any());
        }
    }

    @Nested
    @DisplayName("constructor validation")
    class ConstructorValidation {

        @Test
        @DisplayName("should throw NullPointerException for null cache")
        void shouldThrowExceptionForNullCache() {
            assertThrows(NullPointerException.class, () -> new GeolocationService(null, provider,
                    mock(GeolocationProperties.class)));
        }

        @Test
        @DisplayName("should throw NullPointerException for null provider")
        void shouldThrowExceptionForNullProvider() {
            assertThrows(NullPointerException.class,
                    () -> new GeolocationService(cache, null, mock(GeolocationProperties.class)));
        }

        @Test
        @DisplayName("should throw NullPointerException for null properties")
        void shouldThrowExceptionForNullProperties() {
            assertThrows(NullPointerException.class,
                    () -> new GeolocationService(cache, provider, null));
        }
    }
}
