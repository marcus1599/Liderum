# Liderum — Catálogo de Tecnologias e Dependências

> Documento de referência para todas as tecnologias, bibliotecas e dependências utilizadas no projeto Liderum.
>
> **Regra:** Não inventar versões ou bibliotecas. Todas as entradas devem ser derivadas diretamente dos arquivos de configuração reais do projeto (`pom.xml`, `package.json`, Dockerfiles, `docker-compose.yml`, workflows).

---

## 1. Runtime & Ambientes Principais

| Tecnologia | Versão | Local de Configuração | Finalidade |
| --- | --- | --- | --- |
| **Java** | 21 | `backend/pom.xml`, `notification-service/pom.xml`, `.github/workflows/backend.yml` | Linguagem runtime principal do Backend e Notification Service |
| **Node.js** | 24.x | Ambiente de desenvolvimento / build do frontend | Runtime para ferramentas frontend e scripts local |
| **TypeScript** | 5.7.2 | `frontend/package.json` | Linguagem fortemente tipada para a aplicação Angular |
| **Docker / Compose** | Specification v3+ | `docker-compose.yml`, `Dockerfile` | Containerização e orquestração dos serviços em ambiente local/dev |

---

## 2. Backend API (`backend/pom.xml`)

### Framework Base & Core

* **Spring Boot Parent**: `3.2.0`
* **Spring Boot Starter Web**: `3.2.0` (REST Controllers, Jackson JSON integration)
* **Spring Boot Starter Actuator**: `3.2.0` (Métricas e endpoints de monitoramento/health)
* **Spring Boot DevTools**: `3.2.0` (Hot reload e experiência de desenvolvimento)
* **Lombok**: `1.18.30` (Geração de getters/setters/constructors/builders)

### Persistência & Banco de Dados

* **Spring Boot Starter Data JPA**: `3.2.0` (ORM, Repositories, JPA Specs)
* **Hibernate Validator / Jakarta Validation**: Integration Spring Boot (Validação de DTOs e entidades)
* **PostgreSQL Driver**: Runtime Spring Boot (Driver JDBC para banco PostgreSQL de produção/dev)
* **H2 Database**: Runtime Spring Boot (Banco em memória para runtime/testes rápidos)
* **Flyway Core**: Integration Spring Boot (Gerenciamento de migrations de banco de dados)

### Segurança & Autenticação

* **Spring Boot Starter Security**: `3.2.0` (Filtros de segurança, autorização, BCrypt password encoder)
* **Spring Security Crypto**: Integration (Hashing seguro de senhas)
* **JJWT (Java JWT)**: `0.11.5` (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) (Geração e validação de tokens JWT)

### Mensageria

* **Spring Boot Starter AMQP**: `3.2.0` (Integração com RabbitMQ para eventos assíncronos)
* **Jackson Datatype JSR310**: Integration (Serialização/deserialização de datas Java 8+ `LocalDate`/`LocalDateTime`)

### Documentação API

* **Springdoc OpenAPI Starter WebMVC UI**: `2.2.0` (Geração automática de documentação Swagger UI e OpenAPI v3 spec)

### Testes Backend

* **Spring Boot Starter Test**: `3.2.0` (Suíte base de testes Spring)
* **JUnit Jupiter**: `5.10.0` (Engine e anotações para testes unitários e de integração)
* **Mockito Core & JUnit Jupiter**: `5.3.1` (Framework de mocking)
* **Spring Security Test**: Integration (Utilitários para simular contexto de segurança e usuários autenticados)
* **AssertJ Core**: `3.24.2` (Assertions fluentes para testes)

---

## 3. Frontend (`frontend/package.json`)

### Framework & UI

* **Angular Core / Common / Compiler / Forms / Router / Platform-Browser**: `^19.2.0`
* **Angular CDK**: `^19.2.19` (Component Development Kit para acessibilidade e comportamentos UI)
* **Angular Material**: `^19.2.9` (Biblioteca de componentes visuais baseada em Material Design)
* **Angular Animations**: `^19.2.6` (Suporte a transições e animações de interface)

### Gráficos & Visualização de Dados

* **Chart.js**: `^4.5.0` (Biblioteca de gráficos JavaScript)
* **ng2-charts**: `^4.1.1` (Wrapper Angular para Chart.js)

### Reatividade & Utilitários

* **RxJS**: `~7.8.0` (Programação reativa com Observables)
* **tslib**: `^2.3.0` (Helpers de runtime TypeScript)
* **zone.js**: `~0.15.0` (Gerenciamento de contextos assíncronos no Angular)

### Testes & Tooling Frontend

* **Angular CLI & Build Angular**: `^19.2.7`
* **Karma**: `~6.4.0` (Test runner para Angular)
* **Karma Chrome Launcher**: `~3.2.0`
* **Karma Coverage**: `~2.2.0` (Relatórios de cobertura de código)
* **Karma Jasmine HTML Reporter**: `~2.1.0`
* **Jasmine Core**: `~5.6.0` (Framework BD/TDD de testes unitários em JavaScript/TypeScript)
* **@types/jasmine**: `~5.1.0`

---

## 4. Notification Service (`notification-service/pom.xml`)

* **Spring Boot Starter Parent**: `3.2.0`
* **Java**: `21`
* **Spring Boot Starter Web**: `3.2.0` (Endpoints HTTP como `StatusController`)
* **Spring Boot Starter AMQP**: `3.2.0` (Consumidor de filas RabbitMQ)
* **Jackson Datatype JSR310**: Integration
* **Spring Boot Starter Actuator**: `3.2.0`

---

## 5. Infraestrutura & Containers (`docker-compose.yml`)

* **RabbitMQ Container**: `rabbitmq:3.13-management` (Portas: `5672` AMQP, `15672` Management UI)
* **Liderum Producer Container**: Backend Java (Porta `8080`)
* **Notification Consumer Container**: Notification Service (Porta `8081`)

---

## 6. Regras de Atualização da Biblioteca (`lib.md`)

1. **Nunca adicionar bibliotecas especulativas**: Adicionar uma dependência aqui apenas quando for incluída no código real do projeto.
2. **Verificar vulnerabilidade**: Antes de propor novas bibliotecas, o agente `Security` deve verificar se existem CVEs conhecidas.
3. **Manter alinhamento de versões**: Se a versão de um pacote for alterada nos arquivos de build, este documento deve ser atualizado.
