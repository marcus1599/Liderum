# Liderum — Current Handoff

> Atualizado pelo workflow de finalização de tasks.

## Estado atual

As seis fases do roadmap original estão concluídas. A task `enforce-relational-database-integrity` concluiu gates de task: V3 passou em H2 e PostgreSQL 18.6 fresh/upgrade, Hibernate `validate` e suíte backend completa (74 testes). Em 2026-10-13, após push dos commits `f860b2a` e `93caf70`, Render aplicou V3 e o serviço ficou live. Flyway/constraints, health/CORS e smoke autenticado na rota tenant-scoped `/members` foram verificados; rollout V3 aprovado.

## Última task concluída

- Nome: `add-authenticated-password-change`
- Resultado: QA, Security, SRE/DevOps e Task Verdict APROVADOS. Backend `clean verify` 81 testes; PostgreSQL 18.6 fresh/upgrade 3/3; frontend 70/70 e build SUCCESS. Release Verdict BLOQUEADO até rollout coordenado drenar/substituir instâncias antigas e validar a transição de JWT. Implementação/testes versionados localmente em `63fb032`; nenhum deploy ou push foi feito.
- Data: 2026-10-15
- Referência: [[tasks/completed/add-authenticated-password-change/audit.md]]

## Task ativa

- Nenhuma.
- Skill `ux-review` disponível para Frontend Dev, QA e Planner durante o Ciclo 3.

## Validações recentes

- Task password change: `clean verify` backend 81 testes; PostgreSQL 18.6 descartável fresh V1–V4 e upgrades V1/V3→V4 3/3; frontend 70/70 e production build SUCCESS. `git diff --check` sem erros. Container de teste foi removido; nenhum recurso Render foi alterado.
- Backend V3: `clean verify`, 74 testes, 0 failures/errors/skipped, BUILD SUCCESS, exit code 0; H2 direcionado 3/3.
- PostgreSQL 18.6 descartável: fresh V1→V2→V3 e upgrade V1→User legado→V2→V3 aprovados (2/2), Hibernate `validate` e contexto `prod` iniciados. Container removido após o teste; Flyway warning de suporte testado até PostgreSQL 15 permanece como dívida técnica não bloqueante.
- Frontend da task de feedback HTTP: 65/65 testes ChromeHeadless e production build SUCCESS, ambos exit 0; `git diff --check` sem erros. Warnings Sass/CSS e browser mapping continuam não bloqueantes. Validação visual manual desktop/handset não executada.
- Git: password change versionado em commits locais separados para implementação/testes e artefatos `.ai`; nenhum push ou deploy desta task. O feedback HTTP foi versionado anteriormente em `2e60125` e `c74e528`.

## Bloqueadores e pendências

- Release da task password change permanece bloqueado até executar rollout coordenado, confirmar drenagem/substituição de todas as instâncias antigas (que ainda podem aceitar JWT sem `uid`/`cv`) e validar reautenticação. Rollback para código antigo pode reabilitar aceitação de JWT legado; não houve deploy nesta execução.
- Task de feedback HTTP sem bloqueador de gate. Encaminhamentos não bloqueantes: avaliar respostas de erro backend que podem incluir detalhes internos; RBAC visual das áreas de domínio; inspeção visual autenticada em desktop/handset não realizada nesta task.
- Rollout V3: sem bloqueador conhecido após QA, Security, SRE/DevOps e Auditor aprovados. Limitação não bloqueante: status HTTP direto dos requests autenticados não foi capturado nos request logs Render; a conclusão baseia-se no fluxo de login/guard e na tela tenant-scoped funcional.
- Dívida técnica não bloqueante: Flyway 9.22.3 declara suporte testado até PostgreSQL 15; os cenários executados em PostgreSQL 18.6 passaram. Avaliar upgrade do Flyway em task separada.
- O Ciclo 1 e a implementação/validação do Ciclo 2 estão concluídos, incluindo aplicação da V3 em produção. O preflight imediatamente anterior ao deploy não encontrou violações candidatas; dados do ambiente de teste foram declarados descartáveis.

## Próximo passo recomendado

Após revisão/publicação autorizada dos commits locais, planejar rollout coordenado e smoke operacional da mudança de JWT; manter o Release Verdict bloqueado até essa validação. Próxima candidata de produto no Ciclo 3: desativação segura de Staff preservando ao menos um MARECHAL ativo. Clareza do líder de Team, revisão dos handlers backend e RBAC visual continuam separadas. As seis fases originais e Ciclos 1/2 permanecem concluídos; Ciclo 3 continua em andamento.

## Contexto relevante

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
