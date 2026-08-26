# Plano — route-domain-areas-and-add-http-ux-feedback

## Routing

- Domínios: `planning`, `frontend`, `backend`, `testing`, `security`, `audit`, `documentation`.
- Agents: Planner → Backend Developer (consultivo e executor somente do contrato Team comprovado) → Frontend Developer → QA → Security → Auditor.
- Skills: `create-prd`, `create-plan`, `test-frontend`, `test-backend` (condicional ao ajuste de Team), `security-review`, `audit-task`, `finish-task`.
- Não selecionados: Arquiteto/`create-adr` (sem decisão estrutural nova); SRE/DevOps; `create-migration`.

## Etapas de execução

1. Confirmar no início os DTOs/controllers citados no PRD e o diff inicial. Se outra divergência exigir mudança backend fora do update Team, parar e escalar.
2. Criar/ajustar o shell autenticado para navegar por Router, com `/dashboard`, `/members`, `/teams`, `/events`, `/attendance` e `/users`, todos guardados; remover o fluxo de flags e a entrada Settings sem produto persistente.
3. Alinhar modelos e serviços frontend:
   - Member com `SOLDADO`, `BARDO`, sem `MACACO`, `teamId` em request e `teamName` em response;
   - Team com `TeamRequest`/`TeamResponse`, `leaderName` e add/remove `Observable<void>`;
   - Event e Attendance com DTOs de resposta completos e datas ISO locais;
   - nenhuma inclusão de `guildId`.
4. Corrigir somente o update de Team no backend: controller recebe `@Valid TeamRequestDTO`, service altera o nome da Team encontrada na Guild atual, salva e devolve `TeamResponseDTO`; manter PreAuthorize e TenantService. Cobrir o contrato e a fronteira tenant/RBAC no nível proporcional já usado no backend.
5. Em cada página, adicionar estado local claro para carregamento/vazio/erro e feedback de sucesso seguro; mapear 403 e 404 de forma distinta. Reutilizar interceptor global para loading e 401, sem novo gerenciador de estado.
6. Derivar apenas a visibilidade das ações administrativas de `AuthService.profile$`: MARECHAL/GENERAL/MAJOR podem mutar domínio; demais papéis permanecem leitores. O servidor continua autoritativo.
7. Adicionar/atualizar testes de rotas, menu responsivo, componentes e serviços; executar testes frontend, build, teste backend proporcional e depois os gates QA → Security → Auditor.

## Arquivos prováveis

- `frontend/src/app/app.routes.ts`, `dashboard/**`, `shared/sidebar/**` e, somente se necessário, shell/layout autenticado;
- `frontend/src/app/members/**`, `groups/**`, `events/**`, `attendence/**`;
- `frontend/src/app/services/member.service.ts`, `group.service.ts`, `event.service.ts`, `attendance.service.ts` e specs;
- modelos de Member/Team/Event/Attendance e testes;
- `backend/src/main/java/com/example/Liderum/Controllers/TeamController.java`, `Services/TeamService.java`, `Services/Impl/TeamServiceImpl.java` e testes diretamente necessários;
- artefatos de execução/revisão da task em `.ai/tasks/active/...`.

## Testes planejados

- rotas: URLs privadas, `authGuard`, menu para cada área, fechamento do sidenav em handset e ausência de rota Settings;
- Members: enums/`teamName`, verbos/payloads e loading/vazio/403/404;
- Teams: create/update com DTO preciso, add/remove `void` com refresh, 403/404 e ações visuais por role;
- Events e Attendance: URLs, payloads/respostas, loading/empty/error e feedback seguro;
- RBAC visual: MARECHAL/GENERAL/MAJOR veem mutações; CAPITÃO/SOLDADO não; sem afirmar autorização client-side;
- regressão: login, onboarding, sessão/interceptor 401 e users;
- backend (se Team update mudar): update válido, recurso fora da Guild bloqueado e regras de autorização preservadas;
- validações finais: `npm test -- --watch=false --browsers=ChromeHeadless` (duas execuções se viável), `npm run build`, Maven proporcional caso alterado e `git diff --check`.

## Riscos e controles

- Contrato Team atualmente quebrado: corrigir apenas o comportamento ausente, sem adicionar operações novas.
- Mudança de navegação pode afetar o layout: conservar navbar/sidenav/handset e não executar redesign.
- Feedback pode duplicar 401: o interceptor continua único responsável por expiração e redirect.
- Release segue bloqueado unicamente pelo antiabuso de registro público; não absorver esse trabalho.

## Critérios de conclusão

- Critérios de aceite do PRD comprovados por testes e build.
- Sem novo `guildId`, token, segredo, dependência ou state management.
- Task Security Verdict e Task Verdict APROVADOS; somente então `finish-task`.
- Release Verdict documentado separadamente e ainda BLOQUEADO pelo achado global preexistente.

## Status

PRONTO PARA EXECUÇÃO mediante autorização explícita do usuário.
