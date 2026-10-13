# Liderum — Current Handoff

> Atualizado pelo workflow de finalização de tasks.

## Estado atual

As seis fases do roadmap original estão concluídas. A task `enforce-relational-database-integrity` concluiu gates de task: V3 passou em H2 e PostgreSQL 18.6 fresh/upgrade, Hibernate `validate` e suíte backend completa (74 testes). Em 2026-10-13, após push dos commits `f860b2a` e `93caf70`, Render aplicou V3 e o serviço ficou live. Flyway/constraints, health/CORS e smoke autenticado na rota tenant-scoped `/members` foram verificados; rollout V3 aprovado.

## Última task concluída

- Nome: `consolidate-authenticated-navigation-and-expose-users`
- Resultado: Task Verdict, Task Security Verdict e Release Verdict APROVADOS. Dashboard e páginas autenticadas usam o shell compartilhado; link Users limitado visualmente ao perfil MARECHAL/GENERAL. Frontend: 40/40 testes e production build SUCCESS.
- Data: 2026-10-13
- Referência: [[tasks/completed/consolidate-authenticated-navigation-and-expose-users/audit.md]]

## Task ativa

- Nenhuma.
- Skill `ux-review` disponível para Frontend Dev, QA e Planner durante o Ciclo 3.

## Validações recentes

- Backend V3: `clean verify`, 74 testes, 0 failures/errors/skipped, BUILD SUCCESS, exit code 0; H2 direcionado 3/3.
- PostgreSQL 18.6 descartável: fresh V1→V2→V3 e upgrade V1→User legado→V2→V3 aprovados (2/2), Hibernate `validate` e contexto `prod` iniciados. Container removido após o teste; Flyway warning de suporte testado até PostgreSQL 15 permanece como dívida técnica não bloqueante.
- Frontend da task de navegação: 40/40 testes em uma execução e production build SUCCESS; `git diff --check` sem erros. Build mantém warnings Sass/CSS e import já identificados.
- Git: commits V3 `f860b2a` e `93caf70` já publicados pelo usuário. Esta finalização altera apenas documentação `.ai`; nenhum stage, commit ou push foi executado.

## Bloqueadores e pendências

- Rollout V3: sem bloqueador conhecido após QA, Security, SRE/DevOps e Auditor aprovados. Limitação não bloqueante: status HTTP direto dos requests autenticados não foi capturado nos request logs Render; a conclusão baseia-se no fluxo de login/guard e na tela tenant-scoped funcional.
- Dívida técnica não bloqueante: Flyway 9.22.3 declara suporte testado até PostgreSQL 15; os cenários executados em PostgreSQL 18.6 passaram. Avaliar upgrade do Flyway em task separada.
- O Ciclo 1 e a implementação/validação do Ciclo 2 estão concluídos, incluindo aplicação da V3 em produção. O preflight imediatamente anterior ao deploy não encontrou violações candidatas; dados do ambiente de teste foram declarados descartáveis.

## Próximo passo recomendado

Planejar `standardize-domain-http-feedback-states` para Members, Teams, Events e Attendance, começando pelo inventário de loading, estados vazios, falhas HTTP e feedback atual. Manter fora do escopo redesign, infraestrutura nova e mudanças de contrato. Alteração de senha e desativação de Staff permanecem no backlog.

## Contexto relevante

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
