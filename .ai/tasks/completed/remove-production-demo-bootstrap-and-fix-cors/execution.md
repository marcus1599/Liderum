# Execução — remove-production-demo-bootstrap-and-fix-cors

## Routing executado

- Domínios: backend, security, SRE/DevOps, testing e documentação.
- Agents: Backend Developer → QA → Security + SRE/DevOps → Auditor.
- Skills: `test-backend`, `security-review`, `audit-task`, `finish-task` condicional.
- Não selecionados: Frontend Developer, Database e `create-adr`; não houve alteração Angular, schema, migration, dependência ou decisão arquitetural duradoura.

## Implementação

- `DataInitializer` passou a ser carregado somente sob `@Profile("dev")`.
- A ativação implícita de `dev` foi removida de `application.properties`.
- `application-dev.properties` declara `liderum.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:4200}`; `prod` usa a mesma variável com fallback vazio.
- `CorsConfig` passou a fornecer `CorsConfigurationSource`, separando a lista CSV em origins individuais, com métodos e headers explícitos, sem wildcard.
- `SecurityConfig` habilita a integração CORS e remove o matcher de URL absoluta que não configurava CORS.
- Compose continua explicitamente em `dev`, agora expõe `CORS_ALLOWED_ORIGINS` para demonstração local; `.env.example` contém apenas origem local, sem segredo.
- README documenta que `dev` é explícito e o único profile que carrega dados de demonstração.
- Testes de integração que dependiam da configuração H2 de desenvolvimento agora ativam `dev` explicitamente; não dependem mais da configuração base ativar esse profile.

## Evidência de testes

### Direcionados

Comando:

```powershell
.\mvnw.cmd clean -Dtest=DataInitializerDefaultProfileIntegrationTest,DataInitializerDevProfileIntegrationTest,ProductionConfigurationIntegrationTest,CorsConfigurationIntegrationTest,GuildOnboardingIntegrationTest,MultiTenantIsolationIntegrationTest,RbacUserTenantBoundariesIntegrationTest test
```

Resultado nos relatórios Surefire: 25 testes, 0 failures, 0 errors, 0 skipped.

Durante a primeira execução, os testes existentes de onboarding falharam porque a remoção de `dev` implícito eliminou o `ddl-auto=update` que eles recebiam indiretamente. A causa foi confirmada por `Table "GUILDS" not found`; a correção restrita foi ativar `dev` explicitamente nos três testes de integração que dependiam dessa configuração e declarar `create-drop` no teste de profile default. A repetição direcionada passou integralmente.

### Suíte completa

Comando:

```powershell
.\mvnw.cmd clean verify
```

- Tests run: 51
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS
- Exit code: 0

Avisos não bloqueantes observados: suporte do Flyway à versão de H2 e ausência de migrations. Ambos pertencem à task P1 de Flyway já registrada e não foram alterados.

## Escopo confirmado

Não houve alteração de JWT, RBAC, TenantService, endpoints públicos, schema, Flyway, antiabuso, frontend, RabbitMQ, CI/CD ou dependências.
