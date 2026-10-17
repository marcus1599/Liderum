# Plano — safely-deactivate-and-reactivate-staff-users

**Classificação:** P1, STRUCTURAL (lifecycle de identidade, revogação de sessão e RBAC). **Status:** concluída; QA, Security e Auditor aprovados.

## Routing

- Domínios: planning, backend, frontend, security, testing, audit, documentation.
- Agents: Backend Dev → Frontend Dev → QA → Security → Auditor; Planner consultivo já aplicado. Arquiteto consultivo caso a regra de Team/locking se revele estrutural; SRE condicional se houver mudança operacional.
- Skills: `create-prd`, `create-plan`; implementação `test-backend`, `test-frontend`, `security-review`, `audit-task`, `finish-task` apenas com Task Verdict APROVADO; `ux-review` para ação/feedback em `/users`.
- Não selecionados: `create-migration` (schema existente); `create-adr` (ADR-001/modelo suficiente); Rabbit/MCP (fora de escopo); SRE obrigatório (sem mudança de infra prevista).

## Etapas

1. Política aprovada: rejeitar desativação de User líder de Team com 409 seguro até reassociação em task própria; nenhuma troca automática de liderança.
2. Implementar contrato explícito de status, tenant-scoped pelo `TenantService`, transições permitidas, hierarquia, proibição de self e reativação somente com hash existente.
3. Serializar por Guild a proteção de MARECHAL ACTIVE para status, role e exclusão; após lock, revalidar alvo e contagem. Incrementar `credentialVersion` ao desativar; a sessão antiga não deve voltar após reativação.
4. Garantir que User que lidera Team recebe conflito conforme decisão; não apagar FK ou trocar líder implicitamente.
5. Adicionar ações na lista Users mantendo Angular standalone/Material, confirmação, loading/feedback e controles RBAC apenas visuais.
6. Criar testes backend HTTP/integrados e unitários necessários; validar persistência, JWT, hierarquia, cross-Guild, último MARECHAL, Team e concorrência. Se locking não for comprovado em H2, executar PostgreSQL 18 descartável.
7. Criar specs Angular para RBAC/status/erro e regressão, rodar testes frontend completos e build; executar `mvnw.cmd clean verify`.
8. QA → Security → Auditor. Não fazer commit/push. Atualizar state/handoff e finalizar apenas se Task Verdict aprovado; manter Release Verdict global com RabbitMQ adiado.

## Testes mínimos

- MARECHAL e GENERAL permitidos segundo hierarquia; papéis inferiores/self/superior 403; anônimo 401; cross-Guild 404 e nenhuma mutação.
- ACTIVE→DISABLED→ACTIVE; PENDING e transição repetida recusadas; resposta não expõe campos confidenciais.
- JWT inicial 401 depois do disable e permanece 401 depois de reativação; novo login 200; senha permanece hash sem alteração.
- Último MARECHAL ACTIVE protegido em status/role/delete, inclusive com MARECHAL pendente/desabilitado; concorrência segura.
- Team leader bloqueado ou tratado conforme decisão explícita; sem mutação parcial.
- Frontend exibe comandos coerentes com RBAC, confirmação e retorno HTTP sem substituir autorização backend.

## Decisão registrada

O usuário escolheu bloquear desativação de User líder de Team até reassociação. O backend não oferece reassociação de liderança neste escopo; a API retorna conflito 409 sem alteração parcial. O erro RabbitMQ está fora do escopo e foi deliberadamente adiado, mantendo o bloqueio de release global.
