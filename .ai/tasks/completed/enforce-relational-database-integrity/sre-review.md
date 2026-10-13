# SRE/DevOps Review — Integridade relacional

## Avaliação operacional

- Banco Render production é PostgreSQL 18 com uma Guild e um User; as tabelas de domínio estão vazias. Preflight read-only encontrou zero violações nas constraints candidatas.
- A V3 executa `ALTER COLUMN SET NOT NULL` e adiciona uma UNIQUE. PostgreSQL pode bloquear escrita nas tabelas durante validação/instalação da constraint; o volume observado é mínimo, mas a validação PostgreSQL 18 e a janela de deploy continuam necessárias.
- Flyway aplica migration versionada no startup e Hibernate permanece em `ddl-auto=validate`. Nenhum comando manual de schema, `baseline` ou `repair` foi usado.
- PostgreSQL 18.6 descartável fresh e upgrade V1→User legado→V2→V3 passaram; contexto Spring e Hibernate `validate` iniciaram. Não aplicamos migration na produção.
- O container descartável foi iniciado exclusivamente nesta execução e parado ao final. Nenhum banco existente foi acessado.
- Rollback automático de versão não foi projetado; mudanças de constraints devem ser revertidas por migration corretiva após análise. Os cenários PostgreSQL confirmaram execução da migration, mas não substituem plano de rollback do deploy.

## Veredito

**SRE/DevOps: APROVADO para a task/testes**. A migration passou em PostgreSQL 18.6 fresh e upgrade. **Release continua BLOQUEADO para aplicar V3 no Render** até repetir contagens read-only no momento do rollout, confirmar janela/rollback e obter autorização específica. Nenhuma alteração no Render foi feita.
