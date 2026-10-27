# Auditoria — `decouple-team-leadership-from-user-status`

## Veredictos

- **Task Verdict: APROVADO, com limitações registradas.** A implementação está alinhada ao PRD/plano; as validações disponíveis passaram e não foi identificado desvio relevante de escopo nesta revisão.
- **Release Verdict: BLOQUEADO.** A validação de produção RabbitMQ permanece adiada conforme o estado global. É um bloqueio independente e não relacionado a esta task.

## Evidências

| Área | Evidência | Resultado / limite |
|---|---|---|
| Escopo e diff | Revisão de PRD, plano, migração V5, serviços, entidades, contratos HTTP, testes e UI | A liderança é associada a um `Member` do mesmo Team; elegibilidade não depende de User, status ou cargo. Candidatos e reassociação são limitados ao Team e Guild resolvidos no servidor. RBAC existente permanece no controller. |
| Lifecycle | `TeamServiceImpl`, `MemberServiceImpl`, `UserServiceImpl` e teste HTTP `disablingLinkedUserDoesNotChangeTacticalTeamLeadership` | Movimentar/remover o Member líder limpa a marcação; desativar e excluir o User vinculado preserva o marcador. As operações de alteração de membership/leadership são transacionais e seguem lock do Member antes dos Teams afetados ordenados por ID. |
| Migração | `V5__decouple_team_leadership_from_user_status.sql`; `FlywaySchemaIntegrationTest.v5BackfillsOnlySameTeamGuildMembersAndPreservesLegacyLeaderIds` | Validação H2/Flyway aprovada: backfill compatível com o vínculo Team/Guild, líderes sem correspondência ficam nulos no novo campo, IDs legados são preservados e a FK antiga é removida. Não foram consultados dados de produção nem inferidas contagens de registros. |
| Backend | Execução isolada `mvnw clean verify`, após execução concorrente anterior | **95 testes, 0 falhas, 0 erros, 0 ignorados; BUILD SUCCESS.** Inclui Flyway 4/4, TeamService 7/7, MemberService 4/4 e RBAC/tenant 25/25. Os 3 erros da execução concorrente anterior não se reproduziram no clean verify isolado. |
| Frontend | Relato contemporâneo do executor: `npm test` 122/122 antes do ajuste final de `mat-hint`; após o ajuste, testes direcionados 26/26, incluindo teste de descrição acessível, e build aprovado | O resultado 122/122 não é atribuído à revisão após o ajuste final; os 26 testes direcionados e o build são posteriores a ele. |
| QA | Parecer formal recebido: **APROVADO COM LIMITAÇÕES** | Aprovação limitada à apresentação e às evidências reportadas; não representa WCAG completa nem validação visual de estados não inspecionados. |
| Security | `.ai/tasks/active/decouple-team-leadership-from-user-status/security-review.md` | **Task Security Verdict: APROVADO.** O Release Verdict separado permanece bloqueado por RabbitMQ. |
| Inspeção visual | Não realizada nesta auditoria | Não alego comprovação visual renderizada. Estados visuais e comportamento em viewport real permanecem limitados ao parecer QA e aos testes/análise estática reportados. |
| PostgreSQL | Não há execução/report Failsafe disponível nesta sessão | A migration V5 foi exercitada em H2; aplicação PostgreSQL não foi comprovada. |

## Conclusão e encaminhamento

A task satisfaz o objetivo de representar a liderança como uma marcação tática, mutável e pertencente a um membro do grupo, sem acoplamento ao ciclo de vida da conta. Não foi encontrada vulnerabilidade ou regressão de tenant/RBAC introduzida pelo diff revisado. As limitações acima devem permanecer registradas; em particular, não se deve declarar validação PostgreSQL nem inspeção visual que não ocorreu.

O bloqueio de release RabbitMQ permanece independente e não deve reativar nem ampliar esta task.
