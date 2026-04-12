# Plano de Implementação

## Overview

Este documento organiza todas as tarefas necessárias para implementar o ip-geolocation-service do zero.

**Status:** ✅ **Concluído** (182 testes passando, 96%+ cobertura)

---

## Fase 1: Setup do Projeto (1-2h) ✅

### 1.1 Estrutura Base
- [x] Criar projeto Spring Boot 3.3.x com Spring Initializr
- [x] Configurar Java 21
- [x] Adicionar dependências (pom.xml)
- [x] Criar estrutura de pastas (application/, infrastructure/)
- [x] Configurar .gitignore

### 1.2 Dependências Maven

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Cache -->
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- OpenAPI/Swagger (diferencial) -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
    
    <!-- Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.wiremock</groupId>
        <artifactId>wiremock-standalone</artifactId>
        <version>3.3.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 1.3 Configuração
- [x] Criar application.yml com configurações base
- [x] Criar application-local.yml para desenvolvimento
- [x] Configurar GeolocationProperties com @ConfigurationProperties

---

## Fase 2: Domain Layer (1h) ✅

### 2.1 Models (Records)
- [x] `Country` - código e nome do país
- [x] `Region` - código e nome da região
- [x] `Coordinates` - latitude e longitude
- [x] `GeolocationInfo` - agregado com todos os dados

### 2.2 Exceptions
- [x] `InvalidIpAddressException`
- [x] `PrivateIpAddressException`
- [x] `MissingPlatformHeaderException`
- [x] `InvalidPlatformException`

### 2.3 Ports (Interfaces)
- [x] `GeolocationUseCase` (port/in)
- [x] `GeolocationProvider` (port/out)
- [x] `GeolocationCache` (port/out)

---

## Fase 3: Application Layer (2h) ✅

### 3.1 Service
- [x] `GeolocationService` implementando `GeolocationUseCase`
  - [x] Método `locate(String ip)`
  - [x] Lógica de verificação de cache
  - [x] Chamada ao provider externo
  - [x] Tratamento de fallback
  - [x] Armazenamento em cache

### 3.2 Validação de IP
- [x] `IpValidator` - classe utilitária
  - [x] Regex para IPv4
  - [x] Regex para IPv6
  - [x] Detecção de IPs privados
  - [x] Detecção de localhost

---

## Fase 4: Infrastructure Layer (2-3h) ✅

### 4.1 Controller
- [x] `GeolocationController`
  - [x] Endpoint GET /api/geolocation/v1/locate
  - [x] Validação de parâmetros
  - [x] Validação de headers
  - [x] Conversão Request → Domain → Response

### 4.2 DTOs
- [x] `GeolocationResponse` - resposta de sucesso
- [x] `ErrorResponse` - resposta de erro

### 4.3 Validators
- [x] `@ValidIp` - annotation customizada
- [x] `IpAddressValidator` - implementação
- [x] `@ValidPlatform` - annotation customizada
- [x] `PlatformValidator` - implementação

### 4.4 Exception Handler
- [x] `GlobalExceptionHandler` com @RestControllerAdvice

### 4.5 HTTP Client
- [x] `IpApiClient` implementando `GeolocationProvider`
  - [x] HttpClient configurado
  - [x] Timeout handling
  - [x] JSON parsing
  - [x] Error handling

### 4.6 Cache
- [x] `CacheConfig` - bean do Caffeine
- [x] `CaffeineGeolocationCache` implementando `GeolocationCache`

---

## Fase 5: Testes (2-3h) ✅

### 5.1 Unit Tests
- [x] `GeolocationServiceTest`
  - [x] Teste de cache hit
  - [x] Teste de cache miss
  - [x] Teste de fallback
  - [x] Teste de IPs privados
- [x] `IpValidatorTest`
  - [x] IPv4 válido
  - [x] IPv6 válido
  - [x] IP inválido
  - [x] IPs privados

### 5.2 Integration Tests
- [x] `GeolocationControllerTest`
  - [x] Sucesso com IP válido
  - [x] Erro 400 para IP inválido
  - [x] Erro 400 para header ausente
  - [x] Erro 400 para platform inválido
