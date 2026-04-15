# ip-geolocation-service

Microserviço REST de geolocalização que identifica o país e informações geográficas a partir de um endereço IP.

## Status do Projeto

✅ **MVP Concluído** (317 testes passando, 99% cobertura, 89% mutation coverage)

## Tecnologias

- **Java 21** - LTS com Records, Pattern Matching, Virtual Threads
- **Spring Boot 3.3.x** - Framework web com `@Validated`, `@NotBlank`
- **Spring Security + JWT** - Autenticação stateless
- **Lombok** - Redução de boilerplate (`@RequiredArgsConstructor`, `@Getter`, `@UtilityClass`, `@Slf4j`)
- **Caffeine** - Cache in-memory de alta performance
- **Maven** - Build tool
- **Docker** - Containerização
- **Azure Container Apps** - Deploy serverless

## Estrutura do Projeto

```
ip-geolocation-service/
├── .github/
│   ├── prompts/           # Prompts de boas práticas para AI
│   ├── instructions/      # Instruções de código para AI
│   └── workflows/         # GitHub Actions (CI/CD)
├── docs/
│   ├── ARCHITECTURE.md    # Decisões arquiteturais
│   ├── STRATEGY.md        # Estratégias de implementação
│   └── IMPLEMENTATION-PLAN.md  # Plano de tarefas
├── src/
│   ├── main/java/...
│   └── test/java/...
├── docker/
├── pom.xml
└── README.md
```

## Pré-requisitos

- **Java 21** - [Download](https://adoptium.net/temurin/releases/?version=21)
- **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- **Docker** (opcional) - [Download](https://www.docker.com/products/docker-desktop/)

Verifique a instalação:

```bash
java -version   # deve mostrar "21.x.x"
mvn -version    # deve mostrar "3.9.x"
docker --version  # (opcional)
```

## Instalação

```bash
# Clonar o repositório
git clone https://github.com/seu-usuario/ip-geolocation-service.git
cd ip-geolocation-service

# Instalar dependências e compilar
mvn clean install -DskipTests

# Ou apenas baixar dependências
mvn dependency:resolve
```

## Como Executar

### Localmente (Maven)

```bash
# Build com testes
mvn clean package

# Executar (profile local com logs DEBUG)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Ou executar o JAR diretamente
java -jar target/ip-geolocation-service-1.0.0-SNAPSHOT.jar
```

A aplicação estará disponível em: `http://localhost:8080`

### Com Docker

```bash
# Build da imagem
docker-compose build

# Executar em background
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar
docker-compose down
```

### Verificar se está rodando

**Linux/Mac/Git Bash:**

```bash
curl http://localhost:8080/actuator/health
```

**PowerShell:**

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
```

**Swagger UI:** Abrir no navegador: http://localhost:8080/swagger-ui.html

## Autenticação

A API utiliza autenticação JWT (JSON Web Token). Todas as rotas `/api/**` requerem um token válido.

### Credenciais

| Usuário | Senha       | Observação                     |
| ------- | ----------- | ------------------------------ |
| `admin` | `Admin123@` | Usuário fixo para demonstração |

### Obtendo o Token

**Linux/Mac/Git Bash:**

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123@"}'
```

**PowerShell:**

```powershell
$body = '{"username":"admin","password":"Admin123@"}'
Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method Post -ContentType "application/json" -Body $body
```

**Resposta:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

### Rotas Públicas

| Rota                   | Descrição    |
| ---------------------- | ------------ |
| `POST /auth/login`     | Autenticação |
| `GET /actuator/health` | Health check |
| `GET /swagger-ui/**`   | Documentação |

---

## Endpoints

### POST /auth/login

Autentica o usuário e retorna um token JWT.

**Body:**

```json
{ "username": "admin", "password": "Admin123@" }
```

**Resposta (200):**

```json
{ "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

---

### GET /api/geolocation/v1/locate

Retorna informações de geolocalização para um IP.

**Parâmetros:**

- `ip` (query, obrigatório): Endereço IPv4 ou IPv6

**Headers:**

- `Authorization` (obrigatório): `Bearer <token>`
- `x-device-platform` (obrigatório): `iOS`, `Android` ou `Web`

**Exemplo:**

**Linux/Mac/Git Bash:**

```bash
# 1. Obter token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123@"}' | jq -r '.token')

# 2. Consultar geolocalização
curl "http://localhost:8080/api/geolocation/v1/locate?ip=8.8.8.8" \
  -H "Authorization: Bearer $TOKEN" \
  -H "x-device-platform: Web"
```

**PowerShell:**

```powershell
# 1. Obter token
$body = '{"username":"admin","password":"Admin123@"}'
$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method Post -ContentType "application/json" -Body $body
$token = $response.token

# 2. Consultar geolocalização
$headers = @{
    "Authorization" = "Bearer $token"
    "x-device-platform" = "Web"
}
Invoke-RestMethod -Uri "http://localhost:8080/api/geolocation/v1/locate?ip=8.8.8.8" -Headers $headers
```

**Resposta (200):**

```json
{
  "ip": "8.8.8.8",
  "country": { "code": "US", "name": "United States" },
  "region": { "code": "CA", "name": "California" },
  "city": "Mountain View",
  "coordinates": { "latitude": 37.386, "longitude": -122.0838 },
  "timezone": "America/Los_Angeles",
  "isp": "Google LLC",
  "source": "api",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

## Testes

```bash
# Executar todos os testes
mvn test

# Gerar relatório de cobertura (JaCoCo)
mvn test jacoco:report
# Relatório: target/site/jacoco/index.html

# Executar mutation testing (PITest)
mvn test pitest:mutationCoverage
# Relatório: target/pit-reports/index.html
```

### Métricas de Qualidade

| Métrica           | Resultado | Threshold |
| ----------------- | --------- | --------- |
| Testes            | 317       | -         |
| Line Coverage     | 99%       | 80%       |
| Mutation Coverage | 89%       | 70%       |
| Test Strength     | 90%       | -         |

## Documentação

- [Arquitetura](docs/ARCHITECTURE.md) - Decisões arquiteturais e diagramas
- [Estratégias](docs/STRATEGY.md) - Detalhes de implementação
- [Plano](docs/IMPLEMENTATION-PLAN.md) - Tarefas e checklist

## Licença

MIT
