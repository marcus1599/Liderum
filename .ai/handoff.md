# Liderum — Current Handoff

> Atualizado automaticamente pelo workflow de finalização de tasks.

## Estado atual

Sistema Agents + Skills + MCP validado para uso controlado. As correções de JWT, alinhamento de testes multi-tenant e remoção da redundância JUnit foram validadas e commitadas localmente.

## Última task concluída

- Nome: `enforce-rbac-and-user-tenant-boundaries`
- Resultado: Task Verdict APROVADO; matriz MARECHAL/GENERAL, perfil próprio, contratos explícitos de User, proteção do último MARECHAL, Team/AdminController e isolamento HTTP validados com 46 testes backend.
- Release Verdict: BLOQUEADO por pendências globais preexistentes registradas na auditoria.
- Data: 2026-08-24
- Referência: [[tasks/completed/enforce-rbac-and-user-tenant-boundaries/audit.md]]

## Task ativa

`Nenhuma task ativa com plano.`

## Bloqueadores

- Release/deploy público permanece bloqueado por três pendências globais preexistentes: bootstrap demo/CORS, migrations Flyway ausentes e proteção antiabuso para registro público.

## Próximo passo recomendado

- Priorizar a task P0 `remove-production-demo-bootstrap-and-fix-cors`, mantendo o Release bloqueado até tratar bootstrap/CORS, migrations e proteção antiabuso do registro público.

## Contexto relevante

- [[state.md]]
- [[roadmap.md]]
- [[docs/adr/ADR-001-single-guild-user-and-server-side-tenant-resolution.md]]
- [[tasks/completed/secure-user-provisioning-and-guild-onboarding/audit.md]]
- [[tasks/completed/secure-user-provisioning-and-guild-onboarding/security-review.md]]
- [[tasks/completed/secure-user-provisioning-and-guild-onboarding/prd.md]]
- [[tasks/completed/secure-user-provisioning-and-guild-onboarding/plan.md]]
- [[tasks/completed/enforce-rbac-and-user-tenant-boundaries/prd.md]]
- [[tasks/completed/enforce-rbac-and-user-tenant-boundaries/plan.md]]
- [[tasks/completed/enforce-rbac-and-user-tenant-boundaries/audit.md]]
- [[tasks/completed/remove-jwt-authorization-logs/audit.md]]
- [[tasks/completed/validate-multi-tenant-isolation-integration-tests/audit.md]]
