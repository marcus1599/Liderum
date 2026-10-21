# Plan — `stabilize-user-management-operational-states`

## Objetivo

Tornar os estados de carregamento, erro, vazio, validação e ações pendentes da gestão de Users claros e não ambíguos.

## Escopo

- Não mostrar vazio durante carregamento ou erro.
- Oferecer retry para falha de listagem.
- Exibir validações mínimas dos campos do formulário.
- Bloquear ações concorrentes por usuário durante requests.
- Cobrir os estados com testes Angular existentes.

## Fora de escopo

Backend, RBAC, tenant, novos fluxos, design system e refactor de estado global.
