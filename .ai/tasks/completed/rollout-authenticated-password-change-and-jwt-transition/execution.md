# Execução — rollout-authenticated-password-change-and-jwt-transition

Data: 2026-10-17. Execução operacional autorizada pelo usuário; nenhuma alteração de produto, configuração, commit ou push nesta rodada.

## Revisão e CI

- GitHub `main` e Git local `HEAD`: `fb0b9a45279534dacb757ea48c7d9cdfbdbe7345` (implementação de auth no ancestral `63fb032`).
- GitHub Actions para esse SHA: [Build Backend](https://github.com/marcus1599/Liderum/actions/runs/39035649835) e [Fullstack validation](https://github.com/marcus1599/Liderum/actions/runs/39035649877) concluídos com sucesso. No primeiro, os jobs `build` e `postgres-flyway` passaram.
- Render já havia feito auto-deploy da revisão exata: `dep-db9n8o67bikc73dt6rc0`, status `live` desde 2026-10-17 13:17 UTC. Não foi disparado deploy redundante. Deploy anterior `dep-db76ikcs728c73dk48og` aparece como `deactivated` no Render desde 13:17 UTC. Serviço configurado com uma instância; não há `healthCheckPath` HTTP configurado (Render usa verificação TCP padrão). Essa evidência é de estado da plataforma, não rastreamento de cada request em uma instância.

## Banco e startup

- PostgreSQL Render 18.6 disponível. Consulta read-only de `flyway_schema_history`: versões 1 a 4 com `success=true`.
- `users.credential_version`: `bigint`, `NOT NULL`, default `0`, confirmado via `information_schema`.
- Logs do deploy: Hikari conectou; Flyway validou quatro migrations, aplicou V4 a partir da versão 3; `EntityManagerFactory` iniciou; Tomcat iniciou na porta 10000; aplicação iniciou com profile `prod`.
- Na retomada após repouso do free tier, Flyway encontrou versão 4 e não reaplicou migration; Hibernate/JPA e Tomcat iniciaram. `/actuator/health` retornou HTTP 200 e `status=UP`. O primeiro timeout durante cold start não foi tratado como falha definitiva.

## Smoke de produção

Foi criada uma Guild/conta de smoke descartável com identificadores aleatórios e e-mail sob `example.invalid`. Senhas e JWTs ficaram somente em variáveis de processo, não foram impressos, versionados nem colocados em URLs. Não existe remoção operacional aprovada para esses registros; permanecem no banco de teste de produção.

| Cenário | HTTP observado |
| --- | ---: |
| `POST /auth/register-guild` | 201 |
| login com senha inicial | 200 |
| `GET /users/me` com JWT inicial | 200 |
| `GET /members` tenant-scoped | 200 |
| `PUT /users/me/password` | 204 |
| reutilização do JWT anterior | 401 |
| login com senha anterior | 401 |
| login com senha nova | 200 |
| `GET /users/me` com JWT novo | 200 |

O script de smoke terminou com exit code 0. Não usou conta de terceiro.

## Limitação e aceite equivalente autorizado

Não havia JWT legado **validamente assinado**, anterior à revisão com `uid`/`cv`, de uma conta de smoke disponível. Portanto não foi executada a prova HTTP direta em produção de rejeição desse tipo de token após a drenagem. O teste de integração `PasswordChangeIntegrationTest` cobre rejeição de JWT legado assinado na revisão implantada; os jobs CI da mesma revisão passaram. Forjar payload sem assinatura válida produziria 401 pelo motivo errado e não será apresentado como prova. Em resposta à solicitação explícita de decisão, o usuário aceitou essa combinação de evidências equivalentes, esclarecendo que no momento apenas ele utiliza o projeto. Essa decisão não transforma o request de produção não executado em teste executado nem altera o comportamento do sistema.

## Achado separado

Logs do backend registram `AMQP reply-code=406 PRECONDITION_FAILED` por argumentos divergentes da fila `notification.guild-event-created`. O mesmo erro aparece em logs de 2026-10-13, antes desta revisão: achado `PREEXISTENTE_NAO_RELACIONADO` ao rollout de auth, com potencial prejuízo à entrega de notificações. Não houve alteração de RabbitMQ nesta task. Requer diagnóstico/task separados; não aplicar alteração de fila por tentativa.

## Estado dos gates

- QA: APROVADO com limitação explícita e aceite equivalente autorizado; ver `qa.md`.
- Security e SRE/DevOps: avaliados após QA; ver revisões específicas.
- Auditoria: realizada após os gates; `finish-task` condicionado a Task Verdict APROVADO.
- Release Verdict: BLOQUEADO pelo achado RabbitMQ global conhecido, independente da transição JWT.

Não realizar rollback para código antigo: ele poderia voltar a aceitar tokens revogados. Nenhuma correção fora do escopo foi feita.
