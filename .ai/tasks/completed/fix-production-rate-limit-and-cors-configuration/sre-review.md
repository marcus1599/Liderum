# SRE/DevOps Review — Rate limit e CORS

## Veredito

**APROVADO**.

## Evidências

- Render implantou `29f9ad3` no deploy `dep-dabgnde8bjmc73ct05hg`, com status `live`.
- A instância iniciou em profile `prod`, conectou ao PostgreSQL 18.6, Flyway validou a V1 e o backend iniciou.
- CORS é configurado por variável operacional, sem valor secreto, e foi validado com allowlist exata da origin Vercel.
- O limite permanece process-local, proporcional à instância única Free atual. Escalar horizontalmente exigirá store distribuído, fora do escopo desta task.

## Riscos remanescentes

- `validate-flyway-migrations-against-postgresql-in-ci` permanece recomendado como qualidade/CI; não bloqueia o release atual.
