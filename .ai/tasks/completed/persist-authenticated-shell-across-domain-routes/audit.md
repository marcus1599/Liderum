# Auditoria — Persistência do Shell Autenticado

## Task Verdict

APROVADO

## Escopo

A alteração está restrita ao frontend, às rotas protegidas e ao novo layout autenticado. Não há mudanças de backend, contratos HTTP, JWT, RBAC, dependências ou state management.

## Evidências

- `AuthenticatedLayoutComponent` contém navbar, sidenav, sidebar e outlet interno.
- Rotas de Users, Members, Teams, Events e Attendance usam o shell protegido.
- Rotas públicas permanecem independentes.
- Testes frontend: 35/35 SUCCESS.
- Build: SUCCESS.
- `git diff --check`: sem erros de whitespace.

## Observação

O Dashboard ainda mantém seu layout próprio histórico; isso não impede o objetivo da task, pois as áreas acessadas pelo menu agora renderizam dentro do shell persistente. A remoção completa das flags legadas do Dashboard fica como refatoração posterior, fora do bloqueador atual.

## Scope creep

Não identificado.

## Release Verdict

APROVADO; esta task não introduz bloqueador de release.
