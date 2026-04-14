package com.example.geolocation.infrastructure.adapter.in.web.dto;

import com.example.geolocation.application.domain.constants.ErrorMessages;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = ErrorMessages.USERNAME_REQUIRED)
        String username,
        
        @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
        String password
) {}
