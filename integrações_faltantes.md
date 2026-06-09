# Manual de Deploy — ProjetoMTA

> Guia único para colocar o sistema em produção: TiDB Cloud (banco) + Render (API) + Vercel (PWA).
>
> **Última atualização:** Validação Etapa 5 — Revisão final (QA concluída)

---

## Resumo do projeto

| Camada      | Pasta        | Stack                          | Hospedagem   |
|-------------|--------------|--------------------------------|--------------|
| Back-end    | `backend/`   | Java 17, Spring Boot 3.2.5     | Render       |
| Front-end   | `frontend/`  | Vite 5, PWA, HTML/JS/CSS       | Vercel       |
| Banco       | —            | MySQL-compatível (TiDB Cloud)  | TiDB Cloud   |

**Protótipo visual original:** `Interface_MTA/` (referência; não é o app em produção).

---

## ⚠️ LINKS EM ABERTO — O que você precisa definir depois

O código **não fixa** URLs de produção. Preencha manualmente quando tiver os serviços no ar.

| # | Destino | Onde configurar | Variável | Valor em dev | Status produção |
|---|---------|-----------------|----------|--------------|-----------------|
| 1 | API back-end | `frontend/.env` + **Vercel** | `VITE_API_BASE_URL` | `http://localhost:8080` | 🔗 **A DEFINIR** |
| 2 | Front-end (CORS) | **Render → Environment** | `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | 🔗 **A DEFINIR** (URL Vercel) |
| 3 | Banco TiDB | **Render → Environment** | `DB_*` | — | 🔗 **A DEFINIR** |
| 4 | URL da API | Anotar após deploy Render | — | — | 🔗 **A DEFINIR** |
| 5 | URL do PWA | Anotar após deploy Vercel | — | — | 🔗 **A DEFINIR** |

### Ordem recomendada (fechar os links)

1. Criar cluster **TiDB Cloud** → executar SQL (seção 5)
2. Deploy do **back-end no Render** → anotar URL (`https://….onrender.com`)
3. Testar `GET https://….onrender.com/actuator/health` → deve retornar `{"status":"UP"}`
4. Deploy do **front na Vercel** com `VITE_API_BASE_URL` = URL do Render
5. Copiar URL da Vercel para `CORS_ALLOWED_ORIGINS` no Render e **redeploy** da API
6. Abrir o PWA, entrar como admin e validar CRM + agenda

---

## Guia rápido de deploy (passo a passo)

### Passo 1 — Banco (TiDB Cloud)

