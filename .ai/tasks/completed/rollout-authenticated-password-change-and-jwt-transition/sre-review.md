# SRE/DevOps — rollout-authenticated-password-change-and-jwt-transition

Data: 2026-10-17. Status da task operacional: **APROVADO**; estado global de release: **BLOQUEADO**.

## Deploy e estabilidade

- Render já havia autoimplantado `fb0b9a4` no serviço backend. Deploy `dep-db9n8o67bikc73dt6rc0` continua `live`; predecessor `dep-db76ikcs728c73dk48og` consta `deactivated`. O serviço configura uma instância. Não houve novo trigger, alteração de ambiente nem intervenção em banco.
- PostgreSQL 18.6 disponível; V1–V4 com `success=true`; logs mostram Hikari, Flyway V4, JPA/Hibernate e Tomcat na porta 10000. Reinício por cold start do free tier voltou ao estado UP sem reaplicar migration. Health HTTP 200/UP. Render usa health TCP padrão, sem `healthCheckPath` HTTP; isto não impediu o deploy observado.
- GitHub Actions da revisão exata: backend (inclusive `postgres-flyway`) e fullstack passaram. Smoke de auth/tenant terminou exit 0, com status em `execution.md`.
- O estado `deactivated` do deploy anterior é evidência de drenagem da plataforma. Não há rastreamento por request da instância antiga; a prova direta de JWT legado assinado em produção não foi feita e foi substituída pelo conjunto aceito pelo usuário. Não confundir esse aceite com telemetria inexistente.

## Risco operacional separado

`PRECONDITION_FAILED` AMQP 406 para `notification.guild-event-created` foi observado tanto antes (2026-10-13) como depois (2026-10-17) desta revisão. O backend permaneceu live, mas a entrega de notificações não está comprovada e pode estar comprometida. Origem: `PREEXISTENTE_NAO_RELACIONADO`. Encaminhar para task própria de diagnóstico de topologia/argumentos de fila; não deletar/recriar fila nem mudar configuração por tentativa. Isso bloqueia o Release Verdict global, não o fechamento do rollout JWT.

## Recuperação

Não fazer rollback para código pré-`uid`/`cv` sem revisão Security: tal código pode aceitar JWT revogado. Preferir correção forward se surgir regressão. A Guild/conta de smoke permanece no banco de teste de produção; dados declarados descartáveis, sem remoção improvisada nesta task.
