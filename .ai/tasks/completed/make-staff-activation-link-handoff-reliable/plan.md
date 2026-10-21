# Plan — `make-staff-activation-link-handoff-reliable`

## Routing

- Domínios: planning, frontend, security, testing, audit.
- Agents: Planner → Frontend Dev → QA → Security → Auditor.
- Skills: `create-prd`, `create-plan`, `ux-review`, `test-frontend`, `security-review`, `audit-task`, `finish-task`.
- SRE/DevOps: não selecionado; não há deploy/configuração.

## Arquivos previstos

- `frontend/src/app/users/users.component.ts`
- `frontend/src/app/users/users.component.html`
- `frontend/src/app/users/users.component.scss` somente se necessário
- `frontend/src/app/users/users.component.spec.ts`
- artefatos desta task

## Implementação

1. Adicionar estado temporário do link e origem da cópia.
2. Centralizar criação/regeneração do link e tratar sucesso/falha do clipboard.
3. Exibir alternativa manual acessível sem persistência.
4. Limpar estado ao iniciar nova operação e ao destruir/navegar quando aplicável.
5. Cobrir criação, regeneração, sucesso, rejeição, API ausente e ausência de persistência.

## Segurança

O token fica apenas em memória do componente e no DOM da tela autorizada. Não será registrado nem enviado a outro endpoint; não altera JWT, Guild, role ou contrato backend.

## Gates

Frontend Developer → QA → Security → Auditor. `finish-task` somente após Task Verdict APROVADO. RabbitMQ AMQP 406 continua separado no Release Verdict global.
