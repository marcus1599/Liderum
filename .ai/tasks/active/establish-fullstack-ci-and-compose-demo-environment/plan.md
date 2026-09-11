# Plano — establish-fullstack-ci-and-compose-demo-environment

## Routing

- Domínios: `planning`, `sre`, `backend`, `frontend`, `testing`, `security`, `audit`, `documentation`.
- Agents: Planner → SRE/DevOps → Backend Developer + Frontend Developer → QA → Security → Auditor.
- Skills: `create-plan`; na execução, `test-backend`, `test-frontend`, `security-review`, `audit-task` e `finish-task` após Task Verdict APROVADO.
- Não selecionados: Arquiteto — não há decisão estrutural nova prevista; consultá-lo somente se a topologia exigir trade-off duradouro. `create-prd` — task de infraestrutura, sem nova regra de negócio. `create-migration` — V1/Flyway existentes permanecem imutáveis. `create-adr` — somente com decisão estrutural e autorização.

## Classificação

P2, LARGE, infraestrutura/qualidade. É a primeira task da Fase 6 e operacionaliza os componentes já implementados e validados isoladamente.

## Objetivo

Entregar um ambiente local de demonstração reproduzível e CI proporcional para frontend, backend e notification-service, preservando o fluxo:

`frontend → backend → PostgreSQL → RabbitMQ → notification-service`.

## Diagnóstico confirmado

- `.github/workflows/backend.yml` valida backend/H2 e Flyway em PostgreSQL 18.6, mas não frontend nem notification-service.
- `docker-compose.yml` possui RabbitMQ, backend e notification-service; não possui PostgreSQL nem frontend.
- Backend e notification-service já têm Dockerfiles; frontend tem Dockerfile/Nginx, mas não integra o Compose.
- Backend usa Flyway e `ddl-auto=validate`; não deve receber schema automático.
- Notification-service expõe status em `GET /`; o backend possui Actuator, cuja exposição pública precisa ser avaliada sem enfraquecer segurança.

## Escopo

1. Completar Compose com PostgreSQL local descartável, frontend e dependências/readiness explícitas.
2. Usar apenas variáveis e credenciais locais de desenvolvimento, documentadas sem valores reais.
3. Garantir que backend use Flyway e Hibernate validate contra o banco do Compose.
4. Garantir que o frontend tenha URL da API adequada ao ambiente Compose, sem inserir tenant/Guild como autoridade.
5. Adicionar CI proporcional para frontend e notification-service, preservando o workflow backend/Flyway existente.
6. Definir healthchecks/requisitos de disponibilidade proporcionais para os quatro serviços.
7. Atualizar README com setup, URLs e smoke de demonstração multi-tenant/mensageria.

## Fora de escopo

- deploy, CD, secrets de produção, Render/Vercel, Kubernetes, autoscaling, tracing, observabilidade ampla;
- mudanças em JWT, RBAC, tenancy, contratos HTTP, schema ou migrations;
- E2E browser/Playwright, novos recursos de produto, billing e redesign;
- upgrade de dependências ou actions apenas para eliminar warnings, salvo bloqueio comprovado.

## Estratégia técnica

### Compose

- Adicionar PostgreSQL com volume nomeado exclusivamente local, healthcheck e credenciais de desenvolvimento via placeholders seguros.
- Subir backend somente após PostgreSQL e RabbitMQ saudáveis; perfil `dev` explícito, sem segredo versionado.
- Subir notification-service após RabbitMQ saudável.
- Incluir frontend servido por Nginx e configurar a URL da API por mecanismo já suportado pelo projeto; se a imagem estática exigir mudança arquitetural, parar e escalar.
- Validar `docker compose config`, subida limpa e reset documentado apenas para o volume local nomeado.

### CI

- Manter o workflow backend e o job PostgreSQL/Flyway existentes.
- Adicionar workflow ou jobs coesos para `frontend`: instalação determinística, testes headless e build de produção.
- Adicionar workflow ou job para `notification-service`: testes unitários, package e, se proporcional e reproduzível, integração RabbitMQ descartável opt-in.
- Não usar credenciais, bancos ou brokers externos.

### Health e documentação

- Usar endpoints atuais quando suficientes; não abrir endpoint sensível apenas para healthcheck.
- Documentar portas, variáveis necessárias, comando de subida, smoke e limpeza do volume local.

## Testes e validações planejados

| Área | Evidência |
| --- | --- |
| Compose | `docker compose config` e subida com serviços saudáveis. |
| PostgreSQL | banco vazio recebe Flyway V1; Hibernate valida, sem DDL implícito. |
| RabbitMQ | producer e consumer conectam; evento é consumido no ambiente local. |
| Backend | `mvnw.cmd clean verify` permanece verde. |
| Frontend | `npm test -- --watch=false --browsers=ChromeHeadless` e `npm run build`. |
| Notification service | `mvn clean test` e package; integração RabbitMQ quando o broker descartável estiver disponível. |
| CI | Jobs executam sem segredos reais e retornam exit code 0. |
| Smoke | registro, login, `/users/me`, endpoint tenant-scoped e criação de evento. |
| Segurança | nenhum secret versionado; CORS/JWT/RBAC/tenant preservados. |

## Riscos e escalonamento

- Frontend não consegue resolver a API em runtime Docker: identificar o mecanismo mínimo compatível; não introduzir proxy/arquitetura nova sem decisão.
- Health backend exige abertura de endpoint: avaliar Security antes de qualquer mudança de autorização.
- Compose exige alterar contrato ou migration: parar e escalar.
- CI excede limites ou fica flaky: capturar evidência e reduzir apenas duplicação, não cobertura útil.
- Dados locais: operações de reset devem atingir somente o volume nomeado do Compose, nunca bancos externos.

## Critérios de conclusão

- Compose sobe frontend, backend, PostgreSQL, RabbitMQ e notification-service de maneira documentada e reproduzível.
- Backend inicia com Flyway/validate contra PostgreSQL local vazio.
- Frontend, backend e notification-service possuem gates CI proporcionais e verdes.
- Smoke local demonstra autenticação, tenant e mensageria.
- Nenhuma credencial real, mudança de segurança indevida ou dependência externa é introduzida.
- QA, Security, SRE e Auditor aprovam; Task Verdict e Release Verdict permanecem separados.

## Autorizações necessárias antes da execução

Autorização para alterar Compose, workflows GitHub Actions, documentação e configurações locais necessárias. Não requer acesso a produção.
