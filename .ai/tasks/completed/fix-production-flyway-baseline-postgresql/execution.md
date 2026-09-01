# Execução — Production Flyway Recovery

## Causa e decisão operacional

- Causa raiz confirmada: SQLState `42P07`; `V1__baseline_schema.sql`, linha 1, `CREATE TABLE guilds (...)`; PostgreSQL: `relation "guilds" already exists`.
- O banco Render contém schema legado criado antes do Flyway e não possui `flyway_schema_history` correspondente.
- Dados legados são descartáveis. A estratégia aprovada é reprovisionar banco novo/vazio, criado exclusivamente por Flyway.
- Descartados: baseline do legado, `baselineOnMigrate`, `IF NOT EXISTS`, repair, edição manual do history e migration de reconciliação.
- V1 permanece imutável salvo incompatibilidade real comprovada em PostgreSQL vazio.

## Reprodução local

- Executada em PostgreSQL 18.6 (imagem oficial `postgres:18`), container descartável `liderum-flyway-pg18-validation`, porta local exclusiva e sem volume persistente.
- Antes do startup: `information_schema` confirmou zero tabelas entre `guilds`, `users`, `member`, `team`, `event`, `attendance` e `flyway_schema_history`.
- Backend iniciado com profile `prod`, datasource local temporário, JWT local efêmero, porta isolada e `ddl-auto=validate`.
- Flyway 9.22.3 detectou schema vazio, criou `flyway_schema_history` e aplicou V1 com sucesso em PostgreSQL 18.6.
- Histórico: versão `1`, `success = true`.
- Schema: as seis tabelas esperadas foram criadas; consulta confirmou 18 constraints PK/FK/unique e os 7 índices tenant previstos na V1.
- Hibernate: `LocalContainerEntityManagerFactoryBean` inicializou e o Spring Boot concluiu startup em profile `prod`; não houve DDL Hibernate no log.
- Smoke local: registro de Guild, login e `/users/me` passaram. `/actuator/health` respondeu 403 porque a configuração atual protege esse endpoint.
- O warning Flyway 9.22.3/PostgreSQL 18.6 permaneceu, mas não impediu a V1 neste cenário. Não houve upgrade.
- O container e a instância Java temporários foram encerrados após a validação.

## Suite backend

- `./mvnw.cmd clean verify` foi iniciado após a validação, mas a captura de terminal não entregou BUILD/exit code final verificável nesta sessão. Não é declarado aprovado por esta execução; a evidência backend anterior permanece separada.

## Produção

- O banco legado `liderum-bd` foi removido pelo usuário após declarar seus dados descartáveis. Nenhuma tabela, schema ou histórico Flyway foi manipulado manualmente.
- Um novo `liderum-bd` free foi provisionado em Oregon com PostgreSQL 18.6, sem dados de domínio.
- As variáveis de conexão do serviço `Liderum` foram atualizadas seletivamente, preservando as demais variáveis. A primeira tentativa usou URL PostgreSQL sem prefixo JDBC e falhou antes de executar Flyway (`URL must start with 'jdbc'`); a única correção operacional foi ajustar `DB_URL` para o formato JDBC.
- Deploy final `dep-da7ir2hsrm7s73ab02f0`: `live` em 2026-08-26. Logs confirmam profile `prod`, schema inicialmente vazio, criação de `flyway_schema_history`, V1 aplicada com sucesso e Spring/Tomcat iniciado na porta 10000.
- O log de produção confirma PostgreSQL 18.6 e o warning conhecido de Flyway 9.22.3 (suporte testado até PostgreSQL 15). A migration executou apesar do warning; não houve upgrade de dependência.
- A consulta SQL read-only via MCP não pôde ser usada para inspeção adicional porque o conector não negociou TLS com o PostgreSQL do Render. A evidência de histórico/migration vem dos logs de produção do Flyway.

## Smoke de produção

- Passaram: `POST /auth/register-guild` (201), login (token emitido), `GET /users/me` (200) e `GET /members` autenticado/tenant-scoped (200). Foram usados somente dados descartáveis de smoke.
- `GET /actuator/health` retorna 403. O serviço Render não possui `healthCheckPath` configurado, portanto o endpoint protegido não bloqueou o deploy, mas permanece uma pendência operacional separada.
- CORS: preflight de origem não permitida retornou 403 sem cabeçalhos CORS. O ambiente Render não possui `CORS_ALLOWED_ORIGINS` configurada, portanto a configuração atual é restritiva e não comprova acesso cross-origin do frontend real.
- Rate limiting: o ambiente não possui override de `REGISTRATION_RATE_LIMIT_LIMIT`, cujo default é 5. Ainda assim, seis registros consecutivos de smoke retornaram 201, sem 429. Isso é um achado de segurança/operacional: o limiter process-local usa `request.getRemoteAddr()` e não foi comprovado como eficaz atrás do proxy do Render. Não foi corrigido nesta task.

## Situação dos gates

- Recuperação Flyway/PostgreSQL: aprovada empiricamente.
- QA/Security/SRE/Auditor final: pendentes de reavaliação, pois o smoke revelou limitação não comprovada do rate limiting e CORS de produção sem origin configurada.
- Release Verdict: permanece **BLOQUEADO** até decisão sobre os dois achados acima. A task não foi finalizada.
