# Guia de Deploy - Render.com

Este guia mostra como fazer deploy gratuito da aplicação no Render.com.

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

## Testar o Deploy

🌐 **URL de Produção:** https://ip-geolocation-service-cn07.onrender.com

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

# Guia de Deploy - IP Geolocation Service

Este guia mostra como fazer deploy gratuito da aplicação em diferentes plataformas.

## Sumário

1. [Render.com](#1-rendercom-recomendado) ⭐ Recomendado
2. [Railway](#2-railway)
3. [AWS Free Tier](#3-aws-free-tier)
4. [GitHub Actions CI/CD](#4-github-actions-cicd)

---

## 1. Render.com (Recomendado)

**Render.com** é a opção mais simples e totalmente gratuita para projetos pequenos.

### Características do Plano Gratuito

- ✅ 750 horas/mês (suficiente para 1 serviço 24/7)
- ✅ Deploy automático do GitHub
- ✅ HTTPS automático
- ✅ Domínio gratuito: `seu-app.onrender.com`
- ⚠️ Serviço "dorme" após 15 min de inatividade (cold start ~30s)

### Passo a Passo

#### 1.1 Criar Conta

1. Acesse [render.com](https://render.com)
2. Clique em "Get Started for Free"
3. Faça login com sua conta GitHub

#### 1.2 Criar Web Service

1. No dashboard, clique em **"New +"** → **"Web Service"**
2. Conecte seu repositório GitHub `ip-geolocation-service`
3. Configure:

| Campo         | Valor                              |
| ------------- | ---------------------------------- |
| Name          | `ip-geolocation-service`           |
| Region        | `Oregon (US West)` ou mais próximo |
| Branch        | `main`                             |
| Runtime       | `Docker`                           |
| Instance Type | `Free`                             |

#### 1.3 Configurar Variáveis de Ambiente

Em **Environment Variables**, adicione:

```
JWT_SECRET=sua-chave-secreta-de-pelo-menos-64-caracteres-para-jwt-hs384
SPRING_PROFILES_ACTIVE=prod
```

> 💡 Gere uma chave segura: `openssl rand -base64 64`

#### 1.4 Deploy

1. Clique em **"Create Web Service"**
2. Aguarde o build (~3-5 minutos)
3. Sua aplicação estará disponível em `https://ip-geolocation-service-cn07.onrender.com`

#### 1.5 Configurar Deploy Automático (Webhook)

1. No Render, vá em **Settings** → **Deploy Hook**
2. Copie a URL do webhook
3. No GitHub, vá em **Settings** → **Secrets and variables** → **Actions**
4. Crie um secret: `RENDER_DEPLOY_HOOK_URL` com a URL copiada

Agora o deploy será automático a cada push na `main`!

---

## 2. Railway

**Railway** é outra opção gratuita com interface amigável.

### Características do Plano Gratuito

- ✅ $5/mês em créditos (suficiente para projetos leves)
- ✅ Deploy automático do GitHub
- ✅ HTTPS automático
- ✅ Não "dorme" como o Render

### Passo a Passo

#### 2.1 Criar Conta

1. Acesse [railway.app](https://railway.app)
2. Faça login com GitHub

#### 2.2 Criar Projeto

1. Clique em **"New Project"**
2. Selecione **"Deploy from GitHub repo"**
3. Escolha `ip-geolocation-service`

#### 2.3 Configurar Variáveis

Em **Variables**, adicione:

```
JWT_SECRET=sua-chave-secreta-aqui
SPRING_PROFILES_ACTIVE=prod
PORT=8080
```

#### 2.4 Gerar Domínio

1. Vá em **Settings** → **Networking** → **Generate Domain**
2. Sua URL será algo como `ip-geolocation-service-production.up.railway.app`

---

## 3. AWS Free Tier

**AWS EC2** oferece 12 meses gratuitos de uma instância t2.micro.

### Características do Free Tier

- ✅ 750 horas/mês de EC2 t2.micro
- ✅ 30 GB de armazenamento EBS
- ⚠️ Requer cartão de crédito (validação, não cobra)
- ⚠️ Mais complexo de configurar

### Passo a Passo

#### 3.1 Criar Conta AWS

1. Acesse [aws.amazon.com](https://aws.amazon.com)
2. Crie uma conta (requer cartão de crédito para validação)
3. Selecione o plano **Free Tier**

#### 3.2 Criar Instância EC2

1. Acesse **EC2 Dashboard** → **Launch Instance**
2. Configure:

| Campo         | Valor                                 |
| ------------- | ------------------------------------- |
| Name          | `ip-geolocation-service`              |
| AMI           | `Amazon Linux 2023`                   |
| Instance type | `t2.micro` (Free tier)                |
| Key pair      | Crie uma nova (salve o arquivo .pem!) |

3. Em **Network settings**, habilite:
   - ✅ Allow SSH traffic (porta 22)
   - ✅ Allow HTTP traffic (porta 80)
   - ✅ Allow HTTPS traffic (porta 443)
   - Adicione regra para porta 8080

4. Clique em **Launch instance**

#### 3.3 Conectar via SSH

```bash
# Dar permissão ao arquivo .pem
chmod 400 sua-chave.pem

# Conectar
ssh -i sua-chave.pem ec2-user@<IP-PUBLICO>
```

#### 3.4 Instalar Docker

```bash
# Atualizar sistema
sudo yum update -y

# Instalar Docker
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Relogar para aplicar permissões
exit
# Conecte novamente via SSH
```

#### 3.5 Deploy da Aplicação

```bash
# Baixar imagem do GitHub Container Registry
docker pull ghcr.io/seu-usuario/ip-geolocation-service:latest

# Executar
docker run -d \
  --name geolocation \
  -p 8080:8080 \
  -e JWT_SECRET=sua-chave-secreta-aqui \
  -e SPRING_PROFILES_ACTIVE=prod \
  --restart unless-stopped \
  ghcr.io/seu-usuario/ip-geolocation-service:latest
```

#### 3.6 Configurar Elastic IP (opcional)

Para ter um IP fixo:

1. EC2 → **Elastic IPs** → **Allocate Elastic IP address**
2. Selecione o IP → **Actions** → **Associate Elastic IP address**
3. Associe à sua instância

Sua aplicação estará em `http://<ELASTIC-IP>:8080`

---

## 4. GitHub Actions CI/CD

O pipeline de CI/CD já está configurado em `.github/workflows/ci.yml`.

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
│  Job 4: Deploy (se configurado)                             │
│  • Trigger deploy no Render via webhook                     │
└─────────────────────────────────────────────────────────────┘
```

### Ativar o Pipeline

1. **Faça push para o GitHub**:

```bash
git add .
git commit -m "feat: add CI/CD pipeline"
git push origin main
```

2. **Verifique no GitHub**:
   - Vá em **Actions** no seu repositório
   - Você verá o pipeline executando

### Configurar Secrets (se necessário)

Em **Settings** → **Secrets and variables** → **Actions**:

| Secret                   | Descrição                           |
| ------------------------ | ----------------------------------- |
| `RENDER_DEPLOY_HOOK_URL` | URL do webhook do Render (opcional) |

> 📝 O `GITHUB_TOKEN` é automático, não precisa configurar.

---

## Comparativo das Opções

| Plataforma  | Facilidade | Custo           | Cold Start           | Setup  |
| ----------- | ---------- | --------------- | -------------------- | ------ |
| **Render**  | ⭐⭐⭐⭐⭐ | Grátis          | 30s após inatividade | 5 min  |
| **Railway** | ⭐⭐⭐⭐   | $5 crédito/mês  | Nenhum               | 5 min  |
| **AWS EC2** | ⭐⭐       | Grátis 12 meses | Nenhum               | 30 min |

### Recomendação

Para começar rapidamente: **Render.com**

Se precisar de uptime constante: **Railway** ou **AWS EC2**

---

## Testar o Deploy

Após o deploy, teste os endpoints:

```bash
# Health check
curl https://seu-app.onrender.com/actuator/health

# Login
curl -X POST https://seu-app.onrender.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123@"}'

# Geolocalização (com token)
curl https://seu-app.onrender.com/api/geolocation/v1/locate?ip=8.8.8.8 \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "x-device-platform: Web"
```

---

## Troubleshooting

### Erro: "Application failed to start"

- Verifique se `JWT_SECRET` está configurado
- O secret deve ter pelo menos 64 caracteres para HS384

### Erro: "Port already in use"

- Verifique se a variável `PORT` está configurada
- No Render/Railway, a porta é definida automaticamente

### Cold start lento no Render

- Normal no plano gratuito (~30s)
- Considere usar um serviço de health check como [UptimeRobot](https://uptimerobot.com) para manter o serviço "acordado"
