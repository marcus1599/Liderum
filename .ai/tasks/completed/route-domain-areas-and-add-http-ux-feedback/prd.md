# PRD — route-domain-areas-and-add-http-ux-feedback

## Classificação

- Prioridade: P1, Fase 3 — Fluxo funcional de Guild no frontend.
- Tipo: funcionalidade / qualidade.
- Esforço: MEDIUM.

## Objetivo

Transformar as áreas de domínio já existentes no Angular em páginas protegidas e navegáveis, alinhar seus contratos aos endpoints reais e fornecer feedback HTTP consistente, sem criar funcionalidades de domínio novas nem enfraquecer autorização server-side.

## Contexto e problema comprovado

`DashboardComponent` hoje controla Members, Groups/Teams, Events, Attendance e Settings por flags locais e renderiza os componentes no mesmo dashboard. Apenas `/members` possui rota dedicada; `/teams`, `/events` e `/attendance` não existem. Isso impede navegação direta, refresh com URL estável e testes de rota proporcionais.

O levantamento do código confirmou as seguintes divergências:

| Domínio | API real | Divergência atual no Angular | Requisito desta task |
| --- | --- | --- | --- |
| Member | `GET/POST /members`, `GET/PUT/DELETE /members/{id}`; `MemberRequestDTO` usa `teamId`; `MemberResponseDTO` retorna `teamName` | tipo usa `SOLDIER`, inclui `MACACO`, omite `BARDO` e espera `team` objeto | tipar enums e request/response conforme o backend e adaptar a exibição para `teamName` |
| Team | `GET/POST /teams`, `GET/DELETE /teams/{id}`, `POST /teams/{teamId}/add-member/{memberId}`, `DELETE /teams/{teamId}/remove-member/{memberId}` | serviço espera `Group` no add/remove, porém a API retorna `Void` | usar `void` e recarregar a fonte de verdade após mutação |
| Team update | `PUT /teams/{teamId}` atualmente não recebe corpo, retorna `Void` e o service não modifica campo algum | Angular envia `Group` e espera `Group` | incompatibilidade comprovada: corrigir minimamente o contrato backend para aceitar `TeamRequestDTO`, atualizar o nome e retornar `TeamResponseDTO`, com teste backend proporcional; se a inspeção de implementação revelar decisão além disso, escalar |
| Event | `GET/POST /events`, `GET/PUT/DELETE /events/{id}`; DTOs usam `name`, `LocalDateTime date`, `description` | sem rota própria e sem estados de operação/erro | expor rota e tipar os contratos com data compatível com `datetime-local`/ISO local |
| Attendance | `GET/POST /attendances`, `GET/PUT/DELETE /attendances/{id}`, `GET /attendances/consecutive-absences?threshold=`; resposta inclui nomes | sem rota própria e tipo omite `memberName` e `eventName` | expor rota e completar tipos/respostas sem mudar a semântica do backend |
| Settings | somente `SettingsService` em memória local | menu tenta exibir uma tela sem persistência de produto | não criar `/settings`; remover/ocultar a entrada até existir capacidade real suportada |

## Requisitos funcionais

1. Disponibilizar rotas protegidas explícitas para `/dashboard`, `/members`, `/teams`, `/events`, `/attendance` e manter `/users` como rota protegida existente.
2. Substituir a alternância local do dashboard por navegação via Router, preservando o shell autenticado (navbar, sidenav, logout e fechamento no handset) e o `authGuard` em todas as áreas privadas.
3. Não criar `/settings` enquanto suas configurações forem apenas memória local e não houver contrato de produto/backend.
4. Corrigir os modelos e serviços Angular dos quatro domínios somente para corresponder aos DTOs e códigos HTTP reais listados acima.
5. Para update de Team, corrigir o endpoint/service backend exclusivamente no contrato mínimo comprovadamente quebrado: corpo `TeamRequestDTO`, atualização do nome e resposta `TeamResponseDTO`; preservar resolução do tenant e RBAC já existentes.
6. Em add/remove de membro de Team, não assumir corpo de resposta: concluir a operação e recarregar os dados do servidor.
7. Aplicar um padrão pequeno e reutilizável de estados por página: carregando, vazio, sucesso após mutação, erro seguro, proibido (403) e não encontrado (404). Reutilizar Material, `LoadingService`/interceptor e recursos já instalados; não criar design system ou state management novo.
8. Manter 401 sob responsabilidade do `AuthInterceptor`/`AuthService`: limpar sessão e redirecionar para login. As páginas não devem duplicar nem suprimir esse fluxo.
9. Refletir visualmente a matriz atual: ações de mutação de Member, Team, Event e Attendance somente para MARECHAL, GENERAL e MAJOR; leitores podem consultar/listar. A UI usa o perfil atual apenas para apresentação, nunca como fonte de autorização.
10. Não enviar nem armazenar `guildId`; o tenant continua resolvido pelo backend através do usuário autenticado.

## Segurança

- Preservar `authGuard` e interceptor Authorization em cada rota privada.
- Não confiar em ocultamento de botão para autorização; o backend segue como fronteira de RBAC e tenant.
- Mapear 403 sem revelar detalhes internos; 404 deve ser tratado como recurso indisponível, sem tentar contornar o tenant.
- Não expor token, senha, dados de outra Guild ou erros internos em snackbar/tela.
- O bloqueador global de rate limiting/CAPTCHA para `/auth/register-guild` é **PREEXISTENTE_NAO_RELACIONADO**: permanece no Release Verdict, fora desta task.

## Escopo

- rotas, shell/sidebar e componentes existentes de Members, Teams/Groups, Events e Attendance;
- modelos, serviços e testes HTTP desses domínios;
- correção mínima e testada do contrato de update de Team no backend, por incompatibilidade comprovada;
- feedback de loading, sucesso, erro, vazio, 403 e 404 nas áreas afetadas;
- testes Angular e, para o endpoint Team corrigido, testes backend proporcionais.

## Fora do escopo

- proteção antiabuso, billing, multi-Guild, CI, observabilidade, NgRx, Playwright/E2E e redesign geral;
- novas features de backend, alterações de JWT/RBAC, settings persistente e responsividade avançada;
- alterar contratos que não estejam comprovadamente incompatíveis.

## Critérios de aceite

1. As cinco áreas reais podem ser acessadas por URL protegida e pelo menu, sem flags de tela no dashboard; Settings não é oferecido como rota funcional.
2. Os requests/responses de Member, Team, Event e Attendance correspondem aos DTOs/controladores reais; as divergências enumeradas deixam de existir.
3. `PUT /teams/{id}` atualiza nome com o contrato explícito e permanece tenant-scoped/RBAC-protegido.
4. Cada página afetada apresenta estados de carregamento, vazio e falha seguros; 403/404 são distintos e 401 mantém expiração global.
5. Ações administrativas visíveis obedecem à matriz de roles sem criar fonte de autoridade no cliente.
6. Testes direcionados, suíte Angular completa e build passam; testes backend proporcionais passam caso o TeamController seja alterado.
7. QA, Security e Auditor emitem Task Verdict APROVADO antes de `finish-task`; Release Verdict continua separado.

## Dependências e riscos

- Sem novas bibliotecas ou ADR: ADR-001 já cobre tenancy/JWT e a mudança de Team é corretiva de contrato, não decisão arquitetural.
- Risco de o shell de dashboard precisar de componente de layout com `router-outlet`; a implementação deve usar a menor extração que preserve responsividade e evitar refactor visual.
- Risco de regressão nos contratos existentes: cobrir URLs, verbos, payloads, `void` de Team e tratamentos de erro com `HttpTestingController`.
