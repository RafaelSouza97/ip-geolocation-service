# Plano de Implementação

## Overview

Este documento organiza todas as tarefas necessárias para implementar o ip-geolocation-service do zero.

**Estimativa Total:** 8-12 horas de desenvolvimento

---

## Fase 1: Setup do Projeto (1-2h)

### 1.1 Estrutura Base
- [ ] Criar projeto Spring Boot 3.3.x com Spring Initializr
- [ ] Configurar Java 21
- [ ] Adicionar dependências (pom.xml)
- [ ] Criar estrutura de pastas (application/, infrastructure/)
- [ ] Configurar .gitignore

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
- [ ] Criar application.yml com configurações base
- [ ] Criar application-local.yml para desenvolvimento
- [ ] Configurar GeolocationProperties com @ConfigurationProperties

---

## Fase 2: Domain Layer (1h)

### 2.1 Models (Records)
- [ ] `Country` - código e nome do país
- [ ] `Region` - código e nome da região
- [ ] `Coordinates` - latitude e longitude
- [ ] `GeolocationInfo` - agregado com todos os dados

### 2.2 Exceptions
- [ ] `InvalidIpAddressException`
- [ ] `PrivateIpAddressException`
- [ ] `MissingPlatformHeaderException`
- [ ] `InvalidPlatformException`

### 2.3 Ports (Interfaces)
- [ ] `GeolocationUseCase` (port/in)
- [ ] `GeolocationProvider` (port/out)
- [ ] `GeolocationCache` (port/out)

---

## Fase 3: Application Layer (2h)

### 3.1 Service
- [ ] `GeolocationService` implementando `GeolocationUseCase`
  - [ ] Método `locate(String ip)`
  - [ ] Lógica de verificação de cache
  - [ ] Chamada ao provider externo
  - [ ] Tratamento de fallback
  - [ ] Armazenamento em cache

### 3.2 Validação de IP
- [ ] `IpValidator` - classe utilitária
  - [ ] Regex para IPv4
  - [ ] Regex para IPv6
  - [ ] Detecção de IPs privados
  - [ ] Detecção de localhost

---

## Fase 4: Infrastructure Layer (2-3h)

### 4.1 Controller
- [ ] `GeolocationController`
  - [ ] Endpoint GET /api/geolocation/v1/locate
  - [ ] Validação de parâmetros
  - [ ] Validação de headers
  - [ ] Conversão Request → Domain → Response

### 4.2 DTOs
- [ ] `GeolocationResponse` - resposta de sucesso
- [ ] `ErrorResponse` - resposta de erro

### 4.3 Validators
- [ ] `@ValidIp` - annotation customizada
- [ ] `IpAddressValidator` - implementação
- [ ] `@ValidPlatform` - annotation customizada
- [ ] `PlatformValidator` - implementação

### 4.4 Exception Handler
- [ ] `GlobalExceptionHandler` com @RestControllerAdvice

### 4.5 HTTP Client
- [ ] `IpApiClient` implementando `GeolocationProvider`
  - [ ] HttpClient configurado
  - [ ] Timeout handling
  - [ ] JSON parsing
  - [ ] Error handling

### 4.6 Cache
- [ ] `CacheConfig` - bean do Caffeine
- [ ] `CaffeineGeolocationCache` implementando `GeolocationCache`

---

## Fase 5: Testes (2-3h)

### 5.1 Unit Tests
- [ ] `GeolocationServiceTest`
  - [ ] Teste de cache hit
  - [ ] Teste de cache miss
  - [ ] Teste de fallback
  - [ ] Teste de IPs privados
- [ ] `IpValidatorTest`
  - [ ] IPv4 válido
  - [ ] IPv6 válido
  - [ ] IP inválido
  - [ ] IPs privados

### 5.2 Integration Tests
- [ ] `GeolocationControllerIT`
  - [ ] Sucesso com IP válido
  - [ ] Erro 400 para IP inválido
  - [ ] Erro 400 para header ausente
  - [ ] Erro 400 para platform inválido
- [ ] `IpApiClientIT` (WireMock)
  - [ ] Parsing de resposta sucesso
  - [ ] Handling de timeout
  - [ ] Handling de erro

### 5.3 Coverage
- [ ] Verificar 80%+ na camada service
- [ ] Configurar JaCoCo

---

## Fase 6: Documentação (1h)

### 6.1 README.md
- [ ] Descrição do projeto
- [ ] Tecnologias utilizadas
- [ ] Pré-requisitos
- [ ] Como executar
- [ ] Exemplos de uso (curl)
- [ ] Como rodar os testes
- [ ] Decisões técnicas

### 6.2 OpenAPI (Diferencial)
- [ ] Annotations no Controller
- [ ] Configuração do Swagger UI

---

## Fase 7: Docker & Deploy (1h) - Diferencial

### 7.1 Containerização
- [ ] Dockerfile multi-stage
- [ ] docker-compose.yml
- [ ] .dockerignore

### 7.2 Azure Deploy
- [ ] Configurar Azure Container Apps
- [ ] CI/CD com GitHub Actions

---

## Checklist de Entrega

### Obrigatório
- [ ] Código organizado em camadas
- [ ] Testes unitários (80% service)
- [ ] application.yml configurado
- [ ] README.md completo
- [ ] .gitignore configurado
- [ ] Build funcionando (mvn clean package)
- [ ] Commits incrementais com mensagens descritivas

### Diferencial
- [ ] Aplicação hospedada
- [ ] Dockerfile
- [ ] docker-compose.yml
- [ ] Swagger/OpenAPI
- [ ] GitHub Actions
- [ ] Collection Postman

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
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Test
mvn test
mvn test -Dtest=GeolocationServiceTest
mvn verify  # com integration tests

# Coverage report
mvn jacoco:report

# Docker
docker build -t ip-geolocation-service .
docker-compose up -d

# Testar API
curl "http://localhost:8080/api/geolocation/v1/locate?ip=8.8.8.8" \
  -H "x-device-platform: Web"
```
