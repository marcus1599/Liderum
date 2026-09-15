# QA — establish-fullstack-ci-and-compose-demo-environment

## Veredito

**APROVADO**

## Evidências

- Backend: `mvnw.cmd clean verify` — 66 testes, 0 failures, 0 errors, 0 skipped.
- Frontend: `npm test -- --watch=false --browsers=ChromeHeadless` — 35/35 aprovados; `npm run build` aprovado.
- Notification-service: `mvn clean test` — 4 testes, 0 failures, 0 errors, 0 skipped.
- Compose: build e subida aprovados após o mount compatível com PostgreSQL 18; PostgreSQL e RabbitMQ saudáveis, backend, consumer e frontend ativos.
- Smoke local: registro, login, perfil autenticado, criação de evento e consumo RabbitMQ aprovados.

## Observações

- Warnings Sass do frontend e vulnerabilidades reportadas pelo `npm ci` são preexistentes e não foram introduzidos nem alterados por esta task.
