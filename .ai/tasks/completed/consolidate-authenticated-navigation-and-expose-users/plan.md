# Plan — consolidate-authenticated-navigation-and-expose-users

## Routing e classificação

- Domínios: planning, frontend, UX, testing, security (consulta sobre RBAC visual), audit.
- Porte: MEDIUM; prioridade P1 do Ciclo 3 de UX operacional.
- Agents: Planner → Frontend Developer → QA → Security (revisão proporcional da apresentação por role) → Auditor.
- Skills: `create-prd`, `create-plan`, `ux-review`, `test-frontend`, `security-review` (RBAC visual), `audit-task` e `finish-task` somente se aprovada.
- Não selecionados: Backend Developer (sem mudança de contrato/código backend), Arquiteto/`create-adr` (reúso do shell e arquitetura atuais), SRE/DevOps (sem deploy/configuração/migration).

## Diagnóstico e proposta mínima

`app.routes.ts` mantém `/dashboard` fora de `AuthenticatedLayoutComponent`, enquanto as demais páginas privadas usam o layout. O Dashboard monta navbar/sidebar próprias e ainda carrega componentes de domínio por flags. A Sidebar compartilhada já navega por Router, mas não mostra `/users` nem marca a rota ativa. A área `/users` e as restrições MARECHAL/GENERAL já existem. A menor mudança coerente é colocar Dashboard como filho do layout atual, reduzir seu template ao conteúdo de métricas, e completar o menu compartilhado com indicação de rota e link condicional de Usuários.

## Etapas de execução futura

1. Revisar o diff/working tree no início para separar documentação já pendente do rollout V3 e da skill `ux-review`; confirmar rotas, shell e RBAC no código atual.
2. Aplicar `ux-review` sobre Dashboard, Sidebar, layout autenticado e `/users`; registrar estado desktop/handset, foco e comportamento de refresh/direta antes de editar.
3. Colocar `/dashboard` sob `AuthenticatedLayoutComponent` mantendo `authGuard`; garantir um único navbar/sidenav e preservar conteúdo/serviço de métricas.
4. Remover apenas flags, bindings e imports de views embutidas tornados obsoletos no Dashboard/Sidebar; não alterar componentes de domínio nem criar rota Settings.
5. Incluir link **Usuários** na Sidebar baseado no perfil atual de `AuthService.profile$` (MARECHAL/GENERAL), oculto quando perfil nulo/carregando; usar Router e fechar a sidebar em handset.
6. Associar indicação visual e acessível à rota ativa para todas as entradas da Sidebar, preservando estilos existentes e navegação por teclado.
7. Validar visualmente desktop e handset, navegação entre todas as rotas, refresh, URL direta, voltar/avançar, logout e estados de perfil. Ajustar somente CSS necessário para o shell compartilhado.
8. Usar `test-frontend` para testes focados, suíte Angular completa e build. Encaminhar QA → Security → Auditor e `finish-task` somente se Task Verdict APROVADO.

## Arquivos prováveis

- `frontend/src/app/app.routes.ts` e `app.routes.spec.ts`;
- `frontend/src/app/dashboard/dashboard.component.ts`, `.html`, `.scss` e `.spec.ts`;
- `frontend/src/app/shared/sidebar/sidebar.component.ts`, `.html`, `.scss` (se necessário) e `.spec.ts`;
- `frontend/src/app/shared/authenticated-layout/authenticated-layout.component.ts` somente se a integração com Dashboard exigir ajuste;
- artefatos desta task em `.ai/tasks/active/consolidate-authenticated-navigation-and-expose-users/`.

## Testes e evidências previstos

- Rotas: Dashboard e domínios permanecem guardados dentro do layout, rotas públicas de login/registro/ativação permanecem públicas, e `/users` segue privada.
- Menu: MARECHAL/GENERAL veem e abrem Usuários; MAJOR/CAPITÃO/SOLDADO e perfil ausente não veem; alteração/restauração do perfil atualiza visibilidade sem flash indevido.
- Navegação: link de cada área chega à rota correta; estado ativo e `aria-current` acompanham clique, URL direta, refresh e voltar/avançar; logout e fechamento do sidenav no handset continuam funcionando.
- Dashboard: um único shell, métricas presentes, nenhuma área de domínio renderizada inline por toggles.
- Regressão: `/users` por URL direta mantém tela de acesso negado para papel não administrativo e não faz listagem; comportamento de auth guard/401 continua o existente.
- Validação manual de UX: viewport desktop e handset, foco/teclado, contraste/legibilidade de item ativo e ausência de overflow relevante. Registrar evidência observada, não declarar WCAG completa.
- Comandos após implementação: `npm test -- --watch=false --browsers=ChromeHeadless`, `npm run build`, `git diff --check`; repetir teste somente para investigar flakiness concreta. Não há teste backend previsto sem alteração backend.

## Riscos e limites

- Layout duplicado pode persistir se Dashboard continuar envolvendo o shell: QA deve conferir no DOM/UI e não só nos testes de rota.
- O SCSS do Dashboard contém estilos de sidenav próprios; remover/ajustar somente o que ficar obsoleto, sem redesenhar cards/gráficos.
- Ocultar menu por role melhora a experiência, mas não autoriza nem substitui verificação server-side. Não usar `guildId` ou role do token como autoridade.
- A padronização ampla de feedback HTTP e o comportamento de Settings são trabalhos separados, conforme PRD.

## Critérios de conclusão

- Todos os critérios de aceite do PRD verificados com evidência de testes e inspeção de UI.
- Sem código backend, nova dependência, mudança em auth/RBAC/tenancy ou refactor fora do escopo.
- QA e Security aprovam os gates aplicáveis; Auditor registra Task Verdict e Release Verdict separadamente; `finish-task` somente se Task Verdict APROVADO.

## Resultado da execução

Implementação concluída após autorização do usuário. Evidências e gates estão registrados em `execution.md`, `qa.md`, `security-review.md` e `audit.md`. Task Verdict APROVADO; Release Verdict APROVADO. A task foi finalizada em `tasks/completed/` sem commit ou push.
