# Execution — establish-fullstack-ci-and-compose-demo-environment

## Implementação

- Compose inclui PostgreSQL 18.6, frontend Angular servido por Nginx e dependências de readiness.
- Backend Compose usa profile `prod`, datasource PostgreSQL local, Flyway e Hibernate `validate`.
- Workflow `fullstack.yml` adiciona testes/build Angular e testes/package do notification-service.
- Dockerfiles frontend e arquivos `.dockerignore` foram adicionados; nenhum segredo real foi incluído.

## Validação

- `docker compose config --quiet`: aprovado com `JWT_SECRET` efêmero de interpolação.
- Frontend `npm test -- --watch=false --browsers=ChromeHeadless`: 35/35 SUCCESS.
- Frontend `npm run build`: SUCCESS; warnings Sass existentes e não bloqueantes.
- Notification-service `mvn clean test`: 4 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS.
- Backend `mvnw.cmd clean verify`: 66 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS.

## Compose validado

- Após ajuste do volume para o layout PostgreSQL 18 (`/var/lib/postgresql`), `docker compose up -d --build` concluiu com exit code 0.
- PostgreSQL 18.6 iniciou saudável e aceitou conexões; RabbitMQ iniciou saudável.
- Backend, notification-consumer e frontend permaneceram em execução.
- Frontend respondeu HTTP 200; RabbitMQ Management respondeu HTTP 200.
- O endpoint raiz do backend respondeu HTTP 403, conforme a política de segurança atual; o processo permaneceu ativo.
- O primeiro build havia falhado porque o mount legado `/var/lib/postgresql/data` é incompatível com PostgreSQL 18; o volume descartável foi recriado sem manipulação de dados persistentes.

## Smoke local

- `POST /auth/register-guild`: 201 com dados descartáveis locais.
- `POST /auth/login`: 200; o token não foi registrado em arquivos ou logs.
- `GET /users/me`: 200 autenticado.
- `POST /events`: 200 com o contrato real `EventRequestDTO` (`name`, `date`, `description`).
- O notification-consumer registrou o recebimento do evento criado pelo RabbitMQ.
- Uma primeira tentativa usou o campo incorreto `title` e retornou 400, sem mutação; a repetição com o DTO real foi aprovada.
