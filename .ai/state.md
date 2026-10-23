# Liderum — Estado Atual do Projeto

> Documento de snapshot técnico do projeto.
>
> Este arquivo representa o estado conhecido do Liderum com base no código, configuração e histórico Git disponíveis.
>
> **Última inspeção:** 2026-10-21
>
> **Repositório:** `marcus1599/Liderum`
>
> **Branch principal:** `main`

---

# 1. Snapshot

## Estado geral

### Evolução pós-MVP (2026-09-15)

- Task operacional `rollout-authenticated-password-change-and-jwt-transition` concluída em 2026-10-17 com QA, Security, SRE/DevOps e Task Verdict APROVADOS. GitHub/CI da revisão `fb0b9a4` confirmados; Render já a havia autoimplantado e marcou a revisão anterior `deactivated`. PostgreSQL 18.6 registrou V4 com sucesso, Hibernate/JPA e Tomcat iniciaram, e health retornou 200/UP. Smoke de conta descartável aprovou registro, login, perfil, rota tenant-scoped, troca de senha, revogação do JWT anterior e novo login. O request direto com JWT legado assinado não foi executado por ausência de artefato de smoke pré-deploy; o usuário aceitou explicitamente a evidência equivalente de teste de integração assinado na revisão implantada, CI, deploy antigo desativado e revogação em produção. Release Verdict permanece BLOQUEADO por erro AMQP 406 da fila de notificações observado também em 2026-10-13, achado preexistente não relacionado a este rollout; requer diagnóstico separado. Evidências: `tasks/completed/rollout-authenticated-password-change-and-jwt-transition/audit.md`.

- Task `safely-deactivate-and-reactivate-staff-users` concluída em 2026-10-17 com QA, Task Security e Auditor APROVADOS. `PUT /users/{id}/status` permite somente ACTIVE↔DISABLED, preserva senha hash, incrementa credentialVersion ao desativar e requer novo login ao reativar. Tenant/RBAC, JWT revogado, proteção concorrente do último MARECHAL ACTIVE e bloqueio 409 seguro para líder de Team foram validados. Backend `clean verify`: 86 testes; frontend: 73/73 e build SUCCESS. Task Verdict APROVADO; Release Verdict continua BLOQUEADO somente pelo erro RabbitMQ AMQP 406 preexistente e adiado. Evidências: `tasks/completed/safely-deactivate-and-reactivate-staff-users/audit.md`.

- Task `reassign-team-leader-before-staff-deactivation` concluída em 2026-10-19 com QA, Security e Auditor aprovados. Leaders de Team podem ser transferidos explicitamente para outro User ACTIVE da mesma Guild; candidatos retornam apenas ID/username, as operações permanecem tenant-scoped e `POST /teams` revalida o líder padrão sob lock. Backend `clean verify`: 92 testes; Angular: 79/79 e production build SUCCESS. Task Verdict APROVADO; Release Verdict permanece BLOQUEADO pelo achado RabbitMQ AMQP 406 preexistente e adiado. Evidências: `tasks/completed/reassign-team-leader-before-staff-deactivation/audit.md`.

- Task `align-domain-action-visibility-with-rbac` concluída com QA, Security e Auditor aprovados. Members, Teams, Events e Attendance exibem mutações apenas a MARECHAL/GENERAL/MAJOR; perfis CAPITÃO/SOLDADO mantêm leitura permitida. Role vem de `/users/me`; backend/RBAC não foram alterados. Angular: 86/86 testes e production build SUCCESS. Task Verdict APROVADO; Release Verdict permanece BLOQUEADO pelo achado RabbitMQ AMQP 406 preexistente e adiado. Evidências: `tasks/completed/align-domain-action-visibility-with-rbac/audit.md`.

