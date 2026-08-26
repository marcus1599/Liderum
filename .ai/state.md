# Liderum — Estado Atual do Projeto

> Documento de snapshot técnico do projeto.
>
> Este arquivo representa o estado conhecido do Liderum com base no código, configuração e histórico Git disponíveis.
>
> **Última inspeção:** 2026-08-24
>
> **Repositório:** `marcus1599/Liderum`
>
> **Branch principal:** `main`

---

# 1. Snapshot

## Estado geral

O roadmap SaaS canônico foi aprovado e está registrado em `roadmap.md`. A Fase 2 — Persistência versionada e contratos de API — está em execução após a conclusão das tasks da Fase 1. As tasks `secure-user-provisioning-and-guild-onboarding`, `enforce-rbac-and-user-tenant-boundaries`, `remove-production-demo-bootstrap-and-fix-cors` e `baseline-flyway-and-production-database-schema` foram concluídas. O backend possui onboarding transacional, User tenant-scoped, BCrypt, perfil próprio, hierarquia MARECHAL/GENERAL, RBAC administrativo, bootstrap demo exclusivo de `dev`, CORS configurável sem wildcard, schema Flyway validado e 57 testes backend aprovados. Todas têm Task Verdict **APROVADO**; a proteção antiabuso do registro público foi validada e o Release Verdict está **APROVADO**, com validação PostgreSQL real mantida como pendência operacional não bloqueante. ADR-001 formaliza User de Guild única e resolução server-side do tenant, sem `guildId` como autoridade no JWT.

### Consolidação de produto concluída (2026-08-18)

As correções de produto foram versionadas localmente. O sistema de agentes, tarefas e MCP em `.ai/` está pronto para seu commit de consolidação.

Correções de produto consolidadas:

* `fda42c5`: a correção do segredo JWT exige `JWT_SECRET`, sem fallback versionado; `.env.example` usa placeholders e `.env` permanece ignorado;
* `3f4e7b1`: quatro testes de serviço foram alinhados ao multi-tenancy já implementado;
* `87f5aaa`: as declarações redundantes de JUnit foram removidas do `pom.xml`;
* o contexto Spring usa uma propriedade JWT fictícia exclusiva de teste; a configuração normal continua fail-fast;
* a suíte backend foi validada: 24 testes, 0 failures e 0 errors.

Validação concluída nesta consolidação:

* MCP: `npm run build` e `npm test` executados com sucesso (14/14 testes).
* Backend: `./mvnw.cmd clean verify` executado com sucesso (24 testes, 0 failures, 0 errors).

O estado acima descreve as correções já presentes no histórico Git local e a consolidação `.ai/` pronta para versionamento.

O Liderum é uma aplicação web para gerenciamento de guildas de RPG.

O projeto atualmente possui três componentes principais:

* Backend (Spring Boot 3.2.0, Java 21);
* Frontend (Angular 19.2.0);
* Notification Service (Spring Boot 3.2.0, Java 21).

A aplicação possui funcionalidades de gerenciamento de usuários, membros, equipes, eventos e presença, além de autenticação JWT e suporte a multi-tenancy baseado em Guild.

Também existe comunicação assíncrona utilizando RabbitMQ para eventos de guild e um serviço separado para processamento de notificações.

---

# 2. Produto

## Objetivo

Centralizar a organização e gestão de guildas de RPG.

## Funcionalidades existentes

* gerenciamento de usuários;
* gerenciamento de membros;
* gerenciamento de equipes/grupos;
* criação e gerenciamento de eventos;
* controle de presença;
* autenticação;
* autorização;
* gerenciamento de guilds;
* isolamento multi-tenant;
* dashboard;
* notificações orientadas a eventos.

## Estado

O MVP funcional está implementado e o projeto encontra-se em fase de evolução e finalização das funcionalidades, principalmente no frontend, testes, qualidade, infraestrutura e evolução para SaaS.

---

# 3. Arquitetura atual

A aplicação possui uma arquitetura distribuída em componentes:

```text
                    Frontend
                     Angular
                        │
                        ▼
                  Backend API
                 Spring Boot
                        │
             ┌──────────┼──────────┐
             │          │          │
             ▼          ▼          ▼
          Database   RabbitMQ   Security
                        │
                        ▼
               Notification Service
```

