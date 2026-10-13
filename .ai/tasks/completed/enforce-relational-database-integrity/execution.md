# Execução — enforce-relational-database-integrity

## Routing

- Agents: Planner/Arquiteto consultivos; Backend Developer executor; QA, Security e SRE/DevOps nas revisões; Auditor no gate final.
- Skills utilizadas: `create-migration`, `test-backend`, `security-review`, `audit-task` e `finish-task` após aprovação dos gates.

## Inventário read-only de produção — 2026-10-11

- Render PostgreSQL 18, schema `public`; Flyway V1 e V2 aplicadas com sucesso.
- Contagens agregadas: 1 Guild, 1 User, 0 Member, 0 Team, 0 Event, 0 Attendance, 0 activation token.
- Todas as contagens de nulos candidatos, duplicatas Member/Event e relações cross-Guild consultadas: zero.
- A inspeção não leu valores pessoais e foi executada pelo conector em transação somente leitura. Nenhum DDL ou DML foi executado em produção.
- Índices e constraints conferidos. V2 tem dois índices únicos para `user_activation_tokens.token_hash`; observação não relacionada à migration V3, sem alteração.

## Implementação

- Migration criada: `backend/src/main/resources/db/migration/V3__enforce_relational_integrity.sql`.
- V3 torna `users.guild_id`, `users.guild_role`, `event.name`, `event.date` e `attendance.status` NOT NULL e cria `uk_attendance_member_event`.
- V1 e V2 permanecem inalteradas. `users.password` continua nullable para usuários PENDING.
- Mappings JPA foram alinhados às cinco colunas obrigatórias.
- Testes fresh H2 verificam as três migrations, constraints, rejeição de nulos e de presença duplicada. Teste de tenancy não persiste mais User guildless, estado agora proibido pelo schema; mantém verificação de acesso sem autenticação.
- PostgreSQL fresh/upgrade CI foi atualizado para esperar V3 e verificar as constraints.

## Validação

- `.\mvnw.cmd -Dtest=FlywaySchemaIntegrationTest test`: 3 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS, exit code 0. H2 aplicou V1→V2→V3 e Hibernate `validate` iniciou o contexto.
- Primeira execução de `clean verify`: 74 testes, 0 failures, 1 error; o teste tenant tentou persistir User sem Guild. O fixture era incompatível com a nova invariável. Ajustado para manter apenas a asserção sem autenticação; a impossibilidade de User sem Guild é testada na integração de constraints.
- Segunda execução de `.\mvnw.cmd clean verify`: 74 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS, exit code 0.
- `clean verify`: 74 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS, exit code 0.

## Validação PostgreSQL 18 descartável — 2026-10-11

- Docker Engine 29.5.3 operacional; container exclusivo `liderum-relational-integrity-pg18`, imagem PostgreSQL 18.6, bind local `127.0.0.1:54329`, banco descartável sem volume. Credencial gerada para o processo e não versionada nem registrada.
- `PostgreSqlFlywaySchemaCiIT`: 1 teste, 0 failures/errors/skipped. Banco/schema inicialmente vazio; Flyway validou/aplicou V1, V2 e V3, registrando versão atual V3; Spring iniciou sob profile `prod` e Hibernate `ddl-auto=validate` inicializou o EntityManagerFactory. BUILD SUCCESS.
- `PostgreSqlFlywayUpgradeCiIT`: 1 teste, 0 failures/errors/skipped. O teste aplicou V1, inseriu User legado válido, avançou V2→V3, preservou o registro e inicializou contexto Spring/Hibernate `validate`; BUILD SUCCESS.
- Execução Maven direcionada: `.\mvnw.cmd '-Dtest=PostgreSqlFlywaySchemaCiIT,PostgreSqlFlywayUpgradeCiIT' test`; total 2 testes, 0 failures/errors/skipped, BUILD SUCCESS, exit code 0. Log confirmou PostgreSQL 18.6 e Flyway 9.22.3; warning de suporte testado até PostgreSQL 15 permaneceu, sem impedir os cenários.
- Container descartável foi parado e removido após os testes. Nenhum banco existente ou Render foi usado/modificado.

## Estado

- Nenhuma mudança foi aplicada ao PostgreSQL Render. Produção continua na V2.
- Render continua sem alteração, na V2. V3 está validada em H2 e PostgreSQL 18.6 fresh/upgrade. Uma futura aplicação em produção exige preflight read-only atualizado, janela/rollback operacional e autorização própria; esta execução não fez deploy.
- Nenhum commit ou push realizado.
