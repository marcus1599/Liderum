# Plan — enforce-relational-database-integrity

## Classificação e routing

- Tipo: qualidade de dados / database; porte: STRUCTURAL; prioridade: próximo Ciclo 2.
- Domínios: planning, architecture, database, backend, testing, security, sre, audit.
- Agents: Planner → Arquiteto/Backend consultivos → Backend executor → QA → Security → SRE/DevOps → Auditor.
- Skills: `create-prd`, `create-plan` (planejamento); `create-migration`, `test-backend`, `security-review`, `audit-task`, `finish-task` (somente na execução/gates).
- Não selecionado: Frontend Dev, pois nenhum contrato/tela frontend está no escopo.

## Sequência executável

1. Inventariar schema real e dados com consultas read-only de `inventory.md`, no mínimo no banco-alvo; registrar contagens agregadas, schema/Flyway version e eventuais divergências. Se acesso ao banco ou evidência faltar, não avançar ao DDL.
2. Confirmar que uma presença por Member/Event é a regra vigente e que todas as contagens candidatas são zero. Se não forem, pausar e escalar plano de saneamento ou ajuste de escopo; sem `UPDATE`/`DELETE` tácito.
3. Desenhar a menor migration nova após V2 para as constraints compatíveis, sem editar migrations antigas. Verificar compatibilidade H2/PostgreSQL 18 e locking/tempo operacional proporcionais ao volume observado.
4. Ajustar mappings/testes somente se estritamente necessário. Manter `User.password` nullable para PENDING. Não alterar regras de tenant ou autorização.
5. Testar fresh V1→V2→nova versão e upgrade V1→V2→nova versão com User e dados válidos existentes; testar rejeição de duplicata/nulos, ausência de perda, Hibernate `validate`, onboarding, ativação, Event/Attendance e suíte backend completa.
6. Executar PostgreSQL 18 real descartável e job/fluxo CI PostgreSQL pertinente; registrar `BUILD`/exit code e `flyway_schema_history`. Preparar deploy apenas após preflight do alvo.
7. QA verifica regressões e idempotência dos testes; Security verifica vínculos e tenant; SRE avalia risco de migration/rollback/recuperação; Auditor compara diff, PRD, inventário e evidências. `finish-task` somente após aprovação.

## Critérios de parada

- Não criar migration por tentativa. Dados incompatíveis, schema drift, indisponibilidade do inventário real ou exigência de remediação não aprovada interrompem a execução.
- A abordagem para FKs compostas de mesma Guild, se desejada, precisa decisão arquitetural separada antes de ampliar este escopo.
- Release Verdict pode diferir do Task Verdict; registrar ambos.

## Estado desta rodada

Planejamento apenas. Nenhum dado de banco foi alterado, nenhum SQL DDL foi criado/executado e nenhuma validação de testes foi reivindicada. Dados existentes no Render permanecem não inventariados nesta sessão; este é o primeiro gate da futura implementação.
