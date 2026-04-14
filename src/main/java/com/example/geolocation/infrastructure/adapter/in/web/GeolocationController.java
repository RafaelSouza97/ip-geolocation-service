package com.example.geolocation.infrastructure.adapter.in.web;

import com.example.geolocation.application.domain.exception.InvalidPlatformException;
import com.example.geolocation.application.domain.exception.MissingPlatformHeaderException;
import com.example.geolocation.application.port.in.GeolocationUseCase;
import com.example.geolocation.infrastructure.adapter.in.web.dto.GeolocationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Controller REST para geolocalização de IPs.
 */
@Slf4j
@RestController
@RequestMapping("/api/geolocation/v1")
@RequiredArgsConstructor
@Tag(name = "Geolocation", description = "API de geolocalização por IP")
@SecurityRequirement(name = "bearerAuth")
public class GeolocationController {

    private static final Set<String> VALID_PLATFORMS = Set.of("iOS", "Android", "Web");
    
    private final GeolocationUseCase geolocationUseCase;

    @Operation(
        summary = "Localiza informações geográficas de um IP",
        description = "Retorna informações de geolocalização (país, região, cidade, coordenadas) para o IP informado"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Geolocalização encontrada com sucesso",
            content = @Content(schema = @Schema(implementation = GeolocationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "IP inválido, header ausente ou plataforma inválida"
        )
    })
    @GetMapping("/locate")
    public ResponseEntity<GeolocationResponse> locate(
            @Parameter(description = "Endereço IP (IPv4 ou IPv6)", example = "8.8.8.8", required = true)
            @RequestParam String ip,
            
            @Parameter(description = "Plataforma do dispositivo", example = "Web", required = true)
            @RequestHeader(value = "x-device-platform", required = false) String platform
    ) {
        log.debug("Received locate request for IP: {} from platform: {}", ip, platform);
        
        // Validar header de plataforma
        validatePlatform(platform);
        
        // Executar caso de uso
        var result = geolocationUseCase.locate(ip);
        
        log.info("Geolocation for IP {} returned from {}", ip, result.source());
        
        return ResponseEntity.ok(GeolocationResponse.fromDomain(result));
    }

    private void validatePlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            throw new MissingPlatformHeaderException();
        }
        
        if (!VALID_PLATFORMS.contains(platform)) {
            throw new InvalidPlatformException(platform);
        }
    }
}
