# Execução — RBAC e fronteiras tenant de User

## Implementação

- Criados contratos explícitos `UserCreateRequestDTO` e `UserRoleUpdateRequestDTO`; nenhum contrato aceita `guildId` ou atualização genérica de entidade.
- `GET /users/me` resolve o User autenticado persistido, sem ID fornecido pelo cliente.
- `POST /users`, `GET /users`, `GET /users/{id}`, `PUT /users/{id}/role` e `DELETE /users/{id}` aceitam somente `MARECHAL` ou `GENERAL` no controller; o service reaplica a política de papel e Guild.
- `GENERAL` cria/gerencia somente `MAJOR`, `CAPITÃO` e `SOLDADO`; não pode agir sobre `GENERAL` ou `MARECHAL`.
- `MARECHAL` pode administrar todos os papéis. Alterar/remover um `MARECHAL` bloqueia quando ele é o último da Guild.
- A verificação do último `MARECHAL` executa dentro de transação e bloqueia pessimisticamente os MARECHAIS da Guild antes de decidir, evitando corrida entre remoções/rebaixamentos concorrentes no mesmo tenant.
- Team recebeu guards de papel em `add-member` e `update`; `AdminController` passou a usar `hasAnyRole` corretamente.
- JWT e `TenantService` não foram alterados: tenant continua vindo do User persistido, sem claim `guildId` autoritativa.

## Testes

Novo teste HTTP/integrado: `RbacUserTenantBoundariesIntegrationTest` (10 cenários), cobrindo GENERAL, MARECHAL, papéis inferiores, perfil próprio, cross-Guild, último MARECHAL, Team e AdminController.

Atualizados:

- `UserServiceImplTest` para o contrato explícito e as regras de GENERAL/último MARECHAL;
- `GuildOnboardingIntegrationTest` para usar o contrato de criação com campo `role`.

### Testes direcionados

```text
.\mvnw.cmd clean -Dtest=UserServiceImplTest,GuildOnboardingIntegrationTest,RbacUserTenantBoundariesIntegrationTest test
```

- Tests run: 19
- Failures: 0
- Errors: 0
- Skipped: 0
- Resultado: `BUILD SUCCESS`
- Exit code: 0

### Suíte completa

```text
.\mvnw.cmd clean verify
```

- Tests run: 46
- Failures: 0
- Errors: 0
- Skipped: 0
- Resultado: `BUILD SUCCESS`
- Exit code: 0

As duas execuções usaram cópias temporárias reproduzíveis do backend devido ao lock preexistente da IDE em `backend/target`; nenhuma fonte do repositório foi alterada por essa estratégia.
