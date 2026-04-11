---
mode: agent
description: "Use when: writing Java code, Spring Boot configuration, creating services, controllers or any Java class. Applies best practices for Java 21 and Spring Boot 3.x"
---

# Java & Spring Boot Best Practices

## Java 21+ Features
- Use **Records** para DTOs e Value Objects (imutáveis, compactos)
- Use **Pattern Matching** com `instanceof` e `switch` expressions
- Use **Sealed Classes** para hierarquias de tipos fechadas
- Prefira **Virtual Threads** para operações I/O-bound quando apropriado
- Use **Text Blocks** (`"""`) para strings multiline (JSON, SQL)

## Spring Boot 3.x Conventions
- Use **constructor injection** (não @Autowired em fields)
- Prefira `@RequiredArgsConstructor` do Lombok para injeção
- Configure beans com `@Configuration` + `@Bean` ao invés de `@Component` para dependências externas
- Use `@ConfigurationProperties` para configurações tipadas
- Valide inputs com `@Valid` e Bean Validation annotations

## Naming Conventions
```java
// Classes: PascalCase
public class GeolocationService {}

// Interfaces: PascalCase, sem prefixo "I"
public interface GeolocationProvider {}

// Methods/Variables: camelCase
public GeolocationResponse locate(String ipAddress) {}

// Constants: UPPER_SNAKE_CASE
private static final Pattern IP_V4_PATTERN = Pattern.compile("...");

// Packages: lowercase, singular
com.example.geolocation.application.service
```

## Exception Handling
- Crie exceções de domínio específicas (ex: `InvalidIpAddressException`)
- Use `@RestControllerAdvice` para tratamento global
- Nunca exponha stack traces para o cliente
- Log exceções com contexto: IP, timestamp, correlation ID

## Lombok Usage
- `@RequiredArgsConstructor` - injeção de dependências
- `@Builder` - construção de objetos complexos
- `@Slf4j` - logging
- `@Value` - objetos imutáveis (ou use Records)
- Evite `@Data` em entidades JPA (problemas com equals/hashCode)

## HTTP Client Best Practices
- Configure timeouts explícitos (connect, read)
- Use circuit breaker pattern (Resilience4j)
- Implemente retry com exponential backoff
- Log requests/responses em DEBUG level

## Performance
- Compile Regex patterns como `static final`
- Use cache para dados que mudam raramente
- Configure pool de conexões adequadamente
- Use `@Async` para operações que podem ser paralelas
