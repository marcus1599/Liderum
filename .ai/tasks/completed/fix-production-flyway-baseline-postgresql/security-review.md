# Security Review — Production Flyway Recovery

## Superfície avaliada

- credenciais de banco mantidas somente no Render;
- Flyway, schema e Hibernate `validate`;
- registro público, JWT/login, `/users/me`, endpoint tenant-scoped e CORS em produção.

## Achados

| Achado | Evidência | Origem | Impacto |
| --- | --- | --- | --- |
| Rate limiter não comprovou 429 após seis registros consecutivos | limite default 5 sem override; seis respostas 201; filtro consta no SecurityFilterChain | `PREEXISTENTE_NAO_RELACIONADO` | bloqueia release até investigação/correção própria |
| CORS sem origin de frontend configurada | `CORS_ALLOWED_ORIGINS` ausente; preflight de origin não permitida retorna 403 sem liberação | `PREEXISTENTE_NAO_RELACIONADO` | bloqueia validação do frontend real; não abre CORS indevidamente |
| Health protegido | `/actuator/health` retorna 403 e Render não usa health check path | `PREEXISTENTE_NAO_RELACIONADO` | pendência operacional não bloqueante para esta recuperação |

## Vereditos

- **Task Security Verdict: APROVADO.** A recuperação não introduziu nem agravou secrets, JWT, RBAC, tenancy ou schema inseguro.
- **Release Verdict: BLOQUEADO.** O controle antiabuso público não está comprovado no proxy Render e a origin HTTPS real do frontend não foi configurada/validada.

## Encaminhamento

Planejar task P0 separada para diagnosticar e corrigir rate limiting atrás do proxy Render e configurar/validar CORS com origin exata, sem wildcard.
