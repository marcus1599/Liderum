# Liderum — Agent/Skill Router

> Regras declarativas para a sessão principal/orquestrador. O Router não é Agent, Skill, MCP Tool ou roteador de modelos.

## Princípios

- Roteie pelo estado real da task, não apenas por palavras do pedido.
- `handoff.md` é entrada rápida; código, Git, task, `state.md` e ADRs específicos prevalecem.
- Selecione somente Agents e Skills necessários; consultivos não implementam e executores não aprovam a própria task.
- Registre uma seleção curta em `plan.md` ou no relatório operacional. Não repita Agent sem nova evidência e não faça loops de escalonamento.

## Contexto mínimo antes de rotear

1. `handoff.md`
2. `roles.md`
3. `state.md`
4. `lib.md`
5. task ativa
6. `plan.md`
7. `prd.md`, quando existir
8. ADRs relacionados
9. Git/diff, quando relevante

Sem task ativa, plano ou requisito suficiente, pare e informe a lacuna; não invente task nem workflow.

## Classificação de domínio

Classifique um ou mais domínios: `planning`, `architecture`, `backend`, `frontend`, `testing`, `security`, `sre`, `audit`, `database`, `messaging`, `documentation`.

| Sinal real | Agent principal | Skill condicional |
| --- | --- | --- |
| plano, escopo ou requisito ambíguo | Planner | `create-plan`, `create-prd` proporcionalmente |
| decisão estrutural, tecnologia, trade-off duradouro, tenant ou mensageria relevante | Arquiteto | `create-adr` somente com autorização |
| Java, Spring, API, services, repositories, notification-service, RabbitMQ producer/consumer | Backend Dev | `test-backend` |
| Angular, componentes, guards, formulários, Material, RxJS ou API UI | Frontend Dev | `test-frontend` |
| schema, tabela, coluna, constraint ou índice | Backend Dev; Arquiteto se estrutural | `create-migration`, `test-backend` |
| JWT, auth, autorização, secrets, IDOR, Guild, dado sensível, integração externa ou dependência sensível | Security com executor afetado | `security-review` |
| Docker, Compose, CI/CD, ambiente, observabilidade, health check, RabbitMQ operacional ou deploy | SRE/DevOps | `security-review` se houver superfície sensível |
| comportamento a validar após implementação | QA | `test-backend` e/ou `test-frontend` |
| entrega pronta para gate final | Auditor | `audit-task`; `finish-task` apenas se aprovado |

## Ordem de workflow

`Planning → Architecture (quando necessário) → execução Backend e/ou Frontend → QA → Security/SRE (quando aplicável) → Auditoria → Finalização`.

Security e Auditoria distinguem o veredito da task do veredito de release. Achado preexistente não relacionado deve abrir ou recomendar task própria e pode bloquear release/deploy, mas não reprova automaticamente uma task que não o introduziu, agravou nem depende dele para validar a própria alteração.

Para fullstack, Backend define/valida contrato antes de Frontend quando o contrato ainda não existe; com contrato estável, os executores podem atuar conforme o plano. Security e SRE são gates condicionais, não etapas automáticas. QA valida comportamento; Auditor valida escopo, evidências, segurança, dependências, migrations e documentação.

## Regras de Skills

- `create-plan`: task sem plano, planejamento pedido ou escopo ambíguo.
- `create-prd`: somente tarefa funcional relevante com regras de negócio suficientes; não automatizar para TRIVIAL/SMALL.
- `create-adr`: somente decisão relevante identificada pelo Arquiteto e autorizada pelo usuário.
- `create-migration`: toda mudança estrutural de schema; nunca migration aplicada.
- `test-backend` / `test-frontend`: quando os respectivos domínios forem alterados.
- `security-review`: somente quando houver superfície real de segurança.
- `audit-task`: implementação concluída e pronta para gate.
- `finish-task`: somente após `Task Verdict = APROVADO` no `audit-task`; Release Verdict bloqueado deve permanecer registrado no handoff e na task de remediação.

## Execução de task ativa

Para “Execute a task ativa”, leia o contexto mínimo, localize exatamente uma task em `tasks/active/`, leia seu plano e classifique os arquivos/risco afetados. Selecione Agents, Skills e ordem; registre os não selecionados relevantes com motivo. Se não houver task ativa, houver mais de uma sem prioridade clara, ou faltar plano essencial, pause e solicite direção.

## Escalonamento e anti-loop

Executor encontra decisão estrutural inesperada → Arquiteto; vulnerabilidade/superfície insegura → Security; problema ambiental → SRE; comportamento sem cobertura → QA; scope creep ou autorização necessária → sessão principal. Após escalar, retome o Agent anterior somente com decisão ou evidência nova; caso contrário permaneça bloqueado.

## Registro mínimo de roteamento

```text
Routing:
- Domínios: <...>
- Agents: <selecionados, em ordem>
- Skills: <selecionadas, em ordem>
- Não selecionados: <Agent — motivo relevante>
- Gate: <condições para QA/auditoria/finalização>
```

## Limites

O Router não altera produto, MCP, Agents ou Skills em nome próprio; apenas orienta a sessão principal. Não introduz model routing e não chama todos os Agents por padrão.
