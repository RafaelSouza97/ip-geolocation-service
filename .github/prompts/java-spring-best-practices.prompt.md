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

## Swagger/OpenAPI Best Practices

### Configuração Global

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
                        .title("API Name")
                        .version("1.0.0")
                        .description("Descrição detalhada da API")
                        .contact(new Contact().name("Dev").email("dev@example.com"))
                        .license(new License().name("MIT")));
    }
}
```

### Annotations em Controllers

```java
// Agrupamento com @Tag no nível da classe
@Tag(name = "Geolocation", description = "API de geolocalização por IP")
@SecurityRequirement(name = "bearerAuth")  // Requer autenticação
public class GeolocationController {

    // Documentação do endpoint
    @Operation(
        summary = "Título curto do endpoint",
        description = "Descrição detalhada do que o endpoint faz"
    )
    // Respostas possíveis com schema
    @ApiResponse(responseCode = "200", description = "Sucesso",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @GetMapping("/endpoint")
    public ResponseEntity<SuccessResponse> method(
            @Parameter(description = "Descrição do parâmetro", example = "valor", required = true)
            @RequestParam String param) {
        // ...
    }
}
```

### Documentação de DTOs

```java
@Schema(description = "Resposta de geolocalização")
public record GeolocationResponse(
    @Schema(description = "Endereço IP consultado", example = "8.8.8.8")
    String ip,

    @Schema(description = "Código do país (ISO 3166-1 alpha-2)", example = "BR")
    String countryCode,

    @Schema(description = "Fonte dos dados", example = "cache",
            allowableValues = {"cache", "api", "fallback"})
    String source
) {}
```

### Documentação de Erros

```java
@Schema(description = "Resposta de erro padronizada")
public record ErrorResponse(
    @Schema(description = "Código do erro", example = "INVALID_IP_FORMAT")
    String error,

    @Schema(description = "Mensagem legível", example = "Invalid IP address format")
    String message,

    @Schema(description = "Momento do erro")
    Instant timestamp
) {}
```

### Boas Práticas

| Annotation             | Onde              | Propósito                                |
| ---------------------- | ----------------- | ---------------------------------------- |
| `@Tag`                 | Classe controller | Agrupa endpoints no Swagger UI           |
| `@SecurityRequirement` | Classe/método     | Indica endpoints protegidos              |
| `@Operation`           | Método            | Documenta endpoint (summary/description) |
| `@ApiResponse`         | Método            | Documenta cada status code possível      |
| `@Parameter`           | Parâmetro         | Documenta parâmetro com exemplo          |
| `@Schema`              | DTO/campo         | Documenta modelos e campos               |

**Regras:**

- Sempre documente a resposta de erro (`ErrorResponse`) em endpoints que podem retornar 4xx
- Use `summary` para título curto e `description` para detalhes
- Forneça `example` em `@Parameter` e `@Schema` para melhorar a documentação
- Para endpoints públicos (sem auth), omita `@SecurityRequirement`
- Use `allowableValues` para enums/valores fixos

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
