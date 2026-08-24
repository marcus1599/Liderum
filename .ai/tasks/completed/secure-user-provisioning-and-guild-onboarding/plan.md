# Plano — Provisionamento seguro de usuários e onboarding de Guild

**Classificação:** STRUCTURAL

## Routing

- Domínios: `planning`, `architecture`, `backend`, `security`, `testing`, `database`.
- Agents: Planner → Arquiteto → Security → Backend Dev → QA → Security → Auditor.
- Skills: `create-prd` → `create-plan` → `test-backend` → `security-review` → `audit-task` → `finish-task`.
- Não selecionados agora: Frontend Dev (nenhuma UI será implementada nesta task), SRE/DevOps (sem mudança operacional), `create-migration` (a necessidade de schema será avaliada antes de executar; a baseline Flyway é tarefa própria da Fase 2).

**ADR relacionado:** [[docs/adr/ADR-001-single-guild-user-and-server-side-tenant-resolution.md]].

## Decisão de lifecycle

1. **Criação de Guild:** qualquer visitante pode chamar um endpoint público único de registro. Não haverá endpoint público genérico de criação de User.
2. **Nascimento da Guild:** serviço transacional cria Guild e o primeiro User; não há Guild sem administrador persistida após erro.
3. **Primeiro administrador:** é criado junto à Guild, recebe `MARECHAL` e senha BCrypt via `PasswordEncoder` existente.
4. **Usuários subsequentes:** somente `MARECHAL` da Guild atual os cria e atribui papel permitido. O usuário criado pertence à mesma Guild do solicitante.
5. **Usuário sem Guild:** não recebe acesso a recursos tenant-scoped; o comportamento explícito de `TenantService` é preservado.
6. **Associação multi-Guild:** não será implementada. O `User.guild` atual representa uma associação única; uma tabela de membership seria complexidade prematura.
7. **JWT e tenant:** manter subject por username e resolver Guild/usuário no banco via `TenantService`. Não incluir `guildId` no token. Avaliar, durante a implementação, usar authorities carregadas de `UserDetails` em vez de confiar em claims de role já emitidos.

## Escopo

- Definir e implementar posteriormente contrato de registro público de Guild + administrador.
- Corrigir provisionamento de senha e associação Guild/role de User.
- Tornar operações de User administrativas, tenant-scoped e exclusivas de `MARECHAL`.
- Definir e testar regras de acesso para User e o comportamento de ausência de Guild.
- Manter compatibilidade de login/JWT sem nova claim de tenant.

## Fora do escopo

- implementação de frontend, e-mail/convites, recuperação de senha, MFA, rate limiting, billing e usuário multi-Guild;
- revisão completa de RBAC dos domínios existentes;
- baseline Flyway, alterações de infraestrutura, mensageria e deploy.

## Etapas de execução futura

1. Conferir modelo/DTOs/repositories e definir contratos HTTP mínimos, com validações e resposta de erro consistente.
2. Implementar serviço transacional de onboarding e provisionamento tenant-scoped usando `PasswordEncoder`.
3. Restringir endpoints e consultas de User; remover o caminho genérico inseguro ou substituí-lo pelo contrato explícito.
4. Ajustar apenas a derivação de authorities necessária para evitar role desatualizada, sem mudar formato de JWT além do indispensável.
5. Criar testes unitários e integração HTTP para registro, hash, atomicidade, login, RBAC e IDOR.
6. QA, Security, auditoria e finalização conforme os vereditos da task e de release.

## Impacto arquitetural

**Estado atual:** `User` possui `guild` e `guildRole`, mas `UserService` cria usuário global sem hash; `TenantService` identifica tenant consultando o usuário autenticado. JWT contém subject e roles.

**Mudança proposta:** consolidar `User → Guild` como associação única e fonte de tenant; onboarding como boundary pública estreita; administração de User no contexto do tenant. Nenhum novo serviço, token de tenant ou modelo de membership será introduzido.

**Alternativas descartadas:**

- `guildId` no JWT: duplica fonte de verdade e pode divergir da associação persistida.
- tabela de membership multi-Guild: útil somente quando houver requisito real de troca de Guild.
- endpoint público `/users`: permite provisionamento sem lifecycle/controle de tenant.

## Riscos e escalonamento

- Alteração de schema necessária para constraint indispensável: escalar ao Arquiteto e usar `create-migration`; não aplicar `ddl-auto` como solução de produção.
- Descoberta de acesso cross-tenant: interromper e escalar a Security.
- Necessidade de alterar claims, expiração ou algoritmo JWT além da avaliação prevista: escalar antes de ampliar escopo.
- ADR-001 formaliza a decisão duradoura: **User pertence a exatamente uma Guild e o tenant é resolvido por banco, sem `guildId` no JWT**.

## Estratégia de testes

- unitários para hash e regras de serviço;
- `@SpringBootTest`/HTTP com H2 para atomicidade, autenticação, RBAC, isolamento e ausência de Guild;
- regressão de JWT para confirmar subject/autenticação e ausência de logs sensíveis;
- não depender de `.env`, segredo real, RabbitMQ ou banco externo.

## Critérios de conclusão

- critérios de aceitação do PRD atendidos com evidência de testes;
- nenhuma senha, token ou credencial em logs/respostas;
- nenhuma operação de User atravessa Guild;
- Security emite Task Security Verdict e Release Verdict;
- Auditor confirma escopo, migrations se aplicável, documentação e ausência de scope creep.

## Status

**PRONTO PARA EXECUÇÃO, condicionado apenas à autorização para implementar.**
