# PRD — RBAC e fronteiras tenant de User

**Classificação:** LARGE — segurança e qualidade de autorização

## Contexto e problema

A task de onboarding concluiu o vínculo `User → Guild`, o hash BCrypt, o `TenantService` como fonte server-side do tenant e o CRUD administrativo de User restrito a `MARECHAL`. Ela também comprovou leitura cross-Guild de User como `404`.

O código ainda não possui uma matriz RBAC canônica e aplicada uniformemente. `UserController` não oferece perfil próprio nem atualização; `UserRequestDTO` permite enviar qualquer `GuildRole`, sem política explícita de delegação. Nos domínios de Guild, as mutações de Member, Event e Attendance usam `MARECHAL`/`GENERAL`/`MAJOR`, mas Team possui operações administrativas sem anotação de método e `AdminController` usa expressão de autorização inválida para múltiplos papéis. As leituras de domínio são autenticadas e tenant-scoped nos services, mas essa escolha não está documentada como política de RBAC.

## Objetivo

Aplicar e provar uma matriz RBAC explícita para User e para as operações administrativas de Guild já expostas, preservando o isolamento `usuário autenticado → User persistido → Guild → TenantService` e sem permitir ao cliente escolher tenant, Guild ou fonte de autoridade no JWT.

## Requisitos funcionais

1. Todo endpoint administrativo já exposto deve ter autorização de papel explícita e comportamento testado para permitido, `403` e `404` quando aplicável.
2. A administração de User deve continuar exclusiva de `MARECHAL`, limitada à Guild corrente e sem aceitar Guild enviada pelo cliente.
3. Usuários autenticados devem poder consultar apenas o próprio perfil por um contrato dedicado, sem depender de ID arbitrário.
4. O contrato de alteração desta task será limitado à role de User, continuará tenant-scoped e aplicará a política de delegação definida neste PRD.
5. Consultas e mutações que recebem IDs de User, Member, Team, Event ou Attendance devem manter comportamento seguro entre Guilds: não revelar nem modificar recurso de outra Guild.
6. `MARECHAL`, `GENERAL` e `MAJOR` mantêm as permissões administrativas de domínio já previstas para Member, Team, Event e Attendance; `CAPITÃO` e `SOLDADO` não recebem administração por inferência.
7. JWT continua apenas com subject/claims atuais; `guildId` não será adicionado nem tratado como autoridade. Authorities continuam derivadas do usuário persistido.

## Política de delegação aprovada

- `MARECHAL` é a autoridade máxima da Guild. Pode executar todas as ações de `GENERAL`, criar ou promover `GENERAL` e outro `MARECHAL`, e alterar/remover `GENERAL` ou outro `MARECHAL`.
- A Guild deve conservar pelo menos um `MARECHAL` persistido. Como o modelo atual não possui estado de conta, “ativo” nesta regra significa User existente e vinculado à Guild com `guildRole = MARECHAL`; o último não pode ser removido nem rebaixado.
- `GENERAL` é administrador delegado: pode criar `MAJOR`, `CAPITÃO` e `SOLDADO`, alterar role e remover apenas usuários abaixo de `GENERAL`.
- `GENERAL` não pode criar/prometer `GENERAL` ou `MARECHAL`, nem alterar/remover `GENERAL` ou `MARECHAL`.
- `MAJOR` não administra contas nesta task. `CAPITÃO` e `SOLDADO` também não.
- Alterações de username, e-mail, senha ou autoatendimento não pertencem a esta task; o novo contrato administrativo limita-se à role.

## Matriz RBAC final

| Operação | Público | MARECHAL | GENERAL / MAJOR | CAPITÃO / SOLDADO |
| --- | --- | --- | --- | --- |
| Registro de Guild e primeiro administrador | Sim | — | — | — |
| Login | Sim | Sim | Sim | Sim |
| Consultar próprio perfil | Não | Sim | Sim | Sim |
| Listar e consultar Users da Guild | Não | Sim | Sim | Não |
| Criar Users da Guild | Não | Qualquer role, inclusive MARECHAL/GENERAL | MAJOR, CAPITÃO ou SOLDADO | Não |
| Alterar role de User da Guild | Não | Sim, preservando ao menos um MARECHAL | Somente usuário abaixo de GENERAL | Não |
| Remover User da Guild | Não | Sim, preservando ao menos um MARECHAL | Somente usuário abaixo de GENERAL | Não |
| Administrar Member, Team, Event e Attendance da Guild | Não | Sim | Sim | Não |
| Ler recursos tenant-scoped da própria Guild | Não | Sim | Sim | Sim |
| Ler ou manipular dados de outra Guild | Não | Não | Não | Não |

## Escopo técnico

- controllers, services, DTOs, repositories e testes necessários para User, perfil próprio e alteração administrativa de role;
- anotações/expressões de Spring Security nos endpoints administrativos já existentes de Guild;
- testes HTTP/de integração de RBAC e isolamento com ao menos duas Guilds;
- tratamento consistente de `401`, `403` e `404` já suportado pelo projeto, apenas quando necessário ao contrato desta task.

## Fora do escopo

- frontend, multi-Guild, claims de tenant no JWT, MFA, e-mail, billing e recuperação de senha;
- rate limiting/CAPTCHA, CORS, bootstrap demo, Flyway/migrations, RabbitMQ, CI/CD e refactors estéticos;
- mudança de modelo de membership ou de ADR-001;
- alteração de username, e-mail, senha ou status de conta;

## Critérios de aceitação

- matriz RBAC documentada e coberta por testes negativos para MARECHAL, MAJOR, GENERAL e papéis inferiores existentes;
- endpoints administrativos sem bypass de autorização;
- User e recursos de Guild permanecem tenant-scoped por `TenantService`/consultas de Guild;
- IDs cross-Guild retornam resultado seguro e não produzem mutação;
- não há mass assignment de Guild ou role: DTOs de criação/alteração são específicos e a política de role aprovada é aplicada no service, não apenas no controller;
- JWT não passa a autorizar por `guildId`, senha e token não são expostos;
- suíte backend passa sem estado externo, segredo de máquina ou RabbitMQ.

## Segurança e riscos

- **alto:** Broken Access Control/vertical privilege escalation nas operações Team sem `@PreAuthorize` e no `AdminController` com expressão incorreta;
- **alto:** privilege escalation se a política de criação/promoção/rebaixamento de papéis não for validada no service;
- **alto:** IDOR horizontal se qualquer operação por ID deixar de resolver a Guild pelo `TenantService`;
- **médio:** mass assignment de `guildRole` se o DTO de alteração aceitar papel sem validação no service;
- achados de bootstrap/CORS, migrations e antiabuso do registro permanecem preexistentes e fora do escopo.
