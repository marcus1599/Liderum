# Execution — consolidate-authenticated-navigation-and-expose-users

## Routing

- Domínios: frontend, UX, testing, security, audit.
- Agents: Frontend Developer → QA → Security → Auditor.
- Skills: `ux-review`, `test-frontend`, `security-review`, `audit-task`, `finish-task`.
- Não selecionados: Backend Dev, Architect e SRE/DevOps — sem mudança de contrato, arquitetura estrutural ou infraestrutura.

## Implementação

- `/dashboard` foi movido para as rotas filhas protegidas de `AuthenticatedLayoutComponent`, junto a Users, Members, Teams, Events e Attendance.
- Dashboard agora contém somente métricas; foram removidos o shell duplicado, os toggles locais e a instanciação inline de telas de domínio/Settings.
- Sidebar agora navega por links do Router, marca a rota ativa com classe visual e `aria-current="page"`, e mantém fechamento em handset e logout.
- A entrada Users só é renderizada quando `AuthService.profile$` fornece perfil com role MARECHAL ou GENERAL. Perfil ausente e demais roles não veem o link. A fonte de perfil permanece `/users/me`; não foi adicionado `guildId` nem regra de autorização client-side.
- Sem mudança em backend, autenticação, RBAC, tenancy, contratos, dependências ou Settings persistente.

## Testes e validações

- `npm test -- --watch=false --browsers=ChromeHeadless`: 40/40 SUCCESS, exit code 0 (execução final após todas as alterações).
- `npm run build`: SUCCESS, exit code 0.
- `git diff --check`: sem erros de whitespace. Git informou apenas avisos de conversão LF/CRLF nos arquivos do working tree.
- Build apresentou avisos preexistentes de dados `baseline-browser-mapping` desatualizados, deprecações Sass, orçamento CSS e import `MatTable` não usado em Groups; não foram alterados por estarem fora do escopo.

## QA

Ver `qa.md`. Testes cobrem composição das rotas protegidas, visibilidade do link por role, perfil ausente, estado ativo e `aria-current`, fechamento em handset, logout preservado por conexão do layout e Dashboard sem shell/telas inline.

## Security

Ver `security-review.md`. O link é apenas apresentação; rota permanece guardada, Users mantém a verificação existente e o backend continua autoridade de RBAC/tenant.

## Estado

Implementação e gates proporcionais concluídos. A task foi aprovada pela auditoria e movida para `tasks/completed/`. Nenhum commit ou push foi feito.
