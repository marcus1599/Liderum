# PRD — Baseline Flyway e schema de produção

## Classificação

P1 — infraestrutura / qualidade — **STRUCTURAL**.

## Contexto

O backend já declara `flyway-core`, mas `src/main/resources/db/migration` não existe. Tanto `dev` (H2) quanto `prod` (PostgreSQL por `DB_URL`) usam `spring.jpa.hibernate.ddl-auto=update`; portanto, o schema atual é criado ou alterado silenciosamente pelo Hibernate, sem histórico versionado e sem caminho operacional seguro para banco existente.

O modelo consolidado na Fase 1 contém Guild, User, Member, Team, Event e Attendance. User, Member, Team e Event são tenant-scoped por `guild_id`; Attendance pertence a Member e Event. User possui unicidade global já mapeada para `username` e `email`; os demais relacionamentos e nullabilities dependem majoritariamente de convenções JPA/Hibernate.

O Compose atual não possui PostgreSQL ou volume de banco: o producer usa explicitamente `dev`/H2 e o `DataInitializer` semeia apenas esse profile. Não há banco persistente controlado pelo repositório a migrar automaticamente. Um `DB_URL` externo existente não pode ser assumido vazio nem seguro para baseline automática.

## Objetivo

Estabelecer um schema inicial versionado por Flyway para banco vazio, compatível com PostgreSQL e validado com H2 quando tecnicamente compatível; substituir `ddl-auto=update` por comportamento que não crie/altere schema silenciosamente nos ambientes que usam migrations.

## Decisões de produto e operação

1. Criar uma única migration inicial `V1__baseline_schema.sql`, pois não há migrations anteriores versionadas e o modelo pós-Fase 1 é o baseline canônico.
2. Não habilitar `baselineOnMigrate` nem adotar automaticamente bancos não vazios. Banco existente deve falhar de forma visível até passar por backup, inspeção e procedimento operacional explícito.
3. Banco vazio deve receber todo o schema exclusivamente pelo Flyway antes do startup da aplicação e antes do `DataInitializer` de `dev`.
4. `prod` deve usar `spring.jpa.hibernate.ddl-auto=validate` após a baseline; não usar `update`, `create` ou `create-drop`.
5. `dev` e testes de integração devem usar migrations e `ddl-auto=validate`, não depender de Hibernate para criar schema. H2 continuará apenas se a migration SQL puder ser executada e testada nela; nenhuma dependência nova será adicionada para contêiner PostgreSQL nesta task.
6. O profile default não deve criar schema por Hibernate. Seu comportamento será configurado para validar schema quando um datasource for fornecido ou falhar de maneira clara por configuração de datasource ausente.

## Schema alvo e integridade

### Tabelas e ordem

1. `guilds`
2. `users`
3. `teams`
4. `members`
5. `events`
6. `attendance`

### Foreign keys esperadas

- `users.guild_id → guilds.id`;
- `users.member_id → member.id` (opcional, pois o vínculo User–Member atual é opcional);
- `teams.guild_id → guilds.id`;
- `teams.leader_id → users.id` (opcional conforme o modelo atual);
- `members.guild_id → guilds.id`;
- `members.team_id → teams.id` (opcional);
- `events.guild_id → guilds.id`;
- `attendance.member_id → member.id`;
- `attendance.event_id → event.id`.

### Constraints e índices

