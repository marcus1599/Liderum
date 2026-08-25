# Execution — Guild Onboarding UI

## Implementação

- Criada rota pública `/register` e links bidirecionais entre login e onboarding.
- Adicionados contrato e serviço tipados para `POST /auth/register-guild`.
- Criado formulário standalone com Reactive Forms, Angular Material, validações proporcionais, confirmação local de senha, loading e proteção contra submit simultâneo.
- O payload contém somente `guildName`, `serverName`, `username`, `email` e `password`.
- Sucesso informa o usuário e redireciona para login sem criar sessão; erros 4xx, rede e 5xx recebem mensagens seguras.

## Validação

- Suíte Angular: 30/30, 0 failures, 0 errors, 0 skipped, exit code 0.
- Repetição da suíte: 30/30, exit code 0.
- Build: `npm run build`, exit code 0.
- `git diff --check`: limpo.

Warnings pré-existentes de Sass, budgets de estilo, `MatTable` não utilizado e `baseline-browser-mapping` permanecem fora do escopo.