- [x] `IpApiClientTest` (WireMock)
  - [x] Parsing de resposta sucesso
  - [x] Handling de timeout
  - [x] Handling de erro

### 5.3 Coverage
- [x] Verificar 80%+ na camada service (96%+ alcançado)
- [x] Configurar JaCoCo

---

## Fase 6: Documentação (1h) ✅

### 6.1 README.md
- [x] Descrição do projeto
- [x] Tecnologias utilizadas
- [x] Pré-requisitos
- [x] Como executar
- [x] Exemplos de uso (curl)
- [x] Como rodar os testes
- [x] Decisões técnicas

### 6.2 OpenAPI (Diferencial)
- [ ] Annotations no Controller
- [ ] Configuração do Swagger UI

---

## Fase 7: Docker & Deploy (1h) - Diferencial ✅

### 7.1 Containerização
- [x] Dockerfile multi-stage
- [x] docker-compose.yml
- [x] .dockerignore

### 7.2 Azure Deploy
- [ ] Configurar Azure Container Apps
- [ ] CI/CD com GitHub Actions

---

## Fase 8: Segurança - JWT Authentication ✅

### 8.1 Configuração
- [x] Adicionar dependências Spring Security e jjwt
- [x] Criar `SecurityProperties` com configuração externalizável
- [x] Configurar usuário fixo (admin/Admin123@)
- [x] Configurar expiração de token (24h)

### 8.2 Componentes
- [x] `JwtService` - geração e validação de tokens
- [x] `JwtAuthenticationFilter` - filtro Bearer token
- [x] `SecurityConfig` - configuração de segurança
- [x] `AuthController` - endpoint de login

### 8.3 DTOs
- [x] `LoginRequest` - username e password
- [x] `LoginResponse` - token JWT

### 8.4 Testes
- [x] `JwtServiceTest` - testes de geração/validação de tokens
- [x] `AuthControllerTest` - testes de autenticação
- [x] Atualizar testes existentes para considerar segurança

---

## Checklist de Entrega ✅

### Obrigatório
- [x] Código organizado em camadas
- [x] Testes unitários (96%+ coverage)
- [x] application.yml configurado
- [x] README.md completo
- [x] .gitignore configurado
- [x] Build funcionando (mvn clean package)
- [x] Commits incrementais com mensagens descritivas

### Diferencial
- [ ] Aplicação hospedada
- [x] Dockerfile
- [x] docker-compose.yml
- [ ] Swagger/OpenAPI annotations
- [ ] GitHub Actions
- [x] Collection Insomnia

---

## Ordem de Execução Recomendada

```
1. Setup projeto básico (Spring Initializr + dependências)
2. Domain models e exceptions
3. Ports (interfaces)
4. Service com lógica básica (sem cache/external)
5. Testes unitários do service
6. IP Validator
7. Testes do validator
8. Controller + DTOs
9. Testes do controller
10. HTTP Client (IpApiClient)
11. Testes do client (WireMock)
12. Cache implementation
13. Testes de integração completos
14. README.md
15. Docker + Deploy (se tempo permitir)
```

---

## Comandos Úteis

```bash
# Criar projeto (Spring Initializr CLI)
spring init --dependencies=web,validation,actuator,lombok \
  --java-version=21 --type=maven-project \
  --name=ip-geolocation-service ip-geolocation-service

# Build
mvn clean package -DskipTests
mvn clean package

# Run
mvn spring-boot:run
mvn spring-boot:run "-Dspring-boot.run.profiles=local"

# Test
mvn test
mvn test -Dtest=GeolocationServiceTest
mvn verify  # com integration tests

# Coverage report
mvn jacoco:report
# Abrir: target/site/jacoco/index.html

# Docker
docker build -t ip-geolocation-service .
docker-compose up -d

# Autenticação - Obter token JWT
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123@"}'

# Testar API (com token)
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123@"}' | jq -r '.token')

curl "http://localhost:8080/api/geolocation/v1/locate?ip=8.8.8.8" \
  -H "Authorization: Bearer $TOKEN" \
  -H "x-device-platform: Web"
```
