# Arquitetura do ip-geolocation-service

## Visão Geral

O **ip-geolocation-service** é um microserviço REST que identifica informações geográficas a partir de um endereço IP, utilizando princípios de Clean Architecture para manter separação clara entre regras de negócio e detalhes de implementação.

## Stack Tecnológica

| Componente       | Tecnologia                   | Versão | Justificativa                                            |
| ---------------- | ---------------------------- | ------ | -------------------------------------------------------- |
| Linguagem        | Java                         | 21 LTS | Records, Pattern Matching, Virtual Threads               |
| Framework        | Spring Boot                  | 3.3.x  | Ecossistema maduro, produtividade                        |
| Build Tool       | Maven                        | 3.9.x  | Padrão corporativo, CI/CD friendly                       |
| Boilerplate      | Lombok                       | 1.18.x | @RequiredArgsConstructor, @Getter, @UtilityClass, @Slf4j |
| Cache            | Caffeine                     | 3.x    | In-memory, alta performance                              |
| HTTP Client      | Java HttpClient              | 21     | Nativo, suporte a HTTP/2                                 |
| Segurança        | Spring Security + JWT        | 6.3.x  | Autenticação stateless                                   |
| Validação        | Hibernate Validator          | 8.x    | Bean Validation 3.0 (@Validated, @NotBlank)              |
| Documentação     | SpringDoc OpenAPI            | 2.x    | Swagger UI integrado (@Schema)                           |
| Testes           | JUnit 5 + Mockito + WireMock | -      | Stack padrão (317 testes)                                |
| Mutation Testing | PITest                       | 1.15.x | 89% mutation coverage                                    |
| Containers       | Docker + Docker Compose      | -      | Portabilidade                                            |
| Cloud            | Azure Container Apps         | -      | Serverless containers                                    |

## Diagrama de Arquitetura

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              INFRASTRUCTURE                              │
│  ┌─────────────────────────────────────────────────────────────────────┐│
│  │                         adapter/in/web                               ││
│  │  ┌─────────────────────┐  ┌────────────────────┐                    ││
│  │  │ GeolocationController│  │ GlobalExceptionHandler│                 ││
│  │  └─────────┬───────────┘  └────────────────────┘                    ││
│  │            │                                                         ││
│  │            │ (implements)                                            ││
│  └────────────┼─────────────────────────────────────────────────────────┘│
│               │                                                          │
│  ┌────────────▼─────────────────────────────────────────────────────────┐│
│  │                          APPLICATION                                  ││
│  │  ┌───────────────────────────────────────────────────────────────┐   ││
│  │  │                        port/in                                 │   ││
│  │  │  ┌──────────────────────────────────────────────────────────┐ │   ││
│  │  │  │ <<interface>> GeolocationUseCase                         │ │   ││
│  │  │  │ + locate(ip: String): GeolocationInfo                    │ │   ││
│  │  │  └──────────────────────────────────────────────────────────┘ │   ││
│  │  └───────────────────────────────────────────────────────────────┘   ││
│  │                              │                                        ││
│  │                              │ (implements)                           ││
│  │                              ▼                                        ││
│  │  ┌───────────────────────────────────────────────────────────────┐   ││
│  │  │                         service                                │   ││
│  │  │  ┌──────────────────────────────────────────────────────────┐ │   ││
│  │  │  │ GeolocationService                                       │ │   ││
│  │  │  │ - cache: GeolocationCache                                │ │   ││
│  │  │  │ - provider: GeolocationProvider                          │ │   ││
│  │  │  │ + locate(ip: String): GeolocationInfo                    │ │   ││
│  │  │  └──────────────────────────────────────────────────────────┘ │   ││
│  │  └───────────────────────────────────────────────────────────────┘   ││
│  │                    │                     │                            ││
│  │       (uses)       │                     │       (uses)               ││
│  │                    ▼                     ▼                            ││
│  │  ┌───────────────────────────────────────────────────────────────┐   ││
│  │  │                        port/out                                │   ││
│  │  │  ┌────────────────────────┐  ┌─────────────────────────────┐  │   ││
│  │  │  │<<interface>>           │  │<<interface>>                 │  │   ││
│  │  │  │GeolocationCache        │  │GeolocationProvider           │  │   ││
│  │  │  │+ get(ip): Optional<>   │  │+ lookup(ip): GeolocationInfo │  │   ││
│  │  │  │+ put(ip, info): void   │  │                              │  │   ││
│  │  │  └────────────────────────┘  └─────────────────────────────┘  │   ││
│  │  └───────────────────────────────────────────────────────────────┘   ││
│  │                                                                       ││
│  │  ┌───────────────────────────────────────────────────────────────┐   ││
│  │  │                      domain/model                              │   ││
│  │  │  ┌─────────────────┐  ┌──────────────┐  ┌───────────────────┐ │   ││
│  │  │  │ GeolocationInfo │  │ Country      │  │ Coordinates       │ │   ││
│  │  │  │ (Record)        │  │ (Record)     │  │ (Record)          │ │   ││
│  │  │  └─────────────────┘  └──────────────┘  └───────────────────┘ │   ││
│  │  └───────────────────────────────────────────────────────────────┘   ││
│  └───────────────────────────────────────────────────────────────────────┘│
│                    │                     │                                │
│       (implements) │                     │ (implements)                   │
│                    ▼                     ▼                                │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                        adapter/out                                   │ │
│  │  ┌────────────────────────────────┐  ┌────────────────────────────┐ │ │
│  │  │ CaffeineGeolocationCache       │  │ ProviderSelector (@Primary)  │ │ │
│  │  │ - cache: Cache<String, Info>   │  │ - primary: IpApiClient       │ │ │
│  │  │ + get(ip): Optional<>          │  │ - secondary: IpApiCoClient   │ │ │
│  │  │ + put(ip, info): void          │  │ + lookup(ip): Info           │ │ │
│  │  └────────────────────────────────┘  └──────────────┬─────────────┘ │ │
│  │                                            ┌────────┴────────┐        │ │
│  │                                            │                 │        │ │
│  │                                      ┌─────▼─────┐    ┌──────▼─────┐  │ │
│  │                                      │IpApiClient│    │IpApiCoClient│ │ │
│  │                                      └─────┬─────┘    └──────┬─────┘  │ │
│  └────────────────────────────────────────────┼─────────────────┼────────┘ │
│                                               │                 │          │
└───────────────────────────────────────────────┼─────────────────┼──────────┘
                                                │                 │
                                                ▼                 ▼
                                      ┌──────────────────┐ ┌──────────────┐
                                      │   ip-api.com     │ │   ipapi.co   │
                                      │   (Primary)      │ │  (Secondary) │
                                      └──────────────────┘ └──────────────┘
