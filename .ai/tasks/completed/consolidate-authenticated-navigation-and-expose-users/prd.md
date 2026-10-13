# PRD — Navegação autenticada coerente e gestão de usuários acessível

## Classificação

- Ciclo 3 do `product-evolution-roadmap.md`: UX operacional.
- Prioridade: P1. Tipo: qualidade / UX de funcionalidade existente. Esforço: MEDIUM.

## Objetivo

Permitir que MARECHAL e GENERAL encontrem `/users` pela navegação autenticada e fazer Dashboard e áreas de domínio compartilharem o mesmo shell, sem criar funcionalidades novas nem alterar autorização do backend.

## Problema comprovado no código

- `/users` já é rota filha de `AuthenticatedLayoutComponent`, mas `SidebarComponent` não oferece acesso a ela. `UsersComponent` já diferencia MARECHAL/GENERAL das demais roles.
- `/dashboard` é uma rota privada separada e `DashboardComponent` monta outro navbar/sidenav. As demais áreas são filhas de `AuthenticatedLayoutComponent`, que possui seu próprio estilo de shell. Isso explica diferenças de apresentação ao navegar entre Dashboard e outras áreas.
- O Dashboard ainda possui `showMembers`, `showGroups`, `showEvents`, `showAttendence` e `showSettings` e renderiza essas telas inline, embora as áreas reais já tenham rotas. A Sidebar navega por Router e também emite outputs desses toggles.
- A classe visual `.active` existe no SCSS da Sidebar, mas o template não a associa à rota atual. Settings não possui rota de produto e não deve reaparecer no menu.
- O backend restringe `GET/POST /users` e operações administrativas de User a MARECHAL/GENERAL. `/users/me` fornece o perfil da sessão; a UI usa `AuthService.profile$` para apresentação, enquanto autorização e tenant continuam no servidor.

## Requisitos

1. Usar um único shell autenticado para `/dashboard`, `/members`, `/teams`, `/events`, `/attendance` e `/users`, preservando navbar, sidebar, logout, fechamento do menu em handset e auth guard.
2. Deixar o Dashboard responsável apenas por suas métricas e conteúdo próprio. Retirar do Dashboard a navegação por flags e a renderização inline das áreas roteadas; não alterar o funcionamento interno dessas áreas.
3. Incluir entrada **Usuários** na Sidebar somente quando o perfil atual obtido por `/users/me` tiver role MARECHAL ou GENERAL. Não usar `role` salvo do login, `guildId`, JWT claim ou dado informado pelo cliente como fonte de autoridade.
4. Ao entrar, atualizar ou restaurar a sessão, refletir o perfil disponível sem mostrar temporariamente a entrada administrativa a roles não autorizadas. Para outras roles, a entrada fica ausente.
5. Indicar a rota atual no menu, com estado visual existente e semântica acessível, inclusive ao abrir uma URL diretamente ou usar voltar/avançar do navegador. A indicação deve funcionar para todas as áreas do menu, não apenas Usuários.
6. Em viewport menor, manter abertura/fechamento da sidebar, navegação utilizável por teclado, nome acessível dos controles e layout legível. Preservar o design atual, sem redesign.
7. Manter acesso direto a `/users` protegido pelo auth guard e pelas verificações existentes na tela/backend. A visibilidade do link não substitui RBAC nem exige criar guard novo nesta task.
8. Não oferecer Settings como rota ou menu funcional sem capacidade persistente de produto.

## Escopo

- Rotas e shell autenticado existentes; Sidebar/Navbar somente no necessário à navegação.
- Dashboard somente para retirar shell e toggles legados, preservando conteúdo de métricas e seus serviços.
- Testes das rotas, Sidebar, Dashboard e regressões diretamente ligadas à navegação e às roles.

## Fora de escopo

- Padronização geral de loading/success/empty/error dos domínios, mensagens HTTP e drag-and-drop de Teams; serão avaliados em tasks posteriores do Ciclo 3.
- Alteração de User management, fluxo de ativação, senha, backend, DTOs, RBAC, JWT, TenantService, autenticação ou contratos HTTP.
- Settings persistente, redesign, novo design system, NgRx, biblioteca UI, E2E/Playwright ou responsividade avançada.
- Commit e push nesta etapa de planejamento.

## Critérios de aceite

1. Há um único navbar/sidenav ao navegar entre Dashboard e as áreas de domínio, com aparência coerente em desktop e handset; nenhuma rota privada perde o auth guard.
2. Usuários MARECHAL/GENERAL veem e alcançam `/users` pelo menu; MAJOR/CAPITÃO/SOLDADO não veem essa entrada. O perfil ainda vem de `/users/me` via `AuthService`.
3. A rota atual é indicada visualmente e de forma acessível em todas as entradas, inclusive após refresh, acesso direto e navegação voltar/avançar.
4. Sidebar mantém fechamento em handset e logout; `/users` por URL direta não ganha autorização client-side indevida. Backend continua sendo a fronteira de RBAC/tenant.
5. Dashboard não renderiza Members, Teams, Events, Attendance ou Settings por flags locais; seus gráficos e métricas continuam funcionando.
6. Testes relevantes de navegação/role e suíte Angular completa passam; `npm run build` passa. QA valida desktop, handset, teclado e ausência de duas sidebars. Security confirma que ocultação do menu não é tratada como autorização. Auditoria confirma escopo.

## Riscos e controles

- Migrar `/dashboard` para o layout compartilhado pode alterar altura, espaçamento e backdrop: inspecionar a UI e ajustar apenas CSS necessário, sem refazer métricas/cards.
- O perfil chega de forma assíncrona: menu administrativo deve permanecer oculto enquanto o perfil não estiver disponível; usar a fonte já existente da sessão.
- A Sidebar emite eventos antigos usados pelo Dashboard: remover somente os vínculos obsoletos após a rota compartilhada estar definida. Não mudar fluxos dos componentes de domínio.

## Dependências

- Componentes Angular, AuthService, rota `/users` e RBAC backend já existentes. Nenhuma dependência ou ADR novo.
- A implementação começa apenas após aprovação deste PRD e do plano. Este documento não autoriza mudança de produto por si só.
