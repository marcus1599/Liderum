# Plano — RBAC e fronteiras tenant de User

**Classificação:** LARGE

## Status

**PRONTO PARA IMPLEMENTAÇÃO, condicionado à autorização explícita do usuário.** A política aprovada limita o update administrativo à role; username, e-mail, senha e autoatendimento continuam fora do escopo.

## Routing

- Domínios: `planning`, `backend`, `security`, `testing`, `audit`.
- Agents: Planner → Security → Backend Dev → QA → Security → Auditor.
- Skills: `create-prd` → `create-plan` → `test-backend` → `security-review` → `audit-task` → `finish-task`.
- Não selecionados: Arquiteto (ADR-001 já decide tenancy, Guild única e JWT; a pendência é política funcional de papéis), Frontend Dev (fora do escopo), SRE/DevOps (sem mudança operacional), `create-adr` e `create-migration` (não há decisão estrutural ou schema aprovado).

## Evidência de diagnóstico

- `UserController` exige `MARECHAL` para criar/listar/ler/remover; `UserServiceImpl` cria sempre na Guild corrente e consulta por `guildId`.
- Não existe endpoint de perfil próprio nem atualização de User/role, apesar de o PRD anterior prever perfil autenticado quando necessário e administração de papel.
- `UserRequestDTO` transporta `guildRole` e o service persiste o valor sem política de papéis-alvo; não há `guildId` no DTO. A regra aprovada define os papéis-alvo por ator.
- `JwtFilter` carrega authorities do `UserDetails` persistido; `TenantService` volta a consultar o User autenticado e sua Guild. Não há `guildId` autoritativo no JWT.
- Member, Event e Attendance restringem mutações a `MARECHAL`/`GENERAL`/`MAJOR`; Team deixa `add-member` e `update` sem `@PreAuthorize`; `AdminController` usa `hasRole` com múltiplos argumentos em vez de `hasAnyRole`.
- Services/repositories atuais de User, Member, Team, Event e Attendance usam consultas tenant-scoped. A cobertura HTTP de RBAC ainda é limitada ao CRUD de User com MARECHAL/SOLDADO e a uma leitura User cross-Guild.

## Escopo de execução após desbloqueio

1. Implementar o contrato de perfil próprio e a alteração administrativa de role, sem criar update de credenciais ou status de conta.
2. Aplicar autorização de método consistente aos endpoints administrativos de Guild existentes, sem alterar a política de leitura autenticada já observada sem requisito aprovado.
3. Validar, no service, papel alvo, hierarquia e a invariável de ao menos um `MARECHAL` persistido por Guild; não confiar apenas em `@PreAuthorize` ou no DTO.
4. Criar testes unitários e HTTP/integrados de permissão, IDOR e mutação cross-Guild.
5. Executar `test-backend`, QA, `security-review`, `audit-task` e `finish-task` conforme os vereditos.

## Estratégia de testes

Criar fixtures reproduzíveis com Guild A (`MARECHAL`, `MAJOR`, `GENERAL`, `CAPITÃO`, `SOLDADO`) e Guild B (`MARECHAL` e outro usuário). Cobrir:

- perfil próprio permitido para cada papel e sem exposição de senha;
- GENERAL cria MAJOR, CAPITÃO e SOLDADO; tentativas de criar GENERAL ou MARECHAL recebem `403`;
- GENERAL altera role e remove usuário abaixo de GENERAL; tentativas contra GENERAL ou MARECHAL recebem `403`;
- MARECHAL cria/promove GENERAL e MARECHAL, altera/remove GENERAL ou outro MARECHAL, e não consegue remover/rebaixar o último MARECHAL;
- MAJOR, CAPITÃO e SOLDADO recebem `403` nas operações de gerenciamento de User;
- leitura e mutação de User/recursos administrativos de Guild B por ator de Guild A retornam `404` ou `403` conforme o contrato, sem persistência;
- Team `add-member`/`update` e `AdminController` não aceitam usuário autenticado sem papel administrativo;
- ausência de autenticação retorna `401`; JWT permanece sem `guildId`; TenantService sem Guild continua falhando de forma explícita;
- suíte completa sem banco, RabbitMQ ou segredo externo.

## Riscos e escalonamento

- Descoberta de acesso cross-Guild confirmado: parar a implementação e encaminhar a Security.
- Necessidade de alterar a associação `User → Guild`, claims JWT ou adicionar membership: escalar ao Arquiteto e solicitar autorização de ADR.
- O modelo não possui status de conta: não introduzir campo novo; tratar “ativo” como User persistido com role MARECHAL até que exista requisito separado de lifecycle de conta.
- Bootstrap/CORS, migrations e antiabuso de registro são achados preexistentes não relacionados; registrar, não absorver.

## Critérios para iniciar implementação

1. Autorização do usuário para executar este PRD/plan.
2. A implementação permanece limitada a role, perfil próprio e autorização dos endpoints administrativos existentes.
