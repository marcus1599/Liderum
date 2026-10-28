# Liderum — Current Handoff

> Atualizado pelo workflow de finalização de tasks.

## Estado atual

As seis fases do roadmap original estão concluídas. As tasks UX/UI incrementais elegíveis foram executadas sequencialmente; `normalize-domain-page-visual-rhythm` foi aprovada, arquivada e commitada em `24d647a50205a49487c60374ad76172303382833`, sem push. QA APROVADO COM LIMITAÇÕES e Auditor Task Verdict APROVADO. Angular 119/119, build SUCCESS com avisos preexistentes; alteração somente scoped em Attendance. Capturas não exportadas; estados populados/loading/erro/sucesso, WCAG completa, toque/zoom e RBAC por papel não comprovados. A task 6 de padrões reutilizáveis é condicional e não elegível: o inventário não encontrou repetição/custo de manutenção que justifique abstração. Não há task ativa. A direção visual canônica B está em [[docs/liderum-visual-direction.md]]. A iniciativa RabbitMQ está encerrada administrativamente com limitações; o Release Verdict global permanece BLOQUEADO pelo smoke de produção não executado.

Atualização 2026-10-27: a task separada `contextualize-attendance-loading-feedback` recebeu QA APROVADO COM LIMITAÇÕES e Auditor Task Verdict APROVADO COM LIMITAÇÕES. Attendance agora usa loading e feedback por membro sem overlay global nas chamadas da rota, mantendo o padrão global em Dashboard/autenticação. Angular 132/132, build exit 0, Impeccable detector `[]` e inspeção renderizada local com 100 membros sintéticos em desktop/mobile. Sem backend real, WCAG/leitor de tela completos ou toque/zoom; warning CSS Attendance +102 bytes. Task arquivada e commitada localmente em `5b0226325e2e64a2331e8bfa2adb2058a80061cd`, sem push. O roadmap UX/UI anterior continua COMPLETED, sem próxima task elegível. Release global RabbitMQ segue BLOQUEADO.

Atualização após autorização do proprietário: a task fora do roadmap `decouple-team-leadership-from-user-status` está concluída e arquivada. Liderança é uma marcação tática temporária para GvG e pode ser atribuída a qualquer Member atualmente associado ao mesmo Team, sem dependência de User, status de conta ou GuildRole; mudanças de associação limpam a marca antiga. Backend `clean verify` 95/95; Angular 122/122 antes do ajuste final de acessibilidade, focused 26/26 depois e build final SUCCESS. QA APROVADO COM LIMITAÇÕES, Security APROVADO, Auditor Task Verdict APROVADO COM LIMITAÇÕES. Migração PostgreSQL e inspeção visual renderizada não foram executadas. Commits locais isolados: implementação/testes `3c22f61`, documentação/governança `02d09a0`, sem push. Roadmap UX/UI continua COMPLETED; não há task ativa ou próxima task elegível. Release global continua BLOQUEADO pelo achado RabbitMQ independente.

## Última task concluída

- Nome: `contextualize-attendance-loading-feedback`
- Resultado: feedback contextual por membro em `/attendance`, com padrão global preservado em outras rotas. Testes Angular 132/132, build exit 0, Impeccable detector `[]`; inspeção renderizada com fixture sintética de 100 membros em 1280×800 e 390×844. QA APROVADO COM LIMITAÇÕES, Auditor Task Verdict APROVADO COM LIMITAÇÕES. Sem backend real, WCAG/leitor de tela completos e toque/zoom; warning CSS +102 bytes. Commit local `5b0226325e2e64a2331e8bfa2adb2058a80061cd`, sem push. Release Verdict global BLOQUEADO pelo RabbitMQ independente.
- Data: 2026-10-27
- Referência: [[tasks/completed/contextualize-attendance-loading-feedback/audit.md]]

