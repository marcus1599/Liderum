# Plano — align-frontend-auth-session-and-api-configuration

## Routing

- Domínios: `planning`, `frontend`, `security`, `testing`, `audit`, `documentation`.
- Agents: Planner → Arquiteto (consultivo sobre sessão) → Frontend Developer → QA → Security → Auditor.
- Skills: `create-prd`, `create-plan`, `test-frontend`, `security-review`, `audit-task` e `finish-task` nos gates correspondentes.
- Não selecionados: Backend Developer executor (contrato existente e sem alteração backend), SRE/DevOps (sem infra/CI), `create-adr` (decisão reversível e limitada ao frontend; ADR-001 permanece suficiente).

## Prioridade e dependências

P1 da Fase 3. A base de testes Angular está verde; esta task é a dependência técnica imediata para onboarding e gestão de Users. Não depende de antiabuso para ser desenvolvida, embora o Release permaneça bloqueado por esse achado global.

## Diagnóstico confirmado

- `AuthService` retorna `Observable<any>`, guarda somente `token` no `localStorage` e não consulta `/users/me`.
- Login possui `onSubmit` e `login` duplicados; o template usa `onSubmit`, que registra erro no console e não usa o loading/snackbar do segundo fluxo.
- Guard verifica apenas presença do token.
- Auth interceptor adiciona Bearer, mas não reage a 401.
- Loading interceptor é global e deve ser preservado.
- Não há tratamento frontend de 403/404.
- `environment.ts` aponta para Render mesmo com `production: false`.
- `environment.development.ts` usa `https://localhost:8080`; o backend local é HTTP.
- `angular.json` não define `fileReplacements`; `environment.prod.ts` não é efetivamente selecionado pelo build de produção atual.
- Rotas protegidas existem para dashboard/members; não há rota de onboarding/profile/User, que permanecem fora do escopo.

## Estratégia de implementação

1. Frontend/Arquiteto: consolidar um contrato mínimo `AuthResponse`, `UserProfile` e estado de sessão em memória, sem nova biblioteca.
2. Frontend: fazer AuthService persistir apenas token, expor perfil/role carregados de `/users/me`, restaurar sessão no refresh e limpar tudo no logout.
3. Frontend: corrigir login para um único fluxo tipado, com sucesso, inválido, loading e propagação de erro.
4. Frontend: ajustar guard para aguardar restauração de sessão e tratar ausência/expiração sem confiar em role ou Guild do cliente.
5. Frontend/Security: adicionar interceptor global de 401 com exclusão de endpoints públicos e sem loop; manter 403/404 distinguíveis.
6. Frontend: alinhar `environment.ts`, `environment.development.ts`, `environment.prod.ts` e `angular.json` com replacements explícitos.
7. QA/Security: validar token ausente/inválido/expirado, `/users/me`, Authorization, logout, redirects, 401/403/404, loading e ausência de `guildId` autoritativo.

## Arquivos prováveis

- `frontend/src/app/auth/auth.service.ts`
- `frontend/src/app/auth/auth.guard.ts`
- `frontend/src/app/auth/login.component.ts` e spec
- `frontend/src/app/core/interceptors/auth.interceptor.ts` e novo spec
- `frontend/src/app/core/interceptors/LoadingInterceptor.ts` e spec se necessário
- `frontend/src/environments/environment*.ts`
- `frontend/angular.json`
- `frontend/src/app/app.config.ts` e `app.routes.ts` somente se necessário para restauração/guards
- models/interfaces de auth e specs correspondentes

## Testes planejados

- login válido: token persistido, `/users/me` carregado e perfil disponível;
- login inválido: erro propagado, storage limpo e sem redirect indevido;
- persistência/refresh: token válido restaura sessão e chama `/users/me`;
- token ausente, expirado e inválido: sessão rejeitada e redirect para login;
- logout: token e perfil removidos;
- 401 global: limpeza e redirect único; endpoints públicos excluídos;
- 403 e 404: erros permanecem distinguíveis e não são convertidos em login silencioso;
- guard: permite sessão restaurada e bloqueia sessão ausente;
- Authorization interceptor: Bearer correto ou request inalterada sem token;
- LoadingInterceptor: contagem e finalização preservadas;
- environment local/prod: URLs e replacements verificáveis;
- segurança: nenhum teste ou implementação usa `guildId` como autoridade.

## Riscos e controles

- `localStorage` expõe token a XSS: aceitar explicitamente no escopo atual, não armazenar perfil sensível e registrar evolução futura para cookie HttpOnly.
- Role stale no login: usar `/users/me` como fonte de contexto atual; JWT/backend continuam autoridade final.
- Interceptor 401 em login: excluir endpoints públicos para evitar loop.
- API offline: expor erro de disponibilidade ao fluxo, sem apagar sessão válida indiscriminadamente; comportamento exato deve ser coberto no plano de testes.
- Tenant: não criar `guildId` em storage, models de autoridade ou headers.

## Critérios de conclusão

- Testes Angular com Jasmine/Karma e `HttpTestingController` aprovados.
- Build de desenvolvimento e produção aprovado com environment replacement correto.
- Nenhum código backend alterado.
- Nenhuma biblioteca de estado nova.
- QA e Security aprovados; Auditoria emite Task Verdict separado do Release Verdict.
