# ip-geolocation-service

Microserviço REST de geolocalização que identifica o país e informações geográficas a partir de um endereço IP.

## Status do Projeto

🚧 **Em desenvolvimento**

## Tecnologias

- **Java 21** - LTS com Records, Pattern Matching, Virtual Threads
- **Spring Boot 3.3.x** - Framework web
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

## Endpoints

### GET /api/geolocation/v1/locate

Retorna informações de geolocalização para um IP.

**Parâmetros:**
- `ip` (query, obrigatório): Endereço IPv4 ou IPv6

**Headers:**
- `x-device-platform` (obrigatório): `iOS`, `Android` ou `Web`

**Exemplo:**
```bash
curl "http://localhost:8080/api/geolocation/v1/locate?ip=8.8.8.8" \
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
