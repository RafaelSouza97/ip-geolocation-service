package com.example.geolocation.infrastructure.adapter.in.web.dto;

import com.example.geolocation.application.domain.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição de autenticação")
public record LoginRequest(
                @Schema(description = "Nome de usuário", example = "admin") @NotBlank(
                                message = ErrorCode.Validation.USERNAME_REQUIRED) String username,

                @Schema(description = "Senha do usuário", example = "Admin123@") @NotBlank(
                                message = ErrorCode.Validation.PASSWORD_REQUIRED) String password) {
}

