package com.example.geolocation.infrastructure.security;

import com.example.geolocation.infrastructure.config.SecurityProperties;
import com.example.geolocation.infrastructure.config.SecurityProperties.JwtProperties;
import com.example.geolocation.infrastructure.config.SecurityProperties.UserProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        var jwtProperties = new JwtProperties(SECRET_KEY, EXPIRATION);
        var userProperties = new UserProperties("admin", "Admin123@");
        var securityProperties = new SecurityProperties(jwtProperties, userProperties);
        jwtService = new JwtService(securityProperties);
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("should generate a valid JWT token")
        void shouldGenerateValidJwtToken() {
            // Act
            String token = jwtService.generateToken("testuser");

            // Assert
            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3); // Header.Payload.Signature
        }

        @Test
        @DisplayName("should generate different tokens for different users")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            // Act
            String token1 = jwtService.generateToken("user1");
            String token2 = jwtService.generateToken("user2");

            // Assert
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("extractUsername")
    class ExtractUsername {

        @Test
        @DisplayName("should extract username from token")
        void shouldExtractUsernameFromToken() {
            // Arrange
            String token = jwtService.generateToken("testuser");

            // Act
            String username = jwtService.extractUsername(token);

            // Assert
            assertThat(username).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("should return true for valid token and matching username")
        void shouldReturnTrueForValidTokenAndMatchingUsername() {
            // Arrange
            String token = jwtService.generateToken("testuser");

            // Act
            boolean isValid = jwtService.isTokenValid(token, "testuser");

            // Assert
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("should return false for valid token but non-matching username")
        void shouldReturnFalseForValidTokenButNonMatchingUsername() {
            // Arrange
            String token = jwtService.generateToken("testuser");

            // Act
            boolean isValid = jwtService.isTokenValid(token, "otheruser");

            // Assert
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("token expiration")
    class TokenExpiration {

        @Test
        @DisplayName("should create non-expired token with default expiration")
        void shouldCreateNonExpiredTokenWithDefaultExpiration() {
            // Arrange
            String token = jwtService.generateToken("testuser");

            // Act & Assert
            assertThat(jwtService.isTokenValid(token, "testuser")).isTrue();
        }

        @Test
        @DisplayName("expired token should be invalid")
        void expiredTokenShouldBeInvalid() {
            // Arrange - create service with very short expiration
            var jwtProperties = new JwtProperties(SECRET_KEY, 1L); // 1ms expiration
            var userProperties = new UserProperties("admin", "Admin123@");
            var securityProperties = new SecurityProperties(jwtProperties, userProperties);
            var shortExpirationService = new JwtService(securityProperties);

            String token = shortExpirationService.generateToken("testuser");

            // Wait for token to expire
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Act & Assert
            assertThat(shortExpirationService.isTokenValid(token, "testuser")).isFalse();
        }
    }
}
