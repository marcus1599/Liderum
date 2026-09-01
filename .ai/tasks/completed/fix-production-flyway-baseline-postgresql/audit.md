# Auditoria — Production Flyway Recovery

## Escopo versus evidência

| Item | Evidência | Resultado |
| --- | --- | --- |
| Causa raiz | SQLState `42P07` no schema legado sem histórico Flyway | confirmada |
| Estratégia aprovada | reprovisionamento completo em banco vazio; sem `baselineOnMigrate`, repair ou edição de V1 | respeitada |
| PostgreSQL real | Render PostgreSQL 18.6 vazio; V1 aplicada; history criada | aprovada |
| Hibernate/Spring | `ddl-auto=validate`, Tomcat e aplicação iniciados | aprovada |
| Smoke mínimo | registro, login, `/users/me` e Members tenant-scoped aprovados | aprovada |
| Escopo | nenhuma alteração de código, migration, JWT, RBAC, tenancy ou frontend | aprovado |

## Segurança e release

- Rate limit sem 429 após seis registros: `PREEXISTENTE_NAO_RELACIONADO`; encaminhar em task própria.
- CORS sem origin produtiva configurada: `PREEXISTENTE_NAO_RELACIONADO`; encaminhar na mesma task operacional, sem wildcard.
- Esses achados não impedem validar a alteração da task Flyway, mas impedem declarar o ambiente global pronto para release.

## Vereditos

- **Task Verdict: APROVADO.**
- **Release Verdict: BLOQUEADO.**

## Próximos encaminhamentos

1. Criar task P0 `fix-production-rate-limit-and-cors-configuration`.
2. Manter `extend-tenant-integration-coverage-to-events-and-attendance` pausada.
3. Preservar `validate-flyway-migrations-against-postgresql-in-ci` no backlog.
