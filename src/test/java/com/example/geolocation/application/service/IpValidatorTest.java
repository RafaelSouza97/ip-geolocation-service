package com.example.geolocation.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("IpValidator")
class IpValidatorTest {

    @Nested
    @DisplayName("IPv4 validation")
    class Ipv4Validation {

        @ParameterizedTest
        @ValueSource(strings = {"8.8.8.8", "1.1.1.1", "192.168.1.1", "255.255.255.255", "0.0.0.0",
                "177.45.123.45", "10.0.0.1"})
        @DisplayName("should accept valid IPv4 addresses")
        void shouldAcceptValidIpv4Addresses(String ip) {
            assertTrue(IpValidator.isValidIpv4(ip));
            assertTrue(IpValidator.isValid(ip));
        }

        @ParameterizedTest
        @ValueSource(strings = {"256.1.1.1", "1.256.1.1", "1.1.256.1", "1.1.1.256",
                "999.999.999.999", "1.1.1", "1.1.1.1.1", "abc.def.ghi.jkl", "1.1.1.a", "1.1.1.-1"})
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
        @ValueSource(strings = {"2001:4860:4860::8888", "2001:0db8:85a3:0000:0000:8a2e:0370:7334",
                "::1", "::", "fe80::1", "2001:db8::1", "2001:db8:85a3::8a2e:370:7334"})
        @DisplayName("should accept valid IPv6 addresses")
        void shouldAcceptValidIpv6Addresses(String ip) {
            assertTrue(IpValidator.isValidIpv6(ip));
            assertTrue(IpValidator.isValid(ip));
        }

        @ParameterizedTest
        @ValueSource(strings = {"2001:4860:4860::8888::1", ":::1",
                "2001:db8:85a3:0000:0000:8a2e:0370:7334:extra", "gggg::", "12345::"})
        @DisplayName("should reject invalid IPv6 addresses")
        void shouldRejectInvalidIpv6Addresses(String ip) {
            assertFalse(IpValidator.isValidIpv6(ip));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("should reject null, empty and blank IPv6 inputs")
        void shouldRejectNullEmptyBlankIpv6(String ip) {
            assertFalse(IpValidator.isValidIpv6(ip));
        }

        @ParameterizedTest
        @ValueSource(strings = {"8.8.8.8", "192.168.1.1", "10.0.0.1"})
        @DisplayName("should reject IPv4 addresses when checking for IPv6")
        void shouldRejectIpv4WhenCheckingIpv6(String ip) {
            assertFalse(IpValidator.isValidIpv6(ip));
        }
    }

    @Nested
    @DisplayName("private/reserved IP detection")
    class PrivateIpDetection {

        @ParameterizedTest
        @ValueSource(strings = {"10.0.0.1", "10.255.255.255", "172.16.0.1", "172.31.255.255",
                "192.168.0.1", "192.168.255.255", "127.0.0.1", "127.255.255.255", "169.254.1.1",
                "0.0.0.0"})
        @DisplayName("should detect private IPv4 addresses")
        void shouldDetectPrivateIpv4Addresses(String ip) {
            assertTrue(IpValidator.isPrivateOrReserved(ip),
                    "IPv4 " + ip + " should be private/reserved");
            assertFalse(IpValidator.isPublic(ip), "IPv4 " + ip + " should not be public");
        }

        @ParameterizedTest
        @ValueSource(strings = {"::1", "fe80::1", "fc00::1", "fd00::1"})
        @DisplayName("should detect private/localhost IPv6 addresses")
        void shouldDetectPrivateIpv6Addresses(String ip) {
            assertTrue(IpValidator.isPrivateOrReserved(ip),
                    "IPv6 " + ip + " should be private/reserved");
            assertFalse(IpValidator.isPublic(ip), "IPv6 " + ip + " should not be public");
        }

        @ParameterizedTest
        @ValueSource(strings = {"8.8.8.8", "1.1.1.1", "177.45.123.45", "2001:4860:4860::8888"})
        @DisplayName("should detect public IP addresses")
        void shouldDetectPublicIpAddresses(String ip) {
            assertTrue(IpValidator.isPublic(ip), ip + " should be public");
            assertFalse(IpValidator.isPrivateOrReserved(ip),
                    ip + " should not be private/reserved");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "invalid", "not-an-ip"})
        @DisplayName("should return false for null, empty, blank and invalid IPs")
        void shouldReturnFalseForInvalidInputs(String ip) {
            assertFalse(IpValidator.isPrivateOrReserved(ip));
            assertFalse(IpValidator.isPublic(ip));
        }
    }

    @Nested
    @DisplayName("localhost detection")
    class LocalhostDetection {

        @ParameterizedTest
        @ValueSource(strings = {"127.0.0.1", "127.0.0.2", "127.255.255.255", "::1"})
        @DisplayName("should detect localhost addresses")
        void shouldDetectLocalhostAddresses(String ip) {
            assertTrue(IpValidator.isLocalhost(ip));
        }

        @ParameterizedTest
        @ValueSource(strings = {"8.8.8.8", "192.168.1.1", "2001:4860:4860::8888"})
        @DisplayName("should not detect non-localhost addresses")
        void shouldNotDetectNonLocalhostAddresses(String ip) {
            assertFalse(IpValidator.isLocalhost(ip));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("should return false for null, empty and blank inputs")
        void shouldReturnFalseForNullEmptyBlank(String ip) {
            assertFalse(IpValidator.isLocalhost(ip));
        }

        @Test
        @DisplayName("should detect 0:0:0:0:0:0:0:1 as localhost")
        void shouldDetectFullFormIpv6Localhost() {
            assertTrue(IpValidator.isLocalhost("0:0:0:0:0:0:0:1"));
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
        @DisplayName("should not modify IPv4 addresses")
        void shouldNotModifyIpv4Addresses() {
            assertEquals("192.168.1.1", IpValidator.normalize("192.168.1.1"));
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

        @Test
        @DisplayName("should throw exception for empty input")
        void shouldThrowExceptionForEmptyInput() {
            assertThrows(IllegalArgumentException.class, () -> IpValidator.normalize(""));
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