- Nome: `decouple-team-leadership-from-user-status`
- Resultado: liderança de Team vinculada a Member atual do próprio grupo como marca tática temporária de GvG; criação sem líder, elegibilidade sem relação com status/conta/role, limpeza atômica em move/remove/delete, preservação de RBAC/tenant e migração V5 com histórico legado inerte. Backend `clean verify` 95/95; Angular completa 122/122 antes do ajuste final e testes focados 26/26 depois; build final SUCCESS. QA APROVADO COM LIMITAÇÕES, Security APROVADO, Task Verdict APROVADO COM LIMITAÇÕES. PostgreSQL e inspeção visual não comprovados. Commits locais isolados: implementação/testes `3c22f61`, documentação/governança `02d09a0`; sem push. Release Verdict global continua BLOQUEADO pelo RabbitMQ preexistente.
- Data: 2026-10-23
- Referência: [[tasks/completed/decouple-team-leadership-from-user-status/audit.md]]

- Nome: `normalize-domain-page-visual-rhythm`
- Resultado: inventário renderizado de Dashboard, Members, Teams, Events, Attendance e Users a 1280×800/390×844; ajuste CSS localizado em Attendance. Angular 119/119, build SUCCESS com avisos preexistentes, Impeccable layout `[]`; QA APROVADO COM LIMITAÇÕES e Auditor Task Verdict APROVADO. Estados vazios observados; screenshots não exportados e estados populated/loading/erro/sucesso, WCAG completa, toque/zoom e RBAC por papel não comprovados. Release Verdict global BLOQUEADO pelo RabbitMQ preexistente e não relacionado. Commit isolado `24d647a50205a49487c60374ad76172303382833`, sem push.
- Data: 2026-10-23
- Referência: [[tasks/completed/normalize-domain-page-visual-rhythm/audit.md]]

- Nome: `harmonize-login-and-activation-presentation`
- Resultado: Login/Ativação harmonizados visualmente sem alterar autenticação, token, contratos, rotas, RBAC ou tenant. QA APROVADO COM LIMITAÇÕES; Task Verdict APROVADO; Release Verdict global BLOQUEADO pelo RabbitMQ preexistente e não relacionado. Angular 119/119, build exit 0 com avisos preexistentes Sass/budget, Impeccable `[]`; inspeção renderizada em 1280×800/390×844 sem overflow e com erros/loading/foco/reduced-motion observados. WCAG completa e teste contra backend real não realizados. Commit local isolado `f2e9f68a9347efa38a2018237bfb01a3b9791bfe`; sem push.
- Data: 2026-10-23
- Referência: [[tasks/completed/harmonize-login-and-activation-presentation/audit.md]]

- Nome: `explain-empty-dashboard-analytics`
- Resultado: estados vazios do Dashboard distinguem ausência de eventos, eventos sem registros de presença e ausência de membros; taxa sem denominador é `—`, contagens zero permanecem. Sem mudança de cálculo, endpoint, RBAC ou tenant. Angular 111/111 e build SUCCESS; QA APROVADO COM LIMITAÇÕES, Auditor Task Verdict APROVADO. Inspeção visual renderizada/autenticada em 1280×800 e 390×844 não realizada por ausência de fixture segura; WCAG completa não reivindicada. Commit local isolado pendente; sem push. Release Verdict global continua BLOQUEADO pelo RabbitMQ preexistente.
- Data: 2026-10-23
- Referência: [[tasks/completed/explain-empty-dashboard-analytics/audit.md]]

## Task anterior concluída

- Nome: `clarify-team-assignment-and-leadership-flow`
- Resultado: atribuição membro→grupo e troca de liderança contextualizadas em `/teams`, sem mudar RBAC, tenant, contratos ou lógica de negócio. Angular 107/107 e build SUCCESS; QA APROVADO COM LIMITAÇÕES, Auditor Task Verdict APROVADO; inspeção visual autenticada/390px não realizada. Commit local: `8aa771a615c9904a534fe96de0baf61ad3d165a2` (sem push). Release Verdict global continua BLOQUEADO pelo achado RabbitMQ preexistente.
- Data: 2026-10-23
- Referência: [[tasks/completed/clarify-team-assignment-and-leadership-flow/audit.md]]

