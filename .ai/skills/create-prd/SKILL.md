---
name: create-prd
description: Escreva PRDs verificáveis do Liderum para tarefas MEDIUM, LARGE ou STRUCTURAL; não use para burocratizar tarefas triviais nem para implementar.
---
# Create PRD
**Consumidor:** Planner. Leia regras, estado, biblioteca, plano, ADRs e código relacionado.
## Procedimento
Crie `tasks/active/<task>/prd.md` com objetivo, contexto/problema, requisitos, regras de negócio, escopo, fora do escopo, aceite, dependências, riscos, testes e segurança/observabilidade aplicáveis.
## Bloqueadores
Requisito ambíguo, decisão arquitetural pendente ou aceite não verificável bloqueiam. PRD não autoriza código fora do escopo.
## Saída
Requisitos testáveis, contratos/arquivos impactados e pendências.
