# Inventário inicial — integridade relacional

Data: 2026-10-11. Fonte: migrations V1/V2, entidades, repositories e services do backend no working tree. Isto é inventário do **schema versionado e do código**, não uma inspeção concluída dos dados de produção.

## Constraints já versionadas

| Tabela | PK/FK/unique/not-null relevantes | Índices explícitos |
| --- | --- | --- |
| `guilds` | PK `id`; `name` NOT NULL | PK |
| `users` | PK; FK `guild_id → guilds`, FK `member_id → member`; `username`/`email` UNIQUE e NOT NULL; `status` NOT NULL pela V2; `password` nullable pela V2 | `guild_id`; `(guild_id, guild_role)` |
| `member` | PK; FK `guild_id → guilds` NOT NULL; FK `team_id → team` nullable | `guild_id` |
| `team` | PK; FK `guild_id → guilds` NOT NULL; FK `leader_id → users` nullable | `guild_id` |
| `event` | PK; FK `guild_id → guilds` NOT NULL | `guild_id` |
| `attendance` | PK; FK `member_id → member` NOT NULL; FK `event_id → event` NOT NULL | `member_id`; `event_id` |
| `user_activation_tokens` | PK; FK `user_id → users` NOT NULL; `token_hash` UNIQUE/NOT NULL; `expires_at` NOT NULL | `user_id`; índice da unique de token |

`V1__baseline_schema.sql` e `V2__user_activation_lifecycle.sql` são imutáveis nesta task. O mapping `User.member` é `@OneToOne`, mas a V1 não declara UNIQUE para `users.member_id`. Não adicionar essa constraint sem requisito e preflight próprios.

## Lacunas candidatas e compatibilidade

- `attendance (member_id, event_id)` não é UNIQUE. `AttendanceServiceImpl.create` resolve ambos pela Guild atual, mas não rejeita duplicidade. Antes de impor UNIQUE, confirmar regra de negócio e contar duplicatas.
- `users.guild_id` e `users.guild_role` são nullable na V1. O provisionamento atual atribui ambos; a V2 apenas torna `status` NOT NULL e permite `password` NULL para `PENDING`. Confirmar legados órfãos/nulos antes de endurecer.
- `event.name`, `event.date` e `attendance.status` são nullable no schema. Os DTOs de escrita os exigem, mas caminhos antigos/importação podem ter deixado nulos.
- As FKs simples não garantem igualdade de Guild entre `member`/`event` de uma presença, nem entre `member`/`team`, `team`/`leader` ou `users`/`member`. A camada de serviço e testes tenant são a proteção atual. Constraint composta para esses vínculos exige decisão e desenho próprios; não assumir como parte automática desta migration.
- Consultas reais filtram `users`, `member`, `team` e `event` por `guild_id`, e `attendance` por `event.guild_id`; índices tenant básicos já existem. Novo índice exige consulta/plano medido, não somente o rótulo tenant-scoped.

## Dados existentes — Render production, inspeção read-only em 2026-10-11

- Instância do Liderum disponível, PostgreSQL 18, schema `public`; Flyway versions 1 e 2 têm `success = true`.
- Contagens agregadas: guilds 1, users 1, member 0, team 0, event 0, attendance 0, user_activation_tokens 0. Nenhum valor pessoal foi consultado ou registrado.
- Violações candidatas: users sem Guild 0; users sem role 0; Events sem nome 0; Events sem data 0; Attendance sem status 0; duplicatas Member/Event 0; Attendance, Member/Team, Team/leader e User/Member cross-Guild 0.
- Colunas em produção compatíveis com o inventário V1/V2: `users.guild_id` e `users.guild_role`, `event.name`/`date` e `attendance.status` permanecem nullable; `users.password` permanece nullable como requerido pelo estado PENDING.
- Índices tenant e FKs esperados estão presentes. Há dois índices únicos redundantes sobre `user_activation_tokens.token_hash` (`idx_activation_token_hash` e `user_activation_tokens_token_hash_key`) oriundos da declaração V2; registrar como observação, sem removê-los nesta task.
- A instância tem dados reais (um User e uma Guild), portanto não é um banco descartável/vazio. Esta consulta só permite concluir que as contagens verificadas são compatíveis agora; a migration de produção ainda requer backup/rollback operacional e nova confirmação imediatamente antes do deploy.
- Docker local não respondeu. O preflight de produção foi executado pelo conector Render em transação read-only. Não houve mutação de dados ou schema.
- PostgreSQL 18 instalado como serviço Windows está ativo, mas `psql` local não autenticou sem senha; não tentei descobrir/ler credenciais nem conectar a bases existentes. Uma base descartável local ainda precisa de acesso seguro para teste.

## Preflight read-only obrigatório antes da implementação

Repetir imediatamente antes do deploy e executar também contra o banco PostgreSQL descartável de teste. Registrar somente contagens agregadas e versão Flyway:

```sql
SELECT installed_rank, version, success FROM flyway_schema_history ORDER BY installed_rank;
SELECT 'guilds' AS tabela, count(*) FROM guilds UNION ALL
SELECT 'users', count(*) FROM users UNION ALL
SELECT 'member', count(*) FROM member UNION ALL
SELECT 'team', count(*) FROM team UNION ALL
SELECT 'event', count(*) FROM event UNION ALL
SELECT 'attendance', count(*) FROM attendance UNION ALL
SELECT 'user_activation_tokens', count(*) FROM user_activation_tokens;
SELECT count(*) AS users_sem_guild FROM users WHERE guild_id IS NULL;
SELECT count(*) AS users_sem_role FROM users WHERE guild_role IS NULL;
SELECT count(*) AS events_sem_nome FROM event WHERE name IS NULL;
SELECT count(*) AS events_sem_data FROM event WHERE date IS NULL;
SELECT count(*) AS presencas_sem_status FROM attendance WHERE status IS NULL;
SELECT count(*) AS grupos_duplicados FROM (
  SELECT member_id, event_id FROM attendance GROUP BY member_id, event_id HAVING count(*) > 1
) d;
SELECT count(*) AS presencas_cross_guild FROM attendance a
JOIN member m ON m.id = a.member_id JOIN event e ON e.id = a.event_id
WHERE m.guild_id <> e.guild_id;
SELECT count(*) AS membros_em_team_outra_guild FROM member m
JOIN team t ON t.id = m.team_id WHERE m.guild_id <> t.guild_id;
SELECT count(*) AS lideres_de_outra_guild FROM team t
JOIN users u ON u.id = t.leader_id WHERE t.guild_id <> u.guild_id;
SELECT count(*) AS users_ligados_a_member_de_outra_guild FROM users u
JOIN member m ON m.id = u.member_id WHERE u.guild_id <> m.guild_id;
```

Se qualquer contagem candidata for positiva, parar a migration e levar a decisão de remediação/escopo ao usuário; não corrigir dados silenciosamente. Comparar `information_schema`/`pg_indexes` do banco-alvo com V1/V2 para detectar drift antes de aplicar V3.
