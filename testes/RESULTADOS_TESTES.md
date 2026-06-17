# Resultados dos Testes — ProjetoMTA

> **Última execução:** 10/06/2026 — após integração unificada do banco de dados

---

## Resumo executivo

| Área | Resultado | Detalhe |
|------|-----------|---------|
| Integração banco (config) | ✅ **Consolidada** | Perfis `dev` / `prod` / `test` em `src/main/resources` |
| Back-end (JUnit) | ✅ **32/32 passou** | `mvn test` — BUILD SUCCESS |
| Segurança JWT (prod) | ✅ **3 novos testes** | Secret padrão bloqueado em `prod` |
| Front-end (build) | ✅ **OK** | `npm run build` + PWA |
| Pasta incorreta removida | ✅ | `backend/src/resources/` (não era lida pelo Spring) |

---

## 1. Análise — `backend/src/main/resources`

### Problema encontrado

Existia uma pasta **`backend/src/resources/`** (caminho **incorreto**). O Spring Boot **só carrega** `src/main/resources/`. O IntelliJ podia estar apontando para o YAML errado.

Além disso, o YAML na pasta incorreta continha **credenciais TiDB em texto plano** — removido. **Recomendação:** rotacione a senha do TiDB Cloud se esse arquivo chegou a ser commitado.

### Estrutura corrigida

| Arquivo | Perfil | Função |
|---------|--------|--------|
| `application.yml` | base | Datasource via env, pool HikariCP, health DB, perfil padrão `dev` |
| `application-dev.yml` | `dev` | `ddl-auto: update` — cria/atualiza tabelas automaticamente |
| `application-prod.yml` | `prod` | `ddl-auto: validate`, SSL TiDB, lazy-init |
| `application-test.yml` | `test` | H2 in-memory (`create-drop`) |
| `schema.sql` | referência | Script manual para TiDB em produção |

### Variáveis de banco (todo o projeto)

```
DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD
DB_SSL_ENABLED, DB_JDBC_URL (opcional, URL completa)
DB_POOL_MAX_SIZE, DB_POOL_MIN_IDLE
SPRING_PROFILES_ACTIVE=dev|prod|test
```

Modelo: `backend/.env.example` → copiar para `backend/.env` (ignorado pelo git).

### Fluxo por ambiente

| Ambiente | Perfil | DDL | Banco |
|----------|--------|-----|-------|
| IntelliJ local | `dev` | `update` | TiDB ou MySQL local |
| Render produção | `prod` | `validate` | TiDB (executar `schema.sql` antes) |
| Testes Maven | `test` | `create-drop` | H2 memória |

### Componentes novos

| Classe | Função |
|--------|--------|
| `DatabaseStartupLogger` | Valida conexão JDBC no boot (falha rápido se BD inacessível) |
| `JwtSecretValidator` | Bloqueia `changeme` em perfil `prod` |

---

## 2. Back-end — `mvn test`

```
Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 2.1 `JwtSecretValidatorTest` — 3 testes ✅ (novo)

| Teste | Objetivo |
|-------|----------|
| `deveAceitarSecretForteEmProducao` | Secret válido em `prod` |
| `deveRejeitarSecretPadraoEmProducao` | `changeme` → exceção em `prod` |
| `devePermitirSecretPadraoEmDesenvolvimento` | `changeme` permitido em `dev` |

### 2.2 Demais suítes (29 testes) — ✅

- `MembroServiceTest` (4)
- `EventoServiceTest` (5)
- `GlobalExceptionHandlerTest` (2)
- `MembroRepositoryIntegrationTest` (2)
- `AuthControllerIntegrationTest` (3)
- `MembroControllerIntegrationTest` (7)
- `AgendaControllerIntegrationTest` (5)
- `ProjetoMtaApplicationTests` (1)

---

## 3. Validação de segurança

| Item | Status | Evidência |
|------|--------|-----------|
| Credenciais removidas do código | ✅ | Pasta `src/resources` apagada |
| `.env` no `.gitignore` | ✅ | `backend/.gitignore` |
| JWT ≥ 32 bytes no boot | ✅ | `JwtSecretValidator` |
| JWT padrão bloqueado em `prod` | ✅ | `JwtSecretValidatorTest` |
| BCrypt strength 12 | ✅ | `SecurityConfig` |
| 401/403 JSON | ✅ | `RestSecurityHandlers` |
| Bloqueio após 5 logins | ✅ | `AuthControllerIntegrationTest` |
| E-mail duplicado membro | ✅ | `MembroServiceTest` + controller |
| Falha BD → 503 | ✅ | `GlobalExceptionHandlerTest` |
| Health sem detalhes sensíveis | ✅ | `show-details: never` |

---

## 4. Validação de banco / resiliência

| Item | Status |
|------|--------|
| HikariCP pool configurável | ✅ |
| `SELECT 1` health check pool | ✅ |
| Actuator `/actuator/health` + DB | ✅ |
| Conexão validada no startup | ✅ `DatabaseStartupLogger` |
| Lazy init em produção | ✅ |
| Conflito horário eventos | ✅ testes agenda |
| Transação login (`REQUIRES_NEW`) | ✅ bloqueio persiste |

---

## 5. Front-end — build e usabilidade

| Item | Status |
|------|--------|
| `npm run build` | ✅ |
| PWA (SW + manifest + ícones PNG) | ✅ |
| CSS carregado via `index.html` | ✅ |
| Splash / spinners de carregamento | ✅ |
| Modo visitante (agenda/avisos públicos) | ✅ |
| Temas persistidos (`localStorage`) | ✅ |
| Scripts Windows (`dev.bat`, `build.bat`) | ✅ |

### Lighthouse (referência Etapa 4)

| Categoria | Score |
|-----------|-------|
| Performance | 99 |
| Accessibility | 95 |
| Best Practices | 96 |
| SEO | 91 |

---

## 6. Como conectar ao TiDB (IntelliJ)

1. Copie `backend/.env.example` → `backend/.env`
2. Preencha `DB_HOST`, `DB_PORT=4000`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SSL_ENABLED=true`
3. **Run Configuration:**
   - Main: `com.projetomta.ProjetoMtaApplication`
   - **Active profiles:** `dev` (ou env `SPRING_PROFILES_ACTIVE=dev`)
   - **Working directory:** `backend/`
   - **Não use** `src/resources` — use apenas `src/main/resources`
4. Suba o front: `frontend/dev.bat` → `http://localhost:5173`
5. Login admin: use conta configurada em `application-admins.yml`

---

## 7. Como reexecutar

```powershell
# Back-end
$env:JAVA_HOME = "C:\Users\guife\.jdks\openjdk-24.0.2+12-54"
cd backend
mvn test

# Front-end
cd frontend
npm run build
```

---

## 8. Itens não testados automaticamente nesta rodada

| Item | Motivo |
|------|--------|
| Conexão real TiDB Cloud | Credenciais não ficam no repositório (por segurança) |
| E2E browser (Playwright) | Não implementado |
| Deploy Render/Vercel | Links em aberto — ver `integrações_faltantes.md` |

---

## 9. Histórico

| Data | Testes | Observação |
|------|--------|------------|
| 05/06/2026 | 29/29 | QA Etapas 1–5 |
| 10/06/2026 | 32/32 | Integração BD unificada + segurança JWT prod |
