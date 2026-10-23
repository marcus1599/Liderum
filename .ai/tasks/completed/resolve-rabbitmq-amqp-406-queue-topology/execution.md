# Execução — `resolve-rabbitmq-amqp-406-queue-topology`

## Diagnóstico somente leitura — 2026-10-21

### Código e histórico

- `backend/.../Config/RabbitMQConfig.java` e `notification-service/.../config/RabbitMQConfig.java` declaram `notification.guild-event-created` como durável e solicitam dead-letter exchange `liderum.events.dlx` e routing key `guild.event.created.dlq`.
- `git show 5e6779c^` confirma que, antes da task `make-event-notifications-reliable-and-testable`, ambos os serviços declaravam a mesma fila durável sem argumentos.
- `5e6779c` adicionou aos dois serviços os argumentos do DLX/DLQ. A leitura posterior do broker confirma que a fila persistida ainda tem `arguments={}`.
- A integração automatizada anterior usa broker descartável e confirma o caminho de mensagem em uma topologia criada do zero; ela não testa a redeclaração contra a fila persistente anterior.

### Render

- Backend `Liderum`: serviço live, deploy atual reportado no commit `8c145419`, concluído em 2026-10-19.
- `notification-service`: serviço live, deploy atual reportado no commit `bb8f6192`, concluído em 2026-09-15. Esse commit já contém a declaração DLX introduzida em `5e6779c`.
- Logs do backend em 2026-10-17 registram repetidamente: `reply-code=406`, `PRECONDITION_FAILED - Existing queue 'notification.guild-event-created' declared with other arguments`.
- A consulta dos logs do Render não revelou a comparação de argumentos `current`/`received`; a API do broker, consultada depois, confirmou `arguments={}`. Os logs de aplicação do consumer não retornaram eventos no período consultado e não comprovam operação funcional.

### Broker CloudAMQP — inventário read-only

- Instância `liderum`; engine informado pela console: **LavinMQ 2.10.2**; região AWS `ap-northeast-1` (Tokyo); vhost `lcqnaebs`.
- API read-only da fila `notification.guild-event-created`: durable `true`; argumentos `{}`; effective arguments vazios; consumers `0`; messages `0`; ready `0`; unacked `0`; estado `running`.
- O vhost contém apenas uma fila. A fila `notification.guild-event-created.dlq` não existe.
- Exchanges `liderum.events` e `liderum.events.dlx` existem, ambos direct/durable. O único binding é `liderum.events` → `notification.guild-event-created` com routing key `guild.event.created`. Não há binding DLX para DLQ.
- Não há policy regular aplicável à fila. Operator policy `lcqnaebs-max-length` aplica `max-length=20000` e `max-length-bytes=1073741824`.
- Overview no instante de leitura: conexões `0`, canais `0`, consumers `0`. Não foi publicada nem consumida mensagem durante a inspeção.
- O Render confirma que `notification-service` é um `web_service` no plano `free`, sem health-check path configurado, e não está manualmente suspenso (`not_suspended`). A consulta de eventos desde 1º de outubro não retornou eventos. Isso não comprova que o processo estava ativo naquele instante.
- A documentação oficial do Render informa suspensão automática de Web Services gratuitos após 15 minutos sem tráfego HTTP/WebSocket; Background Workers executam continuamente, mas não possuem plano gratuito. O dado `consumers=0` é confirmado, mas a causa imediata pode ser o 406, suspensão por inatividade ou ambos. Não se presume qual sem evidência adicional.
- Nenhuma URI ou senha foi aberta, copiada ou registrada.

## Implicação operacional adicional

Resolver a declaração incompatível exige remover e recriar somente a fila principal, com preflight imediato e autorização específica. Porém, mesmo com topologia corrigida, o modelo atual de Web Service gratuito pode suspender o processo consumer por ausência de requisições. Logo, a correção da fila, isoladamente, não comprova consumo confiável nem permite liberar o Release.

Não foram alterados o tipo/plano Render, variáveis, código, fila, exchanges, bindings ou deploys. Não foi criado keepalive. É necessária decisão do usuário sobre o requisito de disponibilidade do consumer e o modelo operacional/custo aceitável (por exemplo, worker contínuo pago versus manter o serviço gratuito de demonstração e aceitar indisponibilidade por idle). Nenhuma opção foi aplicada.

