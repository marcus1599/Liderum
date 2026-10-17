# Liderum — Current Handoff

> Atualizado pelo workflow de finalização de tasks.

## Estado atual

As seis fases do roadmap original estão concluídas. No Ciclo 3, a task `safely-deactivate-and-reactivate-staff-users` foi finalizada com Task Verdict APROVADO. Backend `clean verify` (86 testes), suíte Angular (73/73) e build Angular passaram. Release global permanece BLOQUEADO pelo erro RabbitMQ AMQP 406 preexistente na fila de notificações; o usuário pediu para adiar sua investigação.

## Última task concluída

- Nome: `safely-deactivate-and-reactivate-staff-users`
- Resultado: QA, Security e Auditor aprovados; `PUT /users/{id}/status` implementado para ACTIVE↔DISABLED, com revogação persistente de JWT, RBAC/tenant, proteção concorrente do último MARECHAL ACTIVE e conflito 409 sem mutação para líder de Team. Backend: 86 testes; frontend: 73/73 e build SUCCESS. Task Verdict APROVADO; Release Verdict BLOQUEADO pelo erro RabbitMQ preexistente e adiado.
- Data: 2026-10-17
- Referência: [[tasks/completed/safely-deactivate-and-reactivate-staff-users/audit.md]]

## Task ativa

- Nenhuma.
- Skills `ux-review` e Impeccable disponíveis para tarefas de interface no Ciclo 3; Impeccable é de uso manual no Codex, sem hooks automáticos.

## Validações recentes

- Desativação/reativação de Staff: backend `clean verify` 86 testes, 0 failures/errors/skipped, BUILD SUCCESS; Angular 73/73 e build SUCCESS. QA, Security e Auditor aprovaram a task; release global segue bloqueado pelo achado RabbitMQ preexistente.
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
- A task `safely-deactivate-and-reactivate-staff-users` está concluída e foi movida para `tasks/completed/`. A desativação de líder de Team depende de um fluxo de reassociação ainda não implementado; esta task não alterou a liderança.

## Próximo passo recomendado

Sem task ativa. Próxima candidata de produto no Ciclo 3: planejar o fluxo explícito de escolha/reassociação de líder de Team, incluindo UX e autorização, para que um Team possa ser transferido antes de desativar seu líder. A investigação RabbitMQ continua um bloqueador global de Release, mas está adiada por orientação do usuário; não iniciar automaticamente. Revisão dos handlers backend e RBAC visual permanecem separadas. As seis fases originais e Ciclos 1/2 permanecem concluídos; Ciclo 3 continua em andamento.

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
