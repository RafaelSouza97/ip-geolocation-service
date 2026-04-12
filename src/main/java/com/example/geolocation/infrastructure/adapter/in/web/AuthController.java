package com.example.geolocation.infrastructure.adapter.in.web;

import com.example.geolocation.infrastructure.adapter.in.web.dto.LoginRequest;
import com.example.geolocation.infrastructure.adapter.in.web.dto.LoginResponse;
import com.example.geolocation.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            
            String token = jwtService.generateToken(request.username());
            return ResponseEntity.ok(new LoginResponse(token));
            
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
