# Execution — secure-staff-user-activation-flow

## Estado

Implementação iniciada, mas ainda não concluída nem aprovada pelos gates.

## Implementado até o momento

- estados `PENDING`, `ACTIVE` e `DISABLED` no modelo User;
- entidade/repositório de activation token com hash SHA-256, token aleatório de 32 bytes, expiração de 24 horas, uso único e revogação;
- criação administrativa sem uso de senha, com resposta de ativação única;
- endpoint público `/auth/activate` com BCrypt e transação;
- contas existentes/onboarding inicial explicitamente `ACTIVE`;
- `CustomUserDetails.isEnabled()` limitado a `ACTIVE`;
- migration incremental V2, sem alteração da V1;
- frontend `/activate`, status de usuário e remoção da senha do formulário administrativo.

## Validação pendente

- Maven não concluiu porque processos/arquivos preexistentes bloqueiam `backend/target/classes` e `backend/target/maven-status` (`Acesso negado`);
- Angular build falhou por `spawn EPERM` do esbuild;
- suíte frontend foi interrompida sem resultado final verificável após ausência de progresso do ChromeHeadless;
- testes direcionados, `clean verify`, PostgreSQL/Flyway, QA, Security, Auditoria e `finish-task` ainda não podem ser executados como gates aprovados.

## Retomada 2026-09-15

- `mvnw -DskipTests compile`: BUILD SUCCESS após correção do import `HexFormat`.
- Contextos default/dev/prod direcionados: 3 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS.
- `npm run build`: SUCCESS; warnings existentes de Sass/budgets permaneceram não bloqueantes.
- Frontend: 35/35 SUCCESS após tratar rejeição assíncrona da Clipboard API.
- `GuildOnboardingIntegrationTest`: 4 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS.
- Correções aplicadas dentro do escopo: seed dev marcado ACTIVE, compatibilidade JSON da resposta de provisionamento, login de contas não ACTIVE retorna 401 e tokens são removidos antes de User.
- A primeira suíte completa ainda registrou falhas porque foi executada antes dessas correções e contra helpers de teste que não ativavam Staff PENDING; os helpers foram então adaptados ao fluxo real.

## Decisão operacional

Não alterar V1, não fazer commit/push e não finalizar a task enquanto `clean verify` final, PostgreSQL V1→V2, QA, Security e Auditoria não produzirem resultados verificáveis.

## Gates finais — 2026-09-15

- Testes direcionados após as correções: 13 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS.
- `backend\\mvnw.cmd clean verify`: 66 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS.
- Frontend: segunda execução `npm test -- --watch=false --browsers=ChromeHeadless`: 35/35 SUCCESS. Build de produção já havia passado após a última alteração frontend.
- Correções de testes finais: mock de `UserActivationTokenRepository`; helpers Event/Attendance ativam Staff PENDING antes do login e esperam o contrato HTTP 204 de ativação.
- PostgreSQL V1→V2 real não foi executado nesta sessão: Docker Engine indisponível (`dockerDesktopLinuxEngine` não encontrado). O workflow existente valida apenas V1 e não cobre V2 nem o cenário de upgrade exigido.
- Inspeção do código confirmou que o endpoint autenticado de regeneração previsto no PRD/plan ainda não existe. Portanto os cenários de regeneração/revogação por endpoint não podem ser aprovados.

### Decisão dos gates

QA e Security permanecem bloqueados por critérios obrigatórios não comprovados (PostgreSQL V1→V2/upgrade e regeneração). A task continua ativa. `finish-task` não executado.

## Retomada final — 2026-09-15

- Implementado `POST /users/{id}/activation`, restrito a MARECHAL/GENERAL, usuário PENDING da Guild atual e hierarquia existente; emissão revoga tokens ativos anteriores.
- Frontend adicionou ação de regeneração/cópia do link para usuários PENDING.
- `/activate` remove a query string via `Location.replaceState` após capturar o token; token permanece somente em memória.
- PostgreSQL 18.6 fresh: 1 teste PostgreSQL passou, V1 e V2 aplicadas, Hibernate validate aprovado, warning de suporte do Flyway 9.22.3 registrado como não bloqueante.
- Upgrade: o ensaio com schema legado sem histórico foi rejeitado corretamente pelo Flyway; o teste de upgrade completo ainda não está automatizado/comprovado sem alterar o harness existente.
- Backend final após alterações: 66 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS.
- Frontend após alterações: 35/35 SUCCESS; production build SUCCESS.

### Gate atual

A task permanece ativa porque falta evidência automatizada do upgrade V1→V2 com User existente e testes direcionados do endpoint de regeneração (incluindo cross-tenant, ACTIVE/DISABLED, revogação e replay).

## Retomada — 2026-10-07

