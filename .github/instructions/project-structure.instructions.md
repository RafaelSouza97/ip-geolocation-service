---
applyTo: "**"
description: "Project structure and organization guidelines for ip-geolocation-service"
---

# Project Structure Instructions

## Directory Layout

```
ip-geolocation-service/
├── .github/
│   ├── prompts/              # AI prompts de boas práticas
│   ├── instructions/         # Instruções de código
│   └── workflows/            # GitHub Actions (CI/CD)
│
├── docs/                     # Documentação do projeto
│   ├── ARCHITECTURE.md       # Decisões arquiteturais
│   ├── STRATEGY.md           # Estratégias de implementação
│   └── api/                  # Documentação da API
│
├── src/
│   ├── main/
│   │   ├── java/com/example/geolocation/
│   │   │   ├── Application.java
│   │   │   ├── application/          # Camada de negócio
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   └── exception/
│   │   │   │   ├── port/
│   │   │   │   │   ├── in/
│   │   │   │   │   └── out/
│   │   │   │   └── service/
│   │   │   │
│   │   │   └── infrastructure/       # Camada de infraestrutura
│   │   │       ├── adapter/
│   │   │       │   ├── in/web/
│   │   │       │   └── out/
│   │   │       │       ├── cache/
│   │   │       │       └── client/
│   │   │       ├── config/
│   │   │       └── validation/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-local.yml
│   │
│   └── test/
│       └── java/com/example/geolocation/
│           ├── unit/
│           ├── integration/
│           └── fixtures/
│
├── docker/
│   └── Dockerfile
├── docker-compose.yml
├── pom.xml (ou build.gradle)
├── README.md
└── .gitignore
```

## File Naming Conventions

| Tipo | Padrão | Exemplo |
|------|--------|---------|
| Controller | `*Controller.java` | `GeolocationController.java` |
| Service | `*Service.java` | `GeolocationService.java` |
| Repository | `*Repository.java` | `IpCacheRepository.java` |
| DTO Request | `*Request.java` | `GeolocationRequest.java` |
| DTO Response | `*Response.java` | `GeolocationResponse.java` |
| Port (Input) | `*UseCase.java` | `GeolocationUseCase.java` |
| Port (Output) | `*Provider.java`, `*Cache.java` | `GeolocationProvider.java` |
| Exception | `*Exception.java` | `InvalidIpException.java` |
| Config | `*Config.java` | `CacheConfig.java` |
| Properties | `*Properties.java` | `GeolocationProperties.java` |
| Test | `*Test.java`, `*IT.java` | `GeolocationServiceTest.java` |

## Package Guidelines

- `application.domain.model` - Entidades e Value Objects
- `application.domain.exception` - Exceções de domínio
- `application.port.in` - Interfaces de casos de uso
- `application.port.out` - Interfaces de serviços externos
- `application.service` - Implementações dos casos de uso
- `infrastructure.adapter.in.web` - Controllers REST
- `infrastructure.adapter.out.cache` - Implementações de cache
- `infrastructure.adapter.out.client` - Clientes HTTP
- `infrastructure.config` - Configurações Spring
- `infrastructure.validation` - Validadores customizados

## Configuration Files

### application.yml
```yaml
# Configurações base (todos os ambientes)
spring:
  application:
    name: ip-geolocation-service

server:
  port: 8080

geolocation:
  api:
    url: http://ip-api.com/json
    timeout: 5s
  cache:
    ttl: 24h
  fallback:
    country-code: BR
    country-name: Brazil
```

### application-local.yml
```yaml
# Configurações para desenvolvimento local
logging:
  level:
    com.example.geolocation: DEBUG
```

### application-prod.yml
```yaml
# Configurações de produção (via variáveis de ambiente)
geolocation:
  api:
    url: ${GEOLOCATION_API_URL}
    timeout: ${GEOLOCATION_API_TIMEOUT:5s}
```
