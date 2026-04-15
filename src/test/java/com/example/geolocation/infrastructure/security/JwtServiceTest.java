package com.example.geolocation.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.example.geolocation.infrastructure.config.SecurityProperties;
import com.example.geolocation.infrastructure.config.SecurityProperties.JwtProperties;
import com.example.geolocation.infrastructure.config.SecurityProperties.UserProperties;

@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET_KEY =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 86400000L;

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
            String token = jwtService.generateToken("testuser");
            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("should generate different tokens for different users")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            String token1 = jwtService.generateToken("user1");
            String token2 = jwtService.generateToken("user2");
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("extractUsername")
    class ExtractUsername {

        @Test
        @DisplayName("should extract username from token")
        void shouldExtractUsernameFromToken() {
            String token = jwtService.generateToken("testuser");
            String username = jwtService.extractUsername(token);
            assertThat(username).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("should return true for valid token and matching username")
        void shouldReturnTrueForValidTokenAndMatchingUsername() {
            String token = jwtService.generateToken("testuser");
            boolean isValid = jwtService.isTokenValid(token, "testuser");
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("should return false for valid token but non-matching username")
        void shouldReturnFalseForValidTokenButNonMatchingUsername() {
            String token = jwtService.generateToken("testuser");
            boolean isValid = jwtService.isTokenValid(token, "otheruser");
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("token expiration")
    class TokenExpiration {

        @Test
        @DisplayName("should create non-expired token with default expiration")
        void shouldCreateNonExpiredTokenWithDefaultExpiration() {
            String token = jwtService.generateToken("testuser");
            assertThat(jwtService.isTokenValid(token, "testuser")).isTrue();
        }

        @Test
        @DisplayName("expired token should be invalid")
        void expiredTokenShouldBeInvalid() {
            var jwtProperties = new JwtProperties(SECRET_KEY, 1L);
            var userProperties = new UserProperties("admin", "Admin123@");
            var securityProperties = new SecurityProperties(jwtProperties, userProperties);
            var shortExpirationService = new JwtService(securityProperties);

            String token = shortExpirationService.generateToken("testuser");
            await().pollInterval(Duration.ofMillis(5)).atMost(Duration.ofMillis(100))
                    .until(() -> !shortExpirationService.isTokenValid(token, "testuser"));
            assertThat(shortExpirationService.isTokenValid(token, "testuser")).isFalse();
        }
    }
}
