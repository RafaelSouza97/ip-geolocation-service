---
mode: agent
description: "Use when: organizing code structure, creating new classes, defining layers, implementing use cases. Follows Clean Architecture principles for the ip-geolocation-service"
---

# Clean Architecture Guidelines

## Layer Structure

```
src/main/java/com/example/geolocation/
├── application/           # Core business logic (innermost layer)
│   ├── domain/            # Domain models, value objects
│   │   ├── model/         # Entities e Aggregates
│   │   └── exception/     # Domain exceptions
│   ├── port/              # Interfaces (ports)
│   │   ├── in/            # Input ports (use cases)
│   │   └── out/           # Output ports (repositories, external services)
│   └── service/           # Use case implementations
│
└── infrastructure/        # External concerns (outermost layer)
    ├── adapter/
    │   ├── in/
    │   │   └── web/       # Controllers, DTOs de request/response
    │   └── out/
    │       ├── cache/     # Cache implementations
    │       └── client/    # HTTP clients para APIs externas
    ├── config/            # Spring configurations
    └── validation/        # Validators customizados
```

## Dependency Rules

### ✅ PERMITIDO
- `infrastructure` → `application` (depende de)
- `adapter.in` → `port.in` (implementa/usa)
- `adapter.out` → `port.out` (implementa)
- `service` → `port.out` (usa interfaces)

### ❌ PROIBIDO
- `application` → `infrastructure` (NUNCA!)
- `domain` → qualquer coisa externa
- Annotations do Spring em `domain/model`

## Naming Conventions por Camada

### Application Layer
```java
// Ports (interfaces)
public interface GeolocationUseCase {}        // Input port
public interface GeolocationProvider {}        // Output port
public interface GeolocationCache {}           // Output port

// Domain Models
public record GeolocationInfo(...) {}          // Value Object
public class Country {}                        // Entity

// Exceptions
public class InvalidIpException extends RuntimeException {}

// Services (use case implementations)
public class GeolocationService implements GeolocationUseCase {}
```

### Infrastructure Layer
```java
// Controllers
@RestController
public class GeolocationController {}

// DTOs
public record GeolocationRequest(...) {}
public record GeolocationResponse(...) {}

// Adapters
public class IpApiClient implements GeolocationProvider {}
public class CaffeineGeolocationCache implements GeolocationCache {}

// Config
@Configuration
public class CacheConfig {}
```

## Mapping Between Layers

- Controllers recebem DTOs de Request
- Controllers chamam Use Cases com Domain Objects
- Use Cases retornam Domain Objects
- Controllers convertem para DTOs de Response
- Use `MapStruct` ou métodos `toXxx()` para conversões

## Testing by Layer

| Layer | Test Type | Dependencies |
|-------|-----------|--------------|
| domain | Unit | Nenhuma |
| service | Unit | Mocks de ports |
| adapter.out | Integration | WireMock, TestContainers |
| adapter.in | Integration | MockMvc, WebTestClient |

## Package Visibility

- Classes de `application` devem ser `public` (usadas por infrastructure)
- Classes internas de `infrastructure` podem ser `package-private`
- DTOs de API são `public`
