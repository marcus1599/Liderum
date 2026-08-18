---
name: audit-task
description: Execute auditoria final de tarefa Liderum antes do encerramento; faça gate de escopo, diff, testes, segurança, migrations, dependências e documentação. BUILD SUCCESS isolado não aprova.
---
# Audit Task
**Consumidores:** QA, Security, SRE/DevOps, Auditor. Leia PRD/plano, security review, estado, biblioteca, migrations, `git status` e `git diff`.
## Procedimento
Compare diff com escopo; valide arquivos, scope creep, testes, security review, secrets, dependências, migrations, documentação e estado. Registre `audit.md` com veredito e evidência.
## Bloqueadores
Testes falhos; security review reprovado; secret exposto; migration necessária ausente; dependência sem avaliação; scope creep relevante não autorizado => REPROVADO.
## Saída
Tabela de evidências, APROVADO/REPROVADO e pendências.