O backend utiliza arquitetura em camadas.

O domínio principal está organizado em entidades, controllers, repositories, services e configurações.

---

# 4. Backend

## Stack

* Java 21
* Spring Boot 3.2.0
* Spring Web
* Spring Data JPA
* Hibernate
* Spring Security
* Spring AMQP
* Flyway
* PostgreSQL
* H2 (dev/test runtime)
* JWT (jjwt 0.11.5)
* Lombok 1.18.30
* Springdoc OpenAPI 2.2.0
* Spring Boot Actuator

## Estrutura observada

O backend contém, entre outros:

```text
src/main/java/com/example/Liderum/
├── config/
├── controllers/
├── dto/
├── entities/
├── repositories/
├── services/
└── security/
```

Existem controllers para:

* autenticação;
* usuários;
* membros;
* equipes;
* eventos;
* presença;
* administração.

## Estado

Backend funcional e em evolução.

---

# 5. Frontend

## Stack

* Angular 19.2.0
* TypeScript 5.7.2
* Angular Material 19.2.9
* RxJS 7.8.0
* Chart.js 4.5.0
* ng2-charts 4.1.1

## Estrutura observada

```text
src/app/
├── auth/
├── core/
├── dashboard/
├── events/
├── groups/
├── members/
├── attendence/
├── services/
└── settings/
```

Também existem componentes/configurações principais da aplicação e configuração de rotas.

## Estado

Frontend funcional em várias áreas, porém ainda em evolução e sujeito a finalização de fluxos, integração e melhorias de UX/UI.

---

# 6. Notification Service

Existe um serviço separado denominado `notification-service`.

## Responsabilidade conhecida

Processar eventos relacionados às notificações recebidos por mensageria.

## Estado

Serviço implementado.

Existe endpoint de health/status no serviço (`StatusController`).

---

# 7. Multi-tenancy

## Estado

Implementado.

Os testes de serviço que acompanham o isolamento por Guild foram ajustados e validados na task `align-service-tests-with-multi-tenancy`.

A task `validate-multi-tenant-isolation-integration-tests` acrescentou seis testes de integração reais para Member e Team, com H2, `SecurityContext`, repositories e `TenantService`. Eles validam a fronteira Guild em listagem, leitura, atualização, exclusão e referências cross-tenant; a suíte backend passou com 30 testes, sem failures ou errors.

## Tenant

A entidade `Guild` representa o tenant da aplicação.

## Contexto

Existe `TenantService` responsável pelo contexto atual da guild.

## Isolamento

Serviços e repositories foram adaptados para considerar a guild associada aos dados.

O acesso aos dados deve respeitar o tenant atual.

## Risco

Alterações futuras em funcionalidades de domínio devem preservar o isolamento entre guilds.

Qualquer alteração em autenticação, autorização ou acesso a dados deve considerar explicitamente o contexto do tenant.

---

# 8. Autenticação e autorização

## Estado

Autenticação baseada em JWT implementada.

O backend utiliza Spring Security.

Existe configuração específica de segurança.

## Pontos de atenção

* armazenamento e gerenciamento do segredo JWT;
* autorização baseada no usuário;
* isolamento entre tenants;
* exposição de dados;
* validação de endpoints administrativos.

## Remediação de logs JWT

O bloqueador de release causado por logs de Authorization/JWT em `JwtFilter` foi removido e coberto por teste de regressão. A remoção preservou a autenticação e não alterou segredo, claims, expiração ou autorização.

Alterações nessa área devem envolver o agente Security.

---

# 9. Banco de dados

## Tecnologias observadas

* PostgreSQL para ambiente principal;
* H2 disponível para runtime/testes;
* JPA/Hibernate;
* Flyway para migrations.

## Estado

Persistência implementada.

Alterações estruturais devem utilizar migrations versionadas.

---

# 10. Mensageria

## Tecnologia

RabbitMQ.

## Estado

Implementado.

## Uso conhecido

Eventos de guild são publicados para processamento assíncrono.

Existe configuração de:

