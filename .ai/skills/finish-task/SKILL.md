---
name: finish-task
description: Finalize tarefa Liderum somente após auditoria aprovada, preservando seus artefatos e atualizando state.md e handoff.md sem fazer commit ou push.
---
# Finish Task
**Consumidor:** Auditor. Leia `audit.md`, PRD, plano, estado, handoff, documentação e Git.
## Procedimento
Confirme `audit-task` APROVADO; registre pendências futuras separadas; preserve `prd.md`, `plan.md` e `audit.md` quando existirem; mova `tasks/active/<task>/` para `tasks/completed/<task>/`. Antes de mover, confirme que o diretório ativo existe e o destino não existe. Se o destino já existir, não sobrescreva, mescle nem mova novamente: reconcilie filesystem, auditoria e Git; se já estiver concluída/commitada, trate como recuperação idempotente e não crie commit duplicado.

Atualize `state.md` somente se a task alterar o snapshot técnico. Em seguida, atualize obrigatoriamente `handoff.md`: registre a task encerrada em **Última task concluída**, recalcule **Task ativa** a partir de `tasks/active/`, mantenha apenas bloqueadores atuais, derive o próximo passo do estado e das pendências verificadas e preserve o resumo curto. `handoff.md` é porta de entrada; código, Git, `state.md`, tasks e ADRs específicos prevalecem em caso de divergência.

Se `ROADMAP_AUTO_EXECUTION` estiver ativo, atualize também `.ai/roadmap-execution-state.json` após a movimentação e atualização do handoff, apontando a fase seguinte para validação final/staging. Não marque task commitada nem selecione a próxima task aqui: isso exige confirmação Git posterior pelo Router. O checkpoint referencia esta task e seus artefatos; não substitui os vereditos nem os registros acima. Após confirmação do commit e avaliação da elegibilidade, marque o roadmap `COMPLETED` quando não houver trabalho obrigatório; condições de tarefa opcional não satisfeitas devem ser registradas `NOT_APPLICABLE`, não como entrega concluída.
## Bloqueadores
Auditoria ausente/reprovada, testes falhos ou artefatos incompletos bloqueiam. Nunca faça commit ou push.
## Saída
Destino, validações, atualizações de `state.md`/`handoff.md` e pendências.
