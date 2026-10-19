# Plano — reassign-team-leader-before-staff-deactivation

**Status:** planejamento aprovado para execução pelo usuário em 2026-10-19. **Classificação:** MEDIUM, funcionalidade/segurança, P1 do Ciclo 3.

## Routing

- Domínios: planning, backend, frontend, testing, security, audit.
- Agents: Planner → Backend Dev → Frontend Dev → QA → Security → Auditor. Arquiteto consultivo para a ordem de locks/contrato caso apareça conflito estrutural; SRE/DevOps apenas se surgir mudança operacional.
- Skills: `create-prd`, `create-plan`, `ux-review` nesta etapa; `test-backend`, `test-frontend`, `security-review`, `audit-task`, `finish-task` durante execução e gates. Impeccable opcional e manual para detalhe visual, sem ampliar UX.
- Não selecionados: `create-migration` (schema existente); `create-adr` (ADR-001 suficiente); SRE obrigatório (sem infra); Rabbit/MCP (fora de escopo).
- Gate: QA valida comportamento e testes; Security valida RBAC/IDOR/status/corrida; Auditor compara diff com PRD/plan e separa Task/Release Verdict; `finish-task` somente se Task Verdict APROVADO.

## Sequência de implementação proposta

1. Backend: confirmar handlers HTTP e política de locks entre `UserServiceImpl.updateStatus` e reassociação; registrar ordem segura e teste de concorrência proporcional. Escalar se exigir mudar política aprovada da task anterior.
2. Criar DTO de reassociação com `leaderId` obrigatório; consulta tenant-scoped de candidatos ACTIVE com projeção mínima; endpoint protegido pelas mesmas roles administrativas de Team. Preservar semântica de `PUT /teams/{id}` para nome.
3. Revalidar Team e candidato no contexto da Guild do ator, sob transação/controle concorrente; persistir somente `leader`. Aplicar a mesma elegibilidade ao `leaderId` explícito no POST existente, mantendo o default = criador. Adicionar `leaderId` à resposta sem expor User integral. Não alterar o bloqueio 409 do status salvo defeito comprovado dentro do fluxo aprovado.
4. Testes backend HTTP/integração: MARECHAL/GENERAL/MAJOR permitidos; CAPITÃO/SOLDADO 403, anônimo 401; Team e candidato cross-Guild 404 sem mutação; PENDING/DISABLED inválidos também no POST com `leaderId` explícito; campos extras rejeitados/ignorados sem alterar dados; mesmo líder e múltiplas Teams; corrida reassociação/desativação; resposta/lista mínima. Priorizar PostgreSQL 18 descartável se H2 não demonstrar a garantia de lock.
5. Frontend: exibir líder e controle Material de reassociação em `/teams`, usando `AuthService.profile$` para RBAC visual. Carregar candidatos apenas ao abrir o fluxo; tratar loading, vazio, erro e confirmação; enviar somente `leaderId`; atualizar por resposta/refetch seguro.
6. Specs Jasmine/Karma para service e component: payload, RBAC, dados mínimos, estado de seleção, loading/sucesso/403/404/409/rede, não-mutação de estado em erro, acessibilidade básica; preservar specs existentes de Teams, Users e auth.
7. Rodar testes backend direcionados, `backend/mvnw.cmd clean verify`, `npm test -- --watch=false --browsers=ChromeHeadless` e `npm run build` em `frontend/`; aguardar exit/result final e registrar contagens reais, sem antecipá-las no planejamento.
8. Gates QA → Security → Auditor; SRE condicional. Atualizar execução e documentação somente por evidência, então `finish-task` se aprovada. Não fazer commit/push sem autorização posterior.

## Riscos e limites

- `MAJOR` não pode usar `/users`; endpoint de candidatos deve ser próprio e mínimo. Não conceder gestão de contas nem criar endpoint público.
- `leaderId` do cliente é identificador de recurso, não fonte de tenant. Não devolver existência de User externo.
- Evitar deadlock/regressão na desativação do líder; sucesso de reassociação não desativa User. O 409 ainda vale se ele lidera outra Team.
- Não confundir User (Staff/conta) com Member (personagem). Sem alteração de `User.member`, `Member.guildRole`, migrations ou JWT.
- Release global permanece BLOQUEADO pelo RabbitMQ AMQP 406 já adiado pelo usuário; esta task não o corrige nem o agrava por definição do escopo.

## Critérios para começar

PRD e este plano aprovados pelo usuário. Não há necessidade de ADR/migration conhecida. Se a validação de concorrência exigir alteração estrutural ou política nova, parar e escalar antes de editar código fora do escopo.
