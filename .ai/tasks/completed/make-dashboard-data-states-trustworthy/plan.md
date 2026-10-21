# Plan — `make-dashboard-data-states-trustworthy`

## Objetivo

Evitar que o Dashboard apresente métricas zeradas como se fossem dados reais durante carregamento ou falha.

## Escopo

- Estados explícitos de carregamento, erro, retry e dados carregados.
- Ocultar métricas até resposta válida.
- Validar o limite do filtro de eventos antes da chamada.
- Testar sucesso, erro e retry.

## Fora de escopo

Backend, contratos, autenticação, RBAC, gráficos novos e redesign.