## Encerramento administrativo recente

- `validate-production-rabbitmq-event-flow` foi encerrada administrativamente em 2026-10-23 como `CLOSED_WITH_LIMITATIONS`, sem `finish-task`, em [[tasks/closed/validate-production-rabbitmq-event-flow/closure.md]]. O último preflight encontrou 0 consumers e as filas vazias; nenhum Event foi criado e o smoke de produção não foi executado. QA, Security e Auditoria não foram executados nesta task; nenhum veredito técnico ou de release foi aprovado. Release Verdict permanece BLOQUEADO.

## Task ativa

- Nenhuma task ativa após o arquivamento de `contextualize-attendance-loading-feedback`. Roadmap UX/UI incremental `COMPLETED`; a etapa condicional de padrões reutilizáveis continua `NOT_APPLICABLE`, pois o inventário não encontrou benefício concreto para extração. Esta task de feedback foi autorizada separadamente; não iniciar outra task ou roadmap automaticamente.

## Validações recentes

- Desativação/reativação de Staff: backend `clean verify` 86 testes, 0 failures/errors/skipped, BUILD SUCCESS; Angular 73/73 e build SUCCESS. QA, Security e Auditor aprovaram a task; release global segue bloqueado pelo achado RabbitMQ preexistente.
- Reassociação de líder de Team: backend `clean verify` 92 testes, 0 failures/errors/skipped, BUILD SUCCESS; teste direcionado `TeamServiceImplTest` 6/6; Angular 79/79 e production build SUCCESS. QA, Security e Auditor aprovaram; `git diff --check` sem erros de whitespace.
- Rollout password change/JWT: Render confirmou revisão nova `live` e anterior `deactivated`; V4 `success=true`, PostgreSQL 18.6, health 200/UP. Smoke de registro/login/perfil/tenant/troca de senha e revogação passou; CI backend/PostgreSQL/fullstack da revisão exata aprovado. Teste direto de JWT legado assinado em produção não executado e não apresentado como tal; aceite equivalente autorizado.
- Task de implementação password change: `clean verify` backend 81 testes; PostgreSQL 18.6 descartável fresh V1–V4 e upgrades V1/V3→V4 3/3; frontend 70/70 e production build SUCCESS. Container de teste foi removido.
- Backend V3: `clean verify`, 74 testes, 0 failures/errors/skipped, BUILD SUCCESS, exit code 0; H2 direcionado 3/3.
- PostgreSQL 18.6 descartável: fresh V1→V2→V3 e upgrade V1→User legado→V2→V3 aprovados (2/2), Hibernate `validate` e contexto `prod` iniciados. Container removido após o teste; Flyway warning de suporte testado até PostgreSQL 15 permanece como dívida técnica não bloqueante.
- Frontend da task de feedback HTTP: 65/65 testes ChromeHeadless e production build SUCCESS, ambos exit 0; `git diff --check` sem erros. Warnings Sass/CSS e browser mapping continuam não bloqueantes. Validação visual manual desktop/handset não executada.
- Git: password change versionado em commits separados para implementação/testes e artefatos `.ai`; a execução daquela task não incluiu push ou deploy. O feedback HTTP foi versionado anteriormente em `2e60125` e `c74e528`.

## Bloqueadores e pendências

