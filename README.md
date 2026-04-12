# ip-geolocation-service

Microserviço REST de geolocalização que identifica o país e informações geográficas a partir de um endereço IP.

## Status do Projeto

✅ **MVP Concluído** (182 testes passando, 96%+ cobertura)

## Tecnologias

- **Java 21** - LTS com Records, Pattern Matching, Virtual Threads
- **Spring Boot 3.3.x** - Framework web
- **Spring Security + JWT** - Autenticação stateless
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

## Como Executar

```bash
# Build
mvn clean package

# Executar localmente
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Com Docker
docker-compose up -d
```

## Autenticação

A API utiliza autenticação JWT (JSON Web Token). Todas as rotas `/api/**` requerem um token válido.

### Credenciais

| Usuário | Senha | Observação |
|---------|-------|------------|
| `admin` | `admin123` | Usuário fixo para demonstração |

### Obtendo o Token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

### Rotas Públicas

| Rota | Descrição |
|------|-----------|
| `POST /auth/login` | Autenticação |
| `GET /actuator/health` | Health check |
| `GET /swagger-ui/**` | Documentação |

---

## Endpoints

### POST /auth/login

Autentica o usuário e retorna um token JWT.

**Body:**
```json
{"username": "admin", "password": "admin123"}
```

**Resposta (200):**
```json
{"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
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
```bash
# 1. Obter token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

# 2. Consultar geolocalização
curl "http://localhost:8080/api/geolocation/v1/locate?ip=8.8.8.8" \
  -H "Authorization: Bearer $TOKEN" \
  -H "x-device-platform: Web"
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

## Documentação

- [Arquitetura](docs/ARCHITECTURE.md) - Decisões arquiteturais e diagramas
- [Estratégias](docs/STRATEGY.md) - Detalhes de implementação
- [Plano](docs/IMPLEMENTATION-PLAN.md) - Tarefas e checklist

## Licença

MIT
