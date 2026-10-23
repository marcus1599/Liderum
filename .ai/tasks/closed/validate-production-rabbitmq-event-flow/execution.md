# Execução — `validate-production-rabbitmq-event-flow`

Data: 2026-10-21

## Autorização e limite

- O proprietário autorizou a execução da task ativa. O plano delimita uma criação de Event sintético pela API autenticada e a exclusão do mesmo Event por ID após confirmação; sem registro de Guild/User, publicação manual em broker, deploy ou alteração de configuração.
- Não foram compartilhadas nem consultadas credenciais de login.

## Preflight somente leitura

| Verificação | Evidência | Resultado |
|---|---|---|
| Serviço backend Render | `Liderum`, serviço `srv-d6jm4ivkijhs73ejdf40`, revisão live `8c145419`; região Oregon; uma instância; não suspenso no snapshot | APROVADO |
| Notification-service Render | `notification-service`, serviço `srv-d8s3lhu8bjmc73bnfup0`, revisão live `bb8f6192`; região Oregon; plano Free; uma instância | APROVADO COM LIMITAÇÃO best-effort |
| Topologia antes de acordar o serviço | Fila principal e DLQ existentes; 0 mensagens ready, 0 unacked; 0 consumers no primeiro snapshot | APROVADO para baseline; consumer ainda indisponível |
| Status HTTP do notification-service | Um GET ao endpoint público `/` retornou `liderum-notification-service is running`, após cold start automático do Render | APROVADO; nenhum restart/deploy/configuração manual |
| Health do backend | GET read-only a `/actuator/health` retornou `{"status":"UP","groups":["liveness","readiness"]}` após o cold start | APROVADO; nenhum restart/deploy manual |
| Conexão AMQP | Log de startup registra conexão criada e aplicação iniciada em 2026-10-21 19:40 UTC; valores de conexão foram omitidos deste registro | APROVADO |
| Topologia após startup | Snapshot novo da UI CloudAMQP: fila principal com 1 consumer; principal e DLQ com 0 ready, 0 unacked e 0 total | APROVADO para prosseguir após demais precondições |
| Discord | Proprietário confirmou nesta execução que Discord continua desabilitado; nenhum valor de webhook foi consultado | APROVADO |
| Sessão/role Liderum | Rota `/events` autenticada carregou, lista vazia e formulário de criação visível. O app obtém perfil por `GET /users/me`; o gate visual indica role permitida para criar Events. Nenhum dado de identidade/role foi copiado para o relatório | APROVADO para sessão/role |
| Baseline CloudAMQP atual | Após reautenticação, snapshot de 2026-10-21 22:01 UTC: fila principal e DLQ com 0 ready, 0 unacked e 0 total; overview e fila principal mostram 0 consumers | BLOQUEADO; consumer obrigatório ausente, não publicar |

### Observação operacional

O primeiro GET de status encontrou a página intersticial de cold start do Render. A inicialização ocorreu automaticamente; os logs indicam que o notification-service conectou ao broker e iniciou, e o CloudAMQP passou a mostrar 1 consumer. Isso foi uma única verificação do endpoint já previsto no plano, não um keep-alive, restart ou deploy. Não houve alteração de recursos/configuração Render ou RabbitMQ.

## Operações de escrita

- `POST /events`: NÃO executado.
- `GET /events/{id}`: NÃO executado.
- `DELETE /events/{id}`: NÃO executado.
- Mensagens/eventos publicados: nenhum por esta task.
- Discord ou outro destino externo: nenhum envio.

## Pausa e próxima ação

A execução está pausada antes de qualquer mutação. A reautenticação permitiu obter um snapshot atual, mas ele mostra 0 consumers; embora as filas principal/DLQ estejam vazias, a precondição de consumer ativo não foi satisfeita. Sessão Liderum, autorização, Discord desabilitado e health UP continuam registrados. Não fazer POST enquanto o consumer não estiver ativo em operação normal e uma nova leitura confirmar consumer ativo e filas sem mensagens preexistentes.

## Retomada read-only — 2026-10-21 22:01 UTC

- O proprietário concluiu a autenticação no CloudAMQP; acesso à console e ao LavinMQ Manager foi confirmado sem revelar ou copiar credenciais.
- Snapshot do overview: 1 conexão, 1 canal, **0 consumers**, 2 filas.
- Snapshot da fila `notification.guild-event-created`: 0 consumers, 0 ready, 0 unacknowledged, 0 total.
- Snapshot da fila `notification.guild-event-created.dlq`: 0 consumers, 0 ready, 0 unacknowledged, 0 total.
- O preflight atual **não** satisfaz a condição do plano (consumer ativo). O consumer estava presente em snapshot anterior, mas não está presente agora; essa divergência não foi atribuída a uma causa sem evidência.
- Nenhuma ação operacional foi tomada para acordar/reiniciar/deployar o serviço. Não houve POST, publicação manual, exclusão, alteração de configuração nem contato com Discord.
- Decisão: parar antes da mutação. Não criar o Event enquanto o consumer não estiver visivelmente ativo e a baseline das filas permanecer sem mensagens.
- Próxima ação: quando o consumer estiver ativo por operação normal do serviço, repetir somente a leitura de overview e filas; não manter serviço acordado artificialmente nem repetir/publicar o smoke se a resposta de uma futura tentativa for ambígua.

## Vereditos provisórios

- QA: NÃO EXECUTADO; não houve smoke funcional.
- Security: NÃO EXECUTADO; nenhuma mutação ocorreu. Discord desabilitado foi reconfirmado; tenant/role serão verificados no request único autorizado.
- SRE/DevOps: PREFLIGHT PARCIAL APROVADO; notification-service conectou e health backend está UP. Baseline atual do broker aguarda reautenticação; best-effort e suspensão permanecem limitações.
- Auditoria / Task Verdict: NÃO EXECUTADOS.
- Release Verdict: BLOQUEADO; não houve evidência do fluxo ponta a ponta desta task.