- O AMQP 406 foi causado pela fila persistida sem argumentos, divergente do DLX exigido pelo código. A topologia foi recriada e nenhum novo 406 foi observado. QA, Security e SRE/DevOps passaram para a task concluída; Release Verdict continua BLOQUEADO porque não foi exercitado um único fluxo EventService→consumer no broker de produção. A confirmação do proprietário é Discord desabilitado e webhook configurada; habilitar Discord exige nova revisão de destino global. Deduplicação permanece em memória e a entrega best-effort não é garantida. Rollback para código anterior a `uid`/`cv` pode reabilitar aceitação de JWT revogado e exige nova revisão Security.
- Task de feedback HTTP sem bloqueador de gate. Encaminhamentos não bloqueantes: avaliar respostas de erro backend que podem incluir detalhes internos; RBAC visual das áreas de domínio; inspeção visual autenticada em desktop/handset não realizada nesta task.
- Rollout V3: sem bloqueador conhecido após QA, Security, SRE/DevOps e Auditor aprovados. Limitação não bloqueante: status HTTP direto dos requests autenticados não foi capturado nos request logs Render; a conclusão baseia-se no fluxo de login/guard e na tela tenant-scoped funcional.
- Dívida técnica não bloqueante: Flyway 9.22.3 declara suporte testado até PostgreSQL 15; os cenários executados em PostgreSQL 18.6 passaram. Avaliar upgrade do Flyway em task separada.
- O Ciclo 1 e a implementação/validação do Ciclo 2 estão concluídos, incluindo aplicação da V3 em produção. O preflight imediatamente anterior ao deploy não encontrou violações candidatas; dados do ambiente de teste foram declarados descartáveis.
- `safely-deactivate-and-reactivate-staff-users` e `reassign-team-leader-before-staff-deactivation` estão concluídas. O offboarding de líder permanece separado: transferir cada Team primeiro, depois desativar o User pelo fluxo existente.

## Próximo passo recomendado

Roadmap UX/UI incremental `COMPLETED`; cinco tasks obrigatórias commitadas e task condicional 6 `NOT_APPLICABLE`. A task separada de loading contextual está arquivada e commitada. Não há task ativa ou próxima task elegível. Em uma solicitação “Continue o roadmap ativo”, reconciliar checkpoint/Git/filesystem e informar que não há pendências; não reativar tasks nem iniciar outro roadmap sem autorização explícita. Release global segue BLOQUEADO pelo achado RabbitMQ preexistente; não retomar RabbitMQ e não fazer push.

### Registro histórico — antes da autorização Docker/best-effort — 2026-10-21

Os gates foram avaliados, mas não aprovados: backend `clean verify` 92/92 e Notification Service 4/4 passam; integração `*IT` não executada porque Docker Engine está inacessível e o POM não a executa no `verify`. Log confirma AuditLog e descarte da publicação repetida sintética. Snapshot mais recente CloudAMQP: 0 consumers/0 conexões, filas sem ready/unacked. Discord/webhook efetivos não confirmados; não compartilhar valores secretos. QA REPROVADO por evidência incompleta, Security BLOQUEADO, SRE ESCALONAR, Auditor Task Verdict BLOQUEADO; Release permanece BLOQUEADO e a task continua ativa. Próximo passo: executar teste isolado completo quando Docker estiver acessível, verificar no Render somente status enabled/configured dos canais sem expor valores e decidir se o consumer free best-effort atende ao produto. Smoke de produção via EventService exige procedimento e autorização explícita.

### Docker/integração e best-effort RabbitMQ — atualização 2026-10-21

O Docker Engine funcionou fora do sandbox com aprovação (v29.5.3, Docker Desktop 4.77.0). `RabbitMqNotificationIntegrationIT`: 1/1, BUILD SUCCESS, exit 0, contra RabbitMQ 3.13 local descartável; container removido, porta livre. O teste cobre publicação direta no exchange→fila/listener→dispatcher mockado, não o publisher real do backend, AuditLog, retry/DLQ ou idempotência após restart. Reavaliação posterior: SRE/DevOps APROVADO COM LIMITAÇÕES; proprietário confirmou Discord desabilitado/webhook configurada e Security APROVADO para configuração atual; QA REPROVADO e Auditor Task Verdict/Release permanecem BLOQUEADOS. Próximo passo: fechar lacunas de testes; habilitar Discord exige nova revisão Security.