* exchange;
* routing key;
* queue;
* conexão RabbitMQ (`addresses`).

O notification-service atua como consumidor.

---

# 11. Infraestrutura

## Docker

Existe `docker-compose.yml`.

Serviços atualmente definidos:

* RabbitMQ (`rabbitmq:3.13-management`);
* backend/producer (`liderum-producer`);
* notification-service/consumer (`notification-consumer`).

Também existem Dockerfiles para componentes da aplicação.

## Estado

Infraestrutura Docker existente.

---

# 12. CI/CD

Existe GitHub Actions.

Arquivo observado:

```text
.github/workflows/backend.yml
```

## Estado

CI do backend existente.

O workflow possui histórico recente de ajustes relacionados ao processo de build/verificação (`./mvnw clean verify`).

---

# 13. Testes

## Backend

O projeto possui:

* JUnit Jupiter fornecido por `spring-boot-starter-test` e gerenciado pelo Spring Boot;
* Mockito 5.3.1;
* Spring Boot Test;
* Spring Security Test;
* AssertJ 3.24.2.

Existem testes relacionados ao backend e às funcionalidades de notificação.

## Frontend

O projeto possui configuração de testes Angular utilizando:

* Jasmine 5.6.0;
* Karma 6.4.0;
* coverage (`karma-coverage`).

---

# 14. Segurança

## Correção do achado crítico de segurança

O segredo JWT hardcoded foi removido localmente de `docker-compose.yml` e `application-dev.properties`. A configuração agora exige `JWT_SECRET` explicitamente: Spring Boot usa `${JWT_SECRET}` e Docker Compose usa `${JWT_SECRET:?JWT_SECRET must be defined}`.

`.env` permanece ignorado e `.env.example` contém apenas placeholders. O teste de contexto Spring usa um segredo fictício exclusivo de teste, configurado na própria anotação do teste, sem depender de variável de ambiente. Essas mudanças foram commitadas localmente.

---

# 15. Documentação

O README descreve o produto e suas principais funcionalidades.

---

# 16. Estado das principais áreas

| Área                 | Estado                                    |
| -------------------- | ----------------------------------------- |
| Backend              | Implementado / em evolução                |
| Frontend             | Implementado parcialmente / em evolução   |
| Autenticação JWT     | Implementado; correção de segredo validada e commitada localmente |
| Autorização          | Implementado / necessita revisão contínua |
| Multi-tenancy        | Implementado                              |
| Guild                | Implementado                              |
| Membros              | Implementado                              |
| Equipes              | Implementado                              |
| Eventos              | Implementado                              |
| Presença             | Implementado                              |
| RabbitMQ             | Implementado                              |
| Notification Service | Implementado                              |
| Docker               | Implementado                              |
| Flyway               | Implementado; baseline V1 validada e `ddl-auto=validate` |
| OpenAPI              | Implementado                              |
| CI Backend           | Implementado                              |
| Testes Backend       | Validados localmente: 57 testes, 0 failures, 0 errors, 0 skipped |
| Testes Frontend      | Gate validado: 35 testes, 0 failures, 0 errors, 0 skipped em duas execuções; sessão/auth, onboarding, Users e rotas de domínio alinhados |
| Security Review      | Configuração JWT fail-fast validada e commitada localmente |
| Documentação         | Estrutura `.ai/docs/` criada; conteúdo ainda sob demanda |

## Pendências técnicas não bloqueantes

* migration validada automaticamente em H2; validação PostgreSQL real pendente.

* rotas protegidas de Dashboard, Members, Teams, Events, Attendance e Users estão disponíveis; Settings permanece não persistente;
* contratos frontend de Member/Team/Event/Attendance foram alinhados; update de Team agora aceita TeamRequestDTO e retorna TeamResponseDTO, preservando TenantService/RBAC;
* a task `route-domain-areas-and-add-http-ux-feedback` foi concluída com Task Verdict APROVADO; o bloqueador antiabuso mencionado em sua auditoria foi tratado posteriormente.
* a task `protect-public-guild-registration-against-abuse` foi concluída com Task Security Verdict, QA, SRE e Task Verdict APROVADOS; o registro público usa limitador local configurável e o Release Verdict foi liberado.
