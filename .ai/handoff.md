# Liderum — Current Handoff

> Atualizado pelo workflow de finalização de tasks.

## Estado atual

As seis fases do roadmap original estão concluídas. No Ciclo 3, `reassign-team-leader-before-staff-deactivation` foi concluída com Task Verdict APROVADO. Backend `clean verify`: 92 testes; Angular: 79/79 e build passaram. Release global permanece BLOQUEADO pelo erro RabbitMQ AMQP 406 preexistente na fila de notificações, cuja investigação foi adiada pelo usuário.

## Última task concluída

- Nome: `reassign-team-leader-before-staff-deactivation`
- Resultado: QA, Security e Auditor aprovados; API e UI transferem explicitamente liderança para User ACTIVE da mesma Guild, com lista reduzida de candidatos, RBAC, locks e proteção contra corrida com desativação. Backend: 92 testes; frontend: 79/79 e build SUCCESS. Task Verdict APROVADO; Release Verdict BLOQUEADO pelo erro RabbitMQ AMQP 406 preexistente e adiado.
- Data: 2026-10-19
- Referência: [[tasks/completed/reassign-team-leader-before-staff-deactivation/audit.md]]

## Task ativa

- Nenhuma task ativa no momento. A task `reassign-team-leader-before-staff-deactivation` foi implementada, testada, aprovada pelos gates e movida para concluídas.
- Skills `ux-review` e Impeccable disponíveis para tarefas de interface no Ciclo 3; Impeccable é de uso manual no Codex, sem hooks automáticos.

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

- Logs RabbitMQ exibem AMQP 406 por divergência de argumentos da fila `notification.guild-event-created`, observado antes e depois do rollout JWT; achado preexistente não relacionado com possível impacto na entrega de notificações. Release Verdict permanece BLOQUEADO até diagnóstico/validação em task separada. Rollback para código anterior a `uid`/`cv` pode reabilitar aceitação de JWT revogado e exige nova revisão Security.
- Task de feedback HTTP sem bloqueador de gate. Encaminhamentos não bloqueantes: avaliar respostas de erro backend que podem incluir detalhes internos; RBAC visual das áreas de domínio; inspeção visual autenticada em desktop/handset não realizada nesta task.
- Rollout V3: sem bloqueador conhecido após QA, Security, SRE/DevOps e Auditor aprovados. Limitação não bloqueante: status HTTP direto dos requests autenticados não foi capturado nos request logs Render; a conclusão baseia-se no fluxo de login/guard e na tela tenant-scoped funcional.
- Dívida técnica não bloqueante: Flyway 9.22.3 declara suporte testado até PostgreSQL 15; os cenários executados em PostgreSQL 18.6 passaram. Avaliar upgrade do Flyway em task separada.
- O Ciclo 1 e a implementação/validação do Ciclo 2 estão concluídos, incluindo aplicação da V3 em produção. O preflight imediatamente anterior ao deploy não encontrou violações candidatas; dados do ambiente de teste foram declarados descartáveis.
- `safely-deactivate-and-reactivate-staff-users` e `reassign-team-leader-before-staff-deactivation` estão concluídas. O offboarding de líder permanece separado: transferir cada Team primeiro, depois desativar o User pelo fluxo existente.

## Próximo passo recomendado

Nenhuma task ativa. Próximo passo: reavaliar e priorizar o backlog do Ciclo 3 com base no roadmap após a entrega do fluxo de reassociação; não iniciar automaticamente uma task nova. O erro RabbitMQ AMQP 406 continua bloqueando Release e foi adiado pelo usuário. As seis fases originais e Ciclos 1/2 permanecem concluídos; Ciclo 3 continua em andamento.

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