- Preservar `UNIQUE` global já previsto para `users.username` e `users.email`.
- Formalizar `NOT NULL` para os vínculos de tenant que o ADR-001 e os services exigem (`users`, `members`, `teams`, `events` → Guild), após conciliar a anotação JPA e os testes.
- Formalizar campos já obrigatórios por modelo/fluxo somente quando houver evidência no código e compatibilidade em H2/PostgreSQL; não inferir novos requisitos de negócio.
- Criar índices de consulta tenant-scoped em `users.guild_id`, `users(guild_id, guild_role)`, `members.guild_id`, `teams.guild_id`, `events.guild_id`, `attendance.event_id` e `attendance.member_id`, após conferir o SQL produzido e evitar redundância com PK/unique.
- Não adicionar nesta task `UNIQUE(guilds.name)`, `UNIQUE(attendance.member_id, attendance.event_id)`, checks de enum ou FKs compostas de mesmo tenant: o código atual não estabelece essas regras como contrato e elas podem alterar comportamento funcional.
- A consistência de que Attendance conecta Member e Event da mesma Guild permanece assegurada pelo service/TenantService. Torná-la uma invariância de banco exigiria FKs compostas ou alteração de modelo e fica fora do baseline.

## Escopo

- migration baseline em `backend/src/main/resources/db/migration/`;
- ajustes mínimos de mapeamento JPA e properties para schema validado por Flyway;
- profiles dev/prod/default e testes relacionados ao schema;
- documentação operacional segura para banco vazio e banco existente;
- revisão proporcional de Docker Compose apenas para documentar que ele continua dev/H2; não adicionar PostgreSQL sem autorização.

## Fora do escopo

- migração automática, destruição, reset ou conversão silenciosa de bancos existentes;
- frontend, novas features, RBAC, JWT, multi-Guild, RabbitMQ, CI/CD, observabilidade, billing e antiabuso de registro;
- PostgreSQL no Compose ou Testcontainers/dependências novas;
- mudança de regras de negócio por unicidade de Guild/Attendance ou enum checks sem decisão separada.

## Critérios de aceitação

- banco vazio H2 recebe V1 pelo Flyway e a aplicação sobe com `ddl-auto=validate`;
- V1 cria schema equivalente ao modelo JPA canônico e relações tenant necessárias;
- profile `prod`, quando apontado a PostgreSQL vazio, aplica a migration e valida schema sem Hibernate `update`;
- `dev` aplica migration antes do seed e o bootstrap continua funcionando apenas em dev;
- testes de onboarding, RBAC e tenant funcionam sobre schema migrado;
- banco não vazio sem histórico Flyway não é baselineado, resetado ou alterado silenciosamente;
- nenhuma migration ou seed contém segredo/credencial real;
- suíte backend completa passa.

## Riscos

- SQL portátil entre H2 e PostgreSQL requer validação explícita; H2 não substitui validação real em PostgreSQL.
- Dados externos existentes podem não satisfazer FKs/nullabilities; mitigação é falhar antes de alteração e exigir backup/inspeção.
- Alterar `ddl-auto` revela divergências entre entidades e baseline; isso é evidência a corrigir dentro do schema, não motivo para reativar `update`.
- Constraints tenant fortalecem integridade, mas não substituem `TenantService`/autorização.

## Segurança

| Área | Situação atual | Tratamento |
| --- | --- | --- |
| Tenant | Guild FK existe por JPA, mas é criada implicitamente | versionar FKs e índices tenant-scoped |
| Órfãos | sem migration, comportamento depende de Hibernate | formalizar FKs e nullability comprovada |
| Dados existentes | origem/desenho desconhecidos | não migrar nem apagar automaticamente |
| Seeds | dados demo são dev-only, com senha conhecida | nunca incluir seed/credencial em V1; Flyway precede seed dev |
| Antiabuso | registro público ainda sem rate limit/CAPTCHA | fora de escopo; continua bloqueador de release |

## Dependências

- `flyway-core` já existe no `pom.xml`; nenhuma nova dependência aprovada.
- ADR-001 é compatível e orienta `User → Guild` obrigatório; não precisa alteração.

## Testes esperados

- Flyway em banco H2 vazio; startup com `validate`;
- confirmação de que Hibernate não cria tabelas fora das migrations;
- onboarding, login, RBAC e isolamento tenant sobre schema migrado;
- falha segura para banco não vazio sem histórico, quando viável em teste;
- validação PostgreSQL manual/documentada em instância descartável quando disponível, sem depender de credenciais locais/CI nesta task.
