# Execution — Alinhar ações das áreas de domínio ao RBAC

## Implementação

- Members, Teams, Events e Attendance agora obtêm role de `AuthService.profile$`, abastecido pelo perfil autenticado (`/users/me`). Perfil nulo/pendente não habilita administração.
- CAPITÃO e SOLDADO mantêm consulta disponível e não veem formulários, botões, drag-and-drop ou radios de mutação. Attendance mostra o status confirmado em modo de leitura.
- MARECHAL, GENERAL e MAJOR mantêm as ações administrativas existentes. Handlers relevantes também verificam a role antes de chamar serviços.
- Nenhum endpoint, controller, SecurityConfig, JWT, backend, contrato HTTP, dependência ou rota foi alterado. O link Members permaneceu acessível porque a consulta `/members` é permitida a usuário autenticado.

## Arquivos de produto

- `frontend/src/app/members/members.component.ts`, `.html`, `.spec.ts`
- `frontend/src/app/groups/groups.component.ts`, `.html`, `.spec.ts`
- `frontend/src/app/events/events.component.ts`, `.html`, `.spec.ts`
- `frontend/src/app/attendence/attendence.component.ts`, `.html`, `.spec.ts`

## Validação

- `npm test -- --watch=false --browsers=ChromeHeadless`: primeira execução antes do reforço final dos specs: 82/82; execução final após reforço: 86/86 SUCCESS, exit 0.
- `npm run build`: SUCCESS, exit 0. Warnings Sass e budgets já existentes permaneceram; nenhum erro de build.
- `git diff --check`: sem erros de whitespace.
- Impeccable `detect --json` nos quatro templates retornou exit 1 por avisos `broken-image` em quatro elementos `<img>` do template de Groups. São imagens existentes de ícones de classe; não foram alteradas por esta task.
- Não foi feita inspeção visual manual autenticada em desktop/handset.

## Escalonamento observado

- `SecurityConfig` contém matcher `/api/members/**`, enquanto `MemberController` usa `/members`. As operações mutáveis continuam protegidas por `@PreAuthorize` no controller; a consulta é intencionalmente autenticada. Nenhum efeito de segurança material foi demonstrado. Não alterado nesta task.
