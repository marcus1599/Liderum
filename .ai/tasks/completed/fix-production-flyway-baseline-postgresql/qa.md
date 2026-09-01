# QA — Production Flyway Recovery

## Evidências avaliadas

- PostgreSQL 18.6 descartável local: V1, `flyway_schema_history`, Hibernate `validate` e Spring em profile `prod` aprovados.
- Render: banco legado substituído por banco vazio; logs confirmam schema vazio, criação de `flyway_schema_history`, aplicação bem-sucedida de V1 e startup do Spring/Tomcat.
- Smoke de produção: registro (201), login, `/users/me` (200) e `GET /members` autenticado (200).
- Nenhum arquivo de produto foi alterado nesta recuperação; V1, dependências e contratos foram preservados.

## Veredito

**APROVADO** para o objetivo da task: recuperar a baseline Flyway em PostgreSQL de produção sem regressão observada nos fluxos mínimos.

Os achados de rate limiting e CORS são registrados separadamente: não invalidam a evidência de recuperação do banco.