### Causa raiz confirmada

O broker conserva a fila durable com `arguments={}`. O código atualmente declara a mesma fila com `x-dead-letter-exchange` e `x-dead-letter-routing-key`; o histórico Git mostra que esses argumentos foram adicionados em `5e6779c` após a fila ser declarada sem argumentos. O 406 aparece no `RabbitAdmin.initialize` ao declarar filas. Isso confirma a incompatibilidade da fila persistente com a topologia desejada. A topologia de dead-letter está incompleta (DLQ e binding ausentes), e nenhum consumer está ativo no instante da inspeção.

## Resultado da rodada

- Causa raiz: **confirmada** — fila persistente sem argumentos redeclarada por código com argumentos DLX incompatíveis.
- Remediação operacional: a fila `notification.guild-event-created` foi excluída somente após autorização específica e preflight confirmando zero mensagens/consumidores/conexões. As aplicações redeclararam a topologia; exchanges e policy permaneceram intactos (evidência detalhada abaixo).
- Limitação operacional identificada: notification-service está no Render como Web Service gratuito, que pode dormir por idle; zero consumers no broker não prova por si só o motivo. A escolha de worker contínuo pode gerar custo e requer autorização separada.
- O estado observado (`messages=0`, `ready=0`, `unacked=0`, consumers `0`) é um snapshot e deve ser consultado outra vez antes da mutação.
- Alterações de produto: nenhuma.
- Operações no broker/deploy: somente exclusão autorizada da fila principal e GETs de status para acordar/validar os dois serviços; nenhum deploy, restart manual, mudança de plano/tipo, variável ou policy.
- Testes: não executados; esta etapa foi apenas diagnóstico.
- Task: permanece ativa para smoke funcional controlado e gates. O modelo de disponibilidade do consumer gratuito ainda precisa ser aceito/reavaliado pelo usuário.
- Release Verdict: **BLOQUEADO**, pois não há declaração compatível nem prova de consumer/entrega ponta a ponta sob operação estável.

## Operação autorizada e validação posterior — 2026-10-21

### Preflight e ação

- Autorização recebida para excluir somente `notification.guild-event-created` no vhost observado.
- Preflight imediatamente antes: a fila correta tinha `messages=0`, `ready=0`, `unacked=0`, `consumers=0`; overview do broker mostrava `connections=0`, `channels=0`. A lista continha apenas a fila principal.
- A fila principal foi excluída. A lista imediatamente após mostrou zero filas. Nenhuma exchange, policy, outra fila, variável, configuração ou deploy foi alterado.

### Recriação e evidências

- Um GET de status acordou o `notification-service` Free Web Service; logs Render confirmam Spring startup completo e conexão AMQP criada. O broker mostrou novamente a fila principal com `x-dead-letter-exchange=liderum.events.dlx` e `x-dead-letter-routing-key=guild.event.created.dlq`, além da DLQ durável `notification.guild-event-created.dlq` e sua ligação ao exchange DLX usando routing key `guild.event.created.dlq`.
- A fila principal mantém o binding do exchange `liderum.events` com routing key `guild.event.created`. O broker mostrou `consumers=1`, `messages=0`, `ready=0`, `unacked=0`.
- Um GET em `/actuator/health` acordou o backend. Render mostrou Spring/Tomcat iniciado em profile `prod`, Flyway schema V4 atualizado e conexão Rabbit criada. `/actuator/health` respondeu `200` com `status=UP`.
- O broker passou a mostrar duas conexões Java ativas: uma corresponde ao `notification-service` (correlação com seu log de conexão às 14:53:46, hora local) e outra ao backend (correlação com log às 14:58:36). Overview mostrou `connections=2`, `channels=4`, `consumers=1`, `queues=2`.
- Nenhum AMQP 406 foi observado nos logs consultados após a recriação. Não se publicou nem consumiu mensagem nesta validação.

### Limitações e próximos gates

