---
name: security-review
description: Revise mudanças Liderum de autenticação, autorização, Guild tenant, secrets, dados sensíveis, endpoints ou integrações externas e emita veredito baseado em evidência.
---
# Security Review
**Consumidores:** Security, Backend Dev, Frontend Dev, SRE/DevOps. Leia regras, estado, biblioteca, plano/PRD, diff, código e configuração.
## Procedimento
Verifique secrets/.env, JWT, autenticação/autorização, IDOR e Guild, validação, CORS, Actuator, logs/dados sensíveis, CVEs e integrações. Classifique achados: confirmado, potencial ou informativo.
## Bloqueadores
Secret real versionado; chave JWT conhecida/fallback conhecido; acesso cross-tenant; token/credencial em log; dependência nova sem avaliação => REPROVADO.
## Saída
APROVADO/REPROVADO, checklist com evidência, achados e correções.
