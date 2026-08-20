---
name: security-review
description: Revise mudanças Liderum de autenticação, autorização, Guild tenant, secrets, dados sensíveis, endpoints ou integrações externas e emita veredito baseado em evidência.
---
# Security Review
**Consumidores:** Security, Backend Dev, Frontend Dev, SRE/DevOps. Leia regras, estado, biblioteca, plano/PRD, diff, código e configuração.
## Procedimento
Verifique secrets/.env, JWT, autenticação/autorização, IDOR e Guild, validação, CORS, Actuator, logs/dados sensíveis, CVEs e integrações. Para cada achado, classifique evidência (`confirmado`, `potencial` ou `informativo`) e origem: `INTRODUZIDO_PELA_TASK`, `AGRAVADO_PELA_TASK`, `PREEXISTENTE_RELACIONADO` ou `PREEXISTENTE_NAO_RELACIONADO`.

Emita dois vereditos:

- **Task Security Verdict**: avalia somente o risco introduzido, agravado ou impeditivo para validar a alteração da task.
- **Release Verdict**: avalia se o estado global conhecido permite release/deploy.

Achado bloqueante introduzido ou agravado pela task reprova a task. Achado preexistente relacionado que impede validar a alteração bloqueia ou reprova a task. Achado preexistente não relacionado deve ser registrado e encaminhado para task separada, sem reprovar automaticamente a task atual; ele pode bloquear release/deploy.
## Bloqueadores
Secret real versionado, chave JWT conhecida/fallback conhecido, acesso cross-tenant, token/credencial em log ou dependência nova sem avaliação bloqueiam quando forem introduzidos, agravados ou impedirem validar a alteração da task. Achados preexistentes não relacionados bloqueiam o Release Verdict, não automaticamente o Task Security Verdict.
## Saída
Task Security Verdict e Release Verdict, checklist com evidência, achados classificados por origem, encaminhamentos e correções quando autorizadas.
