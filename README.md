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

## Insomnia Collection

O arquivo `insomnia-collection.json` contém uma coleção completa para testar todos os endpoints da API.

### Importar no Insomnia

1. Abra o Insomnia
2. Vá em **Application** → **Preferences** → **Data** → **Import Data**
3. Selecione o arquivo `insomnia-collection.json`
4. Configure a variável `jwt_token` no environment após fazer login

### Estrutura da Collection

```
📁 IP Geolocation Service
├── 📁 Authentication
│   └── POST Login (Get JWT Token)
│
├── 📁 Geolocation
│   ├── GET Locate Valid IP (Google DNS)
│   ├── GET Locate Brazil IP
│   ├── GET Locate Private IP (Fallback)
│   ├── GET Locate Invalid IP (Error 400)
│   ├── GET Missing Platform Header (Error 400)
│   └── GET No Auth (Error 403)
│
├── 📁 Health & Metrics
│   ├── GET Health Check
│   ├── GET Application Info
│   └── GET Swagger UI
│
├── 📁 IPv4 vs IPv6
│   ├── GET IPv4 - Google DNS
│   ├── GET IPv6 - Google DNS
│   ├── GET IPv4 - Cloudflare DNS
│   └── GET IPv6 - Cloudflare DNS
│
├── 📁 Brazil Regions
│   ├── 📁 IPv4
│   │   ├── Sul - RS (Porto Alegre)
│   │   ├── Sul - PR (Curitiba)
│   │   ├── Sudeste - SP (Capital)
│   │   ├── Sudeste - RJ (Capital)
│   │   ├── Sudeste - MG (Belo Horizonte)
│   │   ├── Centro-Oeste - DF (Brasília)
│   │   ├── Nordeste - BA (Salvador)
│   │   ├── Nordeste - PE (Recife)
│   │   ├── Norte - AM (Manaus)
│   │   └── Norte - PA (Belém)
│   │
│   └── 📁 IPv6
│       ├── Sul - RS (Porto Alegre)
│       ├── Sul - PR (Curitiba)
│       ├── Sudeste - SP (Capital)
│       ├── Sudeste - RJ (Capital)
│       ├── Sudeste - MG (Belo Horizonte)
│       ├── Centro-Oeste - DF (Brasília)
│       ├── Nordeste - BA (Feira de Santana)
│       ├── Nordeste - PE (Petrolina)
│       ├── Norte - AM (Manaus)
│       └── Norte - PA (Redenção)
│
└── 📁 World Continents
    ├── 📁 IPv4
    │   ├── América do Norte - EUA
    │   ├── América do Norte - Canadá
    │   ├── América do Sul - Chile
    │   ├── América do Sul - Argentina
    │   ├── Europa - Alemanha
    │   ├── Europa - Reino Unido
    │   ├── Ásia - Japão
    │   ├── Ásia - China
    │   ├── Ásia - Índia
    │   ├── África - África do Sul
    │   ├── África - Nigéria
    │   ├── Oceania - Austrália
    │   └── Oceania - Nova Zelândia
    │
    └── 📁 IPv6
        ├── América do Norte - EUA
        ├── América do Norte - Canadá
        ├── América do Sul - Chile
        ├── América do Sul - Argentina
        ├── Europa - Alemanha
        ├── Europa - Reino Unido
        ├── Ásia - Japão
        ├── Ásia - China
        ├── Ásia - Índia
        ├── África - África do Sul
        ├── África - Nigéria
        ├── Oceania - Austrália
        └── Oceania - Nova Zelândia
```

### IPs de Teste por Região

#### Brasil (IPv4) - Universidades Federais

| Região       | Estado              | IP            | Universidade |
| ------------ | ------------------- | ------------- | ------------ |
| Sul          | RS (Porto Alegre)   | 143.54.0.1    | UFRGS        |
| Sul          | PR (Curitiba)       | 200.17.209.1  | UFPR         |
| Sudeste      | SP (Capital)        | 143.107.45.1  | USP          |
| Sudeste      | RJ (Capital)        | 146.164.0.1   | UFRJ         |
| Sudeste      | MG (Belo Horizonte) | 150.164.0.1   | UFMG         |
| Centro-Oeste | DF (Brasília)       | 164.41.0.1    | UnB          |
| Nordeste     | BA (Salvador)       | 200.128.51.1  | UFBA         |
| Nordeste     | PE (Recife)         | 150.161.0.1   | UFPE         |
| Norte        | AM (Manaus)         | 200.129.163.1 | UFAM         |
| Norte        | PA (Belém)          | 200.239.64.1  | UFPA         |

