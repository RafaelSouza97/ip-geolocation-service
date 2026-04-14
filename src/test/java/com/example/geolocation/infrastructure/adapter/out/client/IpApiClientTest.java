package com.example.geolocation.infrastructure.adapter.out.client;

import com.example.geolocation.application.domain.exception.ExternalApiException;
import com.example.geolocation.application.domain.model.DataSource;
import com.example.geolocation.infrastructure.config.GeolocationProperties;
import com.example.geolocation.infrastructure.config.GeolocationProperties.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IpApiClient")
class IpApiClientTest {

    private static WireMockServer wireMockServer;
    private IpApiClient client;
    private ObjectMapper objectMapper;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    private GeolocationProperties createProperties(String baseUrl, Duration timeout) {
        var primary = new ApiProperties("ip-api.com", baseUrl, timeout);
        var secondary = new ApiProperties("ipapi.co", "https://ipapi.co", timeout);
        var providers = new ProviderProperties(primary, secondary, Duration.ofMinutes(5));
        return new GeolocationProperties(
            providers,
            new CacheProperties(Duration.ofHours(24), 10000),
            new FallbackProperties("BR", "Brazil")
        );
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        objectMapper = new ObjectMapper();
        
        var properties = createProperties(
            "http://localhost:" + wireMockServer.port(),
            Duration.ofSeconds(5)
        );
        
        client = new IpApiClient(properties, objectMapper);
    }

    @Nested
    @DisplayName("when API returns success")
    class SuccessfulResponse {

        @Test
        @DisplayName("should parse and return geolocation info")
        void shouldParseAndReturnGeolocationInfo() {
            // Arrange
            var ip = "8.8.8.8";
            stubFor(get(urlEqualTo("/" + ip))
                .willReturn(okJson("""
                    {
                        "status": "success",
                        "country": "United States",
                        "countryCode": "US",
                        "region": "CA",
                        "regionName": "California",
                        "city": "Mountain View",
                        "lat": 37.4056,
                        "lon": -122.0775,
                        "timezone": "America/Los_Angeles",
                        "isp": "Google LLC",
                        "query": "8.8.8.8"
                    }
                    """)));

            // Act
            var result = client.lookup(ip);

            // Assert
            assertEquals(ip, result.ip());
            assertEquals("US", result.country().code());
            assertEquals("United States", result.country().name());
            assertEquals("CA", result.region().code());
            assertEquals("California", result.region().name());
            assertEquals("Mountain View", result.city());
            assertEquals(37.4056, result.coordinates().latitude());
            assertEquals(-122.0775, result.coordinates().longitude());
            assertEquals("America/Los_Angeles", result.timezone());
            assertEquals("Google LLC", result.isp());
            assertEquals(DataSource.API, result.source());
        }

        @Test
        @DisplayName("should handle minimal response")
        void shouldHandleMinimalResponse() {
            // Arrange
            var ip = "1.1.1.1";
            stubFor(get(urlEqualTo("/" + ip))
                .willReturn(okJson("""
                    {
                        "status": "success",
                        "country": "Australia",
                        "countryCode": "AU",
                        "region": "",
                        "regionName": "",
                        "city": "",
                        "lat": 0,
                        "lon": 0,
                        "timezone": "",
                        "isp": "",
                        "query": "1.1.1.1"
                    }
                    """)));

            // Act
            var result = client.lookup(ip);

            // Assert
            assertEquals("AU", result.country().code());
            assertEquals("Australia", result.country().name());
        }
    }

    @Nested
    @DisplayName("when API returns failure")
    class FailureResponse {

        @Test
        @DisplayName("should throw exception when status is fail")
        void shouldThrowExceptionWhenStatusIsFail() {
            // Arrange
            var ip = "invalid";
            stubFor(get(urlEqualTo("/" + ip))
                .willReturn(okJson("""
                    {
                        "status": "fail",
                        "message": "invalid query",
                        "query": "invalid"
                    }
                    """)));

            // Act & Assert
            var exception = assertThrows(ExternalApiException.class, 
                () -> client.lookup(ip));
            assertTrue(exception.getMessage().contains("invalid query"));
        }

        @Test
        @DisplayName("should throw exception when HTTP status is not 200")
        void shouldThrowExceptionWhenHttpStatusIsNot200() {
            // Arrange
            var ip = "8.8.8.8";
            stubFor(get(urlEqualTo("/" + ip))
                .willReturn(serverError().withBody("Internal Server Error")));

            // Act & Assert
            var exception = assertThrows(ExternalApiException.class, 
                () -> client.lookup(ip));
            assertTrue(exception.getMessage().contains("HTTP 500"));
        }

        @Test
        @DisplayName("should throw exception on HTTP 400")
        void shouldThrowExceptionOnHttp400() {
            // Arrange
            var ip = "bad-request";
            stubFor(get(urlEqualTo("/" + ip))
                .willReturn(badRequest().withBody("Bad Request")));

            // Act & Assert
            var exception = assertThrows(ExternalApiException.class, 
                () -> client.lookup(ip));
            assertTrue(exception.getMessage().contains("HTTP 400"));
        }

        @Test
        @DisplayName("should throw exception on connection error")
        void shouldThrowExceptionOnConnectionError() {
            // Arrange - usar porta diferente para simular erro de conexão
            var badProperties = createProperties("http://localhost:1", Duration.ofSeconds(1));
            var badClient = new IpApiClient(badProperties, objectMapper);

            // Act & Assert
            assertThrows(ExternalApiException.class, 
                () -> badClient.lookup("8.8.8.8"));
        }

        @Test
        @DisplayName("should throw exception on invalid JSON")
        void shouldThrowExceptionOnInvalidJson() {
            // Arrange
            var ip = "8.8.8.8";
            stubFor(get(urlEqualTo("/" + ip))
                .willReturn(ok("not valid json {")));

            // Act & Assert
            assertThrows(ExternalApiException.class, 
                () -> client.lookup(ip));
        }
    }

    @Nested
    @DisplayName("request verification")
    class RequestVerification {

        @Test
        @DisplayName("should make GET request to correct URL")
        void shouldMakeGetRequestToCorrectUrl() {
            // Arrange
            var ip = "8.8.8.8";
            stubFor(get(urlEqualTo("/" + ip))
                .willReturn(okJson("""
                    {
                        "status": "success",
                        "country": "United States",
                        "countryCode": "US",
                        "region": "CA",
                        "regionName": "California",
                        "city": "Mountain View",
                        "lat": 37.4056,
                        "lon": -122.0775,
                        "timezone": "America/Los_Angeles",
                        "isp": "Google LLC",
                        "query": "8.8.8.8"
                    }
                    """)));

            // Act
            client.lookup(ip);

            // Assert
            verify(getRequestedFor(urlEqualTo("/" + ip)));
        }
    }
}
