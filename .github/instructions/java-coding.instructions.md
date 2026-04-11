---
applyTo: "**/*.java"
description: "Java coding standards and conventions for ip-geolocation-service"
---

# Java Coding Instructions

## Code Style

### Imports
- Nunca use wildcard imports (`import java.util.*`)
- Organize: java.*, javax.*, org.*, com.*, static imports
- Remova imports não utilizados

### Formatting
- Indentação: 4 espaços (não tabs)
- Limite de linha: 120 caracteres
- Chaves na mesma linha: `if (condition) {`
- Uma linha em branco entre métodos

### Null Safety
```java
// ✅ Use Optional para retornos que podem ser nulos
public Optional<GeolocationInfo> findByIp(String ip) {}

// ✅ Use Objects.requireNonNull para parâmetros obrigatórios
public GeolocationService(GeolocationProvider provider) {
    this.provider = Objects.requireNonNull(provider, "provider cannot be null");
}

// ❌ Nunca retorne null de método público
public GeolocationInfo locate(String ip) {
    return null; // ERRADO!
}
```

### Immutability
```java
// ✅ Prefira Records para DTOs
public record GeolocationRequest(String ip) {}

// ✅ Use List.of(), Set.of(), Map.of() para coleções imutáveis
private static final List<String> VALID_PLATFORMS = List.of("iOS", "Android", "Web");

// ✅ Marque campos como final quando possível
private final GeolocationCache cache;
```

### Logging
```java
@Slf4j
public class GeolocationService {
    
    public GeolocationInfo locate(String ip) {
        log.debug("Looking up geolocation for IP: {}", ip);
        
        try {
            var result = provider.lookup(ip);
            log.info("Geolocation found for IP {} from {}", ip, result.source());
            return result;
        } catch (ExternalApiException e) {
            log.error("External API failed for IP {}: {}", ip, e.getMessage());
            return createFallback(ip);
        }
    }
}
```

### Exception Handling
```java
// ✅ Crie exceções específicas do domínio
public class InvalidIpAddressException extends RuntimeException {
    private final String ip;
    
    public InvalidIpAddressException(String ip) {
        super("Invalid IP address format: " + ip);
        this.ip = ip;
    }
    
    public String getIp() { return ip; }
}

// ✅ Use try-with-resources para recursos
try (var client = HttpClient.newHttpClient()) {
    // ...
}
```

### Regex
```java
// ✅ Compile patterns como constantes estáticas
private static final Pattern IP_V4_PATTERN = Pattern.compile(
    "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
);

// ❌ Não compile dentro de métodos
public boolean isValidIp(String ip) {
    return Pattern.matches("...", ip); // Cria novo Pattern a cada chamada
}
```

## Spring Specifics

### Dependency Injection
```java
// ✅ Constructor injection com Lombok
@Service
@RequiredArgsConstructor
public class GeolocationService {
    private final GeolocationProvider provider;
    private final GeolocationCache cache;
}

// ❌ Field injection
@Autowired
private GeolocationProvider provider; // EVITE
```

### Configuration
```java
// ✅ Use @ConfigurationProperties
@ConfigurationProperties(prefix = "geolocation")
public record GeolocationProperties(
    String apiUrl,
    Duration timeout,
    Duration cacheTtl,
    FallbackProperties fallback
) {
    public record FallbackProperties(String countryCode, String countryName) {}
}
```

### Validation
```java
// ✅ Use Bean Validation
@GetMapping("/locate")
public ResponseEntity<GeolocationResponse> locate(
    @RequestParam @NotBlank @ValidIp String ip,
    @RequestHeader("x-device-platform") @Pattern(regexp = "iOS|Android|Web") String platform
) {}
```
