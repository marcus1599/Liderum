# Liderum — Current Handoff

> Atualizado pelo workflow de finalização de tasks.

## Estado atual

As seis fases do roadmap original estão concluídas. A task `enforce-relational-database-integrity` concluiu gates de task: V3 passou em H2 e PostgreSQL 18.6 fresh/upgrade, Hibernate `validate` e suíte backend completa (74 testes). Render não foi alterado e continua na V2; o Release Verdict para aplicar V3 permanece BLOQUEADO até preflight read-only atualizado, revisão operacional e autorização.

## Última task concluída

- Nome: `enforce-relational-database-integrity`
- Resultado: Task Verdict APROVADO; QA, Security, SRE/DevOps e Auditor aprovados. Release Verdict BLOQUEADO apenas para rollout V3 em produção, que não foi autorizado/executado.
- Data: 2026-10-11
- Referência: [[tasks/completed/enforce-relational-database-integrity/audit.md]]

## Task ativa

- `rollout-flyway-v3-to-render-production` — tentativa em 2026-10-13 parou antes do Render: V3 ainda está no working tree local e não há CI do commit exato. Backup/restore também não foi verificado. Requer versionamento/publicação seletivos separados, CI verde, readiness operacional e autorização final; nenhuma operação Render foi executada.

## Validações recentes

- Backend V3: `clean verify`, 74 testes, 0 failures/errors/skipped, BUILD SUCCESS, exit code 0; H2 direcionado 3/3.
- PostgreSQL 18.6 descartável: fresh V1→V2→V3 e upgrade V1→User legado→V2→V3 aprovados (2/2), Hibernate `validate` e contexto `prod` iniciados. Container removido após o teste; Flyway warning de suporte testado até PostgreSQL 15 permanece como dívida técnica não bloqueante.
- Frontend: 35/35 testes em duas execuções consecutivas e build de produção SUCCESS.
- Git: activation versionada no commit `2ab5d4a`; nenhum push foi realizado.

## Bloqueadores e pendências

- Bloqueador do release da V3: preflight atualizado e autorização/janela operacional antes de aplicar em Render, que permanece na V2.
- Dívida técnica não bloqueante: Flyway 9.22.3 declara suporte testado até PostgreSQL 15; os cenários executados em PostgreSQL 18.6 passaram. Avaliar upgrade do Flyway em task separada.
- O Ciclo 1 e a implementação/validação do Ciclo 2 estão concluídos. Render foi inspecionado read-only em 2026-10-11: 1 Guild, 1 User, tabelas de domínio vazias e zero violações candidatas naquele momento; V3 não foi aplicada em produção. Repetir preflight antes do rollout.

## Próximo passo recomendado

Preparar primeiro o versionamento/publicação autorizados da revisão V3 e confirmar os checks CI. Depois, verificar backup/restore e apresentar preflight read-only para autorização do deploy. Na execução, aplicar exclusivamente via Flyway; não executar DDL manual. A produção permanece na V2.

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
- [[tasks/active/rollout-flyway-v3-to-render-production/prd.md]]
- [[tasks/active/rollout-flyway-v3-to-render-production/plan.md]]
