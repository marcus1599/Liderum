# Security Review — Decouple team leadership from user-account status

## Veredicts

**Task Security Verdict: APROVADO**

**Release Verdict: BLOQUEADO** — esta task não valida nem libera a release global; a validação RabbitMQ permanece adiada conforme o PRD e o handoff existentes.

## Escopo e evidências

- Revisados PRD, plano, diff de backend, migration V5, controllers/services/repositories, RBAC, tenant boundary, lifecycle de Member/User e testes alterados.
- Execução isolada posterior informada pelo executor: `mvnw clean verify` — **BUILD SUCCESS**, 95 testes, 0 failures, 0 errors e 0 skipped.
- Relatórios Surefire atuais confirmam os cenários diretamente relacionados: `RbacUserTenantBoundariesIntegrationTest` (25/25), `MultiTenantIsolationIntegrationTest` (6/6), `FlywaySchemaIntegrationTest` (4/4), `TeamServiceImplTest` (7/7) e `MemberServiceImplTest` (4/4), todos sem falhas ou erros.
- A primeira tentativa de testes focados neste review terminou com `NoClassDefFoundError` de `ErrorResponse`/`AuthResponseDTO`; ela foi operacionalmente inconclusiva e foi superada pela execução limpa e isolada `clean verify`. Não a trato como falha de segurança da alteração.

## Checklist de segurança

- **Autenticação e RBAC — APROVADO:** endpoints de candidatos e reatribuição continuam protegidos por `@PreAuthorize` para MARECHAL, GENERAL e MAJOR. As demais mutações administrativas mantêm a mesma matriz. Nenhuma autoridade passou a vir do frontend/body.
- **Tenant / IDOR — APROVADO:** o Guild é resolvido no servidor por `TenantService`; Team e Member são carregados/consultados com o `guildId` corrente. A consulta de candidatos é limitada ao Team e Guild. Reatribuição valida que o Member encontrado pertence ao Team informado. Testes de integração cobrem acesso negado cross-Guild e Member de outro Team/Guild sem alteração.
- **Validação de membership — APROVADO:** a API aceita o ID do Member, não de User, e só atribui liderança a membro atual do Team. A FK composta `(team.id, guild_id, leader_member_id) -> member(team_id, guild_id, id)` reforça a mesma invariável no banco. A saída candidata contém apenas ID e nickname.
- **Concorrência e lifecycle — APROVADO nos caminhos revisados:** reatribuição, moves e remoções bloqueiam o Member e os Teams envolvidos em ordem; caminhos de remoção/movimentação limpam a marca antes da alteração, dentro de transação. Os cenários de lifecycle e boundaries passaram na suíte completa. O marcador permanece ligado ao Member, não à conta vinculada; desativar ou remover User não altera essa associação.
- **Migration / FK legada — APROVADO com limitação de ambiente:** V5 remove a constraint `fk_team_leader` definida na V1 e conserva `team.leader_id` e seus valores sem uso ativo. O backfill só associa quando o User legado aponta para Member no mesmo Team e Guild. O H2/Flyway `clean verify` passou. A execução PostgreSQL de `PostgreSqlFlywayUpgradeCiIT` não foi observada neste review; a workflow CI `.github/workflows/backend.yml` a executa em job PostgreSQL separado.
- **Segredos, logs e dependências — sem achado introduzido:** diff revisado não adiciona segredos, logs de credenciais, dependências ou integrações externas.

## Achados e limitações

| Classificação | Origem | Evidência / encaminhamento |
|---|---|---|
| Informativo | `INTRODUZIDO_PELA_TASK` | A V5 altera schema e remove a FK de User, mas preserva a coluna/valores históricos. H2 validou; executar/confirmar a job PostgreSQL de CI antes de depender da compatibilidade do upgrade no PostgreSQL. |
| Informativo | `PREEXISTENTE_NAO_RELACIONADO` | A validação operacional RabbitMQ permanece adiada e bloqueia a liberação global conforme os registros do projeto; não é risco introduzido nem agravado por esta task. |

Não foi observado bypass de RBAC, acesso cross-tenant, atribuição a Member fora do Team nem mudança de autorização nesta revisão. PostgreSQL CI foi a única limitação técnica de validação destacada; não foi usada evidência de produção.

## Conclusão

Com a execução limpa de `mvnw clean verify` e as verificações estáticas, o risco introduzido pela task está adequadamente coberto: **Task Security Verdict APROVADO**. **Release Verdict permanece BLOQUEADO** por dependência global já registrada, sem relação com a alteração de liderança.
