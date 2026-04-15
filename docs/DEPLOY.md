# Guia de Deploy - Render.com

Este guia mostra como fazer deploy gratuito da aplicação no Render.com.

🌐 **URL de Produção:** https://ip-geolocation-service-cn07.onrender.com

## Características do Plano Gratuito

- ✅ 750 horas/mês (suficiente para 1 serviço 24/7)
- ✅ Deploy automático do GitHub
- ✅ HTTPS automático
- ✅ Domínio gratuito: `seu-app.onrender.com`
- ⚠️ Serviço "dorme" após 15 min de inatividade (cold start ~30s)

---

## Passo a Passo

### 1. Criar Conta no Render

1. Acesse [render.com](https://render.com)
2. Clique em **"Get Started for Free"**
3. Faça login com sua conta **GitHub**

### 2. Criar Web Service

1. No dashboard, clique em **"New +"** → **"Web Service"**
2. Conecte seu repositório GitHub `ip-geolocation-service`
3. Configure:

| Campo         | Valor                    |
| ------------- | ------------------------ |
| Name          | `ip-geolocation-service` |
| Region        | `Oregon (US West)`       |
| Branch        | `main`                   |
| Runtime       | `Docker`                 |
| Instance Type | `Free`                   |

### 3. Configurar Variáveis de Ambiente

Em **Environment Variables**, adicione:

| Variável                 | Valor                       |
| ------------------------ | --------------------------- |
| `JWT_SECRET`             | (gere com o comando abaixo) |
| `SPRING_PROFILES_ACTIVE` | `prod`                      |

**Gerar JWT_SECRET seguro:**

```bash
# Linux/Mac/Git Bash
openssl rand -base64 64

# PowerShell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

### 4. Deploy

1. Clique em **"Create Web Service"**
2. Aguarde o build (~3-5 minutos)
3. Sua aplicação estará disponível em:
   ```
   https://ip-geolocation-service-cn07.onrender.com
   ```

---

## Configurar Deploy Automático (CI/CD)

Para que o deploy seja automático a cada push na `main`:

### 1. Obter Deploy Hook do Render

1. No Render, vá no seu serviço
2. Acesse **Settings** → **Deploy Hook**
3. Copie a URL do webhook

### 2. Configurar Secret no GitHub

1. No GitHub, vá no repositório
2. Acesse **Settings** → **Secrets and variables** → **Actions**
3. Clique em **"New repository secret"**
4. Crie:
   - **Name:** `RENDER_DEPLOY_HOOK_URL`
   - **Value:** (cole a URL copiada do Render)

Pronto! Agora a cada push na `main`, o pipeline:

1. Executa testes
2. Faz build da imagem Docker
3. Aciona o deploy no Render automaticamente

---

## GitHub Actions CI/CD

O pipeline de CI/CD está configurado em `.github/workflows/ci.yml`.

### O que o pipeline faz:

```
┌─────────────────────────────────────────────────────────────┐
│                    Push para main/develop                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  Job 1: Build & Test                                        │
│  • Compila código Java 21                                   │
│  • Executa testes unitários                                 │
│  • Gera relatório JaCoCo                                    │
│  • Verifica cobertura mínima (80%)                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  Job 2: Quality Check                                       │
│  • Verifica estilo (Checkstyle)                             │
│  • Mutation Testing (PITest)                                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼ (somente main)
┌─────────────────────────────────────────────────────────────┐
│  Job 3: Docker Build & Push                                 │
│  • Build da imagem Docker                                   │
│  • Push para GitHub Container Registry (ghcr.io)            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  Job 4: Deploy                                              │
│  • Trigger deploy no Render via webhook                     │
└─────────────────────────────────────────────────────────────┘
```

---

## Testar o Deploy

```bash
# Health check
curl https://ip-geolocation-service-cn07.onrender.com/actuator/health

# Login (obter token)
curl -X POST https://ip-geolocation-service-cn07.onrender.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123@"}'

# Geolocalização (substitua SEU_TOKEN)
curl "https://ip-geolocation-service-cn07.onrender.com/api/geolocation/v1/locate?ip=8.8.8.8" \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "x-device-platform: Web"
```

---

## Troubleshooting

### Erro: "Application failed to start"

- Verifique se `JWT_SECRET` está configurado
- O secret deve ter pelo menos 64 caracteres

### Cold start lento (~30s)

- Normal no plano gratuito
- Dica: Use [UptimeRobot](https://uptimerobot.com) (gratuito) para fazer ping a cada 5 min e manter o serviço "acordado"

### Ver logs

- No Render, clique em **Logs** no menu lateral para ver logs em tempo real
