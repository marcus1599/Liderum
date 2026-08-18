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
- Notification Service: `http://localhost:8081`
- RabbitMQ Management: `http://localhost:15672`

## Desenvolvimento local

### Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

O profile de desenvolvimento usa H2. Para documentação da API, acesse `http://localhost:8080/swagger-ui/index.html` após iniciar o backend.

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

A última validação local executou 24 testes, sem failures ou errors.

Frontend:

```powershell
cd frontend
npm test
npm run build
```

## CI

O workflow do GitHub Actions em `.github/workflows/backend.yml` executa `./mvnw clean verify` com Java 21 para a branch `main`.

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
