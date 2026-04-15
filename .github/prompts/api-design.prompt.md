---
agent: agent
description: "Use when: designing REST endpoints, creating controllers, defining request/response DTOs, handling HTTP errors"
---

# REST API Design Best Practices

## Endpoint Design

### URL Patterns

```
GET  /api/geolocation/v1/locate?ip={ip}     # Query com parâmetros
GET  /api/geolocation/v1/ips/{ip}           # Path parameter (alternativa)
```

### Versioning

- Use versão na URL: `/api/v1/...`
- Mantenha backward compatibility
- Documente breaking changes

## Request Validation

```java
@RestController
@RequestMapping("/api/geolocation/v1")
@RequiredArgsConstructor
public class GeolocationController {

    @GetMapping("/locate")
    public ResponseEntity<GeolocationResponse> locate(
            @RequestParam @ValidIp String ip,
            @RequestHeader("x-device-platform") @ValidPlatform String platform) {
        // ...
    }
}
```

### Custom Validators

```java
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = IpAddressValidator.class)
public @interface ValidIp {
    String message() default "Invalid IP address format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

## Response DTOs

### Success Response (200)

```java
public record GeolocationResponse(
    String ip,
    CountryDto country,
    RegionDto region,
    String city,
    CoordinatesDto coordinates,
    String timezone,
    String isp,
    String source,         // "cache" | "api" | "fallback"
    Instant timestamp
) {}

public record CountryDto(String code, String name) {}
public record RegionDto(String code, String name) {}
public record CoordinatesDto(double latitude, double longitude) {}
```

### Error Response (4xx/5xx)

```java
public record ErrorResponse(
    String error,          // Código: INVALID_IP_FORMAT, MISSING_HEADER
    String message,        // Mensagem legível
    Instant timestamp,
    String path,           // (opcional) endpoint chamado
    String traceId         // (opcional) para debugging
) {}
```

## HTTP Status Codes

| Cenário            | Status | Response                       |
| ------------------ | ------ | ------------------------------ |
| Sucesso            | 200    | GeolocationResponse            |
| IP inválido        | 400    | ErrorResponse                  |
| Header ausente     | 400    | ErrorResponse                  |
| API externa falhou | 200    | GeolocationResponse (fallback) |
| Erro interno       | 500    | ErrorResponse                  |

## Exception Handling

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidIpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidIp(
            InvalidIpException ex, HttpServletRequest request) {
        log.warn("Invalid IP: {}", ex.getIp());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                "INVALID_IP_FORMAT",
                ex.getMessage(),
                Instant.now(),
                request.getRequestURI(),
                null
            ));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(
            MissingRequestHeaderException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                "MISSING_HEADER",
                "Required header '" + ex.getHeaderName() + "' is missing",
                Instant.now(),
                null,
                null
            ));
    }
}
```

## Headers

### Request Headers Obrigatórios

| Header            | Valores Válidos   | Descrição             |
| ----------------- | ----------------- | --------------------- |
| x-device-platform | iOS, Android, Web | Plataforma do cliente |

### Response Headers

```java
@GetMapping("/locate")
public ResponseEntity<GeolocationResponse> locate(...) {
    return ResponseEntity.ok()
        .header("X-Response-Source", response.source())
        .header("X-Cache-TTL", "86400")
        .body(response);
}
```

## Documentation (OpenAPI/Swagger)

### Controller-Level Annotations

```java
@Tag(name = "Geolocation", description = "API de geolocalização por IP")
@SecurityRequirement(name = "bearerAuth")  // Aplica segurança a todos endpoints
public class GeolocationController {
    // ...
}
```

### Endpoint Documentation

```java
@Operation(
    summary = "Locate IP geolocation",
    description = "Returns geographic information for the given IP address. "
            + "If IP is private/localhost or external API fails, returns fallback with Brazil data."
)
@ApiResponse(responseCode = "200", description = "Geolocation found",
        content = @Content(schema = @Schema(implementation = GeolocationResponse.class)))
@ApiResponse(responseCode = "400", description = "Invalid IP, missing header or invalid platform",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "401", description = "JWT token missing or invalid")
@ApiResponse(responseCode = "403", description = "Access denied")
@GetMapping("/locate")
public ResponseEntity<GeolocationResponse> locate(
    @Parameter(description = "IPv4 or IPv6 address", example = "8.8.8.8", required = true)
    @RequestParam String ip,

    @Parameter(description = "Client platform", example = "Web", required = true)
    @RequestHeader("x-device-platform") String platform) {
    // ...
}
```

### Auth Endpoint (sem @SecurityRequirement)

```java
@Tag(name = "Authentication", description = "API de autenticação JWT")
// NÃO usar @SecurityRequirement aqui - endpoint público
public class AuthController {

    @Operation(summary = "Autenticar usuário",
            description = "Autentica o usuário com username/password e retorna um token JWT")
    @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // ...
    }
}
```

### OpenAPI Config

```java
@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtido via POST /auth/login"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IP Geolocation Service API")
                        .version("1.0.0")
                        .description("""
                                Descrição detalhada da API.

                                ## Autenticação
                                1. POST /auth/login com username/password
                                2. Copie o token JWT
                                3. Clique em Authorize e cole o token
                                """)
                        .contact(new Contact().name("Dev").email("dev@example.com"))
                        .license(new License().name("MIT")));
    }
}
```

## Content Negotiation

```java
@GetMapping(value = "/locate", produces = MediaType.APPLICATION_JSON_VALUE)
```

## Rate Limiting (Diferencial)

```java
@RateLimiter(name = "geolocationApi", fallbackMethod = "rateLimitFallback")
@GetMapping("/locate")
public ResponseEntity<GeolocationResponse> locate(...) {}
```
