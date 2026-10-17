# Auditoria — rollout-authenticated-password-change-and-jwt-transition

Data: 2026-10-17.

## Rastreabilidade e escopo

| Gate | Evidência | Conclusão |
| --- | --- | --- |
| Revisão/CI | GitHub `main` = Render deploy `fb0b9a4`; Build Backend (`build` e `postgres-flyway`) e Fullstack validation concluídos com sucesso | Revisão exata comprovada |
| Drenagem | Render `dep-db9n8o67bikc73dt6rc0` `live`, predecessor `dep-db76ikcs728c73dk48og` `deactivated`; serviço com uma instância | Substituição comprovada no nível da plataforma; sem tracing por request |
| Banco/startup | History V1–V4 `success=true`, `users.credential_version` bigint NOT NULL DEFAULT 0, logs Hikari/Flyway/JPA/Tomcat, health 200/UP | APROVADO |
| Smoke | Registro 201; login/perfil 200; `/members` 200; troca 204; JWT anterior 401; senha anterior 401; novo login/perfil 200; script exit 0 | APROVADO |
| JWT legado | Teste de integração com JWT legado validamente assinado na revisão implantada + CI verde; request equivalente direto em produção não realizado por ausência de artefato seguro pré-deploy | Limitação aceita explicitamente pelo usuário, único usuário atual; não apresentada como execução direta |
| QA | `qa.md` | APROVADO após decisão explícita de aceite equivalente |
| Security | `security-review.md` | Task Security Verdict APROVADO; Release Verdict BLOQUEADO por RabbitMQ preexistente |
| SRE/DevOps | `sre-review.md` | Task operacional APROVADA; Release Verdict BLOQUEADO |
| Git/escopo | `git status`, `git diff`, `git diff --check` | Nenhum produto/configuração/teste modificado nesta task; somente artefatos `.ai` do rollout e snapshot/handoff. Alterações Impeccable preexistentes separadas; nenhum staged/commit/push |

Não houve migration, dependência ou alteração de código nesta task operacional. A V4 já versionada foi inspecionada em PostgreSQL real e não foi reaplicada manualmente. Nenhum valor de secret, senha, JWT, header ou dado de conta de smoke consta dos artefatos. Busca direcionada por padrões de token/segredo nos documentos da task não encontrou valores. `git diff --check` saiu com code 0; avisos de normalização LF/CRLF não são falhas de whitespace.

## Achado externo ao escopo

Erro AMQP 406 `PRECONDITION_FAILED` por argumentos divergentes da fila `notification.guild-event-created` aparece em logs de 2026-10-13 e 2026-10-17. Classificação: confirmado, `PREEXISTENTE_NAO_RELACIONADO`; não foi introduzido/agravado pelo rollout JWT. O backend permaneceu live, mas não há prova de entrega de notificações. Encaminhar diagnóstico/correção em task separada; não manipular fila por tentativa ou incluir no commit desta task.

## Vereditos

**Task Verdict: APROVADO.** O rollout de auth atingiu a revisão exata, drenagem comprovada no Render, V4/startup, revogação e smoke funcional. A lacuna de JWT legado direto permanece descrita sem falsear teste, com aceite equivalente explícito do usuário; QA, Security e SRE aprovaram. Sem scope creep, regressão observada ou alteração de produto.

**Release Verdict: BLOQUEADO.** Achado RabbitMQ preexistente e não relacionado pode impedir notificações; é bloqueador global conhecido até diagnóstico e validação própria. Esse veredito não reprova a task de auth pelo gate policy atual.

`finish-task` está autorizado pelo Task Verdict, preservando o Release Verdict bloqueado em `state.md`/`handoff.md`. Não fazer commit nem push nesta finalização.
