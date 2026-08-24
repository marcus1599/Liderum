# Plano — stabilize-angular-test-baseline

**Classificação:** SMALL — qualidade / infraestrutura de testes.

## Contexto e diagnóstico confirmado

O frontend Angular possui 13 arquivos `*.spec.ts` e 15 casos Jasmine. Em 2026-08-24, o comando `npm test -- --watch=false --browsers=ChromeHeadless` terminou com exit code 1: 10 casos aprovados e 5 falhas determinísticas.

| Spec | Classificação | Evidência / tratamento planejado |
| --- | --- | --- |
| `app.component.spec.ts` | Boilerplate desatualizado | Manter criação e título; substituir apenas a assertion de `Hello, liderum-front`, pois o template real não possui `h1`. |
| `member.service.spec.ts` | Quebrado por infraestrutura | Configurar cliente HTTP de teste e, proporcionalmente, validar uma requisição do service com `HttpTestingController`. |
| `login.component.spec.ts` | Quebrado por infraestrutura | Fornecer mock/spies de `AuthService` e dependências mínimas de rota/snackbar, sem chamar HTTP real. |
| `groups.component.spec.ts` | Quebrado por infraestrutura | Fornecer mock de `GroupService` com observables controlados; não testar o backend nesta task. |
| `attendence.component.spec.ts` | Quebrado por infraestrutura | Fornecer mocks das dependências HTTP indiretas, retornando coleções vazias controladas. |
| Demais specs de componentes | Válidos como smoke tests | Preservar; ajustar TestBed apenas se o runner demonstrar necessidade. |

As falhas confirmadas são ausência de provider de `HttpClient` em MemberService, LoginComponent, GroupsComponent e AttendenceComponent, além de expectation boilerplate obsoleta no AppComponent. Não foi observada flakiness. `npm run build` passou com exit code 0; warnings conhecidos de Sass, tamanho de estilos e `baseline-browser-mapping` não bloqueiam esta task e não serão corrigidos nela.

## Objetivo

Restabelecer a suíte Jasmine/Karma existente como gate confiável, mantendo os testes atuais e corrigindo somente a infraestrutura de TestBed, mocks e expectation obsoleta necessários para todos os 15 casos atuais passarem.

## Escopo

- Ajustar somente arquivos `*.spec.ts` e, se a duplicação comprovadamente justificar, um helper de teste restrito ao diretório de testes.
- Usar `provideHttpClientTesting`/`HttpTestingController` para o teste de `MemberService`, quando compatível com Angular 19.
- Usar mocks/spies de services nos testes de componentes que não precisam testar HTTP.
- Atualizar a expectation obsoleta do template do AppComponent para comportamento real e estável.
- Executar Karma em ChromeHeadless e build Angular para verificar o gate.

## Fora do escopo

- onboarding UI, Users, RBAC visual, rotas de domínio, novos fluxos ou cobertura adicional ampla;
- refactor de componentes, contratos HTTP, environments, interceptors, guards, sessão expirada, E2E/Playwright, CI, redesign e responsividade;
- alterações de código de produção, dependências, `package.json`, Angular/Karma config ou backend;
- correção dos warnings conhecidos de Sass, budgets de estilo e `baseline-browser-mapping`.

## Routing

- Domínios: `planning`, `frontend`, `testing`, `audit`, `documentation`.
- Agents: Planner → Frontend Developer → QA → Auditor.
- Skills: `create-plan` (concluída), `test-frontend` na execução, `audit-task` e `finish-task` somente após QA e auditoria aprovados.
- Não selecionados: Arquiteto (sem decisão estrutural), Backend Developer (sem contrato ou código backend a alterar), Security (mocks/TestBed não mudam auth/runtime), SRE/DevOps (sem ambiente/CI).

## Etapas

1. Frontend Developer: revisar os cinco specs falhos e aplicar providers/mocks mínimos, preservando assertions úteis.
2. Frontend Developer: substituir apenas a assertion boilerplate do AppComponent por uma assertion do shell real.
3. Frontend Developer: executar a suíte Angular em ChromeHeadless; investigar qualquer falha remanescente sem ampliar cobertura.
4. QA: confirmar 15/15 casos existentes, ausência de chamadas HTTP pendentes, estabilidade em repetição proporcional e build Angular aprovado.
5. Auditor: comparar diff com este plano; rejeitar alteração de produção, dependência, configuração de runtime ou feature fora de escopo.
6. `finish-task`: somente após Task Verdict APROVADO. O Release Verdict permanece bloqueado pelo antiabuso de registro público, achado preexistente não relacionado.

## Arquivos previstos

- `frontend/src/app/app.component.spec.ts`
- `frontend/src/app/services/member.service.spec.ts`
- `frontend/src/app/auth/login.component.spec.ts`
- `frontend/src/app/groups/groups.component.spec.ts`
- `frontend/src/app/attendence/attendence.component.spec.ts`
- helper em `frontend/src/app/**` somente se comprovadamente reduzir duplicação sem ocultar comportamento.

## Estratégia de validação

1. `npm test -- --watch=false --browsers=ChromeHeadless`
2. Repetir o comando se houver suspeita de flakiness.
3. `npm run build`
4. `git diff --check`, `git status` e revisão do diff específico da task.

## Critérios de conclusão

- Karma/Jasmine: exit code 0, 15/15 casos atuais aprovados e nenhuma requisição HTTP pendente.
- Nenhuma assertion útil removida; a única expectation substituída é o texto boilerplate inexistente.
- Build Angular aprovado.
- Nenhuma alteração em código de produção, contratos, dependencies ou configuração de runtime.
- Warnings existentes classificados, mas não modificados nesta task.
- QA e Auditoria aprovados; Task Verdict APROVADO.
