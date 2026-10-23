# Liderum — Agent/Skill Router

> Regras declarativas para a sessão principal/orquestrador. O Router não é Agent, Skill, MCP Tool ou roteador de modelos.

## Princípios

- Roteie pelo estado real da task, não apenas por palavras do pedido.
- `handoff.md` é entrada rápida; código, Git, task, `state.md` e ADRs específicos prevalecem.
- Consulte `roadmap.md` ao criar ou priorizar task, planejar trabalho, avaliar decisão arquitetural ou concluir uma fase; não o releia obrigatoriamente em execução pequena já delimitada por PRD e plano.
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
10. `roadmap.md`, somente para criação/priorização/planejamento de task, decisão arquitetural ou conclusão de fase

Sem task ativa, plano ou requisito suficiente, pare e informe a lacuna; não invente task nem workflow.

## Classificação de domínio

Classifique um ou mais domínios: `planning`, `architecture`, `backend`, `frontend`, `testing`, `security`, `sre`, `audit`, `database`, `messaging`, `documentation`.

| Sinal real | Agent principal | Skill condicional |
| --- | --- | --- |
| plano, escopo ou requisito ambíguo | Planner | `create-plan`, `create-prd` proporcionalmente; `ux-review` como critério consultivo em PRDs/planos de interface |
| decisão estrutural, tecnologia, trade-off duradouro, tenant ou mensageria relevante | Arquiteto | `create-adr` somente com autorização |
| Java, Spring, API, services, repositories, notification-service, RabbitMQ producer/consumer | Backend Dev | `test-backend` |
| Angular, componentes, guards, formulários, Material, RxJS ou API UI | Frontend Dev | `ux-review` para tarefas de UX; `test-frontend` para validar mudanças de UI |
| schema, tabela, coluna, constraint ou índice | Backend Dev; Arquiteto se estrutural | `create-migration`, `test-backend` |
| JWT, auth, autorização, secrets, IDOR, Guild, dado sensível, integração externa ou dependência sensível | Security com executor afetado | `security-review` |
| Docker, Compose, CI/CD, ambiente, observabilidade, health check, RabbitMQ operacional ou deploy | SRE/DevOps | `security-review` se houver superfície sensível |
| comportamento a validar após implementação | QA | `ux-review` como critérios de UI quando aplicável; `test-backend` e/ou `test-frontend` para validação/testes |
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
- `ux-review`: em tarefas que planejem ou alterem experiência de interface; critério consultivo de UX, sem substituir `test-frontend`.
- `security-review`: somente quando houver superfície real de segurança.
- `audit-task`: implementação concluída e pronta para gate.
- `finish-task`: somente após `Task Verdict = APROVADO` no `audit-task`; Release Verdict bloqueado deve permanecer registrado no handoff e na task de remediação.

## Execução de task ativa

Para “Execute a task ativa”, leia o contexto mínimo, localize exatamente uma task em `tasks/active/`, leia seu plano e classifique os arquivos/risco afetados. Selecione Agents, Skills e ordem; registre os não selecionados relevantes com motivo. Se não houver task ativa, houver mais de uma sem prioridade clara, ou faltar plano essencial, pause e solicite direção.

## Encerramento administrativo

O proprietário pode encerrar explicitamente uma iniciativa sem aprovação técnica usando `CLOSED_WITH_LIMITATIONS`. Isso não equivale a conclusão, aprovação de QA/Security/Auditoria ou Release; não executa `finish-task` e não apaga pendências ou evidências. Registre a decisão e o estado técnico em `tasks/closed/<id>/closure.md`, preservando os artefatos existentes. Mova a pasta da task de `active/` para `closed/` somente após registrar identificador, motivo, data, gates, limitações, evidências e condições de reabertura. `state.md` e `handoff.md` devem apontar para o encerramento e preservar blockers globais. Tasks em `closed/` não bloqueiam o Router; reabertura exige decisão explícita e retorno a `active/`. Nunca altere os vereditos existentes para viabilizar o encerramento. `finish-task` continua exclusivo para task com Auditoria APROVADA.

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
