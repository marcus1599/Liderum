# PRD — Integridade relacional do banco

## Objetivo e contexto

Executar o Ciclo 2 de `product-evolution-roadmap.md`: tornar invariantes já assumidas pelo backend verificáveis pelo banco por uma migration Flyway incremental, sem editar V1/V2 e sem perder ou ajustar dados existentes silenciosamente. O inventário inicial está em `inventory.md`.

## Requisitos

1. Antes de escolher DDL, verificar schema efetivo e contagens agregadas de dados no banco-alvo, incluindo nulos, duplicatas e vínculos cross-Guild listados no inventário. Registrar fonte/data e preservar privacidade.
2. Havendo compatibilidade de dados e regra de produto confirmada, adicionar `UNIQUE (member_id, event_id)` para impedir mais de uma presença do mesmo Member no mesmo Event.
3. Havendo compatibilidade com dados legados e lifecycle PENDING/ACTIVE/DISABLED, exigir `users.guild_id NOT NULL` e `users.guild_role NOT NULL`; **não** tornar `users.password` NOT NULL (PENDING não possui senha).
4. Havendo compatibilidade, exigir `event.name NOT NULL`, `event.date NOT NULL` e `attendance.status NOT NULL`, alinhados aos DTOs atuais.
5. Preservar PKs, FKs, uniques, índices e dados já existentes; nova migration deve funcionar em banco fresh e upgrade V1/V2 com registros legados válidos, no H2 e PostgreSQL 18.
6. Avaliar índices adicionais apenas com evidência de consultas e benefício proporcional. Não criar índices por suposição.
7. Manter ADR-001: tenant derivado server-side do User persistido; não acrescentar `guildId` ao JWT nem confiar no cliente.

## Regra de parada

Contagem positiva de violação, drift de schema, necessidade de transformar dados, conflito de lifecycle ou decisão de constraint cross-Guild além das candidatas bloqueia a implementação automática. Documentar impacto e solicitar decisão antes de DDL/correção de produção. A task não autoriza limpeza de dados nem alteração de comportamento de domínio por conveniência da migration.

## Escopo

- Inventário read-only; migration incremental a ser proposta após preflight; mappings/validações mínimos se necessários para manter Hibernate `ddl-auto=validate`; testes de schema fresh/upgrade e regressão de escrita.
- Revisão de índices existentes contra consultas reais, sem obrigação de criar novos.

## Fora de escopo

- Editar V1/V2, `baselineOnMigrate`, `repair`, alterações manuais de schema/dados, limpeza silenciosa, alteração de JWT/RBAC/frontend, multi-Guild, novas features, redesign do modelo, refactors oportunistas, constraints compostas cross-Guild sem decisão adicional.

## Aceite

- Inventário do banco-alvo executado e analisado sem exposição de dados sensíveis; ausência de violações para cada constraint aplicada.
- Migration nova e versionada; fresh e upgrade com dados legados válidos aprovados em H2 e PostgreSQL 18; Flyway history registra a nova versão; Hibernate `validate` e contexto Spring sobem.
- Testes provam rejeição de duplicata/nulos pelo banco e preservação dos fluxos normais, inclusive ativação PENDING e isolamento tenant; suíte backend completa aprovada.
- QA, Security, SRE/DevOps (migração/deploy) e Auditor emitem vereditos separados de Task e Release; `finish-task` somente com Task Verdict APROVADO.

## Riscos e dependências

- Dados históricos incompatíveis são risco de deploy; mitigação: preflight e decisão explícita de remediação antes da migration.
- A UNIQUE de presença pode contrariar comportamento de duplicação aceito por algum fluxo não documentado; confirmar semântica antes do DDL.
- Cross-Guild por FK simples permanece potencial lacuna estrutural conhecida, mas ampliar para FKs compostas requer desenho e testes próprios.
- Nenhuma biblioteca nova prevista. ADR novo não se justifica para as constraints candidatas; reavaliar se for necessário alterar o modelo de tenancy ou vínculos cross-Guild.
