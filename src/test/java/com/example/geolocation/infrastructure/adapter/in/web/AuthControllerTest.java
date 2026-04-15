package com.example.geolocation.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController")
@SuppressWarnings("java:S2187")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String LOGIN_URL = "/auth/login";

    @Nested
    @DisplayName("POST /auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("should return 200 with token for valid credentials")
        void shouldReturn200WithTokenForValidCredentials() throws Exception {
            // Arrange
            var request = Map.of("username", "admin", "password", "Admin123@");

            // Act & Assert
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()));
        }

        @Test
        @DisplayName("should return 401 for invalid username")
        void shouldReturn401ForInvalidUsername() throws Exception {
            // Arrange
            var request = Map.of("username", "wronguser", "password", "Admin123@");

            // Act & Assert
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 401 for invalid password")
        void shouldReturn401ForInvalidPassword() throws Exception {
            // Arrange
            var request = Map.of("username", "admin", "password", "wrongpassword");

            // Act & Assert
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 400 when username is missing")
        void shouldReturn400WhenUsernameMissing() throws Exception {
            // Arrange
            var request = Map.of("password", "Admin123@");

            // Act & Assert
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when password is missing")
        void shouldReturn400WhenPasswordMissing() throws Exception {
            // Arrange
            var request = Map.of("username", "admin");

            // Act & Assert
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when body is empty")
        void shouldReturn400WhenBodyEmpty() throws Exception {
            // Act & Assert
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("protected endpoint access")
    class ProtectedEndpointAccess {

        @Test
        @DisplayName("should return 403 when accessing protected endpoint without token")
        void shouldReturn403WhenAccessingProtectedEndpointWithoutToken() throws Exception {
            mockMvc.perform(post("/api/geolocation/v1/locate")
                    .param("ip", "8.8.8.8"))
                .andExpect(status().isForbidden());
        }
    }
}
