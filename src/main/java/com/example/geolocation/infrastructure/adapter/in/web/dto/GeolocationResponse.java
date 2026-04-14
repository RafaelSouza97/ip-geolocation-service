package com.example.geolocation.infrastructure.adapter.in.web.dto;

import com.example.geolocation.application.domain.model.GeolocationInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO de resposta para informações de geolocalização.
 */
@Schema(description = "Informações de geolocalização de um IP")
public record GeolocationResponse(
    @Schema(description = "Endereço IP consultado", example = "8.8.8.8")
    String ip,
    
    @Schema(description = "Informações do país")
    CountryDto country,
    
    @Schema(description = "Informações da região/estado")
    RegionDto region,
    
    @Schema(description = "Nome da cidade", example = "São Paulo")
    String city,
    
    @Schema(description = "Coordenadas geográficas")
    CoordinatesDto coordinates,
    
    @Schema(description = "Fuso horário", example = "America/Sao_Paulo")
    String timezone,
    
    @Schema(description = "Provedor de internet", example = "Google LLC")
    String isp,
    
    @Schema(description = "Origem dos dados: api, cache ou fallback", example = "api")
    String source,
    
    @Schema(description = "Timestamp da consulta")
    Instant timestamp
) {
    @Schema(description = "Informações do país")
    public record CountryDto(
        @Schema(description = "Código ISO 3166-1 alpha-2", example = "BR")
        String code,
        
        @Schema(description = "Nome do país", example = "Brazil")
        String name
    ) {}
    
    @Schema(description = "Informações da região")
    public record RegionDto(
        @Schema(description = "Código da região", example = "SP")
        String code,
        
        @Schema(description = "Nome da região", example = "São Paulo")
        String name
    ) {}
    
    @Schema(description = "Coordenadas geográficas")
    public record CoordinatesDto(
        @Schema(description = "Latitude em graus decimais", example = "-23.5505")
        double latitude,
        
        @Schema(description = "Longitude em graus decimais", example = "-46.6333")
        double longitude
    ) {}

    /**
     * Converte um modelo de domínio para DTO de resposta.
     */
    public static GeolocationResponse fromDomain(GeolocationInfo info) {
        return new GeolocationResponse(
            info.ip(),
            new CountryDto(info.country().code(), info.country().name()),
            new RegionDto(info.region().code(), info.region().name()),
            info.city(),
            new CoordinatesDto(info.coordinates().latitude(), info.coordinates().longitude()),
            info.timezone(),
            info.isp(),
            info.sourceValue(),
            info.timestamp()
        );
    }
}
