# Inventário — reassociação de líder de Team

Inspeção em 2026-10-19; somente planejamento, sem alteração de produto.

- `Team.leader` referencia `User` por `leader_id` anulável. A FK já existe nas migrations; não há necessidade demonstrada de nova migration.
- `POST /teams` aceita `leaderId`, resolve o User pela Guild do `TenantService` e, se omitido, atribui o criador. Não verifica status ACTIVE. `PUT /teams/{id}` usa o mesmo `TeamRequestDTO`, mas altera apenas `name`; `leaderId` no update é ignorado. A resposta informa `leaderName`, não `leaderId`.
- `TeamController` permite mutações a MARECHAL, GENERAL e MAJOR; leituras exigem autenticação. `TeamRepository.findByIdAndGuildId` preserva o boundary da Team.
- `PUT /users/{id}/status` recusa com 409 a desativação de qualquer User que lidera Team, sem reassociação automática. A task anterior documentou que transferir liderança deve ser fluxo separado.
- Angular `/teams` (GroupsComponent) exibe nome e membros, mas não `leaderName`, nem oferece escolha/reassociação. `GroupService` já tipa `leaderId` opcional no request e `leaderName` na resposta; o formulário envia apenas `name`.
- `/users` lista contas somente para MARECHAL/GENERAL. MAJOR administra Teams, portanto reutilizar esse endpoint para selecionar líder mudaria o RBAC de contas; lista reduzida de candidatos, tenant-scoped, precisa de contrato próprio se a UI incluir MAJOR.
- Specs atuais de Team e Groups cobrem operações gerais, não reassociação, candidato inativo, RBAC ou corrida com desativação. O teste HTTP de status cobre o bloqueio 409 existente.
- ADR-001 exige Guild única por User e tenant resolvido server-side. Nenhuma mudança em JWT, Guild ou relação User→Guild é necessária.

Risco central: aceitar `leaderId` de outra Guild, conta PENDING/DISABLED ou atualizada concorrentemente poderia deixar liderança inválida ou revelar User externo. A autorização visual não substitui a checagem no backend.
