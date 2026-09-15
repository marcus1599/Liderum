# Liderum

Aplicação web para organizar guildas de RPG. O Liderum centraliza usuários, membros, equipes, eventos e presença, com autenticação JWT, isolamento por guilda e notificações assíncronas.

## Funcionalidades

- Autenticação e autorização com JWT.
- Gerenciamento de guildas, usuários, membros e equipes.
- Criação de eventos e controle de presença.
- Isolamento multi-tenant: cada `Guild` é o tenant da aplicação.
- Dashboard e interface Angular para os fluxos principais.
- Eventos de guilda publicados via RabbitMQ e consumidos pelo serviço de notificações.
- Documentação OpenAPI/Swagger e endpoints de Actuator no backend.

## Arquitetura

```text
Angular frontend
      |
      v
Spring Boot backend ---- PostgreSQL / H2
      |
      v
   RabbitMQ
      |
      v
Notification Service
```

O repositório contém três aplicações:

- `frontend/`: cliente Angular.
- `backend/`: API principal Spring Boot.
- `notification-service/`: consumidor de eventos RabbitMQ.

## Tecnologias

| Área | Tecnologias |
| --- | --- |
| Backend | Java 21, Spring Boot 3.2, Spring Security, JPA/Hibernate, Flyway, PostgreSQL, H2 |
| Frontend | Angular 19, TypeScript, Angular Material, RxJS, Chart.js |
| Integração | RabbitMQ, Spring AMQP |
| API e operação | Springdoc OpenAPI, Actuator, Docker Compose |
| Testes | JUnit 5, Mockito, Spring Boot Test, Jasmine e Karma |

## Pré-requisitos

- Java 21
- Node.js e npm
- Docker Desktop e Docker Compose (recomendado para executar todos os serviços)

## Configuração de ambiente

Copie o exemplo de variáveis antes de executar o backend ou os containers:

```powershell
Copy-Item .env.example .env
```

Defina um valor aleatório, secreto e com pelo menos 32 bytes para `JWT_SECRET` em `.env`.

`JWT_SECRET` é obrigatório. A aplicação não possui fallback versionado para esse segredo e o arquivo `.env` não deve ser commitado.

## Executando com Docker Compose

Com o `.env` configurado:

```powershell
docker compose up --build
```

Serviços expostos localmente:

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:4200`
- Notification Service: `http://localhost:8081`
- PostgreSQL: `localhost:5432` (somente desenvolvimento local)
- RabbitMQ Management: `http://localhost:15672`

O Compose usa PostgreSQL vazio com Flyway e `ddl-auto=validate`; o profile `prod` é usado dentro do ambiente local para reproduzir a configuração de produção. Registre uma Guild pela interface em `http://localhost:4200/register`. Para resetar somente o banco descartável local, pare os serviços e execute `docker compose down -v`.

Verificações operacionais: `GET http://localhost:8080/actuator/health` (backend) e `GET http://localhost:8081/actuator/health` (notification-service) retornam somente o status de disponibilidade, sem detalhes internos. PostgreSQL e RabbitMQ possuem healthchecks nativos no Compose; use `docker compose ps` para confirmar os estados.

## Desenvolvimento local

### Backend

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE = "dev"
.\mvnw.cmd spring-boot:run
```

O profile `dev` usa H2 e é o único que carrega dados de demonstração. Sem profile explícito, a aplicação não cria dados demo. Para documentação da API, acesse `http://localhost:8080/swagger-ui/index.html` após iniciar o backend.

### Frontend

```powershell
cd frontend
npm install
npm start
```

### Notification Service

```powershell
cd notification-service
.\mvnw.cmd spring-boot:run
```

## Testes e validação

Backend:

```powershell
cd backend
.\mvnw.cmd clean verify
```

A última validação local executou 66 testes, sem failures, errors ou skipped.

Frontend:

```powershell
cd frontend
npm test
npm run build
```

## CI

Os workflows do GitHub Actions validam o backend/Flyway em `.github/workflows/backend.yml` e, em `.github/workflows/fullstack.yml`, executam testes/build do frontend e testes/package do notification-service.

## Roteiro de demonstração

1. Suba o ambiente com `docker compose up --build`.
2. Abra `http://localhost:4200/register` e crie uma Guild descartável.
3. Faça login e confirme o perfil em `/users/me`.
4. Crie um evento e observe o processamento pelo notification-service nos logs do Compose.
5. Use duas contas/Guilds para demonstrar que cada consulta permanece isolada por tenant.

O diagrama resumido da arquitetura está em `.ai/docs/architecture-demo.md`.

## Desenvolvimento assistido por IA

O diretório `.ai/` é versionado e contém o sistema de desenvolvimento assistido do projeto:

- regras, snapshot técnico e handoff operacional;
- oito Agents e nove Skills reutilizáveis;
- tasks, auditorias e documentação de decisões;
- Router declarativo de Agent/Skill;
- servidor MCP somente leitura para contexto seguro do repositório.

Para configurar e testar o MCP localmente, consulte [.ai/mcp/README.md](.ai/mcp/README.md).

## Contribuição

Antes de abrir uma alteração, verifique o estado do Git, mantenha o escopo controlado e execute as validações proporcionais ao impacto. Mudanças em autenticação, autorização, multi-tenancy, secrets e dados sensíveis exigem revisão de segurança.

## Autor

Desenvolvido por Marcus Ferreira.
