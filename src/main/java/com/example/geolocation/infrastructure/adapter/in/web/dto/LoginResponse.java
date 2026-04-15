package com.example.geolocation.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação com token JWT")
public record LoginResponse(@Schema(description = "Token JWT para autenticação",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String token) {
}
