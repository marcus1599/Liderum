# QA — Integridade relacional

## Evidências

- Produção consultada read-only: V1/V2 aplicadas; 1 Guild, 1 User e tabelas de domínio vazias; contagens candidatas de violações iguais a zero.
- Teste direcionado H2 `FlywaySchemaIntegrationTest`: 3/3, BUILD SUCCESS; migration fresh V1/V2/V3, Hibernate `validate`, NOT NULL e UNIQUE verificadas.
- `clean verify`: 74 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS após atualizar fixture que tentava gravar User sem Guild.
- `PostgreSqlFlywaySchemaCiIT` executado em PostgreSQL 18.6 descartável vazio: V1→V2→V3, Flyway history em V3, contexto `prod` e Hibernate `validate`: 1/1 aprovado.
- `PostgreSqlFlywayUpgradeCiIT` executado em PostgreSQL 18.6 descartável: V1→User legado válido→V2→V3, preservação do User, contexto Spring e Hibernate `validate`: 1/1 aprovado.
- Execução PostgreSQL direcionada total: 2 testes, 0 failures/errors/skipped, BUILD SUCCESS, exit code 0. O container foi parado; Render não foi acessado nesta validação.

## Avaliação

- A migration aplica as invariantes solicitadas e preserva a senha nullable do User PENDING.
- Teste de integração do tenant conserva o comportamento de requisição não autenticada e não tenta persistir estado inválido.
- Os cenários H2 e PostgreSQL 18.6 comprovam aplicação fresh e upgrade, incluindo Hibernate `validate`. O warning do Flyway 9.22.3 sobre suporte testado até PostgreSQL 15 permanece como dívida técnica; os casos observados passaram.

## Veredito

**QA: APROVADO** para a task. A migration passou em H2 e nos cenários fresh/upgrade em PostgreSQL 18.6; `clean verify` passou com 74 testes. Não houve alteração no Render.