- Task `enable-accessible-team-member-assignment` concluída com QA, Security e Auditor aprovados. `/teams` permite associar Member disponível a Team por teclado/toque além de drag-and-drop; sem mudança de API ou backend. Angular: 92/92 em duas execuções consecutivas e production build SUCCESS. Não houve inspeção visual autenticada desktop/viewport estreito. Release Verdict permanece BLOQUEADO pelo achado RabbitMQ AMQP 406 preexistente e adiado. Evidências: `tasks/completed/enable-accessible-team-member-assignment/audit.md`.

- Task `validate-authenticated-responsive-ux` concluída com Task Verdict APROVADO. Sete rotas foram revisadas autenticadas em 1280×800 e 390×844; corrigidos em SCSS altura responsiva dos campos de Events e contraste da página Users. Angular 92/92, build SUCCESS e detector Impeccable sem achados. A página observada em produção não foi atualizada após as correções; 401/403, mutações, role/Guild alternativa e aparelho físico não foram testados. Release Verdict permanece BLOQUEADO pelo RabbitMQ AMQP 406 preexistente. Evidências: `tasks/completed/validate-authenticated-responsive-ux/audit.md`.

- Task `add-authenticated-password-change` concluída em 2026-10-15 com QA, Security, SRE/DevOps e Task Verdict APROVADOS. Backend `clean verify`: 81 testes; PostgreSQL 18.6 fresh/upgrade: 3/3; frontend: 70/70 e build SUCCESS. V4 aditiva preserva Users e defaults de credential version; Hibernate `validate` e contextos `prod` passaram. Implementação e testes versionados em `63fb032`. O Release Verdict daquela auditoria ficou BLOQUEADO até rollout coordenado, realizado posteriormente na task operacional acima; durante a execução da task de implementação não houve deploy ou push. A inspeção visual autenticada desktop/handset não foi manual. ADR-002 recomendado, não criado sem autorização explícita; ADR-001 intacto. Referência: `tasks/completed/add-authenticated-password-change/audit.md`.

- Task `standardize-domain-http-feedback-states` concluída em 2026-10-13 com QA, Task Security e Task Verdict APROVADOS; Release Verdict APROVADO. Members, Teams, Events e Attendance distinguem loading, vazio real, erro e confirmação de mutações; 65/65 testes Angular e build de produção passaram (exit 0). Não houve sessão manual autenticada em desktop/handset. Possível exposição de detalhes nos handlers backend e RBAC visual de domínio incompleto permanecem achados preexistentes a avaliar separadamente. Versionada em `2e60125` e `c74e528`, com push confirmado pelo usuário e Git sincronizado na abertura do planejamento seguinte.

- As seis fases do roadmap original estão concluídas e não serão reabertas.
- `product-evolution-roadmap.md` inicia a evolução do MVP demonstrável para uso real por uma Guild, preservando ADR-001 e a separação Guild/User/Member.
- O ambiente `.ai` inclui a skill `ux-review`, integrada ao Frontend Dev, QA, Planner e Router para revisão de UX Angular. A skill de projeto Impeccable está instalada em `.agents/skills/impeccable/` para uso manual no Codex, sem hooks automáticos. A primeira task do Ciclo 3, `consolidate-authenticated-navigation-and-expose-users`, foi concluída com Task Verdict e Release Verdict APROVADOS. Dashboard e áreas autenticadas usam o shell compartilhado, e Users está acessível no menu somente para MARECHAL/GENERAL conforme perfil de `/users/me`.
- Ciclo 1 concluído. A task `enforce-relational-database-integrity` foi implementada e validada em H2 e PostgreSQL 18.6 (fresh e upgrade); `clean verify` passou com 74 testes. Em 2026-10-13 Render aplicou V3 após o deploy `dep-db76ikcs728c73dk48og`; Flyway history, constraints, Hibernate/JPA e startup live foram confirmados. Health respondeu 200 e CORS passou nos casos permitido/negado. O rollout V3 foi concluído com Task Verdict e Release Verdict APROVADOS após smoke autenticado na rota tenant-scoped `/members`.
- Task `rollout-flyway-v3-to-render-production` concluída em 2026-10-13: commits `f860b2a` e `93caf70` publicados; primeira tentativa de build encontrou HTTP 429 do Maven Central, retry do mesmo commit ficou live. Preflight read-only não encontrou violações candidatas; dados de teste foram declarados descartáveis, dispensando backup recuperável neste ambiente de teste. O usuário autenticou-se no frontend; login/guard só completam após `/users/me` responder com sucesso, e `/members` carregou o estado vazio normal. O status HTTP autenticado direto não foi capturado; nenhuma credencial/token foi lida ou exposta.
- A task estrutural `secure-staff-user-activation-flow` foi concluída com Task Verdict APROVADO após validar lifecycle PENDING/ACTIVE/DISABLED, ativação/regeneração, RBAC/tenant, migration V2 e testes de replay/concorrência.
- Validação final em 2026-10-07: backend `clean verify` 72 testes, 0 failures/errors/skipped; PostgreSQL 18.6 descartável fresh/upgrade 2/2; frontend 35/35 em duas execuções e build SUCCESS. QA, Security, SRE/DevOps e Auditor aprovaram; Release Verdict APROVADO.

