# Estratégias de Implementação

## Visão Geral das Estratégias

Este documento detalha as estratégias técnicas adotadas para implementar o ip-geolocation-service, incluindo justificativas e trade-offs.

**Princípios norteadores:** SOLID, KISS e DRY (ver [Seção 9](#9-princípios-de-design-solid-kiss-dry))

---

## 1. Estratégia de Validação de IP

### Abordagem

Implementação híbrida usando Regex + biblioteca `inet.ipaddr` para validação robusta.

### Regex Patterns

```java
// IPv4: Cada octeto de 0-255
private static final Pattern IPV4_PATTERN = Pattern.compile(
    "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
);

// IPv6: Formato completo e comprimido
private static final Pattern IPV6_PATTERN = Pattern.compile(
    "^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +
    "([0-9a-fA-F]{1,4}:){1,7}:|" +
    // ... outros formatos
    ")$"
);

// IPs Privados (rejeitar)
private static final Pattern PRIVATE_IP_PATTERN = Pattern.compile(
    "^(10\\.|172\\.(1[6-9]|2\\d|3[01])\\.|192\\.168\\.|127\\.)"
);
```

### Categorias de IP

| Categoria           | Exemplos                             | Ação              |
| ------------------- | ------------------------------------ | ----------------- |
| IPv4 público válido | 8.8.8.8, 177.45.123.45               | Consultar API     |
| IPv6 público válido | 2001:4860:4860::8888                 | Consultar API     |
| IP privado          | 10.x.x.x, 192.168.x.x, 172.16-31.x.x | Retornar fallback |
| Localhost           | 127.0.0.1, ::1                       | Retornar fallback |
| IP inválido         | abc, 999.999.999.999                 | Erro 400          |

### Performance

- Patterns compilados como `static final`
- Validação em O(n) onde n = tamanho do IP
- Nenhuma alocação de objetos durante validação

---

## 2. Estratégia de Cache

### Configuração do Caffeine

```java
@Bean
public Cache<String, GeolocationInfo> geolocationCache(GeolocationProperties props) {
    return Caffeine.newBuilder()
        .expireAfterWrite(props.cache().ttl())  // TTL configurável (default: 24h)
        .maximumSize(10_000)                     // Máximo de 10k entries
        .recordStats()                           // Métricas para monitoramento
        .build();
}
```

### Política de Cache

| Cenário                 | Cachear? | Justificativa                    |
| ----------------------- | -------- | -------------------------------- |
| Resposta sucesso da API | ✅ Sim   | Dados válidos                    |
| Resposta fallback       | ❌ Não   | Evitar cache de dados imprecisos |
| IP privado/localhost    | ❌ Não   | Fallback fixo, não precisa cache |
| Erro de validação       | ❌ Não   | Não há dados para cachear        |

### Cache Key Strategy

```java
// Chave simples: o próprio IP normalizado
String cacheKey = ip.toLowerCase().trim();
```

### Indicação de Source

```java
public record GeolocationInfo(
    // ... outros campos
    String source  // "cache" | "api" | "fallback"
) {}
```

---

## 3. Estratégia de Integração com API Externa

### Escolha: ip-api.com

**Justificativa:**

- ✅ Sem necessidade de API key
- ✅ 45 req/min no plano gratuito (suficiente para demo)
- ✅ Resposta JSON rica
- ⚠️ Apenas HTTP (não HTTPS no plano free)

### Timeout e Retry

```java
HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(2))
    .build();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create(apiUrl + "/" + ip))
    .timeout(Duration.ofSeconds(5))  // Request timeout
    .GET()
    .build();
```

### Tratamento de Erros

| Erro               | Ação              | Log Level |
| ------------------ | ----------------- | --------- |
| Timeout            | Retornar fallback | WARN      |
| Connection refused | Retornar fallback | ERROR     |
| HTTP 4xx           | Retornar fallback | WARN      |
| HTTP 5xx           | Retornar fallback | ERROR     |
| JSON parsing error | Retornar fallback | ERROR     |
| IP não encontrado  | Retornar fallback | INFO      |

### Parsing da Resposta

```java
// Resposta da ip-api.com
{
    "status": "success",          // ou "fail"
    "country": "Brazil",
    "countryCode": "BR",
    "region": "SP",
    "regionName": "São Paulo",
    "city": "São Paulo",
    "lat": -23.5505,
    "lon": -46.6333,
    "timezone": "America/Sao_Paulo",
    "isp": "ISP Name",
    "query": "177.45.123.45"
}

// Mapeamento para domínio
GeolocationInfo.builder()
    .ip(response.query())
    .country(new Country(response.countryCode(), response.country()))
    .region(new Region(response.region(), response.regionName()))
    .city(response.city())
    .coordinates(new Coordinates(response.lat(), response.lon()))
    .timezone(response.timezone())
    .isp(response.isp())
    .source("api")
    .timestamp(Instant.now())
    .build();
```

---

## 4. Estratégia de Tratamento de Erros

### Hierarquia de Exceções

```
RuntimeException
├── GeolocationException (base)
│   ├── InvalidIpAddressException
│   ├── MissingPlatformHeaderException
│   └── InvalidPlatformException
└── ExternalApiException
    ├── ApiTimeoutException
    └── ApiConnectionException
```

### Exception Handler Global

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidIpAddressException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidIp(InvalidIpAddressException ex) {
        return new ErrorResponse(
            "INVALID_IP_FORMAT",
            "Invalid IP address format",
            Instant.now()
        );
    }

    // Outras exceções...
}
```

### Error Codes

| Código                  | HTTP Status | Descrição                        |
| ----------------------- | ----------- | -------------------------------- |
| INVALID_IP_FORMAT       | 400         | IP mal formatado                 |
| PRIVATE_IP_ADDRESS      | 400         | IP privado/reservado             |
| MISSING_PLATFORM_HEADER | 400         | Header x-device-platform ausente |
| INVALID_PLATFORM        | 400         | Platform não é iOS/Android/Web   |
| INTERNAL_ERROR          | 500         | Erro interno inesperado          |

---

## 5. Estratégia de Testes

### Pirâmide de Testes

```
         /\
        /  \
       / E2E\           5%  - Cenários críticos end-to-end
      /------\
     /  INT   \        20%  - Controllers, Clients, Cache
    /----------\
   /    UNIT    \      75%  - Services, Validators, Domain
  /--------------\
```

### Cobertura por Camada

| Camada                     | Cobertura Mínima | Tipo de Teste          |
| -------------------------- | ---------------- | ---------------------- |
| application/service        | 80%              | Unit (Mockito)         |
| application/domain         | 100%             | Unit                   |
| infrastructure/validation  | 100%             | Unit                   |
| infrastructure/adapter/out | 70%              | Integration (WireMock) |
| infrastructure/adapter/in  | 70%              | Integration (MockMvc)  |

### Test Fixtures

```java
public class TestFixtures {
    public static final String VALID_IPV4 = "8.8.8.8";
    public static final String VALID_IPV6 = "2001:4860:4860::8888";
    public static final String PRIVATE_IP = "192.168.1.1";
    public static final String INVALID_IP = "999.999.999.999";
    public static final String BRAZIL_CODE = "BR";

    public static GeolocationInfo createDefaultInfo(String ip) {
        return GeolocationInfo.builder()
            .ip(ip)
            .country(new Country("US", "United States"))
            .source("api")
            .timestamp(Instant.now())
            .build();
    }
}
```

### Estratégia de Correção de Warnings em Testes

**Princípio:** Sempre corrigir na origem em vez de suprimir com `@SuppressWarnings`.

| Warning                      | Problema                                             | Solução                                                          |
| ---------------------------- | ---------------------------------------------------- | ---------------------------------------------------------------- |
| **Null type safety**         | APIs Spring/Jackson sem `@NonNull`                   | `Objects.requireNonNull(MediaType.APPLICATION_JSON)`             |
| **Hamcrest Matcher**         | `notNullValue()`, `containsString()` causam warnings | `jsonPath().exists()` ou AssertJ `assertThat().doesNotContain()` |
| **Thread.sleep (S2925)**     | Testes flaky, código bloqueante                      | Awaitility `await().pollInterval().atMost().until()`             |
| **Regex complexity (S5843)** | Regex IPv6 muito complexo                            | Usar `java.net.InetAddress.getByName()`                          |

**Único @SuppressWarnings aceitável:**

```java
@SuppressWarnings("java:S2187") // Falso positivo: classes @Nested contêm os testes
```

### Mutation Testing (PITest)

**Objetivo:** Avaliar a qualidade dos testes além da cobertura de linhas.

#### Como Funciona

1. **PIT** modifica o código (mutantes): `>=` vira `>`, `true` vira `false`
2. Executa os testes contra cada mutante
3. **Mutante morto** = teste falhou ✅ (teste é eficaz)
4. **Mutante sobreviveu** = teste passou ❌ (teste é fraco)

```java
// Código original
public boolean isAdult(int age) {
    return age >= 18;  // PIT muta para: age > 18
}

// Teste fraco (mutante sobrevive)
@Test void testAdult() {
    assertTrue(isAdult(25));  // Passa com >= ou >
}

// Teste forte (mutante morre)
@Test void testBoundary() {
    assertTrue(isAdult(18));   // Falha se > usado
    assertFalse(isAdult(17));
}
```

#### Configuração

```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <configuration>
        <targetClasses>
            <param>com.example.geolocation.application.*</param>
            <param>com.example.geolocation.infrastructure.validation.*</param>
        </targetClasses>
        <excludedClasses>
            <param>com.example.geolocation.infrastructure.config.*</param>
            <param>com.example.geolocation.infrastructure.adapter.in.web.dto.*</param>
        </excludedClasses>
        <mutators>
            <mutator>STRONGER</mutator>
        </mutators>
        <mutationThreshold>70</mutationThreshold>
    </configuration>
</plugin>
```

#### Execução

```bash
# Executar mutation testing
mvn test pitest:mutationCoverage

# Relatório em: target/pit-reports/index.html
```

#### Thresholds

| Métrica           | Mínimo | Excelente | Descrição                    |
| ----------------- | ------ | --------- | ---------------------------- |
| Mutation Coverage | 70%    | **>80%**  | % de mutantes mortos         |
| Line Coverage     | 80%    | **>90%**  | Cobertura de linhas (JaCoCo) |

> **Objetivo:** Sempre buscar o nível **Excelente** (>80% mutation, >90% coverage)
>
> **Status atual do projeto:** 89% mutation, 99% line ✅

#### Classes Excluídas

- **Configs**: Classes `@Configuration` são wiring, não lógica
- **DTOs**: Records sem comportamento
- **Application.java**: Classe main do Spring Boot

---

## 6. Estratégia de Configuração

### Profiles Spring

| Profile   | Uso             | Configurações            |
| --------- | --------------- | ------------------------ |
| (default) | Produção        | Configs via env vars     |
| local     | Desenvolvimento | Logs DEBUG, config local |
| test      | Testes          | Mocks, timeouts curtos   |

### Configuração Externalizada

```yaml
geolocation:
  api:
    url: ${GEOLOCATION_API_URL:http://ip-api.com/json}
    timeout: ${GEOLOCATION_API_TIMEOUT:5s}
  cache:
    ttl: ${GEOLOCATION_CACHE_TTL:24h}
    max-size: ${GEOLOCATION_CACHE_MAX_SIZE:10000}
  fallback:
    country-code: ${GEOLOCATION_FALLBACK_COUNTRY_CODE:BR}
    country-name: ${GEOLOCATION_FALLBACK_COUNTRY_NAME:Brazil}
```

---

## 7. Estratégia de Deploy (Azure)

### Opções Avaliadas

| Opção                  | Prós                   | Contras         | Escolha         |
| ---------------------- | ---------------------- | --------------- | --------------- |
| Azure Container Apps   | Serverless, auto-scale | Menos controle  | ✅ Recomendado  |
| Azure App Service      | Simples, managed       | Custo fixo      | Alternativa     |
| Azure Kubernetes (AKS) | Flexível               | Complexo demais | Não recomendado |

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Health Checks

```yaml
# Azure Container Apps
resources:
  containers:
    - name: ip-geolocation-service
      probes:
        - type: Liveness
          httpGet:
            path: /actuator/health/liveness
            port: 8080
        - type: Readiness
          httpGet:
            path: /actuator/health/readiness
            port: 8080
```

---

## 8. Estratégia de Logging

### Formato

```java
// Desenvolvimento
2024-01-15 10:30:00 DEBUG [GeolocationService] Looking up IP: 8.8.8.8

// Produção (JSON)
{"timestamp":"2024-01-15T10:30:00Z","level":"INFO","logger":"GeolocationService","message":"Geolocation found","ip":"8.8.8.8","source":"api"}
```

### Níveis por Categoria

| Logger                       | Development | Production |
| ---------------------------- | ----------- | ---------- |
| com.example.geolocation      | DEBUG       | INFO       |
| org.springframework          | INFO        | WARN       |
| com.github.benmanes.caffeine | INFO        | WARN       |

### Informações Logadas

- ✅ IP consultado
- ✅ Source (cache/api/fallback)
- ✅ Tempo de resposta
- ✅ Erros com contexto
- ❌ Headers sensíveis
- ❌ Dados pessoais do usuário

---

## 9. Princípios de Design (SOLID, KISS, DRY)

### SOLID

Os princípios SOLID são aplicados através da Clean Architecture:

| Princípio                 | Aplicação                                            | Exemplo                                                                                           |
| ------------------------- | ---------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| **S**ingle Responsibility | Cada classe tem uma única responsabilidade           | `IpValidator` apenas valida, `GeolocationService` apenas orquestra                                |
| **O**pen/Closed           | Extensível via interfaces (ports) sem modificar core | Trocar `IpApiClient` por outro provider sem alterar `GeolocationService`                          |
| **L**iskov Substitution   | Implementações são intercambiáveis                   | Qualquer `GeolocationProvider` funciona no service                                                |
| **I**nterface Segregation | Interfaces pequenas e focadas                        | `GeolocationCache` (get/put) separada de `GeolocationProvider` (lookup)                           |
| **D**ependency Inversion  | Dependência de abstrações, não implementações        | `GeolocationService` depende de `GeolocationCache` (interface), não de `CaffeineGeolocationCache` |

### KISS (Keep It Simple, Stupid)

**Objetivo:** Manter soluções simples e diretas, evitando complexidade desnecessária.

| Área         | Aplicação                                                   |
| ------------ | ----------------------------------------------------------- |
| Models       | Uso de **Records** ao invés de classes verbosas             |
| Cache        | Caffeine in-memory (sem Redis distribuído para este escopo) |
| HTTP Client  | `java.net.http.HttpClient` nativo (sem Feign/RestTemplate)  |
| Configuração | YAML simples com valores default sensatos                   |
| Validação    | Regex compilado (sem biblioteca externa complexa)           |

```java
// ✅ KISS: Record simples
public record Country(String code, String name) {}

// ❌ Evitar: Over-engineering
public class CountryBuilder {
    private String code;
    private String name;
    // ... 50 linhas de código
}
```

### DRY (Don't Repeat Yourself)

**Objetivo:** Eliminar duplicação de código e conhecimento.

| Técnica                      | Aplicação                                      |
| ---------------------------- | ---------------------------------------------- |
| **Constantes**               | Patterns de regex como `static final`          |
| **Records**                  | Reutilização de estruturas de dados            |
| **Test Fixtures**            | Dados de teste centralizados em `TestFixtures` |
| **Configuration Properties** | Valores centralizados em YAML                  |
| **Global Exception Handler** | Tratamento de erros em um único lugar          |

```java
// ✅ DRY: Fixture reutilizável
public class TestFixtures {
    public static final String VALID_IPV4 = "8.8.8.8";

    public static GeolocationInfo createDefaultInfo(String ip) {
        return new GeolocationInfo(ip, /* ... */);
    }
}

// ❌ Evitar: Duplicação em cada teste
@Test void test1() { String ip = "8.8.8.8"; /* ... */ }
@Test void test2() { String ip = "8.8.8.8"; /* ... */ }
```

### Checklist de Revisão

Antes de cada PR, verificar:

- [ ] **SRP:** Cada classe faz apenas uma coisa?
- [ ] **OCP:** Posso adicionar features sem modificar código existente?
- [ ] **LSP:** Implementações são substituíveis?
- [ ] **ISP:** Interfaces são pequenas e coesas?
- [ ] **DIP:** Dependências são injetadas via interfaces?
- [ ] **KISS:** Existe uma solução mais simples?
- [ ] **DRY:** Há código duplicado que pode ser extraído?

---

## 10. Estratégia de Uso de Lombok e Spring Annotations

### Objetivo

Reduzir código boilerplate mantendo clareza e type-safety.

### Lombok Annotations

| Annotation                 | Uso                                | Benefício                                                    |
| -------------------------- | ---------------------------------- | ------------------------------------------------------------ |
| `@RequiredArgsConstructor` | Services, Controllers, Adapters    | Injeção de dependências via construtor                       |
| `@NonNull`                 | Parâmetros de construtor           | Validação automática de null (gera `Objects.requireNonNull`) |
| `@Getter`                  | Exceções e Enums com campos        | Elimina getters manuais                                      |
| `@UtilityClass`            | Classes com métodos estáticos      | Construtor privado + final automático                        |
| `@Slf4j`                   | Qualquer classe que precisa de log | Logger `log` automático                                      |

```java
// ✅ Service com injeção e validação de null
@Service
@RequiredArgsConstructor
@Slf4j
public class GeolocationService implements GeolocationUseCase {
    private final @NonNull GeolocationCache cache;
    private final @NonNull GeolocationProvider provider;
    private final @NonNull GeolocationProperties properties;
}

// ✅ Exceção com getter automático
@Getter
public class InvalidIpAddressException extends RuntimeException {
    private final String ip;
}

// ✅ Classe utilitária sem boilerplate
@UtilityClass
public class IpValidator {
    public boolean isValid(String ip) { /* ... */ }
}

// ✅ Enum com campos
@Getter
@RequiredArgsConstructor
public enum DataSource {
    API("api"), CACHE("cache"), FALLBACK("fallback");
    private final String value;
}
```

### Spring Annotations

| Annotation   | Uso                            | Benefício                             |
| ------------ | ------------------------------ | ------------------------------------- |
| `@Validated` | Controllers                    | Ativa validação de parâmetros         |
| `@NotBlank`  | Parâmetros String obrigatórios | Rejeita null e strings vazias/brancas |
| `@Schema`    | DTOs (Request/Response)        | Documentação OpenAPI automática       |

```java
// ✅ Controller com validação
@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class GeolocationController {

    @GetMapping("/locate")
    public ResponseEntity<GeolocationResponse> locate(
        @RequestParam @NotBlank String ip,
        @RequestHeader("x-device-platform") String platform
    ) { /* ... */ }
}

// ✅ DTO com documentação OpenAPI
public record LoginRequest(
    @Schema(description = "Username", example = "admin")
    @NotBlank String username,

    @Schema(description = "Password", example = "Admin123@")
    @NotBlank String password
) {}
```

### Records e Validação

Para Records, use `Objects.requireNonNull` no compact constructor (Lombok não suporta):

```java
public record Country(String code, String name) {
    public Country {
        Objects.requireNonNull(code, "code cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
    }
}
```

---

## 11. Estratégia de Autenticação (JWT)

### Escolha: JWT Stateless

**Justificativa:**

- ✅ Sem necessidade de sessão no servidor
- ✅ Escalabilidade horizontal (qualquer instância valida o token)
- ✅ Padrão amplamente adotado em APIs REST
- ⚠️ Token não pode ser invalidado antes da expiração

### Configuração

```yaml
security:
  jwt:
    secret-key: ${JWT_SECRET_KEY:404E635266...} # 256-bit key
    expiration: ${JWT_EXPIRATION:86400000} # 24 horas em ms
  user:
    username: ${SECURITY_USER:admin}
    password: ${SECURITY_PASSWORD:Admin123@}
```

### Componentes de Segurança

| Componente                | Responsabilidade                                  |
| ------------------------- | ------------------------------------------------- |
| `SecurityConfig`          | Configura filtros, endpoints públicos/protegidos  |
| `JwtService`              | Gera e valida tokens JWT                          |
| `JwtAuthenticationFilter` | Intercepta requests, extrai e valida Bearer token |
| `AuthController`          | Endpoint POST /auth/login                         |

### Fluxo de Autenticação

```java
// 1. Login
POST /auth/login
{"username": "admin", "password": "Admin123@"}

// 2. Resposta
{"token": "eyJhbGciOiJIUzI1NiIs..."}

// 3. Requisições autenticadas
GET /api/geolocation/v1/locate?ip=8.8.8.8
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

### Estrutura do Token

```json
// Header
{"alg": "HS256", "typ": "JWT"}

// Payload
{
  "sub": "admin",           // Subject (username)
  "iat": 1712947200,        // Issued At
  "exp": 1713033600         // Expiration (24h)
}

// Signature
HMACSHA256(base64(header) + "." + base64(payload), secret)
```

### Endpoints por Acesso

| Endpoint               | Acesso      | Justificativa                      |
| ---------------------- | ----------- | ---------------------------------- |
| `POST /auth/login`     | Público     | Necessário para obter token        |
| `GET /actuator/health` | Público     | Health checks (k8s, load balancer) |
| `GET /swagger-ui/**`   | Público     | Documentação da API                |
| `GET /api/**`          | Autenticado | Rotas de negócio protegidas        |

### Tratamento de Erros

| Cenário                    | HTTP Status | Resposta     |
| -------------------------- | ----------- | ------------ |
| Sem Authorization header   | 403         | Forbidden    |
| Token expirado             | 403         | Forbidden    |
| Token inválido/mal formado | 403         | Forbidden    |
| Credenciais inválidas      | 401         | Unauthorized |
| Usuário não encontrado     | 401         | Unauthorized |
