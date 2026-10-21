# Plan — `fix-attendance-event-selection-placeholder`

## Objetivo

Impedir que o placeholder nativo do seletor de eventos seja convertido em `NaN` e tratado como evento válido.

## Escopo

- Normalizar valores vazios/`null` recebidos pelo `ngModelChange` para `selectedEventId = null`.
- Preservar a busca de presenças somente para IDs numéricos válidos.
- Adicionar testes unitários do placeholder, seleção válida e ausência de chamada indevida.

## Fora de escopo

Backend, contrato HTTP, RBAC, tenant, redesign, novas dependências e refactors de estado.

## Gates

Frontend Developer → QA → Security (regressão sem impacto de autorização) → Auditor → finish-task.