### Recuperação de produção Flyway (2026-08-26)

- O banco legado do Render foi substituído por PostgreSQL 18.6 vazio em Oregon; V1 aplicou com sucesso em produção, Hibernate `validate` e Spring/Tomcat iniciaram. A causa raiz permanece o schema legado sem `flyway_schema_history` (SQLState `42P07`), não a V1 nem o warning Flyway/PostgreSQL 18.
- `fix-production-flyway-baseline-postgresql` foi concluída com Task Verdict APROVADO. A remediação posterior de rate limit/CORS também foi concluída, sem alterar a conclusão da recuperação do banco.
- O rate limiting do registro público foi corrigido em produção com `CF-Connecting-IP` confiado exclusivamente no profile `prod` do Render/Cloudflare; smoke final retornou `400,400,400,400,400,429`. `CORS_ALLOWED_ORIGINS` foi configurada para a origin Vercel e validada positiva e negativamente.
- Na conclusão da recuperação Flyway de 2026-08-26, o Release Verdict estava **APROVADO**. O veredito global atual está BLOQUEADO pelo achado RabbitMQ posterior. A validação PostgreSQL real de migrations no CI foi implementada depois, na task `validate-flyway-migrations-against-postgresql-in-ci`.
- A task `extend-tenant-integration-coverage-to-events-and-attendance` foi concluída com Task Verdict e Task Security Verdict APROVADOS. Oito cenários HTTP reais comprovam as fronteiras tenant de Event e Attendance, incluindo referências cross-Guild sem mutação parcial. `validate-flyway-migrations-against-postgresql-in-ci` foi concluída com Task Verdict, Security e SRE/DevOps aprovados; o GitHub Actions run #15 validou PostgreSQL 18.6, V1 e Hibernate `ddl-auto=validate` em banco descartável.
- A task `make-event-notifications-reliable-and-testable` foi concluída: RabbitMQ usa retry/DLQ e idempotência proporcional, com testes unitários e integração. `establish-fullstack-ci-and-compose-demo-environment` também foi concluída: Compose local validado com PostgreSQL 18.6, RabbitMQ, backend, notification-consumer e frontend; CI cobre frontend e notification-service, e o smoke local confirmou registro, login, tenant, criação de evento e consumo assíncrono.
- A task `add-proportional-observability-and-portfolio-documentation` foi concluída: Actuator health seguro para backend e notification-service, documentação operacional, diagrama Mermaid e roteiro reproduzível de demonstração foram adicionados e validados no Compose. A Fase 6 do roadmap está concluída.

