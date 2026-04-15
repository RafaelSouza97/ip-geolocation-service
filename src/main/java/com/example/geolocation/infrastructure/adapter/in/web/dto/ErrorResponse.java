package com.example.geolocation.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Resposta de erro da API")
public record ErrorResponse(
    @Schema(description = "Código do erro", example = "INVALID_IP_FORMAT")
    String code,

    @Schema(description = "Mensagem de erro", example = "Invalid IP address format")
    String message,

    @Schema(description = "Timestamp do erro")
    Instant timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, Instant.now());
    }
}

