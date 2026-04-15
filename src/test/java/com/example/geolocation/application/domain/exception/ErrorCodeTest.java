package com.example.geolocation.application.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("ErrorCode")
@SuppressWarnings("java:S2187") // False positive: @Nested classes contain the tests
class ErrorCodeTest {

    @Nested
    @DisplayName("getCode")
    class GetCode {

        @ParameterizedTest
        @EnumSource(ErrorCode.class)
        @DisplayName("should return non-empty code matching enum name")
        void shouldReturnNonEmptyCodeMatchingEnumName(ErrorCode errorCode) {
            String code = errorCode.getCode();
            assertNotNull(code);
            assertFalse(code.isEmpty());
            assertEquals(errorCode.name(), code);
        }
    }

    @Nested
    @DisplayName("getMessage")
    class GetMessage {

        @ParameterizedTest
        @EnumSource(ErrorCode.class)
        @DisplayName("should return non-empty message template")
        void shouldReturnNonEmptyMessageTemplate(ErrorCode errorCode) {
            String message = errorCode.getMessage();
            assertNotNull(message);
            assertFalse(message.isEmpty());
        }
    }

    @Nested
    @DisplayName("format")
    class Format {

        @Test
        @DisplayName("should format message with arguments")
        void shouldFormatMessageWithArguments() {
            String formatted = ErrorCode.INVALID_IP_FORMAT.format("8.8.8.8");
            assertEquals("Invalid IP address format: 8.8.8.8", formatted);
        }

        @Test
        @DisplayName("should return unformatted message when no arguments provided")
        void shouldReturnUnformattedMessageWhenNoArgumentsProvided() {
            String formatted = ErrorCode.VALIDATION_ERROR.format();
            assertEquals("Validation error", formatted);
        }

        @Test
        @DisplayName("should return unformatted message with empty array")
        void shouldReturnUnformattedMessageWithEmptyArray() {
            String formatted = ErrorCode.INTERNAL_ERROR.format(new Object[0]);
            assertEquals("An unexpected error occurred", formatted);
        }

        @Test
        @DisplayName("should format message with multiple arguments")
        void shouldFormatMessageWithMultipleArguments() {
            String formatted = ErrorCode.INVALID_PLATFORM.format("Linux", "iOS, Android, Web");
            assertEquals("Invalid platform: Linux. Valid values: iOS, Android, Web", formatted);
        }

        @Test
        @DisplayName("should format HTTP_ERROR with numeric argument")
        void shouldFormatHttpErrorWithNumericArgument() {
            String formatted = ErrorCode.HTTP_ERROR.format(404);
            assertEquals("HTTP 404", formatted);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringMethod {

        @ParameterizedTest
        @EnumSource(ErrorCode.class)
        @DisplayName("should return non-empty string matching message template")
        void shouldReturnNonEmptyStringMatchingMessageTemplate(ErrorCode errorCode) {
            String result = errorCode.toString();
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(errorCode.getMessage(), result);
        }

        @Test
        @DisplayName("should return message template for specific error codes")
        void shouldReturnMessageTemplateForSpecificErrorCodes() {
            assertEquals("Validation error", ErrorCode.VALIDATION_ERROR.toString());
            assertEquals("An unexpected error occurred", ErrorCode.INTERNAL_ERROR.toString());
        }
    }

    @Nested
    @DisplayName("Validation constants")
    class ValidationConstants {

        @Test
        @DisplayName("USERNAME_REQUIRED should be non-empty string")
        void usernameRequiredShouldBeNonEmptyString() {
            assertNotNull(ErrorCode.Validation.USERNAME_REQUIRED);
            assertTrue(!ErrorCode.Validation.USERNAME_REQUIRED.isEmpty());
        }

        @Test
        @DisplayName("PASSWORD_REQUIRED should be non-empty string")
        void passwordRequiredShouldBeNonEmptyString() {
            assertNotNull(ErrorCode.Validation.PASSWORD_REQUIRED);
            assertTrue(!ErrorCode.Validation.PASSWORD_REQUIRED.isEmpty());
        }
    }
}
