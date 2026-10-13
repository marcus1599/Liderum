# QA — consolidate-authenticated-navigation-and-expose-users

## Cenários verificados

- Roteamento: Dashboard, Users, Members, Teams, Events e Attendance são filhos do mesmo `AuthenticatedLayoutComponent`, cuja rota mantém `authGuard`; registro público continua público.
- Navegação/UX: sidebar usa links semânticos, classe `active` e `aria-current="page"`; teste navega diretamente para `/members` e confirma o estado ativo. Links chamam fechamento em handset; botão Sair preserva a emissão usada pelo layout.
- RBAC visual: perfil ausente não exibe Users; MARECHAL e GENERAL exibem; MAJOR, CAPITÃO e SOLDADO não exibem.
- Dashboard: renderiza a grade de métricas sem outro navbar/sidenav nem componentes inline de Members, Groups, Events, Attendance ou Settings.
- Responsividade/acessibilidade: shell compartilhado conserva BreakpointObserver, drawer e larguras handset existentes; navegação passou a usar anchors de teclado e foco visível. Avaliação limitada aos critérios desta task, sem alegar auditoria WCAG completa.
- Não houve alteração nos componentes de domínio nem nos contratos HTTP.

## Evidência empírica

- `npm test -- --watch=false --browsers=ChromeHeadless`: 40 testes, 0 falhas, exit code 0.
- `npm run build`: build de produção Angular concluído, exit code 0.
- `git diff --check`: sem erro de whitespace.

## Limitações e warnings

- Não foi realizada sessão manual autenticada no navegador; a UX foi validada por specs, inspeção do template/CSS e build. Os estados de loading/erro HTTP continuam fora do escopo.
- O build mantém warnings de deprecação Sass, budgets de CSS, `MatTable` não usado em Groups e dados de `baseline-browser-mapping` desatualizados; nenhum foi introduzido ou corrigido nesta task.

## Veredito

**QA: APROVADO.** Os requisitos observáveis da task e os gates automatizados passaram, e as limitações foram registradas sem extrapolar a evidência.
