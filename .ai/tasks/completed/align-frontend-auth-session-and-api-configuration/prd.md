# PRD — Frontend Auth Session & API Configuration

## Classificação

P1 — funcionalidade / segurança / qualidade — MEDIUM.

## Problema

O Angular autentica via JWT, mas persiste somente uma resposta não tipada, não carrega o perfil autenticado e não trata token expirado ou 401 globalmente. O guard verifica apenas a existência de uma string no `localStorage`. Além disso, os arquivos de environment não são selecionados por `fileReplacements`: o `environment.ts` aponta para Render mesmo em desenvolvimento e o `environment.development.ts` usa HTTPS local, incompatível com o backend local documentado.

## Objetivo

Estabelecer uma base de sessão e configuração de API confiável para as próximas tasks de onboarding e gestão de Users, sem alterar o backend, adicionar estado global ou transformar dados do frontend em autoridade de tenant.

## Decisões de sessão

1. `localStorage` permanece aceito para o escopo atual de portfólio, documentando o risco XSS e mantendo a alternativa de cookies HttpOnly para evolução futura.
2. Persistir somente o JWT. Não persistir `guildId`, nem tratar role salva no storage como autoridade.
3. A resposta de login será tipada (`token`, `role`), mas a role efetiva virá do `GET /users/me` e ficará em memória junto ao perfil.
4. Após login e durante a restauração da aplicação, o frontend carregará `/users/me` usando o token. Em refresh, a sessão só será considerada restaurada após essa consulta.
5. O parsing client-side de `exp`, se usado, servirá apenas para UX preventiva; autenticação e validade final continuam sendo decididas pelo backend.
6. Um 401 de API autenticada limpa a sessão e redireciona para `/login`, exceto fluxos públicos de login/registro, que devem propagar seu erro ao formulário.
7. 403 e 404 permanecem erros de autorização/recurso para a camada visual consumir; esta task define a propagação e o contrato de tratamento, mas não implementa páginas de Users/onboarding.

## Contratos backend confirmados

- `POST /auth/login` recebe `username` e `password` e retorna `AuthResponseDTO { token, role }`.
- `GET /users/me` retorna `UserResponseDTO { id, username, email, guildRole }` e exige autenticação.
- `POST /auth/register-guild` é público e permanece fora da implementação desta task.
- O backend não exige `guildId` no frontend; `TenantService` resolve a Guild server-side.

## Escopo

- Tipar AuthResponse, User/Profile e estado mínimo de sessão.
- Ajustar AuthService para login, restauração via `/users/me`, logout e estado em memória.
- Ajustar guard para considerar sessão restaurada/401/expiração sem confiar em `guildId` ou role persistida.
- Adicionar tratamento global de 401 no interceptor, evitando loop em endpoints públicos.
- Preservar e testar o interceptor Authorization e o LoadingInterceptor.
- Corrigir environments e `fileReplacements` para local/desenvolvimento e produção.
- Manter 403/404 observáveis para futura camada visual, sem implementar onboarding/User management.

## Fora do escopo

- onboarding UI, gestão de Users, RBAC visual, novas rotas de domínio;
- alterações de backend, JWT, claims, TenantService ou contratos HTTP;
- cookies HttpOnly, NgRx/Redux, biblioteca de estado global, E2E/Playwright;
- proteção antiabuso, CORS, redesign, CI e refactors não relacionados.

## Critérios de aceitação

- Login válido persiste somente token e carrega `/users/me`.
- Login inválido exibe erro ao formulário sem redirecionamento indevido.
- Refresh com token válido restaura o perfil; token ausente/expirado/inválido leva a login.
- Logout remove token e estado em memória.
- 401 autenticado limpa sessão e redireciona uma única vez; login/registro não entram em loop.
- Authorization interceptor envia Bearer somente quando há token.
- Nenhum código usa `guildId` como autoridade.
- `ng serve`/build de desenvolvimento aponta para backend local HTTP e build de produção aponta para URL HTTPS de produção via replacement.
- 403/404 continuam distinguíveis para a UI sem mascaramento.
- Jasmine/Karma com `HttpTestingController` cobre todos os cenários previstos; build Angular permanece verde.
