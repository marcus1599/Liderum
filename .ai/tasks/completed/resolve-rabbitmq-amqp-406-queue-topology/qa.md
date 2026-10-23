# QA — `resolve-rabbitmq-amqp-406-queue-topology`

Data: 2026-10-21

## Evidências

- `backend/.\mvnw.cmd clean verify`: 92 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`, exit code 0.
- `..\backend\mvnw.cmd clean verify` em `notification-service/`: 4 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`, exit code 0.
- CloudAMQP: a fila principal e DLQ estavam sem ready/unacked; log Render confirma uma execução AuditLog. A segunda publicação do mesmo ID foi registrada como duplicada e suprimida pelo dispatcher.
- Verificação posterior do broker: 0 consumers e 0 conexões. Esse snapshot não prova a causa nem disponibilidade contínua.

## Lacunas

- O `clean verify` do Notification Service não executa `RabbitMqNotificationIntegrationIT` (`*IT`, habilitado por `rabbitmq.integration=true`, sem Failsafe no POM); por isso o teste direcionado foi executado separadamente.
- O teste isolado passou contra RabbitMQ 3.13 descartável, mas publica diretamente no exchange e usa dispatcher mockado.
- O teste existente publica via RabbitTemplate diretamente no exchange e mocka o dispatcher; não valida estratégia AuditLog, retry ou DLQ.
- O fluxo real `EventService → GuildEventCreatedPublisher/RabbitTemplate → exchange → listener → dispatcher → AuditLog` não foi exercitado.
- O publisher captura `AmqpException`, registra warning e não propaga falha; Event pode ser criado sem notificação. Não foi localizada configuração de publisher confirms/returns. Isso é compatível com best-effort apenas se a falha permanecer observável e não houver alegação de entrega confirmada; o log atual “Published ...” após `convertAndSend` não comprova confirmação do broker.
- Não há evidência automatizada atual para tentativas de retry, encaminhamento à DLQ após falha, mensagem inválida ou duplicidade após reinício. A idempotência in-memory após restart é uma limitação conhecida, não entrega garantida.
- O canal Audit foi observado no smoke. Logs sem `Discord sent/skipped` não comprovam que Discord está desligado. O estado efetivo de `NOTIFICATION_CHANNELS_ENABLED` e a presença (não valor) de `DISCORD_WEBHOOK_URL` continuam desconhecidos.

## Veredito

**QA: REPROVADO por evidência incompleta; não foi observada falha funcional nos testes executados.** Evidência adicionada: `RabbitMqNotificationIntegrationIT` passou 1/1 em RabbitMQ 3.13 descartável. Permanecem sem prova o publisher real do backend, execução da estratégia AuditLog automatizada, retry/DLQ e comportamento observável de falha de publicação. Best-effort remove requisito de disponibilidade contínua, não esses critérios de teste. Manter task ativa; não executar `finish-task`.

## Reavaliação final após cobertura automatizada — 2026-10-21

### Evidências novas

- Em `backend/`: `.\mvnw.cmd '-Dtest=GuildEventCreatedPublisherTest' test`: 1 teste, 0 failures/errors/skipped; `BUILD SUCCESS`, exit 0. A falha simulada do broker é registrada, a exceção não interrompe o fluxo best-effort e não aparece o log de sucesso de publicação.
- Em `backend/`: `.\mvnw.cmd '-Drabbitmq.integration=true' '-Dtest=EventServiceRabbitMqIntegrationIT' test`: 1 teste, 0 failures/errors/skipped; `BUILD SUCCESS`, exit 0. H2/Flyway, TenantService, EventService, publisher e RabbitTemplate reais persistem o Event e publicam payload ao exchange/routing configurados; o teste consome da fila isolada e verifica ID, nome e data.
- Em `notification-service/`: `..\backend\mvnw.cmd '-Drabbitmq.integration=true' '-Dtest=RabbitMqNotificationIntegrationIT,RabbitMqRetryAndDlqIntegrationIT' test`: 2 testes, 0 failures/errors/skipped; `BUILD SUCCESS`, exit 0, contra RabbitMQ 3.13 descartável. O primeiro usa listener, dispatcher e AuditLog strategy reais e confirma duplicata suprimida no mesmo processo. O segundo usa listener, dispatcher, strategy spy, retry e topologia reais; injeta falha somente na fronteira da estratégia e confirma 3 tentativas, mensagem na DLQ e ausência de requeue/loop.
- Em `backend/`: `.\mvnw.cmd clean verify`: 93 testes, 0 failures/errors/skipped; `BUILD SUCCESS`, exit 0.
- Em `notification-service/`: `..\backend\mvnw.cmd clean verify`: 4 testes, 0 failures/errors/skipped; `BUILD SUCCESS`, exit 0. As classes `*IT` são excluídas da suíte padrão e foram executadas separadamente conforme acima.

### Limitações e avaliação

- A publicação backend→RabbitMQ e o consumo RabbitMQ→AuditLog são cobertos por integrações isoladas separadas, não por um único teste atravessando simultaneamente os dois serviços. A integração do backend consome da fila dedicada de teste; a integração do notification-service usa a topologia da fila principal. Não se afirma um teste cross-process único.
- O teste de deduplicação demonstra apenas duplicata durante a vida do mesmo processo; o conjunto `ConcurrentHashMap` continua em memória e perde o histórico ao reiniciar. Persistência/outbox seguem fora do escopo.
- A falha sintética testa que `AmqpException` do publisher é logada e absorvida; `convertAndSend` sem publisher confirms não prova aceite/entrega pelo broker. A política aprovada continua best-effort e não garante entrega.
- Testes usaram somente RabbitMQ 3.13 local descartável e H2 isolado. Não acessaram CloudAMQP/Render, não enviaram Discord nem validaram o fluxo de produção nesta rodada. O container criado para esta execução foi parado/removido; não restou listener na porta local de teste.
- A aprovação não cobre entrega durável após restart, publisher confirms, outbox, Discord ou runtime contínuo. Discord segue desabilitado por confirmação do proprietário; habilitá-lo exige nova Security Review.

### Veredito atualizado

**QA: APROVADO**, restrito aos critérios de topologia, publicação, processamento, falha best-effort, retry, DLQ e deduplicação em memória definidos para esta task. Não houve falha funcional nos testes; as limitações acima são explícitas e não contradizem o modelo operacional best-effort aprovado.

**Release Verdict: BLOQUEADO.** A topologia foi corrigida operacionalmente e as duas metades do fluxo foram verificadas isoladamente, mas o fluxo completo em um único cenário no broker real de produção não foi executado nesta rodada (os testes foram deliberadamente isolados; nenhum teste acessou serviços de produção). Não tratar a cobertura local como evidência de execução de produção.