## Contexto relevante

- [[tasks/completed/rollout-authenticated-password-change-and-jwt-transition/prd.md]]
- [[tasks/completed/rollout-authenticated-password-change-and-jwt-transition/plan.md]]
- [[tasks/completed/rollout-authenticated-password-change-and-jwt-transition/execution.md]]
- [[tasks/completed/rollout-authenticated-password-change-and-jwt-transition/audit.md]]
- [[tasks/completed/add-authenticated-password-change/inventory.md]]
- [[tasks/completed/add-authenticated-password-change/prd.md]]
- [[tasks/completed/add-authenticated-password-change/plan.md]]
- [[state.md]]
- [[roadmap.md]]
- [[product-evolution-roadmap.md]]
- [[tasks/completed/secure-staff-user-activation-flow/prd.md]]
- [[tasks/completed/secure-staff-user-activation-flow/plan.md]]
- [[tasks/completed/secure-staff-user-activation-flow/execution.md]]
- [[tasks/completed/secure-staff-user-activation-flow/qa.md]]
- [[tasks/completed/secure-staff-user-activation-flow/security-review.md]]
- [[tasks/completed/secure-staff-user-activation-flow/sre.md]]
- [[tasks/completed/secure-staff-user-activation-flow/audit.md]]
- [[docs/adr/ADR-001-single-guild-user-and-server-side-tenant-resolution.md]]
- [[tasks/completed/enforce-relational-database-integrity/inventory.md]]
- [[tasks/completed/enforce-relational-database-integrity/prd.md]]
- [[tasks/completed/enforce-relational-database-integrity/plan.md]]
- [[tasks/completed/enforce-relational-database-integrity/execution.md]]
- [[tasks/completed/enforce-relational-database-integrity/qa.md]]
- [[tasks/completed/enforce-relational-database-integrity/security-review.md]]
- [[tasks/completed/enforce-relational-database-integrity/sre-review.md]]
- [[tasks/completed/enforce-relational-database-integrity/audit.md]]
- [[tasks/completed/rollout-flyway-v3-to-render-production/prd.md]]
- [[tasks/completed/rollout-flyway-v3-to-render-production/plan.md]]
- [[tasks/completed/rollout-flyway-v3-to-render-production/execution.md]]
- [[tasks/completed/rollout-flyway-v3-to-render-production/qa.md]]
- [[tasks/completed/rollout-flyway-v3-to-render-production/security-review.md]]
- [[tasks/completed/rollout-flyway-v3-to-render-production/sre-review.md]]
- [[tasks/completed/rollout-flyway-v3-to-render-production/audit.md]]
- [[tasks/completed/consolidate-authenticated-navigation-and-expose-users/prd.md]]
- [[tasks/completed/consolidate-authenticated-navigation-and-expose-users/plan.md]]
- [[tasks/completed/consolidate-authenticated-navigation-and-expose-users/execution.md]]
- [[tasks/completed/consolidate-authenticated-navigation-and-expose-users/qa.md]]
- [[tasks/completed/consolidate-authenticated-navigation-and-expose-users/security-review.md]]
- [[tasks/completed/consolidate-authenticated-navigation-and-expose-users/audit.md]]
- [[tasks/completed/standardize-domain-http-feedback-states/inventory.md]]
- [[tasks/completed/standardize-domain-http-feedback-states/prd.md]]
- [[tasks/completed/standardize-domain-http-feedback-states/plan.md]]
- [[tasks/completed/standardize-domain-http-feedback-states/execution.md]]
- [[tasks/completed/standardize-domain-http-feedback-states/qa.md]]
- [[tasks/completed/standardize-domain-http-feedback-states/security-review.md]]
- [[tasks/completed/standardize-domain-http-feedback-states/audit.md]]