1. Acesse [tidbcloud.com](https://tidbcloud.com/) → cluster **Serverless** (gratuito)
2. Crie o banco `projeto_mta` e execute os scripts da **seção 5** (SQL Editor)
3. Anote: host, porta (`4000`), usuário, senha
4. Em **Render**, defina `DB_SSL_ENABLED=true`

### Passo 2 — API (Render)

| Campo no Render | Valor |
|-----------------|-------|
| **Root Directory** | `backend` |
| **Runtime** | Java (ou Docker — veja nota abaixo) |
| **Build Command** | `./mvnw -DskipTests package` *(ou `mvn -DskipTests package`)* |
| **Start Command** | `java -jar target/projeto-mta-0.0.1-SNAPSHOT.jar` |
| **Health Check Path** | `/actuator/health` |

> **Nota:** `backend/render.yaml` referencia `runtime: docker`, mas o repositório **não inclui Dockerfile**. Use **Native Java** no painel Render ou adicione um Dockerfile antes de usar o Blueprint.

**Variáveis obrigatórias no Render** (seção 3 completa):

```
SPRING_PROFILES_ACTIVE=prod
DB_HOST=…
DB_PORT=4000
DB_NAME=projeto_mta
DB_USERNAME=…
DB_PASSWORD=…
DB_SSL_ENABLED=true
JWT_SECRET=… (≥ 32 caracteres)
CORS_ALLOWED_ORIGINS=https://SEU-PROJETO.vercel.app
SEED_ADMIN_EMAIL=seu@email.com
SEED_ADMIN_PASSWORD=senha-forte-inicial
```

### Passo 3 — PWA (Vercel)

| Campo na Vercel | Valor |
|-----------------|-------|
| **Root Directory** | `frontend` |
| **Framework Preset** | Vite |
| **Build Command** | `npm run build` |
| **Output Directory** | `dist` |

**Variável obrigatória:**

```
VITE_API_BASE_URL=https://SEU-SERVICO.onrender.com
```

Opcional: `VITE_APP_NAME=MTA - Ministério Templo Da Adoração`

### Passo 4 — Verificação pós-deploy

| Teste | Como validar | Resultado esperado |
|-------|--------------|-------------------|
| Health | `GET /actuator/health` | `UP` |
| Agenda pública | Abrir app sem login → aba Agenda | Eventos carregam |
| Avisos públicos | Aba Avisos como visitante | Lista visível |
| Login admin | Botão **Entrar** → credenciais seed | JWT + menu admin |
| CRM | Cadastrar membro | `201 Created` |
| E-mail duplicado | Repetir mesmo e-mail | `400 REGRA_NEGOCIO` |
| Cold start | Primeira req após 15 min idle | Front mostra msg de aguarde (~30–60s) |

---

## Mapa de endpoints

Base: `{VITE_API_BASE_URL}` + caminho.

### Autenticação (público)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/api/auth/login` | Login → JWT |
| `POST` | `/api/auth/recuperar-senha` | Placeholder (sem e-mail real ainda) |

### CRM — Membros

| Método | Caminho | Acesso |
|--------|---------|--------|
| `GET` | `/api/membros?page=&size=&busca=` | Autenticado |
| `GET` | `/api/membros/{id}` | Autenticado |
| `POST` | `/api/membros` | **ADMIN** |
| `PUT` | `/api/membros/{id}` | **ADMIN** |
| `DELETE` | `/api/membros/{id}` | **ADMIN** (exclusão lógica) |

### Agenda — Eventos, escalas, avisos

| Método | Caminho | Acesso |
|--------|---------|--------|
| `GET` | `/api/eventos`, `/api/eventos/{id}` | **Público** (visitante) |
| `GET` | `/api/eventos/proximos` | **Público** |
| `POST` / `PUT` / `DELETE` | `/api/eventos` | **ADMIN** |
| `GET` | `/api/eventos/{id}/escalas` | **Público** |
| `POST` / `PUT` / `DELETE` | `/api/eventos/{id}/escalas` | **ADMIN** |
| `GET` | `/api/avisos`, `/api/avisos/{id}` | **Público** |
| `POST` / `PUT` / `DELETE` | `/api/avisos` | **ADMIN** |

**Arquivos que leem a URL da API:**

- `frontend/src/config/env.js`
- `frontend/src/api/client.js`
- `frontend/.env.example`

---

## 1. Arquitetura e custos

| Componente   | Provedor   | Plano    |
|-------------|------------|----------|
| Back-end    | Render     | Gratuito |
| Front-end   | Vercel     | Gratuito |
| Banco MySQL | TiDB Cloud | Gratuito |

**Limitações do plano gratuito Render:** serviço adormece após ~15 min sem tráfego; cold start 30–60 s. O front trata HTTP `503` e erros de rede com mensagem amigável.

---

## 2. Variáveis — TiDB Cloud

| Variável         | Descrição                         | Exemplo |
|------------------|-----------------------------------|---------|
| `DB_HOST`        | Host do cluster                   | `gateway01….tidbcloud.com` |
| `DB_PORT`        | Porta                             | `4000` |
| `DB_NAME`        | Nome do banco                     | `projeto_mta` |
| `DB_USERNAME`    | Usuário                           | `….root` |
| `DB_PASSWORD`    | Senha                             | `********` |
| `DB_SSL_ENABLED` | SSL obrigatório no TiDB           | `true` |

**JDBC (referência):**

```
jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

---

## 3. Variáveis — Back-end (Render)

| Variável | Descrição | Obrigatória | Padrão / exemplo |
|----------|-----------|-------------|------------------|
| `PORT` | Porta HTTP (Render injeta) | Sim | `8080` |
| `SPRING_PROFILES_ACTIVE` | Perfil Spring | Sim | `prod` |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | Conexão TiDB | Sim | seção 2 |
| `DB_SSL_ENABLED` | SSL na conexão | Sim (prod) | `true` |
| `JWT_SECRET` | Assinatura JWT (≥ 32 bytes) | Sim | ver comando abaixo |
| `JWT_EXPIRATION_MS` | Expiração do token (ms) | Não | `86400000` (24 h) |
| `CORS_ALLOWED_ORIGINS` | URL(s) do front Vercel | Sim | `https://….vercel.app` |
| `LOGIN_MAX_ATTEMPTS` | Tentativas antes do bloqueio | Não | `5` |
| `LOGIN_LOCK_DURATION_MIN` | Minutos de bloqueio | Não | `30` |
| `SEED_ADMIN_EMAIL` | Admin criado na 1ª execução | Recomendado | `admin@igreja.com` |
| `SEED_ADMIN_PASSWORD` | Senha inicial do admin | Recomendado | senha forte |
| `DB_POOL_MAX_SIZE` | Pool HikariCP máximo | Não | `5` |
| `DB_POOL_MIN_IDLE` | Conexões ociosas mínimas | Não | `1` |
| `DB_POOL_CONNECTION_TIMEOUT` | Timeout conexão (ms) | Não | `30000` |
| `DB_POOL_MAX_LIFETIME` | Vida máxima conexão (ms) | Não | `1800000` |
| `TOMCAT_MAX_THREADS` | Threads HTTP | Não | `50` |

### Gerar `JWT_SECRET` (PowerShell)

```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

### Health check Render

| Campo | Valor |
|-------|-------|
| Health Check Path | `/actuator/health` |
| Intervalo sugerido | 60 s |

---

## 4. Variáveis — Front-end (Vercel)

| Variável | Descrição | Obrigatória | Exemplo |
|----------|-----------|-------------|---------|
| `VITE_API_BASE_URL` | URL base da API Render | Sim | `https://….onrender.com` |
| `VITE_APP_NAME` | Nome no título/PWA | Não | `MTA - Ministério Templo Da Adoração` |

### Desenvolvimento local

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Acesse `http://localhost:5173`. API local: `http://localhost:8080` (Spring Boot).

### Build de produção

```bash
cd frontend
npm run build    # gera dist/ + sw.js + manifest
npm run preview  # testa build em http://localhost:4173
```

### PWA (instalação no celular)

1. Abra a URL da Vercel no Chrome/Safari
2. Menu do navegador → **Adicionar à tela inicial** / **Instalar app**
3. Ícones: `frontend/public/icons/icon-192.png` e `icon-512.png`
4. Temas (Claro, Escuro, Azul, Rosa): botão ⚙️ → persistidos em `localStorage` (`mta_theme`)

---

## 5. Scripts SQL — TiDB Cloud

Execute **na ordem**, no SQL Editor do TiDB. O Hibernate usa `ddl-auto: validate` em produção — as tabelas **precisam existir** antes do deploy.

### 5.1 Banco

```sql
CREATE DATABASE IF NOT EXISTS projeto_mta
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE projeto_mta;
```

### 5.2 `usuarios`

```sql
CREATE TABLE IF NOT EXISTS usuarios (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    email            VARCHAR(255) NOT NULL,
    senha_hash       VARCHAR(255) NOT NULL,
    perfil           ENUM('ADMIN', 'USUARIO') NOT NULL DEFAULT 'USUARIO',
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    tentativas_login INT          NOT NULL DEFAULT 0,
    bloqueado_ate    DATETIME     NULL,
    criado_em        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5.3 `membros`

```sql
CREATE TABLE IF NOT EXISTS membros (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nome_completo   VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NULL,
    telefone        VARCHAR(20)  NULL,
    funcao          VARCHAR(100) NULL,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    observacoes     TEXT         NULL,
    criado_em       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_membros_nome (nome_completo),
    KEY idx_membros_ativo (ativo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

> **Regra de negócio:** e-mail duplicado é bloqueado na API (`400 REGRA_NEGOCIO`). Índice único em `email` é opcional mas recomendado se todos os membros tiverem e-mail.

### 5.4 Agenda (`eventos`, `escalas`, `avisos`)

```sql
CREATE TABLE IF NOT EXISTS eventos (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    titulo          VARCHAR(255) NOT NULL,
    descricao       TEXT         NULL,
    data_inicio     DATETIME     NOT NULL,
    data_fim        DATETIME     NOT NULL,
    local           VARCHAR(255) NULL,
    criado_por      BIGINT       NULL,
    criado_em       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_eventos_data_inicio (data_inicio),
    CONSTRAINT fk_eventos_criado_por FOREIGN KEY (criado_por) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS escalas (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    evento_id       BIGINT       NOT NULL,
    membro_id       BIGINT       NOT NULL,
    funcao_escala   VARCHAR(100) NULL,
    confirmado      BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_escala_evento_membro (evento_id, membro_id),
    CONSTRAINT fk_escalas_evento FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE,
    CONSTRAINT fk_escalas_membro FOREIGN KEY (membro_id) REFERENCES membros(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS avisos (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    titulo          VARCHAR(255) NOT NULL,
    mensagem        TEXT         NOT NULL,
    prioridade      ENUM('NORMAL', 'IMPORTANTE', 'URGENTE') NOT NULL DEFAULT 'NORMAL',
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    publicado_por   BIGINT       NULL,
    criado_em       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_avisos_publicado_por FOREIGN KEY (publicado_por) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5.5 Admin inicial

**Opção A (recomendada):** `SEED_ADMIN_EMAIL` + `SEED_ADMIN_PASSWORD` no Render. Criado automaticamente na 1ª execução se o e-mail não existir.

**Opção B (SQL manual):** insira com hash BCrypt gerado localmente.

---

## 6. Regras de negócio (referência QA)

| Regra | HTTP | Código erro |
|-------|------|-------------|
| Eventos com horário sobreposto | 409 | `CONFLITO_HORARIO` |
| Fim do evento ≤ início | 400 | `REGRA_NEGOCIO` |
| Membro escalado em horário conflitante | 409 | `CONFLITO_HORARIO` |
| E-mail de membro duplicado | 400 | `REGRA_NEGOCIO` |
| 5 logins falhos | 423 | `CONTA_BLOQUEADA` |
| Credenciais inválidas | 401 | `CREDENCIAIS_INVALIDAS` |
| Sem permissão | 403 | — |
| Banco indisponível | 503 | `SERVICO_INDISPONIVEL` |

---

## 7. Desenvolvimento e testes locais

### Pré-requisitos

| Ferramenta | Versão | Uso |
|------------|--------|-----|
| JDK | 17+ | Back-end |
| Maven | 3.9+ | `mvn test` / build |
| Node.js | 18+ | Front-end |
| npm | 9+ | `npm run dev` / `build` |

### Back-end

```bash
cd backend
mvn spring-boot:run
# Perfil teste (H2): mvn test
```

Variáveis locais (opcional, `application.yml` já tem defaults para dev):

```
JWT_SECRET=changeme-local-dev-only-min-32-chars!!
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

### Front-end

```bash
cd frontend
npm install
npm run dev
```

### Suíte de testes (validação Etapa 3)

```
Tests run: 29, Failures: 0  ✅
```

Inclui: `MembroServiceTest`, `EventoServiceTest`, `MembroRepositoryIntegrationTest`, testes de controller e auth.

### Lighthouse (build local, Etapa 4)

Após `npm run build` + `npm run preview`:

| Categoria | Score |
|-----------|-------|
| Performance | 99 |
| Accessibility | 95 |
| Best Practices | 96 |
| SEO | 91 |

---

## 8. Solução de problemas

| Sintoma | Causa provável | Ação |
|---------|----------------|------|
| CORS bloqueado no browser | `CORS_ALLOWED_ORIGINS` sem URL Vercel | Adicionar URL exata (sem `/` final) e redeploy Render |
| `503 SERVICO_INDISPONIVEL` | Cold start Render ou TiDB dormindo | Aguardar 30–60 s e tentar de novo |
| `401` em todas as rotas autenticadas | JWT expirado ou `JWT_SECRET` alterado | Fazer login novamente; não trocar secret com tokens ativos |
| App sobe mas `validate` falha | Tabelas SQL não criadas | Executar seção 5 no TiDB |
| `JWT_SECRET` inválido no boot | String &lt; 32 caracteres | Gerar novo secret (seção 3) |
| PWA não instala | Manifest/HTTPS | Vercel já serve HTTPS; conferir ícones em `dist/icons/` |
| Login não bloqueia após 5 erros | Versão antiga sem `LoginAttemptRecorder` | Usar código atual (transação `REQUIRES_NEW`) |

---

## 9. Checklist final de deploy

- [ ] Cluster TiDB criado (plano gratuito)
- [ ] SQL seções 5.1 → 5.4 executado
- [ ] `JWT_SECRET` gerado e salvo com segurança
- [ ] Render: variáveis `DB_*`, `JWT_*`, `CORS_*`, `SEED_*`, `SPRING_PROFILES_ACTIVE=prod`
- [ ] Render: health check `/actuator/health`
- [ ] Render: deploy OK, URL anotada
- [ ] Vercel: `VITE_API_BASE_URL` = URL Render
- [ ] Vercel: deploy OK, URL anotada
- [ ] Render: `CORS_ALLOWED_ORIGINS` atualizado com URL Vercel + redeploy
- [ ] Teste visitante: agenda e avisos sem login
- [ ] Teste admin: login, cadastro membro, evento, aviso
- [ ] PWA instalável no celular

---

## 10. Histórico

| Etapa | Conteúdo |
|-------|----------|
| 1 | Setup monorepo `backend/` + `frontend/` |
| 2 | Auth JWT, seed admin, tabela `usuarios` |
| 3 | CRM membros |
| 4 | Agenda eventos, escalas, avisos |
| 5 | Front PWA Vite + temas + visitante |
| 6 | Front auth + CRM integrado |
| 7 | Front agenda + leitura pública |
| **Val. 1** | Segurança: JWT ≥ 32 bytes, BCrypt 12, handlers 401/403 |
| **Val. 2** | Performance: HikariCP, lazy-init prod, actuator, 503 |
| **Val. 3** | Testes: 29 JUnit/Mockito/H2, e-mail duplicado, bloqueio login |
| **Val. 4** | UX/PWA: spinners, responsivo, ícones PNG, Lighthouse |
| **Val. 5** | Este manual de deploy consolidado |

---

## Integrações ainda não implementadas (futuro)

| Item | Status |
|------|--------|
| Envio real de e-mail (recuperação de senha) | Placeholder na API |
| Refresh token JWT | Não implementado |
| Dockerfile para Render Blueprint | Opcional (`render.yaml` referencia docker) |