- O bloqueio de escrita em `backend/target` foi liberado: `clean verify` limpou e recriou `target`, compilou produção/testes e concluiu sem erros de acesso. A configuração global de `JAVA_HOME` permanece intocada; o Maven foi invocado com o JDK instalado selecionado apenas no processo.
- `RbacUserTenantBoundariesIntegrationTest`: 14 testes, 0 failures, 0 errors, 0 skipped; inclui regeneração MARECHAL/GENERAL, hierarquia/RBAC, isolamento cross-Guild, status ACTIVE/DISABLED, revogação do token anterior, novo token, hash persistido, ausência de senha na resposta e replay recusado.
- Backend `clean verify`: 70 testes, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS`.
- Frontend `npm test -- --watch=false --browsers=ChromeHeadless`: 35/35 SUCCESS. Uma primeira tentativa no sandbox retornou `spawn EPERM`; repetição autorizada conseguiu iniciar ChromeHeadless e concluiu normalmente.
- Frontend `npm run build`: SUCCESS; warnings/depreciações Sass e budgets existentes permaneceram não bloqueantes.
- Novo `PostgreSqlFlywayUpgradeCiIT` implementa V1 → inserção de Guild/User MARECHAL legado com senha BCrypt → V2, checagem de preservação/status/FK/índice → contexto `prod` com Hibernate `validate` e autenticação. O job `postgres-flyway` foi atualizado para executar os testes fresh-schema e upgrade. Os fontes de teste, incluindo o novo `*CiIT`, compilam (`-DskipTests test-compile`: BUILD SUCCESS).
- **Execução PostgreSQL real pendente:** `docker version`/`docker info` confirmaram cliente Docker 29.5.3, mas o Engine `desktop-linux` não está disponível; também não há serviço PostgreSQL local ativo/ouvindo na porta 5432. O teste CI não foi executado localmente nem há resultado remoto verificável nesta retomada. Não foram usados bancos existentes.
- Nenhuma alteração foi feita em commits; `052a5ea` permanece intocado.

### Gates desta retomada

- PostgreSQL descartável inicializado em Docker Desktop 4.77.0/Engine 29.5.3, imagem `postgres:18.6`, sem volume. Banco começou vazio (0 tabelas públicas).
- Execução final combinada: `-Dtest=PostgreSqlFlywaySchemaCiIT,PostgreSqlFlywayUpgradeCiIT test` — 2 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS. PostgreSQL identificou a migration Flyway 9.22.3 e emitiu warning de suporte testado até PG15; ambas as migrations passaram em PG18.6.
- Fresh: V1/V2 aplicadas, 7 tabelas/constraints/índices verificados pelo teste, Hibernate `validate` e contexto Spring `prod` aprovados.
- Upgrade: schema isolado iniciou vazio; V1 aplicada; Guild e User legado MARECHAL inseridos; V2 aplicada; status ACTIVE, ID/Guild e hash BCrypt preservados; contexto servlet `prod` subiu em porta aleatória e autenticação passou.
- Um primeiro ensaio combinado encontrou defeito no harness de teste (`WebApplicationType.NONE` não fornecia `AuthenticationConfiguration`). O harness foi ajustado para `SERVLET`/`server.port=0`; teste upgrade isolado e execução combinada final passaram. Produção não foi alterada por esse ajuste.
- QA: APROVADO. Security: Task Security Verdict APROVADO; Release Verdict APROVADO. SRE/DevOps: APROVADO. Auditor: Task Verdict APROVADO; Release Verdict APROVADO.
- `052a5ea` continua HEAD e intacto. Nenhum staging, commit ou push. O container descartável foi encerrado e removido.
- `finish-task`: gates satisfeitos; finalizar movendo os artefatos preservados para `tasks/completed/` e atualizando handoff/state.

## Suplemento final — 2026-10-07

- Após revisão dos critérios do PRD/plan, foram adicionados os dois cenários que ainda não tinham evidência direta: token inválido/expirado sem alteração da conta PENDING e duas ativações concorrentes do mesmo token com somente um sucesso.
- Testes direcionados finais: `RbacUserTenantBoundariesIntegrationTest` e `UserActivationConcurrencyIntegrationTest`; 16 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`.
- Backend final: `.\mvnw.cmd clean verify`; 72 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`, exit code 0.
- PostgreSQL descartável final: PostgreSQL 18.6 sem volume, banco/schema inicialmente vazio; execução combinada de `PostgreSqlFlywaySchemaCiIT` e `PostgreSqlFlywayUpgradeCiIT`: 2/2, sem falhas/erros/ignorados. Fresh V1/V2, Hibernate `validate`, contexto `prod`, e upgrade V1 → Guild/User MARECHAL existente → V2 com dados e autenticação preservados aprovados. O teste subiu contexto servlet em porta aleatória após ajuste somente no harness.
- Frontend sem alteração desde a validação: suíte 35/35 em duas execuções consecutivas e `npm run build` aprovado.
- O aviso conhecido de suporte declarado pelo Flyway 9.22.3 até PostgreSQL 15 permanece registrado; PostgreSQL 18.6 passou nos cenários exercitados. Nenhuma atualização de dependência foi feita.
- Gate final após as novas evidências: QA APROVADO; Security — Task Security Verdict APROVADO; SRE/DevOps APROVADO; Auditor — Task Verdict APROVADO e Release Verdict APROVADO.
- O container descartável `liderum-activation-pg18-final-20261007` foi parado e removido. Nenhum banco ou serviço preexistente foi tocado.
- O diff contém alterações acumuladas de outras tasks (incluindo evolução de CI PostgreSQL, isolamento Event/Attendance, shell frontend e roadmap de produto); elas não são absorvidas por esta finalização. Nenhum arquivo foi staged. `052a5ea` continua HEAD, inalterado; nenhum commit ou push foi feito.
