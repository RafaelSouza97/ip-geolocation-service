package com.example.geolocation.infrastructure.adapter.out.client;

import com.example.geolocation.application.domain.exception.ExternalApiException;
import com.example.geolocation.application.domain.model.Coordinates;
import com.example.geolocation.application.domain.model.Country;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.domain.model.Region;
import com.example.geolocation.application.port.out.GeolocationProvider;
import com.example.geolocation.infrastructure.config.GeolocationProperties;
import com.example.geolocation.infrastructure.config.GeolocationProperties.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
        var primary = new ApiProperties("ip-api.com", "http://ip-api.com/json", Duration.ofSeconds(5));
        var secondary = new ApiProperties("ipapi.co", "https://ipapi.co", Duration.ofSeconds(5));
        var providers = new ProviderProperties(primary, secondary, Duration.ofMillis(100)); // Short duration for tests
        var properties = new GeolocationProperties(
            providers,
            new CacheProperties(Duration.ofHours(24), 10000),
            new FallbackProperties("BR", "Brazil")
        );

        selector = new ProviderSelector(primaryProvider, secondaryProvider, properties);
    }

    private GeolocationInfo createMockResponse(String ip, String source) {
        return new GeolocationInfo(
            ip,
            new Country("US", "United States"),
            new Region("CA", "California"),
            "Mountain View",
            new Coordinates(37.4056, -122.0775),
            "America/Los_Angeles",
            "Google LLC",
            source,
            Instant.now()
        );
    }

    @Nested
    @DisplayName("when primary provider is healthy")
    class PrimaryProviderHealthy {

        @Test
        @DisplayName("should return result from primary provider")
        void shouldReturnFromPrimaryProvider() {
            // Arrange
            var expected = createMockResponse(TEST_IP, "api");
            when(primaryProvider.lookup(TEST_IP)).thenReturn(expected);

            // Act
            var result = selector.lookup(TEST_IP);

            // Assert
            assertEquals(expected, result);
            verify(primaryProvider).lookup(TEST_IP);
            verifyNoInteractions(secondaryProvider);
        }

        @Test
        @DisplayName("should not be in failover mode")
        void shouldNotBeInFailoverMode() {
            // Arrange
            when(primaryProvider.lookup(TEST_IP)).thenReturn(createMockResponse(TEST_IP, "api"));

            // Act
            selector.lookup(TEST_IP);

            // Assert
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
            // Arrange
            when(primaryProvider.lookup(TEST_IP))
                .thenThrow(new ExternalApiException("ip-api.com", "Connection timeout"));
            var expected = createMockResponse(TEST_IP, "api");
            when(secondaryProvider.lookup(TEST_IP)).thenReturn(expected);

            // Act
            var result = selector.lookup(TEST_IP);

            // Assert
            assertEquals(expected, result);
            verify(primaryProvider).lookup(TEST_IP);
            verify(secondaryProvider).lookup(TEST_IP);
        }

        @Test
        @DisplayName("should switch to failover mode")
        void shouldSwitchToFailoverMode() {
            // Arrange
            when(primaryProvider.lookup(TEST_IP))
                .thenThrow(new ExternalApiException("ip-api.com", "Connection timeout"));
            when(secondaryProvider.lookup(TEST_IP))
                .thenReturn(createMockResponse(TEST_IP, "api"));

            // Act
            selector.lookup(TEST_IP);

            // Assert
            assertTrue(selector.isInFailover());
            assertEquals("SECONDARY", selector.getActiveProviderName());
        }

        @Test
        @DisplayName("should continue using secondary during failover period")
        void shouldContinueUsingSecondaryDuringFailover() {
            // Arrange
            when(primaryProvider.lookup(anyString()))
                .thenThrow(new ExternalApiException("ip-api.com", "Connection timeout"));
            when(secondaryProvider.lookup(anyString()))
                .thenReturn(createMockResponse(TEST_IP, "api"));

            // Act - first call triggers failover
            selector.lookup(TEST_IP);
            
            // Reset mocks to track subsequent calls
            reset(primaryProvider, secondaryProvider);
            when(secondaryProvider.lookup(anyString()))
                .thenReturn(createMockResponse("1.1.1.1", "api"));
            
            // Act - second call should go directly to secondary
            selector.lookup("1.1.1.1");

            // Assert
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
            // Arrange
            when(primaryProvider.lookup(TEST_IP))
                .thenThrow(new ExternalApiException("ip-api.com", "Connection timeout"));
            when(secondaryProvider.lookup(TEST_IP))
                .thenThrow(new ExternalApiException("ipapi.co", "Rate limit exceeded"));

            // Act & Assert
            var exception = assertThrows(ExternalApiException.class, 
                () -> selector.lookup(TEST_IP));
            assertTrue(exception.getMessage().contains("Both providers failed"));
        }
    }

    @Nested
    @DisplayName("failover expiration")
    class FailoverExpiration {

        @Test
        @DisplayName("should reset to primary after failover period expires")
        void shouldResetToPrimaryAfterFailoverExpires() throws InterruptedException {
            // Arrange - trigger failover first
            when(primaryProvider.lookup(TEST_IP))
                .thenThrow(new ExternalApiException("ip-api.com", "Timeout"));
            when(secondaryProvider.lookup(TEST_IP))
                .thenReturn(createMockResponse(TEST_IP, "api"));
            
            selector.lookup(TEST_IP);
            assertTrue(selector.isInFailover());
            
            // Wait for failover to expire (100ms configured in setUp)
            Thread.sleep(150);
            
            // Reset mocks
            reset(primaryProvider, secondaryProvider);
            when(primaryProvider.lookup(TEST_IP))
                .thenReturn(createMockResponse(TEST_IP, "api"));
            
            // Act
            selector.lookup(TEST_IP);

            // Assert
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
            // Arrange - trigger failover first
            when(primaryProvider.lookup(TEST_IP))
                .thenThrow(new ExternalApiException("ip-api.com", "Timeout"));
            when(secondaryProvider.lookup(TEST_IP))
                .thenReturn(createMockResponse(TEST_IP, "api"));
            
            selector.lookup(TEST_IP);
            assertTrue(selector.isInFailover());

            // Act
            selector.reset();

            // Assert
            assertFalse(selector.isInFailover());
            assertEquals("PRIMARY", selector.getActiveProviderName());
        }
    }
}
