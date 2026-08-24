# QA — remove-production-demo-bootstrap-and-fix-cors

## Veredito

**APROVADO**

## Evidências

- `DataInitializerDefaultProfileIntegrationTest` verifica que Guild, User, Member e Event permanecem vazios sem `dev`.
- `DataInitializerDevProfileIntegrationTest` verifica a criação esperada de Guild, MARECHAL, 50 Members e 3 Events somente em `dev`.
- `ProductionConfigurationIntegrationTest` valida profile `prod`: sem seeds e preflight cross-origin bloqueado quando a lista está vazia.
- `CorsConfigurationIntegrationTest` prova duas origins permitidas individualmente, sem wildcard, e bloqueia origin fora da lista.
- `GuildOnboardingIntegrationTest`, `MultiTenantIsolationIntegrationTest` e `RbacUserTenantBoundariesIntegrationTest` continuam aprovados com profile `dev` explícito, preservando login, onboarding e isolamento/RBAC.
- Suíte completa: 51 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`, exit code 0.

## Avaliação

Os testes usam Spring Boot, JPA/H2 e MockMvc reais, sem estado externo e sem variável de ambiente da máquina. Não há ajuste de asserção para mascarar falhas: a única adequação de testes tornou explícito o profile que já fornecia H2/DDL antes da remoção de `dev` implícito.
