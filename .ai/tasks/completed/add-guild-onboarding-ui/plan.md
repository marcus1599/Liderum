# Plano — add-guild-onboarding-ui

## Routing

- Domínios: `planning`, `frontend`, `testing`, `security`, `audit`, `documentation`.
- Agents: Planner → Frontend Developer → QA → Security → Auditor.
- Skills: `create-prd`, `create-plan`, `test-frontend`, `security-review`, `audit-task`, `finish-task`.
- Não selecionados: Backend Developer (contrato já existe e não deve mudar); Arquiteto (ADR-001 já cobre tenancy); SRE/DevOps (sem alteração de ambiente/infra).

## Estratégia

1. Criar contrato TypeScript para request/resposta de onboarding e método HTTP dedicado para `POST /auth/register-guild`.
2. Criar `RegisterGuildComponent` standalone com Reactive Forms/Angular Material e campos `guildName`, `serverName`, `username`, `email`, `password` e `passwordConfirmation` apenas local.
3. Implementar validators equivalentes aos limites conhecidos, igualdade das senhas, loading, bloqueio contra duplo submit e mensagens seguras.
4. Adicionar rota pública `/register`, link no login e retorno claro ao login após `201`, sem token ou sessão automática.
5. Reutilizar a linguagem visual do login sem ampliar o redesign; verificar comportamento em viewport estreito.
6. Cobrir fluxo com Jasmine/Karma e `HttpTestingController`/mocks proporcionais; executar a suíte frontend e build.

## Arquivos prováveis

- `frontend/src/app/auth/guild-registration.models.ts`
- `frontend/src/app/auth/guild-registration.service.ts` e spec
- `frontend/src/app/auth/register-guild.component.ts`, HTML, SCSS e spec
- `frontend/src/app/auth/login.component.html` e spec se necessário
- `frontend/src/app/app.routes.ts`

## Testes planejados

- renderização e disponibilidade pública da rota;
- validators de campos e confirmação de senha;
- botão bloqueado quando inválido ou carregando;
- payload exato, sem confirmação, role ou `guildId`;
- sucesso `201`, snackbar e redirecionamento ao login sem sessão/JWT;
- 400/409, rede e 5xx com mensagem segura e sem detalhe interno;
- ausência de persistência de senha;
- regressão de login e sessão; suíte completa em duas execuções se houver comportamento assíncrono; build Angular.

## Riscos e controles

- O backend possui handlers de exceção sobrepostos e não expõe um contrato estável de conflito por campo: a UI não deve tentar identificar username/e-mail/Guild por parsing de detalhes internos.
- O endpoint público continua sem proteção antiabuso: registrar como bloqueador preexistente de Release, sem absorvê-lo nesta task.
- Não há necessidade de ADR: o fluxo aplica ADR-001 e não muda tenancy, JWT ou multi-Guild.

## Gates

- QA após testes e build verdes.
- Security revisa exposição de senha, dados de tenant, tratamento de erro e ausência de auto-login.
- Auditor verifica PRD, escopo, evidências e separação Task Verdict/Release Verdict.
- `finish-task` somente após Task Verdict APROVADO.
