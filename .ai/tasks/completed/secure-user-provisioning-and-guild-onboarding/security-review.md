# Security Review — Provisionamento seguro de usuários e onboarding de Guild

## Superfície analisada

- registro público de Guild e primeiro administrador;
- hash e exposição de senha;
- autenticação JWT e authorities;
- User CRUD e RBAC;
- TenantService e consultas por Guild;
- leitura cross-tenant/IDOR;
- logs e secrets no diff.

## Evidências

- `GuildOnboardingServiceImpl` é `@Transactional`, associa o primeiro User à Guild criada, atribui `MARECHAL` e usa `PasswordEncoder`.
- `UserServiceImpl` usa `getCurrentGuildId()`, `findAllByGuildId` e `findByIdAndGuildId`; não usa CRUD global para operações administrativas.
- `UserController` exige `hasRole('MARECHAL')` em todas as operações.
- `JwtFilter` não adiciona `guildId` nem usa roles do token como autoridade final; usa authorities do `UserDetails` carregado pelo username.
- Respostas `UserResponseDTO` não possuem senha.
- `GuildOnboardingIntegrationTest` comprovou 4 cenários HTTP, incluindo hash, RBAC e leitura cross-tenant.
- Nenhum secret, token ou credencial foi adicionado ao diff.

## Achados

| Achado | Evidência | Origem | Severidade | Tratamento |
|---|---|---|---|---|
| Registration público sem rate limiting/CAPTCHA | Endpoint público deliberado no PRD; abuso não é mitigado nesta task | PREEXISTENTE_RELACIONADO | média | Limitação registrada; avaliar proteção antiabuso em task própria antes de exposição pública ampla |
| Profiles ainda podem iniciar bootstrap demo em dev | `DataInitializer` é comportamento existente e fora do escopo | PREEXISTENTE_NAO_RELACIONADO | média | Encaminhar para `remove-production-demo-bootstrap-and-fix-cors` |
| Migrations Flyway inexistentes | Warning “No migrations found” na execução; Fase 2 do roadmap | PREEXISTENTE_NAO_RELACIONADO | média | Encaminhar para `baseline-flyway-and-production-database-schema` |

Não foi observado acesso cross-tenant, armazenamento em texto claro, senha em resposta/log, `guildId` autoritativo no JWT ou elevação de privilégio introduzida.

## Vereditos

**Task Security Verdict: APROVADO.** A task introduziu controles de hash, tenant e RBAC sem vulnerabilidade confirmada no próprio diff.

**Release Verdict: BLOQUEADO.** O estado global ainda possui pendências preexistentes de bootstrap demo, migrations ausentes e proteção antiabuso de registro. Esses achados não foram introduzidos pela task e não impedem validar sua alteração, mas não permitem declarar o sistema pronto para deploy público.
