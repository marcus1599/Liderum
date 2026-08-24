# Execution — Frontend Auth Session & API Configuration

## Implementação

- AuthService tipado com login, restauração por `/users/me`, perfil em memória e logout.
- Guard aguarda validação server-side; interceptor trata 401 autenticado sem mascarar 403/404.
- Login consolidado em um único fluxo.
- Environments local HTTP e produção HTTPS com file replacements Angular.
- Nenhum código backend, JWT, guildId ou tenant foi alterado.

## Validação

- `npm test -- --watch=false --browsers=ChromeHeadless`: 22/22, 0 failures, 0 errors, 0 skipped, exit code 0.
- Repetição da suíte: 22/22, exit code 0.
- `npm run build`: sucesso, exit code 0.
- `npm run build -- --configuration=development`: sucesso, exit code 0.
- `git diff --check`: limpo.

Warnings de Sass, budgets e baseline-browser-mapping permanecem não bloqueantes e fora do escopo.