```

## Fluxo de Requisição

```
1. HTTP Request → Controller
   ↓
2. Validação de IP e Headers
   ↓
3. GeolocationUseCase.locate(ip)
   ↓
4. Verifica Cache
   ├── HIT → Retorna do cache (source: "cache")
   └── MISS → Continua
         ↓
5. Chama ProviderSelector (com failover)
   ├── Primário OK → Armazena no cache, retorna (source: "api")
   ├── Primário falha → Tenta secundário
   │     ├── Secundário OK → Armazena no cache, retorna (source: "api")
   │     └── Secundário falha → Retorna fallback Brasil (source: "fallback")
   └── Em modo failover (5 min) → Usa secundário diretamente
         ↓
6. Controller converte para DTO de Response
   ↓
7. HTTP Response → Cliente
```

## Decisões Arquiteturais (ADRs)

### ADR-001: Clean Architecture

**Contexto:** Necessidade de código testável e manutenível.

**Decisão:** Adotar Clean Architecture com camadas application/ e infrastructure/.

**Consequências:**

- ✅ Testabilidade: serviços testados sem dependências externas
- ✅ Flexibilidade: trocar API externa sem alterar regras de negócio
- ⚠️ Mais arquivos e indireção

### ADR-002: Cache In-Memory com Caffeine

**Contexto:** IPs raramente mudam de localização, cache reduz chamadas externas.

**Decisão:** Usar Caffeine para cache local, TTL de 24h.

**Consequências:**

- ✅ Latência baixa (microsegundos)
- ✅ Zero dependência externa
- ⚠️ Cache não compartilhado entre instâncias (OK para este escopo)

**Alternativa futura:** Redis para cache distribuído (já preparado no docker-compose).

### ADR-003: Fallback para Brasil

**Contexto:** API externa pode falhar ou IP pode não ser encontrado.

**Decisão:** Retornar país padrão (Brasil) com source: "fallback".

**Consequências:**

- ✅ Serviço sempre retorna 200 para IPs válidos
- ✅ Cliente pode verificar o campo `source`
- ⚠️ Dados imprecisos quando em fallback

### ADR-004: Records para DTOs e Domain Models

**Contexto:** Java 21 oferece Records nativamente.

**Decisão:** Usar Records para todas as estruturas imutáveis.

**Consequências:**

- ✅ Código conciso
- ✅ Imutabilidade garantida
- ✅ equals/hashCode/toString automáticos

### ADR-005: Dual Provider Failover com Circuit Breaker

**Contexto:** Dependência de uma única API externa (ip-api.com) representa um ponto único de falha. APIs gratuitas têm limites de requisição e podem ficar indisponíveis.

**Decisão:** Implementar sistema de failover com dois providers externos:

- **Primário:** ip-api.com (45 req/min, sem API key)
- **Secundário:** ipapi.co (1000 req/day, sem API key)
- **Circuit Breaker:** Quando primário falha, redireciona para secundário por 5 minutos
- **Fallback local:** Usado apenas quando ambos os providers externos falham

**Componentes:**

```
┌─────────────────────────────────────────────────────────────────┐
│                      ProviderSelector                           │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ State: PRIMARY | SECONDARY                                  ││
│  │ failoverUntil: Instant                                      ││
│  │                                                             ││
│  │ tryWithFailover(ip):                                        ││
│  │   1. Check if failover expired → reset to PRIMARY           ││
│  │   2. Try current provider                                   ││
│  │   3. If fails and state=PRIMARY → switch to SECONDARY (5m)  ││
│  │   4. If both fail → throw exception                         ││
│  └─────────────────────────────────────────────────────────────┘│
│                         │                                       │
│         ┌───────────────┼───────────────┐                       │
│         ▼               ▼               ▼                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐   │
│  │ IpApiClient │ │IpApiCoClient│ │ GeolocationService      │   │
│  │ (PRIMARY)   │ │ (SECONDARY) │ │ (handles local fallback)│   │
│  └──────┬──────┘ └──────┬──────┘ └─────────────────────────┘   │
│         │               │                                       │
└─────────┼───────────────┼───────────────────────────────────────┘
          ▼               ▼
   ┌─────────────┐ ┌─────────────┐
   │ ip-api.com  │ │  ipapi.co   │
   └─────────────┘ └─────────────┘
