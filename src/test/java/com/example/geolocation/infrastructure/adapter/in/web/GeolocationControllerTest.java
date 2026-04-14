package com.example.geolocation.infrastructure.adapter.in.web;

import com.example.geolocation.application.domain.model.Coordinates;
import com.example.geolocation.application.domain.model.Country;
import com.example.geolocation.application.domain.model.DataSource;
import com.example.geolocation.application.domain.model.GeolocationInfo;
import com.example.geolocation.application.domain.model.Region;
import com.example.geolocation.application.port.in.GeolocationUseCase;
import com.example.geolocation.infrastructure.config.SecurityProperties;
import com.example.geolocation.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeolocationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("GeolocationController")
class GeolocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeolocationUseCase geolocationUseCase;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private SecurityProperties securityProperties;

    private static final String LOCATE_URL = "/api/geolocation/v1/locate";
    private static final String PLATFORM_HEADER = "x-device-platform";

    private GeolocationInfo createMockResponse(String ip, DataSource source) {
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
    @DisplayName("GET /locate")
    class LocateEndpoint {

        @Test
        @DisplayName("should return 200 with geolocation for valid IP")
        void shouldReturn200ForValidIp() throws Exception {
            // Arrange
            var ip = "8.8.8.8";
            when(geolocationUseCase.locate(ip)).thenReturn(createMockResponse(ip, DataSource.API));

            // Act & Assert
            mockMvc.perform(get(LOCATE_URL)
                    .param("ip", ip)
                    .header(PLATFORM_HEADER, "Web"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ip").value(ip))
                .andExpect(jsonPath("$.country.code").value("US"))
                .andExpect(jsonPath("$.country.name").value("United States"))
                .andExpect(jsonPath("$.source").value("api"));
        }

        @Test
        @DisplayName("should return 200 with cache source")
        void shouldReturn200WithCacheSource() throws Exception {
            // Arrange
            var ip = "8.8.8.8";
            when(geolocationUseCase.locate(ip)).thenReturn(createMockResponse(ip, DataSource.CACHE));

            // Act & Assert
            mockMvc.perform(get(LOCATE_URL)
                    .param("ip", ip)
                    .header(PLATFORM_HEADER, "iOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("cache"));
        }

        @Test
        @DisplayName("should return 200 with fallback source")
        void shouldReturn200WithFallbackSource() throws Exception {
            // Arrange
            var ip = "192.168.1.1";
            var fallbackResponse = new GeolocationInfo(
                ip,
                new Country("BR", "Brazil"),
                new Region("", ""),
                "",
                Coordinates.zero(),
                "",
                "",
                DataSource.FALLBACK,
                Instant.now()
            );
            when(geolocationUseCase.locate(ip)).thenReturn(fallbackResponse);

            // Act & Assert
            mockMvc.perform(get(LOCATE_URL)
                    .param("ip", ip)
                    .header(PLATFORM_HEADER, "Android"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("fallback"))
                .andExpect(jsonPath("$.country.code").value("BR"));
        }
    }

    @Nested
    @DisplayName("platform header validation")
    class PlatformValidation {

        @Test
        @DisplayName("should return 400 when platform header is missing")
        void shouldReturn400WhenPlatformHeaderMissing() throws Exception {
            mockMvc.perform(get(LOCATE_URL)
                    .param("ip", "8.8.8.8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PLATFORM_HEADER"));
        }

        @Test
        @DisplayName("should return 400 for invalid platform")
        void shouldReturn400ForInvalidPlatform() throws Exception {
            mockMvc.perform(get(LOCATE_URL)
                    .param("ip", "8.8.8.8")
                    .header(PLATFORM_HEADER, "Desktop"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PLATFORM"));
        }

        @ParameterizedTest(name = "should accept {0} platform")
        @ValueSource(strings = {"iOS", "Android", "Web"})
        @DisplayName("should accept valid platforms")
        void shouldAcceptValidPlatforms(String platform) throws Exception {
            when(geolocationUseCase.locate(anyString())).thenReturn(createMockResponse("8.8.8.8", DataSource.API));

            mockMvc.perform(get(LOCATE_URL)
                    .param("ip", "8.8.8.8")
                    .header(PLATFORM_HEADER, platform))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("parameter validation")
    class ParameterValidation {

        @Test
        @DisplayName("should return 400 when IP parameter is missing")
        void shouldReturn400WhenIpParameterMissing() throws Exception {
            mockMvc.perform(get(LOCATE_URL)
                    .header(PLATFORM_HEADER, "Web"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"));
        }
    }
}
