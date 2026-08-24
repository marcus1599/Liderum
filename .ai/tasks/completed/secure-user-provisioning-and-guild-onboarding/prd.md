# PRD — Provisionamento seguro de usuários e onboarding de Guild

**Classificação:** STRUCTURAL — segurança e funcionalidade

## Problema

O backend possui `Guild` e `TenantService`, mas não oferece lifecycle SaaS seguro. `UserServiceImpl` grava a senha recebida sem hash, não associa usuário novo a uma Guild nem define papel; `UserController` não restringe operações por tenant. O bootstrap atual cria um administrador de demonstração, mas não substitui onboarding real.

## Objetivo

Permitir criar uma Guild com seu primeiro administrador de modo público, seguro e atômico; depois, permitir que somente o administrador da Guild provisione usuários da própria Guild conforme a matriz RBAC inicial.

## Requisitos funcionais

1. Um visitante pode registrar uma nova Guild junto com seu primeiro administrador por um endpoint público dedicado.
2. A operação cria exatamente uma Guild e um User administrador vinculados entre si; se qualquer validação falhar, nada é persistido parcialmente.
3. O primeiro administrador recebe `MARECHAL`.
4. Senhas são obrigatórias, validadas e armazenadas exclusivamente pelo `PasswordEncoder`; nenhuma resposta, log ou DTO de leitura devolve senha.
5. Usuário autenticado com `MARECHAL` pode criar usuários somente para sua Guild e definir papéis subsequentes permitidos pela política inicial.
6. Usuários subsequentes pertencem a uma única Guild. Associação multi-Guild não faz parte desta versão.
7. Usuário sem Guild não pode operar recursos tenant-scoped; `TenantService` preserva falha explícita.
8. Operações de leitura, alteração e remoção de User são tenant-scoped e não expõem usuários de outra Guild.
9. O login permanece público. O JWT mantém o `username` como subject; o tenant continua resolvido no banco por `TenantService`, sem `guildId` no token.

## Matriz RBAC inicial

| Operação | Público | MARECHAL | GENERAL / MAJOR | CAPITÃO / SOLDADO |
| --- | --- | --- | --- | --- |
| Registrar Guild + primeiro admin | Sim | — | — | — |
| Login | Sim | Sim | Sim | Sim |
| Ler próprio perfil | — | Sim | Sim | Sim |
| Listar / ler usuários da Guild | — | Sim | Não nesta task | Não |
| Criar, alterar papel ou remover usuários da Guild | — | Sim | Não | Não |
| Acessar dados de outra Guild | Não | Não | Não | Não |

A revisão completa de permissões de Members, Teams, Events e Attendance permanece na task P0 seguinte de RBAC.

## Endpoints e acesso esperado

- Público: `POST /auth/register-guild` e `POST /auth/login`.
- Autenticado: endpoint de perfil do usuário atual, se necessário para completar o contrato.
- Restrito a `MARECHAL`: endpoints administrativos de User, sempre filtrados pela Guild corrente.
- Não devem ser públicos: CRUD genérico de `/users`, Swagger operacional e endpoints de Actuator fora de uma decisão operacional posterior.

## Escopo técnico previsto

- DTOs específicos para registro de Guild/admin e provisionamento de usuário; não reutilizar DTO de leitura para aceitar papel ou Guild arbitrários.
- Serviço transacional para o onboarding.
- Ajustar modelagem e repositórios apenas na medida necessária à associação User → Guild e consultas tenant-scoped.
- Ajustar `SecurityConfig`, controllers e `UserService` ao contrato acima.
- Avaliar o filtro JWT para derivar authorities do `UserDetails` carregado do banco, evitando autoridade obsoleta do claim; não adicionar claim de Guild.
- Testes unitários, HTTP/integrados e de segurança para hash, atomicidade, RBAC e IDOR entre Guilds.

## Fora do escopo

- múltiplas Guilds por usuário, convite por e-mail, verificação de e-mail, recuperação de senha, MFA e billing;
- refatoração geral dos domínios, frontend, RabbitMQ, migrations e deploy;
- matriz completa de RBAC dos recursos de domínio;
- alteração do segredo, expiração ou algoritmo JWT, salvo ajuste mínimo de authorities comprovadamente necessário.

## Critérios de aceitação

- registro público cria Guild + `MARECHAL` com senha BCrypt e sem segredo exposto;
- falha no registro não deixa Guild ou User órfão;
- login do administrador recém-criado funciona;
- MARECHAL só administra usuários da própria Guild;
- nenhuma role inferior acessa administração de usuários;
- identificadores de User cross-tenant não revelam nem alteram dados;
- usuário sem Guild falha nos recursos tenant-scoped;
- JWT continua sem `guildId` e `TenantService` resolve a Guild a partir do usuário persistido;
- testes passam sem dependência de segredo ou banco local.

## Segurança e riscos

- **crítico:** senha em texto claro se a implementação não usar `PasswordEncoder`;
- **alto:** IDOR/escalação de privilégio se User não for consultado pela Guild atual;
- **médio:** papel presente no JWT pode permanecer obsoleto após alteração do papel; avaliar derivação no banco;
- abuso de registro público não será mitigado com CAPTCHA/rate limiting nesta task, mas deve ser documentado como limitação de portfólio.
