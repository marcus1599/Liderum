# Plano — baseline-flyway-and-production-database-schema

## Routing

- Domínios: `planning`, `architecture`, `database`, `backend`, `testing`, `security`, `sre`, `documentation`.
- Agents: Planner → Arquiteto (consultivo) → Backend Developer → QA → Security + SRE/DevOps → Auditor.
- Skills: `create-prd`, `create-plan`; na execução, `create-migration`, `test-backend`, `security-review`, `audit-task` e `finish-task` se aprovado.
- Não selecionados: Frontend Developer — não há contrato/UI a alterar; `test-frontend` — nenhuma mudança Angular; `create-adr` — Flyway já é dependência e uma V1 inicial/fail-fast de banco legado é decisão operacional local, não altera ADR-001 nem o modelo de tenancy.
- Gate: migração revisada pelo Arquiteto antes de execução; QA, Security e SRE devem aprovar antes de Auditoria. Task/Release Verdict permanecem independentes.

## Classificação

P1, STRUCTURAL. É a primeira task da Fase 2 e a próxima prioridade de persistência, apesar de a proteção antiabuso da Fase 1 continuar bloqueando release de forma independente.

## Estratégia aprovada para implementação

1. Inventariar o DDL efetivo das entidades e comparar com services/repositories; não escrever SQL a partir de suposição.
2. Criar uma única `V1__baseline_schema.sql` para banco vazio; não criar uma sequência artificial para histórico que nunca foi versionado.
3. Não usar `baseline-on-migrate`, `clean`, reset ou operação destrutiva. Banco já não vazio sem `flyway_schema_history` deve parar e orientar backup/inspeção/migração legada explícita futura.
4. Fazer Flyway a fonte de criação do schema em dev, test e prod; usar `ddl-auto=validate` onde o datasource/schema existir. Remover `update` das configurações de dev/prod.
5. Manter H2 em testes somente se V1 for compatível. Usar uma configuração de teste explícita para Flyway/H2 e não permitir que `create-drop` esconda lacuna de migration.
6. Não ampliar o modelo: registrar, mas não criar, constraints de unicidade/check que representam regra de negócio ainda não decidida.

## Etapas

1. Arquiteto/Backend: obter DDL de referência das entidades, colunas, tipos, IDs, enums, FKs, nullability e nomes físicos antes de redigir V1.
2. Backend: criar V1 com ordem de tabelas, FKs, unique existentes e índices tenant-scoped previstos no PRD; alinhar entidades somente se preciso para `validate` refletir a migration.
3. Backend: configurar Flyway e `ddl-auto=validate` por profile, incluindo test/dev/prod e comportamento explícito para datasource inexistente no default.
4. Backend: documentar procedimento não destrutivo para banco vazio, dev H2 e banco externo existente; revisar Compose sem adicionar banco/volume fora de escopo.
5. Backend: adicionar testes de migration e ajustar integrações para usar schema Flyway real.
6. QA: validar banco vazio, contexto migrado, onboarding, RBAC, tenant e suíte completa sem schema criado pelo Hibernate.
7. Security: revisar FKs/nullability/indexes tenant, ausência de dados sensíveis em migration e origem dos achados.
8. SRE/DevOps: validar profiles, fail-fast de banco existente, operação local/Compose e instrução de recuperação/backup.
9. Auditor: verificar `db/migration`, propriedades, diff, testes, ausência de alteração funcional e documentação; emitir Task e Release Verdict.

## Matriz inicial de banco

| Objeto | Relações/constraint existente | Baseline proposta |
| --- | --- | --- |
| `guilds` | `name` não nulo | PK, name não nulo; sem unicidade nova |
| `users` | username/email únicos e não nulos; Guild única por ADR | PK, uniques existentes, FK Guild obrigatória; `member_id` opcional |
| `teams` | Guild e leader referenciados | PK, FK Guild obrigatória, FK leader opcional; índice Guild |
| `members` | Guild/Team; User 1:1 inverso | PK, FK Guild obrigatória, FK Team opcional; índice Guild |
| `events` | Guild | PK, FK Guild obrigatória; índice Guild |
| `attendance` | Member/Event/status | PK, FKs Member/Event e índices de join; regra same-Guild continua em service |

## Testes planejados

| Área | Evidência |
| --- | --- |
| Migration H2 | banco vazio aplica V1 e registra `flyway_schema_history` |
| Startup | contexto com schema migrado e `ddl-auto=validate` sobe |
| Hibernate | ausência/erro de tabela não é corrigida por `update` silencioso |
| Onboarding | Guild + primeiro MARECHAL persistem sobre V1 |
| RBAC | regras General/Marechal continuam aprovadas |
| Tenant | Member/Team e User cross-Guild continuam bloqueados |
| Banco existente | teste/documentação prova ausência de baseline/reset automático |
| PostgreSQL | validar V1 em PostgreSQL descartável se ambiente for disponibilizado; H2 em modo compatível não substitui integralmente essa evidência |
| Regressão | `./mvnw.cmd clean verify` completo |

## Riscos e controles

- **Divergência JPA/SQL:** usar `validate` e testes de contexto como gate.
- **Compatibilidade H2/PostgreSQL:** preferir DDL SQL padrão, IDs e timestamps compatíveis; não usar recursos exclusivos sem necessidade.
- **Banco legado:** nenhum comando automático; backup e avaliação humana obrigatórios.
- **Tenant:** FKs/índices reforçam integridade, mas não substituem filtros por Guild do `TenantService`.
- **Rollback:** Flyway não desfaz V1 automaticamente; recuperação é restauração de backup para banco existente ou descarte/recriação apenas de banco de desenvolvimento descartável, nunca `clean` em ambiente compartilhado.

## Critérios de conclusão

- V1 criada e executada de modo reprodutível em banco vazio;
- `ddl-auto=update` removido dos ambientes migrados;
- migrations H2 e validação PostgreSQL proporcional evidenciadas;
- onboarding/RBAC/tenant e suíte completa aprovados;
- nenhuma perda ou alteração silenciosa de banco existente;
- Task Security Verdict e Task Verdict aprovados.
