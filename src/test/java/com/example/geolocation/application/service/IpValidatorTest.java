package com.example.geolocation.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IpValidator")
class IpValidatorTest {

    @Nested
    @DisplayName("IPv4 validation")
    class Ipv4Validation {

        @ParameterizedTest
        @ValueSource(strings = {
            "8.8.8.8",
            "1.1.1.1",
            "192.168.1.1",
            "255.255.255.255",
            "0.0.0.0",
            "177.45.123.45",
            "10.0.0.1"
        })
        @DisplayName("should accept valid IPv4 addresses")
        void shouldAcceptValidIpv4Addresses(String ip) {
            assertTrue(IpValidator.isValidIpv4(ip));
            assertTrue(IpValidator.isValid(ip));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "256.1.1.1",
            "1.256.1.1",
            "1.1.256.1",
            "1.1.1.256",
            "999.999.999.999",
            "1.1.1",
            "1.1.1.1.1",
            "abc.def.ghi.jkl",
            "1.1.1.a",
            "1.1.1.-1"
        })
        @DisplayName("should reject invalid IPv4 addresses")
        void shouldRejectInvalidIpv4Addresses(String ip) {
            assertFalse(IpValidator.isValidIpv4(ip));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should reject null and empty IPv4")
        void shouldRejectNullAndEmptyIpv4(String ip) {
            assertFalse(IpValidator.isValidIpv4(ip));
        }
    }

    @Nested
    @DisplayName("IPv6 validation")
    class Ipv6Validation {

        @ParameterizedTest
        @ValueSource(strings = {
            "2001:4860:4860::8888",
            "2001:0db8:85a3:0000:0000:8a2e:0370:7334",
            "::1",
            "::",
            "fe80::1",
            "2001:db8::1",
            "2001:db8:85a3::8a2e:370:7334"
        })
        @DisplayName("should accept valid IPv6 addresses")
        void shouldAcceptValidIpv6Addresses(String ip) {
            assertTrue(IpValidator.isValidIpv6(ip));
            assertTrue(IpValidator.isValid(ip));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "2001:4860:4860::8888::1",
            ":::1",
            "2001:db8:85a3:0000:0000:8a2e:0370:7334:extra",
            "gggg::",
            "12345::"
        })
        @DisplayName("should reject invalid IPv6 addresses")
        void shouldRejectInvalidIpv6Addresses(String ip) {
            assertFalse(IpValidator.isValidIpv6(ip));
        }
    }

    @Nested
    @DisplayName("private/reserved IP detection")
    class PrivateIpDetection {

        @ParameterizedTest
        @ValueSource(strings = {
            "10.0.0.1",
            "10.255.255.255",
            "172.16.0.1",
            "172.31.255.255",
            "192.168.0.1",
            "192.168.255.255",
            "127.0.0.1",
            "127.255.255.255",
            "169.254.1.1",
            "0.0.0.0"
        })
        @DisplayName("should detect private IPv4 addresses")
        void shouldDetectPrivateIpv4Addresses(String ip) {
            assertTrue(IpValidator.isPrivateOrReserved(ip));
            assertFalse(IpValidator.isPublic(ip));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "::1",
            "fe80::1",
            "fc00::1",
            "fd00::1"
        })
        @DisplayName("should detect private/localhost IPv6 addresses")
        void shouldDetectPrivateIpv6Addresses(String ip) {
            assertTrue(IpValidator.isPrivateOrReserved(ip));
            assertFalse(IpValidator.isPublic(ip));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "8.8.8.8",
            "1.1.1.1",
            "177.45.123.45",
            "2001:4860:4860::8888"
        })
        @DisplayName("should detect public IP addresses")
        void shouldDetectPublicIpAddresses(String ip) {
            assertTrue(IpValidator.isPublic(ip));
            assertFalse(IpValidator.isPrivateOrReserved(ip));
        }
    }

    @Nested
    @DisplayName("localhost detection")
    class LocalhostDetection {

        @ParameterizedTest
        @ValueSource(strings = {
            "127.0.0.1",
            "127.0.0.2",
            "127.255.255.255",
            "::1"
        })
        @DisplayName("should detect localhost addresses")
        void shouldDetectLocalhostAddresses(String ip) {
            assertTrue(IpValidator.isLocalhost(ip));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "8.8.8.8",
            "192.168.1.1",
            "2001:4860:4860::8888"
        })
        @DisplayName("should not detect non-localhost addresses")
        void shouldNotDetectNonLocalhostAddresses(String ip) {
            assertFalse(IpValidator.isLocalhost(ip));
        }
    }

    @Nested
    @DisplayName("normalization")
    class Normalization {

        @Test
        @DisplayName("should trim whitespace")
        void shouldTrimWhitespace() {
            assertEquals("8.8.8.8", IpValidator.normalize("  8.8.8.8  "));
        }

        @Test
        @DisplayName("should lowercase IPv6")
        void shouldLowercaseIpv6() {
            assertEquals("2001:db8::1", IpValidator.normalize("2001:DB8::1"));
        }

        @Test
        @DisplayName("should throw exception for null input")
        void shouldThrowExceptionForNullInput() {
            assertThrows(IllegalArgumentException.class, () -> IpValidator.normalize(null));
        }

        @Test
        @DisplayName("should throw exception for blank input")
        void shouldThrowExceptionForBlankInput() {
            assertThrows(IllegalArgumentException.class, () -> IpValidator.normalize("  "));
        }
    }

    @Nested
    @DisplayName("general validation")
    class GeneralValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "invalid", "not-an-ip"})
        @DisplayName("should reject invalid inputs")
        void shouldRejectInvalidInputs(String ip) {
            assertFalse(IpValidator.isValid(ip));
        }

        @Test
        @DisplayName("should handle whitespace in valid IPs")
        void shouldHandleWhitespaceInValidIps() {
            assertTrue(IpValidator.isValid("  8.8.8.8  "));
            assertTrue(IpValidator.isValid("  2001:db8::1  "));
        }
    }
}
