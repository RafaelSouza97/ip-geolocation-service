package com.example.geolocation.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.geolocation.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.example.geolocation.infrastructure.adapter.in.web.dto.LoginRequest;
import com.example.geolocation.infrastructure.adapter.in.web.dto.LoginResponse;
import com.example.geolocation.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API de autenticação JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Operation(summary = "Autenticar usuário",
            description = "Autentica o usuário com username/password e retorna um token JWT")
    @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login attempt for user: {}", request.username());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.username(), request.password()));

            String token = jwtService.generateToken(request.username());
            log.info("User {} logged in successfully", request.username());
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", request.username());
            return ResponseEntity.status(401).build();
        }
    }
}
