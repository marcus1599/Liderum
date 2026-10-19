# Plan — Alinhar ações das áreas de domínio ao RBAC

## Classificação e routing

- Prioridade: P1, Ciclo 3 — UX operacional. Tamanho: MEDIUM. Domínios: planning, frontend, testing, security, audit.
- Agents: Planner → Frontend Dev → QA → Security → Auditor. Backend Dev apenas consultivo se surgir divergência comprovada de contrato. Arquiteto/SRE não selecionados: não há mudança estrutural ou operacional.
- Skills: `create-prd`, `create-plan`, `ux-review` no planejamento; na execução, `ux-review`, `test-frontend`, `security-review`, `audit-task` e `finish-task` somente com Task Verdict APROVADO. Impeccable é apoio manual opcional à revisão visual, sem ampliar escopo.

## Evidência que motivou a task

- `MemberController`, `TeamController`, `EventController` e `AttendanceController` restringem mutações com `@PreAuthorize` a MARECHAL/GENERAL/MAJOR.
- Templates atuais de Members, Teams, Events e Attendance expõem controles de mutação sem condição de role. `GroupsComponent.canReassignLeader()` é a exceção já alinhada.
- `AuthService.profile$` recebe `/users/me`, padrão já usado na sidebar e em Users; as quatro áreas são rotas autenticadas sob `AuthenticatedLayoutComponent`.
- `SecurityConfig` contém matcher `/api/members/**`, enquanto o controller mapeia `/members`; não interpretar isso como autorização de escrita para roles baixas, pois o método tem `@PreAuthorize`. Não corrigir esse matcher nesta task; escalar se um teste revelar efeito de segurança material.

## Passos de execução (somente após autorização)

1. Inspecionar specs atuais, métodos de mutação e SCSS das quatro áreas; confirmar matriz de leitura e escrita no código e nos testes backend. Registrar qualquer conflito antes de editar.
2. Usar perfil de `/users/me` para uma capacidade de administração de domínio, reaproveitando o padrão local; não introduzir estado global. Renderizar ações só para role autorizada e após perfil carregado.
3. Proteger handlers de mutação correspondentes contra invocação sem permissão; não esconder conteúdo de consulta permitido nem alterar chamadas HTTP de leitura.
4. Em Teams, cobrir criação/edição/exclusão, drag-and-drop, remoção de membro e transferência de líder. Em Attendance, separar indicadores de presença da interação de registro. Revisar controles/estados de Members e Events.
5. Acrescentar testes de componente para as cinco roles agrupadas pela matriz, acesso direto, perfil pendente, ações ausentes/presentes, handlers, leitura e regressões do fluxo administrativo. Atualizar mocks de AuthService nos specs existentes sem remover assertions úteis.
6. Executar suíte Angular completa e build. Fazer revisão UX proporcional de teclado, labels, estado vazio, 403 e viewport menor; não declarar inspeção manual que não ocorreu.
7. Gates QA → Security → Auditor. Somente com Task Verdict APROVADO executar `finish-task`; registrar Release Verdict separado e manter RabbitMQ como bloqueador global adiado.

## Matriz-alvo

| Role | Consultar domínio | Mutar Members/Teams/Events/Attendance | Gerir Users |
| --- | --- | --- | --- |
| MARECHAL, GENERAL, MAJOR | Sim | Sim | Somente MARECHAL/GENERAL, conforme fluxo existente |
| CAPITÃO, SOLDADO | Sim, conforme endpoint autenticado | Não | Não |
| Perfil ainda não carregado | Aguardar perfil/guard | Não | Não |

## Validação e limites

- Testes direcionados dos componentes afetados, depois `npm test -- --watch=false --browsers=ChromeHeadless` e `npm run build`, com exit code/resultados finais.
- Testar que a navegação e `/users` não regrediram; backend continua sendo fonte de autorização. Sem mudança backend prevista, não exigir suíte backend salvo descoberta de incompatibilidade.
- Sem commits/push como parte da execução. Alteração funcional requer autorização posterior do usuário.
- Bloqueio de planejamento: nenhum no escopo conhecido. Se o contrato real de leitura divergir do acima ou surgir vulnerabilidade, pausar e escalar em vez de alterar RBAC silenciosamente.
