package com.example.geolocation.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.geolocation.infrastructure.adapter.in.web.dto.LoginRequest;
import com.example.geolocation.infrastructure.adapter.in.web.dto.LoginResponse;
import com.example.geolocation.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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
