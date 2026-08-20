---
name: audit-task
description: Execute auditoria final de tarefa Liderum antes do encerramento; faça gate de escopo, diff, testes, segurança, migrations, dependências e documentação. BUILD SUCCESS isolado não aprova.
---
# Audit Task
**Consumidores:** QA, Security, SRE/DevOps, Auditor. Leia PRD/plano, security review, estado, biblioteca, migrations, `git status` e `git diff`.
## Procedimento
Compare diff com escopo; valide arquivos, scope creep, testes, security review, secrets, dependências, migrations, documentação e estado. Registre `audit.md` com dois vereditos:

- **Task Verdict**: escopo, regressões, vulnerabilidades introduzidas/agravadas, testes, migrations, dependências e documentação da task.
- **Release Verdict**: riscos conhecidos do estado global que impedem release/deploy, inclusive achados preexistentes encaminhados em task separada.

`finish-task` depende de `Task Verdict = APROVADO`; `Release Verdict = BLOQUEADO` por achado preexistente não relacionado não impede sozinho a conclusão da task.
## Bloqueadores
Testes falhos; Task Security Verdict reprovado; secret exposto introduzido/agravado pela task; migration necessária ausente; dependência nova sem avaliação; scope creep relevante não autorizado => Task Verdict REPROVADO. Achado preexistente não relacionado deve constar no Release Verdict e no encaminhamento.
## Saída
Tabela de evidências, Task Verdict, Release Verdict, pendências e encaminhamentos.