#### Brasil (IPv6)

| Região       | Estado                | IP                  | ISP/Rede      |
| ------------ | --------------------- | ------------------- | ------------- |
| Sul          | RS (Porto Alegre)     | 2801:80::1          | POP-RS        |
| Sul          | PR (Curitiba)         | 2804:7f4::1         | Copel Telecom |
| Sudeste      | SP (Capital)          | 2804:14c:87:9d00::1 | Vivo          |
| Sudeste      | RJ (Capital)          | 2804:388:1::1       | -             |
| Sudeste      | MG (Belo Horizonte)   | 2804:d45::1         | -             |
| Centro-Oeste | DF (Brasília)         | 2804:d50::1         | -             |
| Nordeste     | BA (Feira de Santana) | 2804:46c::1         | -             |
| Nordeste     | PE (Petrolina)        | 2804:2d4::1         | -             |
| Norte        | AM (Manaus)           | 2804:214:1::1       | -             |
| Norte        | PA (Redenção)         | 2804:3e4::1         | -             |

#### Mundo (IPv4)

| Continente       | País          | IP              | Cidade       |
| ---------------- | ------------- | --------------- | ------------ |
| América do Norte | EUA           | 8.8.8.8         | Ashburn      |
| América do Norte | Canadá        | 99.79.0.1       | Toronto      |
| América do Sul   | Chile         | 200.29.0.1      | Santiago     |
| América do Sul   | Argentina     | 200.45.191.35   | Resistencia  |
| Europa           | Alemanha      | 85.214.132.117  | Berlin       |
| Europa           | Reino Unido   | 194.168.0.1     | Birmingham   |
| Ásia             | Japão         | 210.152.135.178 | Kitakyushu   |
| Ásia             | China         | 114.114.114.114 | Jinan        |
| Ásia             | Índia         | 49.44.65.170    | Navi Mumbai  |
| África           | África do Sul | 154.0.0.1       | Johannesburg |
| África           | Nigéria       | 41.58.0.1       | Lagos        |
| Oceania          | Austrália     | 1.128.0.1       | Brisbane     |
| Oceania          | Nova Zelândia | 203.97.0.1      | Auckland     |

#### Mundo (IPv6)

| Continente       | País          | IP                | Cidade       |
| ---------------- | ------------- | ----------------- | ------------ |
| América do Norte | EUA           | 2607:f8b0:4004::1 | Washington   |
| América do Norte | Canadá        | 2607:fea8::1      | Toronto      |
| América do Sul   | Chile         | 2800:300:6291::1  | Santiago     |
| América do Sul   | Argentina     | 2800:810:408::1   | Buenos Aires |
| Europa           | Alemanha      | 2a01:4f8:0:1::1   | Nuremberg    |
| Europa           | Reino Unido   | 2a00:86c0:0:0::1  | London       |
| Ásia             | Japão         | 2001:218:0:0::1   | Chiyoda City |
| Ásia             | China         | 240e:0:0:0::1     | Shenzhen     |
| Ásia             | Índia         | 2405:200:0:0::1   | Navi Mumbai  |
| África           | África do Sul | 2001:4200::1      | Cape Town    |
| África           | Nigéria       | 2c0f:f5c0::1      | Lagos        |
| Oceania          | Austrália     | 2001:388::1       | Sydney       |
| Oceania          | Nova Zelândia | 2001:df0::1       | Auckland     |

### Variáveis de Environment

| Variável      | Valor Padrão            | Descrição            |
| ------------- | ----------------------- | -------------------- |
| `base_url`    | `http://localhost:8080` | URL base da API      |
| `api_version` | `v1`                    | Versão da API        |
| `jwt_token`   | (vazio)                 | Token JWT após login |

## Licença

MIT
