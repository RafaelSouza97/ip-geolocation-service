package com.example.geolocation.infrastructure.adapter.out.client;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.geolocation.application.domain.exception.ExternalApiException;
import com.example.geolocation.application.domain.model.Coordinates;
import com.example.geolocation.application.domain.model.Country;
import com.example.geolocation.application.domain.model.DataSource;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.domain.model.Region;
import com.example.geolocation.application.port.out.GeolocationProvider;
import com.example.geolocation.infrastructure.config.GeolocationProperties;
import com.example.geolocation.infrastructure.config.GeolocationProperties.ApiProperties;
import com.example.geolocation.infrastructure.config.GeolocationProperties.CacheProperties;
import com.example.geolocation.infrastructure.config.GeolocationProperties.FallbackProperties;
import com.example.geolocation.infrastructure.config.GeolocationProperties.ProviderProperties;

@DisplayName("ProviderSelector")
@ExtendWith(MockitoExtension.class)
class ProviderSelectorTest {

    @Mock
    private GeolocationProvider primaryProvider;

    @Mock
    private GeolocationProvider secondaryProvider;

    private ProviderSelector selector;

    private static final String TEST_IP = "8.8.8.8";

    @BeforeEach
    void setUp() {
        var primary =
                new ApiProperties("ip-api.com", "http://ip-api.com/json", Duration.ofSeconds(5));
        var secondary = new ApiProperties("ipapi.co", "https://ipapi.co", Duration.ofSeconds(5));
        var providers = new ProviderProperties(primary, secondary, Duration.ofMillis(100));
        var properties = new GeolocationProperties(providers,
                new CacheProperties(Duration.ofHours(24), 10000),
                new FallbackProperties("BR", "Brazil"));

        selector = new ProviderSelector(primaryProvider, secondaryProvider, properties);
    }

    private GeolocationInfo createMockResponse(String ip, DataSource source) {
        return new GeolocationInfo(ip, new Country("US", "United States"),
                new Region("CA", "California"), "Mountain View",
                new Coordinates(37.4056, -122.0775), "America/Los_Angeles", "Google LLC", source,
                Instant.now());
    }

    @Nested
    @DisplayName("when primary provider is healthy")
    class PrimaryProviderHealthy {

        @Test
        @DisplayName("should return result from primary provider")
        void shouldReturnFromPrimaryProvider() {
            var expected = createMockResponse(TEST_IP, DataSource.API);
            when(primaryProvider.lookup(TEST_IP)).thenReturn(expected);
            var result = selector.lookup(TEST_IP);
            assertEquals(expected, result);
            verify(primaryProvider).lookup(TEST_IP);
            verifyNoInteractions(secondaryProvider);
        }

        @Test
        @DisplayName("should not be in failover mode")
        void shouldNotBeInFailoverMode() {
            when(primaryProvider.lookup(TEST_IP))
                    .thenReturn(createMockResponse(TEST_IP, DataSource.API));
            selector.lookup(TEST_IP);
            assertFalse(selector.isInFailover());
            assertEquals("PRIMARY", selector.getActiveProviderName());
        }
    }

    @Nested
    @DisplayName("when primary provider fails")
    class PrimaryProviderFails {

        @Test
        @DisplayName("should fallback to secondary provider")
        void shouldFallbackToSecondary() {
            when(primaryProvider.lookup(TEST_IP))
                    .thenThrow(new ExternalApiException("ip-api.com", "Connection timeout"));
            var expected = createMockResponse(TEST_IP, DataSource.API);
            when(secondaryProvider.lookup(TEST_IP)).thenReturn(expected);
            var result = selector.lookup(TEST_IP);
            assertEquals(expected, result);
            verify(primaryProvider).lookup(TEST_IP);
            verify(secondaryProvider).lookup(TEST_IP);
        }

        @Test
        @DisplayName("should switch to failover mode")
        void shouldSwitchToFailoverMode() {
            when(primaryProvider.lookup(TEST_IP))
                    .thenThrow(new ExternalApiException("ip-api.com", "Connection timeout"));
            when(secondaryProvider.lookup(TEST_IP))
                    .thenReturn(createMockResponse(TEST_IP, DataSource.API));
            selector.lookup(TEST_IP);
            assertTrue(selector.isInFailover());
            assertEquals("SECONDARY", selector.getActiveProviderName());
        }

        @Test
        @DisplayName("should continue using secondary during failover period")
        void shouldContinueUsingSecondaryDuringFailover() {
            when(primaryProvider.lookup(anyString()))
                    .thenThrow(new ExternalApiException("ip-api.com", "Connection timeout"));
            when(secondaryProvider.lookup(anyString()))
                    .thenReturn(createMockResponse(TEST_IP, DataSource.API));
            selector.lookup(TEST_IP);
            reset(primaryProvider, secondaryProvider);
            when(secondaryProvider.lookup(anyString()))
                    .thenReturn(createMockResponse("1.1.1.1", DataSource.API));
            selector.lookup("1.1.1.1");
            verifyNoInteractions(primaryProvider);
            verify(secondaryProvider).lookup("1.1.1.1");
        }
    }

    @Nested
    @DisplayName("when both providers fail")
    class BothProvidersFail {

        @Test
        @DisplayName("should throw exception when both fail")
        void shouldThrowExceptionWhenBothFail() {
            when(primaryProvider.lookup(TEST_IP))
                    .thenThrow(new ExternalApiException("ip-api.com", "Connection timeout"));
            when(secondaryProvider.lookup(TEST_IP))
                    .thenThrow(new ExternalApiException("ipapi.co", "Rate limit exceeded"));
            var exception =
                    assertThrows(ExternalApiException.class, () -> selector.lookup(TEST_IP));
            assertTrue(exception.getMessage().contains("Both providers failed"));
        }
    }

    @Nested
    @DisplayName("failover expiration")
    class FailoverExpiration {

        @Test
        @DisplayName("should reset to primary after failover period expires")
        void shouldResetToPrimaryAfterFailoverExpires() {
            when(primaryProvider.lookup(TEST_IP))
                    .thenThrow(new ExternalApiException("ip-api.com", "Timeout"));
            when(secondaryProvider.lookup(TEST_IP))
                    .thenReturn(createMockResponse(TEST_IP, DataSource.API));

            selector.lookup(TEST_IP);
            assertTrue(selector.isInFailover());
            await().pollInterval(Duration.ofMillis(20)).atMost(Duration.ofMillis(300))
                    .until(() -> !selector.isInFailover());
            reset(primaryProvider, secondaryProvider);
            when(primaryProvider.lookup(TEST_IP))
                    .thenReturn(createMockResponse(TEST_IP, DataSource.API));
            selector.lookup(TEST_IP);
            assertFalse(selector.isInFailover());
            assertEquals("PRIMARY", selector.getActiveProviderName());
            verify(primaryProvider).lookup(TEST_IP);
        }
    }

    @Nested
    @DisplayName("reset functionality")
    class ResetFunctionality {

        @Test
        @DisplayName("should reset to primary provider")
        void shouldResetToPrimary() {
            when(primaryProvider.lookup(TEST_IP))
                    .thenThrow(new ExternalApiException("ip-api.com", "Timeout"));
            when(secondaryProvider.lookup(TEST_IP))
                    .thenReturn(createMockResponse(TEST_IP, DataSource.API));

            selector.lookup(TEST_IP);
            assertTrue(selector.isInFailover());
            selector.reset();
            assertFalse(selector.isInFailover());
            assertEquals("PRIMARY", selector.getActiveProviderName());
        }
    }
}
