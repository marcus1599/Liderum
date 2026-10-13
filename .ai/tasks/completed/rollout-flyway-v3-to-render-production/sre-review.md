# SRE/DevOps Review — Rollout Flyway V3 em Render

## Evidências operacionais

- Revisão publicada: `f860b2a` (produto) e `93caf70` (documentação/HEAD do deploy); retry do mesmo commit, sem limpar cache nem alterar código.
- Deploy Render `dep-db76ikcs728c73dk48og`: `live` em 2026-10-13 17:28:12Z.
- Startup observado com profile `prod`, datasource conectado, Flyway V1–V3, Hibernate `validate`, EntityManagerFactory e Tomcat ativo na porta 10000.
- Read-only: V3 `success=true`; constraints e colunas esperadas presentes.
- Health 200; CORS permitido/negado conforme esperado.
- Sessão autenticada no frontend alcançou rota protegida `/members`, com estado vazio normal. Requests logs Render não forneceram status para correlacionar a chamada autenticada.
- Não foi necessário rollback, alteração operacional ou mudança de configuração. Nenhum dado foi escrito pelo smoke autenticado.

## Riscos/pendências

- HTTP request logs não estavam disponíveis para confirmar status direto de cada request autenticado; manter essa limitação explícita.
- Flyway 9.22.3 declara suporte testado até PostgreSQL 15; V3 passou empiricamente em PostgreSQL 18.6. O aviso continua dívida técnica não bloqueante, sem evidência de incompatibilidade neste rollout.
- Primeira tentativa de build falhou por HTTP 429 do Maven Central; retry sem alteração passou e serviço ficou live.

## Veredito

**APROVADO** — serviço live e estável na evidência consultada; migration, validação de schema, health e CORS passaram. Nenhum risco operacional observado exige rollback ou bloqueia este rollout.