O roadmap SaaS canônico está registrado em `roadmap.md`. A recuperação Flyway V1, a integridade relacional V3 e o rollout de V3 para Render foram concluídos; PostgreSQL 18.6 validou migrations em ambiente descartável e Render aplicou V3 com Hibernate `validate`. O rollout teve Task Verdict e Release Verdict APROVADOS em 2026-10-13; o status HTTP autenticado não foi capturado diretamente, mas login/guard completaram `/users/me` e a rota `/members` exibiu o estado vazio normal. O warning de suporte testado Flyway/PostgreSQL 18 continua dívida técnica não bloqueante. As fundações de identidade/tenancy e os fluxos planejados da Fase 3 estão concluídos; o backend mantém onboarding transacional, User tenant-scoped, BCrypt, perfil próprio, hierarquia MARECHAL/GENERAL, RBAC, bootstrap demo exclusivo de `dev`, CORS configurável sem wildcard e schema Flyway com `ddl-auto=validate`. ADR-001 formaliza User de Guild única e resolução server-side via `TenantService`, sem `guildId` como autoridade no JWT.

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

* PostgreSQL (`postgres:18.6`);
* RabbitMQ (`rabbitmq:3.13-management`);
* backend/producer (`liderum-producer`);
* notification-service/consumer (`notification-consumer`).
* frontend Angular servido por Nginx (`frontend`).

Também existem Dockerfiles para componentes da aplicação.

## Estado

Infraestrutura Docker existente.

---

# 12. CI/CD

Existe GitHub Actions.

Arquivos observados:

```text
.github/workflows/backend.yml
.github/workflows/fullstack.yml
```

## Estado

CI valida backend/Flyway em PostgreSQL 18.6, frontend e notification-service.

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
| Testes Backend       | Último `clean verify` da task de reassociação de líder: 92 testes, 0 failures/errors/skipped, BUILD SUCCESS; validações PostgreSQL anteriores permanecem documentadas nas respectivas tasks |
| Testes Frontend      | Última suíte completa: 92/92, exit code 0 em duas execuções consecutivas; build Angular SUCCESS. Warnings Sass/CSS/baseline-browser-mapping permanecem não bloqueantes. |
| Security Review      | Task Security Verdict da reassociação de líder APROVADO; Release Verdict global BLOQUEADO pelo achado RabbitMQ preexistente |
| Documentação         | ADR-001, roadmap, handoff e artefatos de tasks versionados em `.ai/`; Impeccable documentada em `.ai/docs/impeccable-codex.md` |

## Pendências técnicas não bloqueantes

* Flyway 9.22.3 emite warning de suporte testado apenas até PostgreSQL 15, enquanto produção usa PostgreSQL 18.6. A V1 foi validada empiricamente em PostgreSQL 18.6 local e no Render; o job PostgreSQL 18.6 no CI foi executado com sucesso. Avaliar upgrade de Flyway separadamente.

* rotas protegidas de Dashboard, Members, Teams, Events, Attendance e Users estão disponíveis; Settings permanece não persistente;
* contratos frontend de Member/Team/Event/Attendance foram alinhados; update de Team agora aceita TeamRequestDTO e retorna TeamResponseDTO, preservando TenantService/RBAC;
* a task `route-domain-areas-and-add-http-ux-feedback` foi concluída com Task Verdict APROVADO; o bloqueador antiabuso mencionado em sua auditoria foi tratado posteriormente.
* a task `protect-public-guild-registration-against-abuse` foi concluída com Task Security Verdict, QA, SRE e Task Verdict APROVADOS; o registro público usa limitador local configurável e o Release Verdict foi liberado.

* O rate limit de registro depende de memória local e da instância única atual. Escala horizontal exige store distribuído em task separada.
* CORS de produção está configurado para `https://theliderum.vercel.app`, sem wildcard, e foi validado positiva e negativamente.

## Estado corrente de execução

