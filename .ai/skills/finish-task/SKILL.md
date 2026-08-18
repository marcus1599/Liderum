---
name: finish-task
description: Finalize tarefa Liderum somente após auditoria aprovada, preservando seus artefatos e atualizando state.md e handoff.md sem fazer commit ou push.
---
# Finish Task
**Consumidor:** Auditor. Leia `audit.md`, PRD, plano, estado, handoff, documentação e Git.
## Procedimento
Confirme `audit-task` APROVADO; registre pendências futuras separadas; preserve `prd.md`, `plan.md` e `audit.md` quando existirem; mova `tasks/active/<task>/` para `tasks/completed/<task>/`.

Atualize `state.md` somente se a task alterar o snapshot técnico. Em seguida, atualize obrigatoriamente `handoff.md`: registre a task encerrada em **Última task concluída**, recalcule **Task ativa** a partir de `tasks/active/`, mantenha apenas bloqueadores atuais, derive o próximo passo do estado e das pendências verificadas e preserve o resumo curto. `handoff.md` é porta de entrada; código, Git, `state.md`, tasks e ADRs específicos prevalecem em caso de divergência.
## Bloqueadores
Auditoria ausente/reprovada, testes falhos ou artefatos incompletos bloqueiam. Nunca faça commit ou push.
## Saída
Destino, validações, atualizações de `state.md`/`handoff.md` e pendências.
