# Plano — Persistência do Shell Autenticado

## Routing

- Domínios: `planning`, `frontend`, `testing`, `audit`.
- Agents: Planner → Frontend Developer → QA → Auditor.
- Skills: `test-frontend`, `audit-task`, `finish-task`.
- Não selecionados: Backend/Security/SRE/Arquiteto; não há mudança de contrato, autenticação, infraestrutura ou decisão arquitetural duradoura.

## Implementação

1. Criar `AuthenticatedLayoutComponent` standalone com navbar, `mat-sidenav`, sidebar e `router-outlet` interno.
2. Organizar `app.routes.ts` com rotas públicas fora do shell e rotas de domínio como filhas protegidas.
3. Remover do Dashboard flags e imports usados apenas para alternância local de telas.
4. Ajustar Sidebar para navegação por rota e fechamento somente em handset.
5. Preservar logout e `authGuard`.

## Testes

- shell presente em todas as rotas protegidas;
- shell ausente em login, registro e ativação;
- redirecionamento de rota protegida sem sessão;
- navegação de cada item do menu;
- fechamento mobile e permanência desktop;
- logout e regressão de login/onboarding/users;
- `npm test -- --watch=false --browsers=ChromeHeadless`;
- `npm run build`.

## Gates

QA valida comportamento e ausência de regressão. Auditor verifica escopo e evidências. `finish-task` somente após Task Verdict aprovado.

## Status

PRONTO PARA EXECUÇÃO mediante autorização explícita.