- A task P0 `resolve-rabbitmq-amqp-406-queue-topology` foi concluída após remediação operacional da topologia. O gate `validate-cycle-3-operational-flows` foi concluído com QA, Security e Auditor aprovados; as cinco tasks funcionais do Ciclo 3 passaram suas validações e a suíte Angular final ficou em 103/103 com build SUCCESS. O Ciclo 3 está encerrado no nível automatizado local.
- Atualização final da task P0 `resolve-rabbitmq-amqp-406-queue-topology` (2026-10-21): QA APROVADO após testes locais isolados de publisher backend/EventService, listener/dispatcher/AuditLog, duplicidade em memória e retry→DLQ; backend `clean verify` 93/93, notification-service 4/4 e integrações RabbitMQ 3/3 (inclui 1 teste backend + 2 notification). Security APROVADO para a configuração atual confirmada com Discord desabilitado; SRE/DevOps APROVADO COM LIMITAÇÕES sob política best-effort; Auditor Task Verdict APROVADO e task finalizada em `tasks/completed/resolve-rabbitmq-amqp-406-queue-topology/`. Release Verdict permanece BLOQUEADO: testes daquela task não acessaram produção e as integrações locais não são um teste único do fluxo completo no broker real. Deduplicação permanece apenas em memória; sem outbox/confirms/entrega garantida. Habilitar Discord reabre Security Review.
- Task operacional ativa: `validate-production-rabbitmq-event-flow` em `tasks/active/validate-production-rabbitmq-event-flow/`. Autorizada; sessão Liderum e role habilitada para Event foram confirmadas pela UI, health retornou UP e proprietário reconfirmou Discord desabilitado. Snapshot CloudAMQP renovado em 2026-10-21 mostrou 0 consumers e ambas as filas vazias. Nenhum Event foi criado; task permanece pausada porque falta consumer ativo. Release Verdict permanece BLOQUEADO.

### Registro histórico RabbitMQ — antes da autorização Docker/best-effort — 2026-10-21

- Backend `clean verify`: 92 testes, 0 failures/errors/skipped, `BUILD SUCCESS`, exit code 0. Notification Service `clean verify`: 4 testes, 0 failures/errors/skipped, `BUILD SUCCESS`, exit code 0. A integração `RabbitMqNotificationIntegrationIT` permanece não executada: classe `*IT` com ativação explícita por system property, sem Failsafe no POM; Docker Engine está inacessível por `permission denied` no named pipe.
- Smoke sintético: log confirma AuditLog na primeira publicação e descarte da repetição do mesmo ID no dispatcher. Na verificação posterior, broker mostrou as duas filas sem mensagens ready/unacked, mas 0 consumers e 0 conexões. Não houve prova do publisher real do backend, nem disponibilidade contínua.
- O canal Audit foi observado em execução. Discord/webhook efetivos permanecem não verificados porque os valores de ambiente não estão acessíveis pela integração Render disponível; a busca de logs do smoke não encontrou mensagens de envio/skip Discord. Não compartilhar nem registrar valores secretos. Risco condicional: uma webhook compartilhada enviaria nome/data/ID de Events de várias Guilds ao mesmo destino.
- O publisher backend captura `AmqpException` e não propaga falha; o Event pode ser retornado como criado sem entrega confirmada. Nenhum publisher confirm/return foi localizado nos arquivos consultados. Risco preexistente de confiabilidade, relevante ao aceite RabbitMQ.
- A integração isolada `RabbitMqNotificationIntegrationIT` foi executada após aprovação fora do sandbox: 1/1, 0 failures/errors/skipped, BUILD SUCCESS, exit 0, contra container RabbitMQ 3.13 temporário em localhost. Ela publica diretamente no exchange e usa dispatcher mockado; não fecha o fluxo EventService→publisher real nem testa AuditLog/retry/DLQ. Container foi removido e porta 5673 liberada. Gates não foram executados nesta continuação; task ativa e Release BLOCKED.
- Gates da task P0 após best-effort e confirmação de Discord: QA REPROVADO por evidência incompleta; Security APROVADO para configuração atual (Discord desabilitado; webhook configurada); SRE/DevOps APROVADO COM LIMITAÇÕES; Auditor Task Verdict BLOQUEADO até QA. Release Verdict **BLOQUEADO**. Task permanece ativa; não executar `finish-task`.
