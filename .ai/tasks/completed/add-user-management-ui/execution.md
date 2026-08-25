# Execution — User Management UI

## Implementação

- Criados contratos e `UserManagementService` para listagem, criação, alteração explícita de role e remoção.
- Criada rota protegida `/users` e tela de gestão com estados de loading, vazio, erro e acesso negado.
- Ações visuais respeitam a matriz MARECHAL/GENERAL; a autorização server-side permanece definitiva.
- Payloads não incluem `guildId`, tenant ou role do ator; senha não aparece na lista nem é persistida.

## Validação

- `npm test -- --watch=false --browsers=ChromeHeadless`: 35/35, 0 failures, 0 errors, 0 skipped, exit code 0.
- Segunda execução: 35/35, exit code 0.
- `npm run build`: sucesso, exit code 0.
- `git diff --check`: limpo.

Warnings existentes de Sass, budgets, `MatTable` e baseline-browser-mapping permanecem fora do escopo.
