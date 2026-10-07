# PRD — Persistência do Shell Autenticado

## Problema

Ao selecionar Members, Teams, Events ou Attendance no menu lateral, o frontend navega para uma rota fora de `DashboardComponent`. Como navbar e sidenav pertencem ao Dashboard, o menu desaparece.

## Objetivo

Manter navbar, sidebar e navegação autenticada visíveis durante a troca entre as áreas protegidas do produto.

## Escopo

- criar um shell/layout autenticado persistente;
- reorganizar as rotas protegidas para usar outlet filho;
- manter as rotas públicas fora do shell;
- preservar `authGuard`, logout e comportamento mobile do sidenav;
- remover a dependência do Dashboard para renderizar áreas de domínio;
- adicionar testes de roteamento e regressão do menu.

## Fora de escopo

- alterações de backend, JWT ou RBAC;
- redesign visual;
- novo state management;
- Settings persistente;
- novas funcionalidades de domínio;
- alteração do contrato HTTP.

## Critérios de aceitação

1. O shell permanece visível em `/dashboard`, `/members`, `/teams`, `/events`, `/attendance` e `/users`.
2. Login, registro e ativação não exibem o shell.
3. Rotas protegidas continuam exigindo autenticação.
4. Em handset, a seleção fecha o drawer; em desktop, o sidenav permanece aberto.
5. Logout remove o shell e retorna ao login.
6. Suíte Angular e build passam sem alteração de comportamento de produção fora do layout.
