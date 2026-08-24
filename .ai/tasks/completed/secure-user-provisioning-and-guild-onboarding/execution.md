# Execução — Provisionamento seguro de usuários e onboarding de Guild

## Implementação

- Criado endpoint público `POST /auth/register-guild`.
- Criado serviço transacional que persiste Guild e primeiro User `MARECHAL` juntos.
- Senhas de onboarding e de usuários subsequentes usam `PasswordEncoder`/BCrypt.
- UserService passou a resolver Guild por `TenantService` e usar consultas por `guildId`.
- CRUD de User ficou restrito a `MARECHAL` e sem exposição de senha.
- JwtFilter passou a usar authorities do `UserDetails` persistido; nenhum `guildId` foi adicionado ao JWT.
- Mapeamentos de `EntityNotFoundException` e `AccessDeniedException` foram ajustados para 404/403, necessários para os contratos de isolamento/RBAC testados.

## Testes adicionados ou alterados

- `GuildOnboardingIntegrationTest`: 4 cenários HTTP de registro, hash, provisionamento, RBAC e leitura cross-tenant.
- `UserServiceImplTest`: 3 cenários de hash, Guild corrente e consultas tenant-scoped.
- `JwtFilterTest`: authorities carregadas do `UserDetails`, mantendo a regressão de ausência de logs sensíveis.

## Validação

Execução afetada, em cópia temporária reproduzível do backend para evitar lock da IDE:

```text
.\mvnw.cmd clean -Dtest=UserServiceImplTest,JwtFilterTest,GuildOnboardingIntegrationTest test
```

- Tests run: 8
- Failures: 0
- Errors: 0
- Skipped: 0
- Resultado: `BUILD SUCCESS`
- Exit code: 0

Suíte completa:

```text
.\mvnw.cmd clean verify
```

- Tests run: 34
- Failures: 0
- Errors: 0
- Skipped: 0
- Resultado: `BUILD SUCCESS`
- Exit code: 0

O `target` do repositório não foi usado como fonte de resultado porque estava bloqueado por processos preexistentes da IDE; a execução final foi feita em cópia temporária do working tree atual. Nenhuma dependência de segredo local foi usada.