- O consumer é Web Service gratuito e pode suspender por inatividade. Uma conexão ativa neste snapshot não prova disponibilidade contínua. O plano do Render não foi alterado; um Background Worker não tem plano gratuito, conforme documentação oficial.
- O smoke de publicar um evento não foi executado: o consumer pode disparar um canal externo se `DISCORD` estiver habilitado no ambiente, cujo valor não foi lido. Não foi feita chamada de webhook nem criado evento de produto.
- Nenhum código/dependência alterado; não foram reexecutados testes backend nesta etapa operacional. As integrações em broker descartável anteriores não substituem o smoke real pendente.
- Task permanece ativa para smoke com efeito externo controlado (ou prova sem side-effect) e gates QA → Security → SRE/DevOps → Auditor. Task Verdict e Release Verdict não foram aprovados nesta rodada. Release continua **BLOQUEADO** até o fluxo funcional e o requisito de disponibilidade do consumer serem aceitos/comprovados.

## Smoke sintético autorizado — 2026-10-21

- Foi publicada uma única mensagem sintética identificada como `SMOKE TEST - no real event`, com `eventId=999999999999`, sem dados pessoais, sem criar registro de Event no backend e sem credenciais no payload.
- Publicação direta para a fila principal: o Render registrou `Audit log: GuildEventCreated received` para o ID sintético. Isso comprova desserialização e execução do listener/dispatcher no notification-service.
- Roteamento: o console confirmou o binding direct `liderum.events` → fila principal com routing key `guild.event.created`. Foi publicada novamente pelo exchange a mesma mensagem/ID; o console confirmou `Published message to liderum.events`. Os contadores finais mostraram fila principal sem ready/unacked e DLQ sem mensagens. A consulta de logs pelo ID retornou somente a linha AuditLog da primeira entrega; não foi encontrada evidência separada de log para a segunda tentativa, portanto não se afirma uma segunda execução do listener.
- No período consultado não apareceu log `Discord notification sent` nem erro de webhook para o ID sintético. A configuração `NOTIFICATION_CHANNELS_ENABLED` não foi lida; portanto isso não é prova independente do valor configurado nem garante ausência de efeito externo em outros eventos.
- Backend `/actuator/health` havia retornado `200 UP`, mas o backend não publicou este evento. O fluxo backend EventService → RabbitTemplate não foi exercitado; este smoke cobre broker → fila → consumer/dispatcher.
- Estado final observado no broker: fila principal com 1 consumer, `ready=0`, `unacked=0`; DLQ `ready=0`, `unacked=0`. A topologia mantém os argumentos DLX e binding esperados.
- Não foram alterados código, configuração, deploy, variáveis, exchanges, filas ou policies nesta rodada. Não foram executados testes de suíte nem gates de QA/Security/SRE/Auditor. Task continua ativa; Release Verdict permanece **BLOQUEADO** até os gates e decisão de disponibilidade do consumer.

## Continuação — validação e gates — 2026-10-21

### Testes executados agora

- Backend: `backend/.\mvnw.cmd clean verify` — **92 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit code 0**. A execução partiu de `clean`, recompilou 88 fontes de produção e 22 de teste.
- Notification Service: `..\backend\mvnw.cmd clean verify` em `notification-service/` — **4 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit code 0**.
- O `clean verify` do Notification Service não executa `RabbitMqNotificationIntegrationIT`: a classe termina em `IT`, depende de `-Drabbitmq.integration=true` e o POM não configura Failsafe para incluí-la no ciclo `verify`. A integração direcionada ainda não foi executada.
- Tentativa de verificar Docker: `docker version` e `docker info` retornam `permission denied` ao conectar ao named pipe `npipe:////./pipe/dockerDesktopLinuxEngine`. O Engine não está acessível nesta sessão. Não foi criado container nem usado broker existente como substituto de broker isolado.
- Comando de integração que permanece pendente: `..\backend\mvnw.cmd -Drabbitmq.integration=true -Dtest=RabbitMqNotificationIntegrationIT test` em `notification-service/`. Ele usa por padrão o broker local `localhost:5673`; não foi executado porque não existe broker isolado acessível sem Docker. Não foi redirecionado para o broker de produção. Impacto: o listener não foi revalidado em integração automatizada nesta rodada, e o fluxo backend→consumer segue sem cobertura.

### Reavaliação do smoke sintético

