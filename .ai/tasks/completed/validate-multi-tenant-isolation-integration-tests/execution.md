# Execução — Validar isolamento multi-tenant por testes de integração

## Implementação realizada

- Adicionado `backend/src/test/java/com/example/Liderum/Tenancy/MultiTenantIsolationIntegrationTest.java`.
- O teste usa `@SpringBootTest`, H2, repositórios e `TenantService` reais e um `SecurityContext` de teste explícito.
- Não houve alteração de código de produção, schema, JWT ou configuração de execução normal.

## Investigação do bloqueio JPA

Em 2026-08-18, a execução isolada com:

```text
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.10'; .\mvnw.cmd -Dtest=MultiTenantIsolationIntegrationTest test
```

produziu, durante uma execução Maven que se sobrepôs a outra instância ainda ativa, relatório Surefire com:

```text
Tests run: 6, Failures: 0, Errors: 6, Skipped: 0
```

A causa raiz ocorreu antes de qualquer cenário de isolamento:

```text
Not a managed type: class com.example.Liderum.Entities.User
```

O stack trace chegou à criação de `userRepository`, solicitada por `UserDetailsServiceImpl` e `JwtFilter`, e falhou no metamodelo JPA com `Not a managed type`. Não havia `@EntityScan`, `@EnableJpaRepositories`, `@ContextConfiguration`, `@Import`, `@DataJpaTest` ou `@TestConfiguration` no novo teste que pudesse substituir o contexto da aplicação.

Uma execução posterior, única e observada até o encerramento, carregou o mesmo `@SpringBootTest`, criou o contexto H2 e executou os seis cenários. Isso elimina configuração de package/entity scanning, H2 ou profile como causa persistente. A causa classificada é operacional: instâncias Maven sobrepostas e saída do wrapper devolvida antes da conclusão do processo filho tornaram inválido o primeiro diagnóstico de contexto.

Os processos Java posteriormente identificados como pertencentes ao VS Code não foram tocados. Nenhum processo preexistente foi encerrado.

## Correção de teste

- Ajustadas somente duas mensagens esperadas em `shouldRejectUnauthenticatedOrGuildlessTenantAccess` para os valores reais de `TenantService`:
  - `Authenticated user is required.`
  - `Authenticated user is not linked to a guild.`
- Não houve alteração de JPA, profiles, entidades, repositories ou código de produção.

## Resultados verificados

Teste isolado:

```text
.\mvnw.cmd -Dtest=MultiTenantIsolationIntegrationTest test
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

Suíte completa:

```text
.\mvnw.cmd clean verify
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
```

## Veredito parcial

- Nenhum acesso cross-tenant foi observado ou confirmado.
- A investigação JPA está concluída; QA e Security ainda não foram executados nesta rodada.
- A task permanece ativa por instrução, sem auditoria ou finalização.
