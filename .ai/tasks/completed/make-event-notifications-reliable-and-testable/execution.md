# Execution — make-event-notifications-reliable-and-testable

## Implementação

- Backend RabbitMQ declara DLX e fila DLQ para `GuildEventCreated`.
- Consumer usa retry limitado a 3 tentativas e rejeição sem requeue infinito.
- Dispatcher deduplica mensagens processadas por `eventId` durante o ciclo de vida da instância e repropaga falhas para acionar retry.
- Nenhuma alteração em JWT, RBAC, tenancy, frontend, schema ou dependências.

## Validação

- Notification service: `mvn clean test` — 3 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS.
- Backend: `mvnw.cmd clean verify` — 66 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit code 0.
- RabbitMQ descartável: `-Drabbitmq.integration=true -Dtest=RabbitMqNotificationIntegrationIT test` — 1 teste, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; conversão JSON e entrega ao listener validadas.
- `git diff --check` limpo.

## Limitações

- A deduplicação atual é em memória e não sobrevive a restart; persistência distribuída exige task futura.
- O teste de integração requer broker descartável e é opt-in para manter a suíte padrão reproduzível.
