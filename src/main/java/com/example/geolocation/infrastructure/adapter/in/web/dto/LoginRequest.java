package com.example.geolocation.infrastructure.adapter.in.web.dto;

import com.example.geolocation.application.domain.exception.ErrorCode;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = ErrorCode.Validation.USERNAME_REQUIRED) String username,

        @NotBlank(message = ErrorCode.Validation.PASSWORD_REQUIRED) String password) {
}
