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

## Execução autônoma opt-in de roadmap

`ROADMAP_AUTO_EXECUTION` é um modo explícito, limitado ao roadmap e à autorização atuais; não é o padrão para pedidos comuns. Ative-o somente quando o proprietário autorizar continuar um roadmap identificado, executar tasks sequencialmente e criar commits locais nas condições descritas. Registre essa autorização em `.ai/roadmap-execution-state.json`, sem credenciais, tokens ou dados pessoais. O checkpoint é operacional, não é uma segunda fonte de requisitos ou de status: `tasks/active/`, PRD/plano, relatórios, Git e decisões do proprietário continuam sendo fontes de verdade.

O arquivo mantém apenas uma task corrente e referências relativas aos artefatos já existentes. Campos mínimos: versão do formato; modo (`ROADMAP_AUTO_EXECUTION` ou `STOPPED`); estado do roadmap (`ACTIVE` ou `COMPLETED`), caminho autorizado e fonte da autorização; task/id, ordem e diretório; status da task; fase (`PLANNING`, `IMPLEMENTING`, `VALIDATING`, `GATES`, `FINALIZING`, `COMMIT_PENDING`, `COMMITTED`, `STOPPED`); último marco confirmado; próxima ação concreta; agentes/skills previstos; evidências e gates por referência/status; branch, commit base e arquivos pendentes; commit (`pending`, `created`, `verified`, `blocked`, hash quando conhecido); bloqueador/condição de retomada; data UTC. Não copie PRD, plano ou logs para o checkpoint. Um roadmap concluído mantém sua autorização apenas como registro histórico, não como autorização para outro roadmap.

Atualize o checkpoint ao ativar uma task, concluir implementação, terminar cada validação/gate, finalizar/arquivar, iniciar staging, criar/verificar commit, parar por bloqueio e selecionar a próxima task. Registre apenas marcos confirmados; saída de ferramenta interrompida ou ambígua não é sucesso. A task ativa no filesystem é autoritativa. Ao retomar, compare checkpoint com diretórios active/completed/closed, PRD/plano/relatórios e `git status`, `git log` e diff relevante. Se checkpoint estiver ausente ou atrasado, reconstrua os campos a partir dessas fontes; se houver divergência não resolvível, marque `STOPPED` e peça decisão. Não repita ação que já esteja confirmada nos artefatos/Git.

Para cada task elegível: confirme que existe exatamente uma task ativa e plano suficiente; selecione Agents e Skills segundo esta tabela e o plano; implemente somente o escopo; rode os testes/gates obrigatórios e registre resultados; execute `finish-task` somente com Task Verdict APROVADO; atualize state/handoff/roadmap; faça staging seletivo e revise o diff; crie um commit local isolado apenas quando autorizado pelo opt-in, gates aprovados e sem alterações independentes misturadas. Nunca faça push, merge, rebase ou amend. Antes do commit, use aprovação escalada específica para escrita em `.git/` se o sandbox exigir. Se negada/indisponível, deixe arquivos documentais e de implementação íntegros, marque `COMMIT_PENDING`/bloqueado e pare antes de ativar a próxima task.

Só depois de confirmar task arquivada, hash do commit e working tree sem alterações pertencentes à task, selecione a próxima task elegível pela ordem e dependências do roadmap. Ative uma por vez. Quando não restar task obrigatória e todos os gatilhos condicionais tiverem sido avaliados, marque o roadmap `COMPLETED`; tarefa condicional cujo gatilho não ocorreu recebe `NOT_APPLICABLE`, nunca status de implementada. Em retomada de checkpoint `COMPLETED`, confirme estado, commits e ausência de task elegível, informe que não há trabalho pendente e não reative tasks concluídas nem inicie outro roadmap. Novo roadmap sempre exige autorização explícita do proprietário. Não avance após gate reprovado, teste obrigatório falho, conflito, divergência de estado, decisão de produto/autorização pendente, alteração independente misturada ou impossibilidade de commit. Inspeção visual indisponível não é aprovação automática nem reprovação automática: aplique o critério declarado no plano/gates, registre exatamente a evidência faltante e pare se o gate exigir evidência visual.

Casos de recuperação: (1) reinício após implementação/testes/gate, mas antes do checkpoint — reconcilie relatórios e Git, marque o marco apenas se evidenciado e prossiga do próximo passo; (2) task movida para completed e commit encontrado — confirme hash/diff e siga para próxima elegível sem repetir `finish-task`; (3) task completed sem commit — não inicie outra task, reavalie autorização e tente apenas o commit isolado; (4) commit existe mas checkpoint não — valide `git show` contra os arquivos da task e registre hash; (5) checkpoint diz commit, mas Git não o confirma ou há arquivos mistos — pare e peça decisão; (6) mais de uma task ativa ou uma task ativa diferente da registrada — pare e reconcilie com o proprietário; (7) checkpoint perdido — reconstrua de filesystem/Git; não inferir gate, autorização ou sucesso sem evidência.

Pedidos explícitos de pausa, stop conditions ou revogação mudam o modo para `STOPPED`. Ao receber “Continue o roadmap ativo”, consulte primeiro `.ai/roadmap-execution-state.json` e reconcilie o estado com Git e filesystem. Se `roadmapState` for `COMPLETED`, confirme commits das tasks obrigatórias, status das condicionais e ausência de task ativa/elegível; informe que não há pendências, não reative tasks concluídas e não inicie outro roadmap. Nova autorização explícita do proprietário é necessária para outro roadmap, push/deploy, ampliar escopo, contornar bloqueio ou iniciar tarefa que não pertença à sequência autorizada.

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
