package com.example.geolocation.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.geolocation.application.domain.exception.InvalidIpAddressException;
import com.example.geolocation.application.domain.exception.PrivateIpAddressException;
import com.example.geolocation.application.port.in.GeolocationUseCase;
import com.example.geolocation.infrastructure.config.SecurityProperties;
import com.example.geolocation.infrastructure.security.JwtService;

@WebMvcTest(GeolocationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

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

    @Nested
    @DisplayName("InvalidIpAddressException handling")
    class InvalidIpAddressExceptionHandling {

        @Test
        @DisplayName("should return 400 with INVALID_IP_FORMAT code")
        void shouldReturn400WithInvalidIpFormatCode() throws Exception {
            when(geolocationUseCase.locate(anyString()))
                    .thenThrow(new InvalidIpAddressException("invalid-ip"));
            mockMvc.perform(
                    get(LOCATE_URL).param("ip", "invalid-ip").header(PLATFORM_HEADER, "Web"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_IP_FORMAT")).andExpect(
                            jsonPath("$.message").value("Invalid IP address format: invalid-ip"));
        }
    }

    @Nested
    @DisplayName("PrivateIpAddressException handling")
    class PrivateIpAddressExceptionHandling {

        @Test
        @DisplayName("should return 400 with PRIVATE_IP_ADDRESS code")
        void shouldReturn400WithPrivateIpAddressCode() throws Exception {
            when(geolocationUseCase.locate(anyString()))
                    .thenThrow(new PrivateIpAddressException("192.168.1.1"));
            mockMvc.perform(
                    get(LOCATE_URL).param("ip", "192.168.1.1").header(PLATFORM_HEADER, "Web"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("PRIVATE_IP_ADDRESS"))
                    .andExpect(jsonPath("$.message")
                            .value("Private or reserved IP address: 192.168.1.1"));
        }
    }

    @Nested
    @DisplayName("Generic exception handling")
    class GenericExceptionHandling {

        @Test
        @DisplayName("should return 500 for unexpected exceptions")
        void shouldReturn500ForUnexpectedExceptions() throws Exception {
            when(geolocationUseCase.locate(anyString()))
                    .thenThrow(new RuntimeException("Unexpected error"));
            mockMvc.perform(get(LOCATE_URL).param("ip", "8.8.8.8").header(PLATFORM_HEADER, "Web"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
        }

        @Test
        @DisplayName("should not expose internal error details")
        void shouldNotExposeInternalErrorDetails() throws Exception {
            when(geolocationUseCase.locate(anyString()))
                    .thenThrow(new NullPointerException("Sensitive internal info"));
            var result = mockMvc
                    .perform(get(LOCATE_URL).param("ip", "8.8.8.8").header(PLATFORM_HEADER, "Web"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                    .andReturn();
            String responseBody = result.getResponse().getContentAsString();
            org.assertj.core.api.Assertions.assertThat(responseBody).doesNotContain("Sensitive");
        }
    }
}
