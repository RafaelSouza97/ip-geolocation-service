package com.example.geolocation.infrastructure.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController")
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
            var request = Map.of("username", "admin", "password", "Admin123@");
            mockMvc.perform(
                    post(LOGIN_URL).contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                            .content(Objects
                                    .requireNonNull(objectMapper.writeValueAsString(request))))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.token").exists());
        }

        @Test
        @DisplayName("should return 401 for invalid username")
        void shouldReturn401ForInvalidUsername() throws Exception {
            var request = Map.of("username", "wronguser", "password", "Admin123@");
            mockMvc.perform(
                    post(LOGIN_URL).contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                            .content(Objects
                                    .requireNonNull(objectMapper.writeValueAsString(request))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 401 for invalid password")
        void shouldReturn401ForInvalidPassword() throws Exception {
            var request = Map.of("username", "admin", "password", "wrongpassword");
            mockMvc.perform(
                    post(LOGIN_URL).contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                            .content(Objects
                                    .requireNonNull(objectMapper.writeValueAsString(request))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 400 when username is missing")
        void shouldReturn400WhenUsernameMissing() throws Exception {
            var request = Map.of("password", "Admin123@");
            mockMvc.perform(
                    post(LOGIN_URL).contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                            .content(Objects
                                    .requireNonNull(objectMapper.writeValueAsString(request))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when password is missing")
        void shouldReturn400WhenPasswordMissing() throws Exception {
            var request = Map.of("username", "admin");
            mockMvc.perform(
                    post(LOGIN_URL).contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                            .content(Objects
                                    .requireNonNull(objectMapper.writeValueAsString(request))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when body is empty")
        void shouldReturn400WhenBodyEmpty() throws Exception {
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON)).content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("protected endpoint access")
    class ProtectedEndpointAccess {

        @Test
        @DisplayName("should return 403 when accessing protected endpoint without token")
        void shouldReturn403WhenAccessingProtectedEndpointWithoutToken() throws Exception {
            mockMvc.perform(post("/api/geolocation/v1/locate").param("ip", "8.8.8.8"))
                    .andExpect(status().isForbidden());
        }
    }
}
