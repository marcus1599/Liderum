# Execution — Angular Test Baseline

## Alterações

- Corrigidos providers/mocks de TestBed nos specs de MemberService, LoginComponent, GroupsComponent e AttendenceComponent.
- Adicionado um teste HTTP proporcional para `MemberService` com `HttpTestingController`.
- Substituída somente a expectation Angular boilerplate inexistente no `AppComponent` por uma assertion do `router-outlet` real.
- Nenhum arquivo de produção, contrato, dependência ou configuração de runtime foi alterado.

## Validação

- Baseline antes da execução: 15 casos, 10 aprovados, 5 falhas.
- Suíte corrigida: `npm test -- --watch=false --browsers=ChromeHeadless`, 16/16 aprovados, exit code 0.
- Repetição da suíte: 16/16 aprovados, exit code 0.
- Build: `npm run build`, exit code 0.
- Warnings de Sass, budgets e `baseline-browser-mapping` permaneceram fora do escopo.
