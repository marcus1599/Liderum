# Plano — validate-flyway-migrations-against-postgresql-in-ci

## Routing

- Domínios: `planning`, `database`, `backend`, `testing`, `security`, `sre`, `audit`, `documentation`.
- Agents: Planner → SRE/DevOps + Backend Developer → QA → Security → Auditor.
- Skills: `create-plan`; na execução, `test-backend` → `security-review` → `audit-task` → `finish-task` somente se o Task Verdict for APROVADO.
- Não selecionados: Arquiteto — não há nova tecnologia, modelo de dados ou decisão duradoura; Frontend Developer — nenhum artefato Angular; `create-prd` — tarefa técnica de infraestrutura sem regra de negócio; `create-migration` — V1 permanece imutável; `create-adr` — não necessário.
- Gate: CI PostgreSQL verde, QA, Security e SRE aprovados antes da Auditoria. Task Verdict e Release Verdict permanecem separados.

## Classificação

P1, MEDIUM, qualidade/infraestrutura. A tarefa reduz a lacuna operacional reconhecida após a recuperação de produção: V1 foi validada manualmente em PostgreSQL 18.6 local e no Render, mas não existe validação contínua de migrations PostgreSQL no CI.

## Objetivo

Fazer o GitHub Actions validar automaticamente, em PostgreSQL descartável da família 18, o fluxo:

`banco vazio → Flyway V1 → flyway_schema_history → Hibernate ddl-auto=validate → Spring context`.

O job existente de `clean verify` com H2 permanece como regressão completa; o novo job é uma prova direcionada de compatibilidade de migration com PostgreSQL, não uma duplicação integral da suíte.

## Diagnóstico confirmado

