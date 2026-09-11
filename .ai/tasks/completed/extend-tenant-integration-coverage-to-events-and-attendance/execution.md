# Execução — Event e Attendance Tenant Integration

## Implementação

- Criado `backend/src/test/java/com/example/Liderum/Tenancy/EventAttendanceTenantIsolationIntegrationTest.java`.
- O teste usa `@SpringBootTest`, `MockMvc`, onboarding e login reais, JWT real, `TenantService`, repositories, H2 e Flyway. As fixtures de Guild A/B são transacionais e independentes de seed ou ambiente externo.
- O publisher RabbitMQ é mockado somente no contexto de teste para impedir dependência de rede; a cadeia HTTP, Security, tenant e persistência continua real.
- Foi necessário um único ajuste de produção, explicitamente autorizado: `EventServiceImpl` agora lança `EntityNotFoundException` quando o Event não pertence à Guild corrente. O handler existente converte essa exceção em HTTP 404, alinhando Event ao contrato de não enumeração já usado por Attendance.

## Evidências de execução

- A primeira execução revelou `RuntimeException` em `EventServiceImpl`, traduzida em 500 para leitura cross-Guild. Não houve acesso nem mutação cross-Guild. Após autorização, a exceção foi substituída por `EntityNotFoundException`.
- Investigação isolada confirmou `GET /events/{id}` cross-Guild com HTTP 404 pelo `ApiExceptionHandler`; update e delete passaram com a mesma fronteira.
- Teste dirigido: `./mvnw.cmd -Dtest=EventAttendanceTenantIsolationIntegrationTest test` — 8 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`.
- Suíte completa: `./mvnw.cmd clean verify` — 66 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS` em 2026-09-01.

## Cenários comprovados

- Event: listagem e criação ficam na Guild autenticada; leitura, atualização e exclusão cross-Guild retornam 404 e preservam o Event remoto.
- Attendance: listagem e leitura são tenant-scoped; update e delete cross-Guild retornam 404 e preservam o registro remoto.
- Referências cross-Guild de Member ou Event na criação de Attendance retornam 404 e não criam mutação parcial.
- Criação com Member e Event da mesma Guild é aceita.
- Um SOLDADO recebe 403 para mutações administrativas; a distinção entre RBAC e recurso cross-Guild (404) permanece verificável.

## Estado

- QA, Security e Auditoria aprovaram; a task foi finalizada em 2026-09-01.
