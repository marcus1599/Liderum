# PRD — Cobertura de integração tenant para Event e Attendance

**Classificação:** P1 — qualidade e segurança — MEDIUM.

## Contexto e problema

`Guild` é o tenant do Liderum, resolvido no servidor pelo `TenantService` a partir do usuário autenticado persistido. A cobertura anterior de integração comprovou essa fronteira para `Member` e `Team`, porém `Event` e `Attendance` possuem apenas testes unitários que usam `TenantService` e repositórios mockados. Eles não comprovam a cadeia HTTP → JWT/Security → `TenantService` → service → repository → H2/Flyway.

O código atual já indica consultas tenant-scoped: `EventRepository.findByIdAndGuildId`, `AttendanceRepository.findByIdAndEventGuildId` e, na criação de presença, busca tanto `Member` quanto `Event` pela Guild corrente. A task deve transformar esses indícios em evidência executável, sem assumir que a implementação está correta antes dos testes.

## Objetivo

Estender a cobertura de integração para demonstrar que usuários autenticados de uma Guild não conseguem listar, ler, atualizar, excluir ou associar dados de `Event` e `Attendance` de outra Guild, sem mutação parcial e usando contexto Spring, JWT, `TenantService`, repositórios, H2 e Flyway reais.

## Requisitos

1. Usar `@SpringBootTest`, `@AutoConfigureMockMvc`, H2/Flyway e fixtures persistidas de pelo menos Guild A e Guild B.
2. Obter tokens por `POST /auth/login` de usuários reais persistidos/onboarded; não simular `TenantService`, JWT ou `SecurityContext`.
3. Para `Event`, cobrir pela API HTTP:
   - listagem de Guild A sem Events de Guild B;
   - leitura, atualização e exclusão de Event de Guild B por usuário autorizado de Guild A bloqueadas;
   - criação em Guild A vinculada somente ao tenant do usuário, pois o request não aceita `guildId` nem referências externas.
4. Para `Attendance`, cobrir pela API HTTP:
   - listagem de Guild A sem Attendance de Guild B;
   - leitura, atualização e exclusão de Attendance de Guild B por usuário autorizado de Guild A bloqueadas;
   - criação com `Member` de Guild B bloqueada;
   - criação com `Event` de Guild B bloqueada;
   - após cada tentativa bloqueada, não haver `Attendance` nova nem alteração de registros existentes.
5. Usar um ator com role administrativa permitida (`MARECHAL`, `GENERAL` ou `MAJOR`) para os cenários de mutação cross-Guild, isolando a barreira de tenant da negação RBAC por role.
6. Diferenciar os resultados HTTP: ação de role não autorizada deve resultar em 403; recurso/referência de outra Guild deve ser não enumerável e retornar 404 conforme os handlers do projeto. Se a API atual responder 500, registrar a evidência como incompatibilidade de tratamento de erro e escalar antes de qualquer correção de produção.
7. Manter testes idempotentes, transacionais e sem dependência de `.env`, banco externo, relógio, rede, RabbitMQ ou seed de desenvolvimento.
8. Executar teste dirigido e `./mvnw.cmd clean verify`, com resultado final verificável.

## Escopo

- novo teste de integração HTTP backend dedicado, preferencialmente no pacote `Security` ou `Tenancy` conforme o padrão encontrado;
- fixtures e helpers internos de teste para Guilds, usuários, Events, Members e Attendances;
- ajustes mínimos exclusivamente em testes, se necessários para manter os cenários determinísticos;
- documentação de execução, QA, Security e auditoria após implementação aprovada.

## Fora do escopo

- frontend, novas funcionalidades, RBAC novo, JWT, rate limiting, RabbitMQ, CI/CD, observabilidade e refactors;
- migrations Flyway, schema ou dependências novas, salvo bloqueio direto comprovado e autorizado;
- correção automática de acesso cross-tenant ou de comportamento de produção revelado pelo teste;
- alteração de contrato público, inclusive normalização de erros, sem escalonamento explícito;
- alterar a regra de Guild única ou a resolução server-side registrada no ADR-001.

## Critérios de aceitação

- os cenários Event e Attendance acima percorrem a pilha HTTP/Security/Tenant/persistência real e passam de forma determinística;
- dados de Guild B não aparecem nas listagens de Guild A;
- toda leitura ou mutação cross-Guild é bloqueada, sem alterar o registro de Guild B;
- referências `Member`/`Event` de Guild B não criam Attendance em Guild A nem em Guild B;
- não há `guildId` recebido do cliente ou usado como autoridade;
- nenhum código de produção, migration ou dependência é necessário se os controles atuais forem suficientes;
- se houver acesso cross-Guild confirmado, a implementação para imediatamente, a task permanece ativa e Security é escalado;
- teste dirigido e suíte backend completa terminam com resultado verificável; `git diff --check` permanece limpo.

## Segurança e riscos

| Risco | Classificação inicial | Controle na task |
| --- | --- | --- |
| IDOR por ID de Event/Attendance | PREEXISTENTE_RELACIONADO a validar | Exercitar IDs de Guild B com JWT administrativo de Guild A. |
| Associação cross-Guild Member/Event | PREEXISTENTE_RELACIONADO a validar | Testar cada referência isoladamente e confirmar ausência de mutação parcial. |
| Falso positivo por mock | Risco de teste | Usar MockMvc, JWT, `TenantService`, repositories e Flyway reais. |
| Confusão entre RBAC e tenancy | Risco de teste | Usar role administrativa permitida nas operações mutáveis. |
| Enumeração de recurso | Risco de API | Esperar 404 para recurso de outro tenant; 403 fica restrito à role insuficiente. |
| Regressão de schema | Baixo | H2 inicia pelo baseline Flyway e Hibernate permanece em `ddl-auto=validate`. |

## Dependências

- Sem nova biblioteca, migration ou ADR.
- Depende dos contratos atuais de `EventController`, `AttendanceController`, JWT, `TenantService`, SecurityConfig e baseline Flyway V1.
- ADR-001 permanece aplicável e não requer alteração.