- `.github/workflows/backend.yml` hoje executa somente `./mvnw clean verify` em `ubuntu-latest`; não há service container ou datasource PostgreSQL.
- A V1 existe em `backend/src/main/resources/db/migration/V1__baseline_schema.sql`; Flyway está habilitado e `baseline-on-migrate=false`.
- `application-prod.properties` exige `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`, e usa `ddl-auto=validate`.
- `FlywaySchemaIntegrationTest` prova V1 somente em H2; não valida driver, DDL ou metadata reais do PostgreSQL.
- PostgreSQL 18.6 vazio aplicou V1 e iniciou a aplicação em validação local e produção. Flyway 9.22.3 emitiu warning de suporte testado até PostgreSQL 15, sem impedir essa execução.
- A documentação oficial do GitHub Actions suporta PostgreSQL como service container em runner Linux/Ubuntu, com health check e porta mapeada para job executado no runner. [GitHub Docs](https://docs.github.com/en/actions/tutorials/use-containerized-services/create-postgresql-service-containers)

## Escopo

1. Evoluir exclusivamente `.github/workflows/backend.yml` para preservar o job H2 existente e adicionar um job PostgreSQL dedicado, também acionado em `pull_request` para que migration inválida não seja descoberta apenas após merge.
2. Usar service container PostgreSQL descartável, pinado à linha 18 (preferencialmente imagem `postgres:18.6`, desde que disponível na execução), com database/usuário/senha não sensíveis e exclusivos do job.
3. Definir no job somente variáveis efêmeras de CI: datasource do service container, `SPRING_PROFILES_ACTIVE=prod`, `JWT_SECRET` fictício exclusivo de teste e sinalização explícita de teste PostgreSQL. Não usar URL, usuário, senha, token ou secret do Render.
4. Criar teste de integração backend dedicado, com sufixo `IT` para não entrar automaticamente no `clean verify` local, e selecioná-lo explicitamente pelo job CI. O teste deve subir o contexto com profile `prod` e datasource PostgreSQL efêmero, deixando Flyway executar V1 e Hibernate validar.
5. Validar `flyway_schema_history` (versão 1/sucesso), as seis tabelas de domínio, constraints/índices principais já definidos pela V1 e a ausência de criação de schema pelo Hibernate.
6. Registrar evidências de execução, QA, Security, SRE e auditoria conforme workflow.

## Fora do escopo

- editar V1, adicionar migration, `IF NOT EXISTS`, `baselineOnMigrate`, `repair`, `clean` ou manipulação de `flyway_schema_history`;
- acesso ao Render, banco compartilhado, dados de produção, backup ou smoke de produção;
- upgrade de Flyway, PostgreSQL driver, Spring Boot ou GitHub Actions fora de necessidade comprovada;
- Testcontainers, dependências novas, Docker Compose, CI de frontend/notification-service, RabbitMQ, deploy e observabilidade;
- alterar JWT, RBAC, tenancy, CORS, rate limit, frontend ou contratos de API.

## Estratégia técnica proposta

### Workflow

- Manter o job atual como gate H2 de regressão completa.
- Adicionar job independente `postgres-flyway` em Ubuntu, com `services.postgres`, `pg_isready` como health check e porta PostgreSQL mapeada para `localhost` do runner.
- Executar checkout, Java 21/Maven cache e o teste PostgreSQL direcionado após o container estar saudável.
- Usar o datasource configurado pelos valores dinâmicos da porta de serviço do próprio job; não assumir porta fixa nem rede de produção.
- Executar o workflow em `push` para `main` e `pull_request`, preservando o escopo de CI backend.

### Teste de integração

- Criar `PostgreSqlFlywaySchemaCiIT` (nome final sujeito ao padrão de pacote existente), em `src/test`, com profile `prod` e propriedades obtidas apenas do ambiente efêmero do workflow.
- Não incluir o sufixo `IT` na convenção padrão do Surefire; o CI o chama explicitamente com `-Dtest=<classe> test`. Assim, `clean verify` local/H2 continua reproduzível sem PostgreSQL e sem skips artificiais.
- Consultar metadata/JDBC para provar `flyway_schema_history` com V1 bem-sucedida, `guilds`, `users`, `member`, `team`, `event` e `attendance`, FKs, uniques e índices relevantes da V1.
- O contexto Spring com profile `prod` e `ddl-auto=validate` é a prova de compatibilidade JPA/schema; nenhum comando Hibernate de DDL é permitido.

## Testes e validação planejados

| Camada | Evidência |
| --- | --- |
| Local H2 | `./mvnw.cmd clean verify` preserva a suíte completa atual, sem exigir PostgreSQL local. |
| CI PostgreSQL 18 | Service inicia saudável e banco novo não possui tabelas do domínio antes de Spring/Flyway. |
| Flyway | V1 é aplicada uma vez; history registra versão `1` com sucesso. |
| Schema | Seis tabelas, FKs, uniques e índices esperados existem em PostgreSQL real. |
| Hibernate | Contexto `prod` sobe com `ddl-auto=validate`; schema não é criado pelo Hibernate. |
| Regressão CI | Job H2 atual continua `clean verify`; job PostgreSQL direcionado retorna exit code 0. |
| Segurança | Nenhuma variável real de Render ou credencial sensível aparece no YAML, logs ou artefatos. |

## Riscos e escalonamento

- **Imagem PostgreSQL 18 indisponível no runner:** parar e registrar a evidência; não substituir silenciosamente por major diferente. A pinagem menor só pode mudar para tag 18 equivalente se comprovada e documentada.
- **V1 falha no PostgreSQL vazio:** parar; capturar SQLState, statement e linha. Não editar V1, adicionar `IF NOT EXISTS`, baseline ou repair por tentativa.
- **Falha específica de Flyway 9.22.3/PostgreSQL 18:** abrir/escalar tarefa de upgrade avaliada; não atualizar dependência nesta task automaticamente.
- **Teste depende de secret/Render:** reprovar planejamento/implementação; substituir somente por valores efêmeros de CI.
- **Flakiness de service container:** revisar health check, readiness e datasource dinâmico; não adicionar retry genérico sem causa comprovada.

## Critérios de conclusão

- Pull request e `main` executam o gate PostgreSQL além do `clean verify` H2.
- PostgreSQL 18 descartável aplica V1 em banco vazio e registra history corretamente.
- Hibernate `validate` e Spring context passam usando profile `prod` e datasource do service container.
- Teste confirma estrutura relevante sem usar dados/secrets externos ou modificar banco Render.
- A suíte local existente permanece reproduzível e verde.
- QA, Security, SRE e Auditor aprovam; o warning Flyway/PostgreSQL 18 passa a ter evidência contínua, sem alegar suporte oficial além do testado.

## Dependências e ADR

- Dependências novas: nenhuma.
- Migration nova: nenhuma.
- ADR: não necessário; a task operacionaliza uma decisão já aplicada e não altera o modelo arquitetural.
- Autorizações necessárias antes de executar: autorização para modificar workflow GitHub Actions e adicionar o teste de integração PostgreSQL no backend. Não requer acesso ao Render nem secret do usuário.
