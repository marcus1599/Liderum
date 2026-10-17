# Security Review — rollout-authenticated-password-change-and-jwt-transition

Data: 2026-10-17.

## Superfície e evidência

- Revisão Git `fb0b9a4` implantada no Render; deploy anterior `deactivated`. Nenhuma alteração de código, segredo, JWT, RBAC, tenant, CORS ou configuração foi feita nesta task operacional.
- V4 `success=true`; versão de credencial persistida com default zero/NOT NULL. `JwtUtil` exige `uid` e `cv` correspondentes ao User persistido; `JwtFilter` não usa claims como autoridade de Guild/role, carrega User atual e exige conta ativa.
- Teste de integração da revisão implantada rejeita JWT legado validamente assinado sem os novos claims e versões divergentes. Produção rejeitou JWT revogado e senha antiga após troca; novo login e `/users/me` passaram. O teste HTTP direto de JWT legado assinado em produção não ocorreu; o usuário aceitou explicitamente o conjunto de evidências equivalentes por ser o único usuário atual. Não se declara esse teste como executado.
- Smoke usou conta descartável; JWT e senhas não foram impressos, persistidos em documentação ou expostos em URL. Não se usou conta de terceiro. Não há valor de secret em artefatos da task.
- Rollback para código antigo não é seguro para revogação e não foi realizado.

## Achados

| Achado | Evidência/origem | Efeito na task |
| --- | --- | --- |
| Ausência de request direto com JWT legado assinado em produção | Confirmado; limitação operacional da prova, com aceite equivalente explícito | Não bloqueia o Task Security Verdict sob essa decisão; permanece limitação documentada |
| AMQP 406 por argumentos divergentes da fila de notificações | Confirmado em logs de 2026-10-13 e 2026-10-17; `PREEXISTENTE_NAO_RELACIONADO` | Não reprova rollout de auth; pode comprometer notificações e bloqueia Release Verdict global até diagnóstico próprio |

Nenhum achado `INTRODUZIDO_PELA_TASK`, `AGRAVADO_PELA_TASK` ou `PREEXISTENTE_RELACIONADO` bloqueante foi identificado. O endpoint de health retornou 200 sem dados sensíveis. O smoke de `/members` confirma acesso tenant-scoped da conta nova, mas não constitui reteste completo de IDOR/RBAC; esses gates pertencem à implementação já testada.

**Task Security Verdict: APROVADO.** A transição da revisão implantada preservou a barreira JWT/usuário persistido e a revogação foi observada em produção. Limitação de token legado direta explicitada e aceita, sem falsear evidência.

**Release Verdict: BLOQUEADO.** Falha RabbitMQ preexistente e não relacionada requer diagnóstico/validação de entrega de notificações em task separada antes de declarar o estado global apto a release. Não ajustar filas ou infraestrutura nesta task.