- O log Render filtrado pelo ID sintético `999999999999` confirma uma execução do `AuditLogNotificationStrategy` às 18:03:31Z. Às 18:04:33Z, o dispatcher registrou `Skipping duplicate GuildEventCreated eventId=999999999999` para a segunda publicação pelo exchange. Isso confirma roteamento e supressão da repetição enquanto o mesmo processo estava vivo; não comprova idempotência durável após restart.
- Consulta read-only posterior ao CloudAMQP mostrou as duas filas esperadas, ambas com `ready=0`, `unacked=0`, e **0 consumers** na principal. A aba Connections mostrou **0 conexões** no instante da consulta. Isso substitui o snapshot anterior de `consumer=1`; o consumer não estava conectado nessa observação posterior.
- `notification-service` permanece `web_service`, plano `free`, uma instância em Oregon, `not_suspended` no metadado lido; Render não retornou eventos no intervalo consultado. Nenhum evento de suspensão/restart foi confirmado, portanto o motivo dos zero consumers não foi provado.
- A documentação oficial informa que Free Web Services suspendem após 15 minutos sem tráfego HTTP/WebSocket de entrada; conexões AMQP não são esse tráfego. O cold start ocorre no próximo HTTP/WebSocket e leva aproximadamente um minuto. Logo este modelo não comprova consumer contínuo. Fonte: [Render — Deploy for Free](https://render.com/docs/free).

### Fluxo de publicação e canais

- Código confirma `EventServiceImpl.create` resolve a Guild pelo `TenantService`, persiste o Event e chama `GuildEventCreatedPublisher`; o publisher usa `RabbitTemplate.convertAndSend(exchangeName, routingKey, message)`. O teste unitário de `EventServiceImpl` verifica a chamada ao publisher, mas o publisher real e o broker não são atravessados por esse teste.
- O publisher captura `AmqpException`, registra warning e retorna sem propagar erro; `EventService` então continua e devolve o Event criado. Não foram encontrados publisher confirms/returns configurados nos arquivos consultados. Isso é um risco preexistente de sucesso aparente/perda de notificação em falhas de publicação e precisa ser avaliado no teste/gate; nenhuma mudança foi feita.
- `EventAttendanceTenantIsolationIntegrationTest` substitui o publisher por mock. `RabbitMqNotificationIntegrationIT` publica diretamente via RabbitTemplate para exchange/routing key e substitui o dispatcher por mock. Portanto nenhum teste atual comprova o fluxo completo `EventService → publisher real → broker → listener → estratégia de auditoria`.
- O smoke de produção também não foi criado por EventService: publicou payload sintético diretamente na fila e depois pelo exchange. O trecho `EventService → RabbitTemplate` continua sem prova funcional ponta a ponta.
- Evidência efetiva de canais: AuditLog foi executado no smoke. A consulta de logs de 18:00–18:15Z não encontrou `Discord notification sent/skipped`; o Render MCP disponível não oferece leitura segura de valores das variáveis do serviço. Assim, não é possível confirmar se Discord está habilitado nem se o webhook está configurado. Nenhum valor secreto foi consultado ou registrado.
- O payload atual contém `eventId`, `eventName` e `date`. Se Discord estiver habilitado, uma webhook única envia detalhes de Events de todas as Guilds para o mesmo destino; essa exposição condicional precisa de configuração/decisão explícita. Não houve envio Discord identificado para o evento sintético.

### Gates

- **QA: REPROVADO por evidência incompleta** (não foi observada regressão nos testes executados). Faltam o teste isolado com broker real, o percurso de publicação pelo backend e os cenários de retry/DLQ. O `clean verify` normal passa, mas não executa a classe `*IT`.
- **Security: BLOQUEADO.** Nenhum secret foi exposto e o smoke de auditoria foi observado. A configuração efetiva de Discord/webhook não pôde ser confirmada; o risco condicional de enviar nome/data/ID de Events de diferentes Guilds ao mesmo destino externo impede atestar isolamento/canais seguros. Nenhum achado foi introduzido por mudança de código nesta rodada (não houve mudança de produto).
- **SRE/DevOps: ESCALONAR decisão operacional.** Topologia correta foi observada, mas o broker está atualmente sem conexão/consumer. O Web Service free não garante runtime contínuo. Não foram alterados plano, tipo, configuração nem aplicado keepalive.
- **Auditor: Task Verdict BLOQUEADO.** Escopo preservado e nenhuma alteração de produto/configuração; faltam evidências/gates obrigatórios e aceite do modelo de disponibilidade. **Release Verdict BLOQUEADO.**
- `finish-task` **não executada**: os gates não aprovaram e os critérios de release não foram comprovados.

### Próximas condições para retomada

1. Tornar o Docker Engine acessível e executar a integração isolada cobrindo publicação real do `EventService`/`RabbitTemplate` até listener/dispatcher; ampliar/acionar cenários de estratégia AuditLog, duplicidade, falha/retry e DLQ sem webhook externo.
2. No Render, verificar sem compartilhar valores: se `NOTIFICATION_CHANNELS_ENABLED` inclui `discord` e apenas se `DISCORD_WEBHOOK_URL` está configurada. Não enviar o valor do webhook. Se Discord estiver ativo, decidir se destino global é aceitável para dados de Events multi-Guild ou desligá-lo por configuração mediante autorização apropriada.
3. Escolher formalmente: aceitar consumer best-effort no Web Service free (sem disponibilidade contínua) ou autorizar orçamento/serviço continuamente ativo. Não criar serviço pago, keepalive nem alterar configuração automaticamente.
4. Se a integração isolada não bastar e for necessário evento de produção, apresentar procedimento/efeitos/limpeza e aguardar autorização explícita antes de criar Event real.

## Integração isolada RabbitMQ — 2026-10-21

### Acesso Docker

- Dentro do sandbox do Codex, `docker version` e `docker info` falharam com `permission denied while trying to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine`.
- Com aprovação de execução fora do sandbox (`require_escalated`), os mesmos comandos funcionaram: Docker Desktop 4.77.0, Engine 29.5.3, contexto `desktop-linux`, Linux/amd64 sob WSL2. Isso confirma que o bloqueio anterior era o sandbox, não o Engine nem uma permissão global do Windows.
- Não foi alterada configuração/permissão do Windows.

### Execução

- Iniciado um novo container local RabbitMQ `rabbitmq:3.13-management`, exposto somente em `127.0.0.1:5673`, com usuário/senha locais descartáveis. Nenhum container anterior foi reutilizado ou modificado.
- Comando em `notification-service/`: `..\backend\mvnw.cmd -Drabbitmq.integration=true -Dtest=RabbitMqNotificationIntegrationIT test`, com `RABBITMQ_TEST_ADDRESSES` apontando para `localhost:5673` do container isolado.
- Resultado: **1 teste, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit code 0**. O log confirma conexão à instância local `localhost:5673`.
- Após o teste, somente o container criado para esta execução foi parado e removido. Verificação: container não existe; porta `5673` livre. Não houve conexão com RabbitMQ de produção.

### Cobertura efetivamente obtida

- O contexto do Notification Service conectou ao broker novo e declarou exchange, fila principal, DLX, DLQ e bindings previstos.
- A publicação do teste usa `RabbitTemplate` diretamente no exchange `liderum.events` com routing key `guild.event.created`; o listener recebeu/desserializou a mensagem e chamou o dispatcher mockado.
- **Não cobre** `EventService → GuildEventCreatedPublisher real → RabbitTemplate do backend`; não executa a estratégia AuditLog real, Discord, retry, dead-letter após falha, nem prova idempotência persistente após restart. Este teste não basta para afirmar o fluxo completo.
- O broker de teste foi RabbitMQ 3.13, não LavinMQ/CloudAMQP; não prova diferenças específicas da engine de produção.

### Bloqueadores ainda identificados; gates não executados nesta rodada

- Prova do publicador real do backend até o consumer/estratégia AuditLog em ambiente isolado.
- Cenários automatizados de falha/retry/DLQ e idempotência conforme plano.
- Estado efetivo de `NOTIFICATION_CHANNELS_ENABLED` e presença (não valor) de `DISCORD_WEBHOOK_URL` ainda não confirmados via Render; nenhuma credencial foi lida.
- Disponibilidade contínua não comprovada: snapshot anterior CloudAMQP tinha 0 consumers e 0 conexões; modelo Free Web Service permanece sujeito a idle sleep.
- Conforme solicitado, não executei QA, Security ou Auditoria nesta rodada. Task permanece ativa e Release permanece BLOQUEADO.

## Reavaliação da decisão operacional e gates — 2026-10-21

### Política best-effort aprovada

O proprietário aprovou explicitamente notifications best-effort para a fase MVP/portfólio: sem requisito de consumer 24/7, SLA, prazo máximo ou garantia fim a fim; o Web Service Render gratuito pode suspender e processamento atrasar; falha de publicação pode resultar em perda. Não haverá worker pago, keep-alive nem serviço adicional. A ausência de disponibilidade contínua deixa de ser blocker isolado. Esta política não dispensa segurança, testes, observabilidade nem autorização para alterar produção.

Não foi apagado o critério de “sem perda silenciosa” do plan. O publisher captura `AmqpException` e registra warning, mas não propaga; `convertAndSend` sem confirms não comprova aceite do broker. O log `Published GuildEventCreated...` após a chamada também não é prova de confirmação. Essa semântica exige validação QA e eventual ajuste mínimo aprovado antes de afirmar sucesso de publicação.

### Estado dos canais e segurança

- Render MCP confirmou metadados do `notification-service` (`web_service`, plano `free`, Oregon, uma instância, `not_suspended` no snapshot), mas `get_service` não expõe variáveis de ambiente. Não foi possível confirmar `NOTIFICATION_CHANNELS_ENABLED` nem presença do webhook; nenhum segredo foi lido/registrado.
- AuditLog foi observado no smoke sintético. A ausência de log Discord no intervalo não demonstra Discord desabilitado.
- Se Discord estiver ativo e configurado, a webhook compartilhada pode enviar `eventId`, `eventName` e `date` de Events de diferentes Guilds ao mesmo destino. É risco preexistente condicional, não afetado pela política best-effort.

### Gates reavaliados

- **QA: REPROVADO** por evidência incompleta. Integração isolada RabbitMQ 3.13 passou 1/1, 0 failures/errors/skipped, `BUILD SUCCESS`, exit 0, mas publica diretamente no exchange e usa dispatcher mockado. Falta prova do publisher real do backend e testes automatizados de AuditLog/retry/DLQ/falha conforme aplicável. Backend `clean verify`: 92/92; Notification Service `clean verify`: 4/4; ambos `BUILD SUCCESS`, exit 0.
- **Security: BLOQUEADO.** Estado de Discord/webhook permanece desconhecido e o destino atual é global compartilhado; best-effort não mitiga divulgação cross-Guild.
- **SRE/DevOps: APROVADO COM LIMITAÇÕES** para o modelo best-effort explicitamente aprovado. A suspensão pode causar atraso/perda; sem 24/7, SLA ou garantia. A indisponibilidade contínua não é blocker isolado. Nenhuma variável/plano/deploy/serviço foi alterado.
- **Auditor: Task Verdict BLOQUEADO**, aguardando QA e Security. **Release Verdict: BLOQUEADO.** `finish-task` não executada.

### Próxima ação

1. Completar testes isolados do publisher real/fluxo necessário e cenários de retry/DLQ/estratégia, ou documentar a lacuna e pedir decisão se exigir mudança arquitetural relevante.
2. Proprietário confirmar somente `Discord habilitado? SIM/NÃO` e `webhook configurado? SIM/NÃO`, sem compartilhar valores. Discord ativo/configurado requer decisão/correção explícita de isolamento antes de aprovação Security.
3. Reexecutar QA, Security e Auditoria somente após as lacunas serem resolvidas; disponibilidade contínua não precisa ser demonstrada sob a política aceita.

### Confirmação de configuração Discord — 2026-10-21

O proprietário confirmou os estados no Render sem informar valores: Discord **não habilitado** e webhook **configurada**. Não alterei variáveis. Security reavaliou e aprovou a configuração atual, registrando que habilitar Discord no futuro reabre a análise de isolamento, pois a webhook é global e o payload não carrega destino por Guild. QA permanece REPROVADO pelas lacunas de cobertura descritas acima; Auditoria e Release permanecem bloqueados até fechamento do gate QA.

## Retomada — testes QA pendentes e gates finais — 2026-10-21

### Acesso de escrita e escopo

- O sandbox inicialmente permitia escrita somente em `.ai`; a gravação de testes em `backend/` e `notification-service/` foi autorizada por execução elevada específica. Arquivos vazios temporários usados na prova de escrita foram removidos. Nenhuma ACL global, permissão Windows ou configuração de segurança foi alterada.
- Revisados `plan.md`, esta execução e os gates QA/Security/SRE. Os critérios que faltavam eram as fronteiras de publicação/processamento e retry/DLQ. Não foi criada task nova e não houve alteração de produção.
- Nenhum serviço RabbitMQ de produção foi usado. O container RabbitMQ 3.13 foi criado apenas para estes testes, vinculado a `127.0.0.1:5673` com credencial descartável local não versionada; foi parado/removido e não restou listener na porta.

### Testes adicionados/ajustados

- `backend/src/test/java/com/example/Liderum/GuildEventCreatedPublisherTest.java`: `AmqpConnectException` sintética; verifica rota, comportamento best-effort, log da falha e ausência de log de sucesso.
- `backend/src/test/java/com/example/Liderum/EventServiceRabbitMqIntegrationIT.java`: Spring real, H2/Flyway, TenantService, EventService, publisher e RabbitTemplate reais; persiste Event da Guild autenticada e verifica payload recebido da rota do broker isolado.
- `notification-service/src/test/java/com/example/liderumnotification/notifications/RabbitMqNotificationIntegrationIT.java`: removido o dispatcher mockado; listener, dispatcher, AuditLog strategy e broker reais; publicação duplicada confirma supressão em memória.
- `notification-service/src/test/java/com/example/liderumnotification/notifications/RabbitMqRetryAndDlqIntegrationIT.java`: listener, dispatcher, strategy spy, retry advice e topology reais; falha sintética apenas na fronteira da strategy; confirma três tentativas, mensagem na DLQ e ausência de requeue/loop.

### Resultados finais verificáveis

- Em `backend/`: `.\mvnw.cmd '-Dtest=GuildEventCreatedPublisherTest' test` — **1 teste**, 0 failures/errors/skipped, `BUILD SUCCESS`, exit 0.
- Em `backend/`: `.\mvnw.cmd '-Drabbitmq.integration=true' '-Dtest=EventServiceRabbitMqIntegrationIT' test` — **1 teste**, 0 failures/errors/skipped, `BUILD SUCCESS`, exit 0, com `RABBITMQ_TEST_ADDRESSES` limitado ao broker local descartável.
- Em `notification-service/`: `..\backend\mvnw.cmd '-Drabbitmq.integration=true' '-Dtest=RabbitMqNotificationIntegrationIT,RabbitMqRetryAndDlqIntegrationIT' test` — **2 testes**, 0 failures/errors/skipped, `BUILD SUCCESS`, exit 0.
- Backend final, após a última alteração de teste: `.\mvnw.cmd clean verify` — **93 testes**, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`, exit 0.
- Notification Service: `..\backend\mvnw.cmd clean verify` — **4 testes**, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`, exit 0. As classes `*IT` não entram no Surefire `clean verify` e foram executadas separadamente.

### Limitações e gates

- As duas fronteiras são verificadas em integrações locais separadas; não foi criado um teste cross-process único EventService→consumer. RabbitMQ 3.13 foi usado em vez de LavinMQ/CloudAMQP. Não houve smoke de produção nesta rodada.
- Deduplicação continua em memória (`ConcurrentHashMap`), perde histórico em restart; não foram introduzidos outbox, confirms/returns ou garantia de entrega. A falha do publisher é logada; best-effort aceita atraso/perda.
- **QA: APROVADO** para os critérios desta task e modelo best-effort. **Security: APROVADO** para configuração atual (Discord desabilitado; habilitação futura exige nova revisão). **SRE/DevOps: APROVADO COM LIMITAÇÕES**.
- **Auditor: Task Verdict APROVADO**, sem scope creep ou alteração de produção. **Release Verdict permanece BLOQUEADO**: esta rodada não acessou produção, e os testes locais separados não comprovam o fluxo completo no broker real. `finish-task` é elegível após a auditoria.
- `git diff --check`: sem erros. Inspeção de whitespace em arquivos novos também não encontrou trailing whitespace. `git status` confirma apenas os testes desta task, `.ai/state.md`, `.ai/handoff.md` e o diretório da task; nada staged. Nenhum commit ou push foi executado.