```

**Fluxo de Failover:**

```
Request → ProviderSelector
    │
    ├── State = PRIMARY?
    │   ├── Yes → Try ip-api.com
    │   │         ├── Success → Return result
    │   │         └── Fail → Switch to SECONDARY (5 min)
    │   │                    └── Try ipapi.co
    │   │                        ├── Success → Return result
    │   │                        └── Fail → Throw exception
    │   │                                    └── Service uses local fallback
    │   │
    │   └── No (State = SECONDARY)
    │       ├── Failover expired? → Reset to PRIMARY, retry
    │       └── Try ipapi.co
    │           ├── Success → Return result
    │           └── Fail → Throw exception
```

**Consequências:**

- ✅ Alta disponibilidade: dois providers externos antes de fallback local
- ✅ Resiliente a falhas temporárias: 5 minutos no secundário permite recuperação
- ✅ Transparente: cliente não percebe qual provider foi usado
- ✅ Thread-safe: AtomicReference para estado
- ⚠️ Limites acumulados: 45 req/min + 1000 req/day entre os dois
- ⚠️ Latência adicional: primeira requisição após falha tenta dois providers

**Configuração (application.yml):**

```yaml
geolocation:
  providers:
    primary:
      name: ip-api.com
      url: http://ip-api.com/json
      timeout: 5s
    secondary:
      name: ipapi.co
      url: https://ipapi.co
      timeout: 5s
    failover-duration: 5m
```

## Considerações de Segurança

1. **Autenticação JWT:** Todas as rotas `/api/**` requerem token Bearer válido
2. **Sessão stateless:** Não há estado no servidor, escalabilidade horizontal
3. **Token expiration:** Tokens expiram em 24 horas
4. **Não expor detalhes internos:** Erros de API externa não vazam para o cliente
5. **Validação de entrada:** Regex para IPs, whitelist para platforms
6. **IPs privados rejeitados:** Não consultar API externa para IPs de rede local
7. **Rate limiting:** (Diferencial) Limitar requisições por cliente

### Fluxo de Autenticação

```
1. POST /auth/login (username, password)
   ↓
2. Validar credenciais
   ↓
3. Gerar JWT (claims: sub, iat, exp)
   ↓
4. Retornar token ao cliente
   ↓
5. Cliente envia: Authorization: Bearer <token>
   ↓
6. JwtAuthenticationFilter valida token
   ↓
7. SecurityContext populado → Request autorizada
```

## Métricas e Observabilidade

- **Health check:** `/actuator/health`
- **Métricas:** `/actuator/metrics`
- **Logs estruturados:** JSON em produção
- **Tracing:** (Diferencial) Correlation ID nos headers
