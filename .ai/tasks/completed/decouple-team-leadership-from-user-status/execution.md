# Execution — `decouple-team-leadership-from-user-status`

## Resultado

A liderança de Team agora é uma marcação tática temporária para GvG associada a um `Member` que pertence atualmente ao próprio Team. Qualquer Member associado pode ser selecionado, independentemente de cargo, existência de User vinculado ou status da conta. Equipes novas começam sem líder; ao mover, remover ou excluir o Member marcado, a associação tática é limpa na mesma transação.

RBAC de gestão (MARECHAL, GENERAL e MAJOR), resolução server-side do Guild, contratos não relacionados e isolamento por tenant foram preservados. A UI agora apresenta a liderança como temporária e lista membros do grupo pelo nickname. A migração V5 mantém `team.leader_id` e seus valores legados inertes, remove sua FK para User e só preenche `leader_member_id` quando a associação histórica corresponde ao mesmo Team e Guild.

## Validações

- Backend: `backend/.\mvnw.cmd clean verify` — **BUILD SUCCESS**, 95 testes, 0 falhas, 0 erros e 0 ignorados. Execução isolada, sem outra execução Maven no `target`.
- Backend direcionado: `TeamServiceImplTest` + `MemberServiceImplTest` — 11/11; relatórios no `clean verify` também confirmam `FlywaySchemaIntegrationTest` 4/4, `RbacUserTenantBoundariesIntegrationTest` 25/25 e `MultiTenantIsolationIntegrationTest` 6/6.
- Migração: V1–V5 aplicada e backfill/restrições verificados em H2/Flyway. A integração PostgreSQL `PostgreSqlFlywayUpgradeCiIT`/job correspondente não foi executada nesta sessão.
- Frontend, conforme execução registrada pelo executor: suíte completa 122/122 antes do último ajuste de acessibilidade; testes focados 26/26 após o ajuste; build final aprovado. A execução independente local `npm test -- --watch=false` não produziu conclusão após longa espera e foi interrompida; os resultados do executor são mantidos como evidência e a cobertura após o ajuste está confirmada pelos testes focados.
- `git diff --check` passou conforme executor frontend e revisão QA.
- Impeccable `detect --json` retornou cinco warnings de `<img>`/`[src]` dinâmico em ícones de classe nos templates de drag preview existentes. As fontes são bindings Angular com caminho formado pelo nome da classe; são falsos positivos do detector para esse padrão, sem justificativa para caminhos estáticos.
- Revisão de UX e acessibilidade por código/specs confirmou label persistente, `mat-hint` associado ao seletor, estado de carregamento, alerta com retry, estados vazios e atualização só após resposta do servidor. Não houve inspeção visual renderizada; WCAG completa não foi declarada.

## Gates

- QA: **APROVADO COM LIMITAÇÕES** — ver [[qa.md]].
- Security: **APROVADO** — ver [[security-review.md]].
- Auditoria: **Task Verdict APROVADO COM LIMITAÇÕES** — ver [[audit.md]].
- Release Verdict global: **BLOQUEADO** pela validação RabbitMQ preexistente, independente desta task; não é reprovação da task nem foi retomada.

## Limitações e próximo passo

Não foi comprovado PostgreSQL nesta sessão nem realizada inspeção renderizada em browser. Não há teste específico para selecionar Member associado a User em status `PENDING`; o caso `DISABLED` passa e a lógica não consulta status/associação de User. Commits locais isolados, sem push: implementação/testes `3c22f61` e governança/encerramento `02d09a0`. Não iniciar nova task automaticamente.
