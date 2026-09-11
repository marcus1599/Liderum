# Security Review — PostgreSQL Flyway no CI

## Achados

| Achado | Classificação | Origem | Resultado |
| --- | --- | --- | --- |
| Credenciais do service container aparecem no workflow | Informativo, efêmero e não produtivo | INTRODUZIDO_PELA_TASK | Restritas ao PostgreSQL descartável do job; não são secrets Render nem reutilizadas fora do CI. |
| V1/DDL executados contra PostgreSQL 18.6 | Confirmado | INTRODUZIDO_PELA_TASK | Migration aplicada e validada; sem alteração de schema existente. |
| Warning Flyway 9.22.3 sobre suporte testado até PostgreSQL 15 | Informativo | PREEXISTENTE_NAO_RELACIONADO | Mantido como dívida técnica; não impediu o teste PostgreSQL 18.6. |
| Warnings GitHub Node.js 20/setup-java v4 | Informativo | PREEXISTENTE_NAO_RELACIONADO | Não afetam segurança do schema; recomendada atualização futura do workflow. |

## Vereditos

**Task Security Verdict: APROVADO**

**Release Verdict: APROVADO**

- Não há secret real, URL Render, token, senha de produção ou dado sensível versionado pela task.
- O job executa banco efêmero isolado e não toca em produção.
- Não houve alteração de JWT, autorização, tenancy, CORS ou endpoints.
- O teste confirma que Hibernate apenas valida o schema; não cria estrutura silenciosamente.
