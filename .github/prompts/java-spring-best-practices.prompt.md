---
agent: agent
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
- Prefira `@RequiredArgsConstructor` com `@NonNull` do Lombok para injeção
- Configure beans com `@Configuration` + `@Bean` ao invés de `@Component` para dependências externas
- Use `@ConfigurationProperties` para configurações tipadas (prefer Records)
- Use `@Validated` em controllers para ativar Bean Validation
- Use `@NotBlank` para parâmetros obrigatórios de String
- Use `@Schema` em DTOs para documentação Swagger/OpenAPI

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
- `@NonNull` - validação automática de null em parâmetros de construtor
- `@Getter` - getters em exceções e enums com campos
- `@Slf4j` - logging
- `@UtilityClass` - classes com métodos estáticos (substitui construtor privado)
- `@Builder` - construção de objetos complexos
- Evite `@Data` em entidades JPA (problemas com equals/hashCode)
- Prefira Records ao invés de `@Value` para DTOs imutáveis

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

## Princípio: Corrigir na Origem

**NUNCA use `@SuppressWarnings` para mascarar problemas.** Corrija a causa raiz.

### Soluções para Warnings Comuns

```java
// ❌ ERRADO: Suprimir null safety
@SuppressWarnings("null")
void test() {
    mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON));
}

// ✅ CORRETO: Objects.requireNonNull()
void test() {
    mockMvc.perform(post(URL)
        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON)));
}

// ❌ ERRADO: Thread.sleep em testes
Thread.sleep(100);

// ✅ CORRETO: Awaitility
await().atMost(Duration.ofMillis(200)).until(() -> condition);

// ❌ ERRADO: Regex complexo com suppress
@SuppressWarnings("java:S5843")
Pattern IPV6_PATTERN = Pattern.compile("...");

// ✅ CORRETO: Usar InetAddress
InetAddress addr = InetAddress.getByName(ip);
return addr instanceof Inet6Address;
```

### @SuppressWarnings Aceitável

- `java:S2187` - Falso positivo do SonarQube com JUnit 5 `@Nested`
