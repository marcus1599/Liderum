# Execution — route-domain-areas-and-add-http-ux-feedback

## Routing

- Agents: Backend Developer (Team contract), Frontend Developer, QA, Security, Auditor.
- Skills: `test-frontend`, `test-backend`, `security-review`, `audit-task`, `finish-task`.
- Arquiteto não selecionado: nenhuma decisão estrutural nova; ADR-001 permanece suficiente.

## Implementação

- Rotas protegidas adicionadas: `/teams`, `/events`, `/attendance`; `/dashboard`, `/members` e `/users` preservados.
- Sidebar navega por Router e não oferece Settings, que continua somente memória local.
- Member alinhado a `SOLDADO`, `BARDO`, `teamName`/`teamId`; `MACACO` removido.
- Team add/remove passou a tratar resposta `void`; update recebeu contrato explícito e atualização de nome tenant-scoped.
- Event/Attendance receberam tipos e estados básicos de loading, vazio e erro 403/404.
- Nenhum `guildId`, token, segredo, dependência ou state management novo foi introduzido.

## Validação

- Frontend: `npm test -- --watch=false --browsers=ChromeHeadless` — 35/35 SUCCESS em duas execuções consecutivas.
- Frontend: `npm run build` — SUCCESS; warnings de Sass/budget já existentes foram registrados, sem erro de compilação.
- Backend direcionado: `mvnw.cmd -Dtest=RbacUserTenantBoundariesIntegrationTest test` — 10 testes, 0 failures, 0 errors, BUILD SUCCESS.
- Backend completo: `mvnw.cmd clean verify` — 52 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS.
