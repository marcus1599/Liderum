---
name: create-plan
description: Planeje tarefas do Liderum antes de implementar mudanças funcionais, estruturais ou de risco; classifique escopo, agentes, testes, riscos e critérios objetivos.
---
# Create Plan
**Consumidores:** Planner, Arquiteto. **Objetivo:** produzir o menor plano executável, sem implementar.
## Contexto
Leia `roles.md`, `state.md`, `lib.md`, task, código, testes, configuração e Git relevantes.
## Procedimento
Classifique TRIVIAL/SMALL/MEDIUM/LARGE/STRUCTURAL; defina objetivo, escopo e fora do escopo; selecione agentes, dependências, etapas, testes, riscos e critérios de conclusão. Registre em `tasks/active/<task>/plan.md`.
## Bloqueadores
Escale requisito essencial ausente, conflito ou decisão arquitetural. Não crie ADR automaticamente; dependência não avaliada ou escopo sem limite reprova o plano.
## Saída
Classificação, escopo, agentes, etapas, testes, riscos, pendências e critérios verificáveis.
