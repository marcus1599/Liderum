# Plano — `validate-production-rabbitmq-event-flow`

## Situação e prioridade

- **Classificação:** qualidade / infraestrutura (validação operacional).
- **Prioridade:** P0 para reavaliar o bloqueio atual de Release; não reabre nem altera as fases já concluídas do roadmap.
- **Esforço:** SMALL para uma execução controlada; **risco operacional:** MEDIUM, pois o cenário cria e depois remove um Event real no ambiente de produção.
- **Estado:** execução autorizada; preflight read-only renovado em 2026-10-21. Filas principal/DLQ estão vazias, mas a fila principal está sem consumer (0); precondição para POST não atendida. Smoke pausado antes de qualquer mutação.
- **PRD:** não necessário; trata-se de uma validação operacional estreita, sem nova funcionalidade ou regra de produto.
- **ADR:** não necessário; nenhuma decisão arquitetural nova é proposta.

## Contexto e evidência anterior

A task `resolve-rabbitmq-amqp-406-queue-topology` foi concluída com Task Verdict APROVADO. Testes locais isolados validaram o publisher backend/EventService e o listener/dispatcher/AuditLog, além de retry/DLQ e deduplicação em memória. O Release Verdict global permaneceu BLOQUEADO porque essas integrações não atravessaram, em um único cenário, a aplicação e o broker real de produção.

A política operacional aprovada é best-effort: sem SLA, disponibilidade contínua ou garantia de entrega; o serviço gratuito pode suspender. Discord está desabilitado segundo confirmação do proprietário. A deduplicação é apenas em memória. Este smoke não deve transformar essas limitações em requisitos novos.

## Objetivo

Com autorização operacional explícita e prévia, validar **uma única vez** no ambiente implantado o caminho funcional:

`POST /events autenticado → TenantService/EventService → persistência do Event → GuildEventCreatedPublisher/RabbitTemplate → RabbitMQ → consumer/listener → dispatcher → AuditLog`

O objetivo é obter evidência limitada de ponta a ponta no instante do teste, não provar disponibilidade contínua, entrega garantida, publisher confirms, processamento após reinício ou supressão durável de duplicatas.

## Contratos observados no código

- `POST /events` recebe `EventRequestDTO` com `name` obrigatório, `date` (`LocalDateTime`) obrigatório e `description` opcional; não recebe `guildId` nem role. Retorna `EventResponseDTO` com HTTP 200.
- O endpoint exige papel MARECHAL, GENERAL ou MAJOR. O serviço resolve o tenant pelo usuário autenticado/TenantService.
- `GET /events/{id}` permite verificar o Event criado no tenant autenticado; `DELETE /events/{id}` é tenant-scoped e exige papel administrativo de domínio.
- O notification-service expõe `GET /`, que retorna `liderum-notification-service is running`; isso comprova apenas resposta HTTP, não conexão AMQP nem consumo.
- O log do publisher após `RabbitTemplate.convertAndSend` não é confirmação do broker. O sucesso do fluxo exige evidência do AuditLog para o mesmo `eventId`, além da persistência/API.

## Escopo

1. Fazer preflight somente leitura dos serviços/deploys envolvidos, estado de health/status, disponibilidade de consumidor, topologia conhecida e contagens de fila principal/DLQ.
2. Confirmar, sem ler valores secretos, que Discord continua desabilitado e que o smoke não acionará canal externo.
3. Usar uma conta já existente, autenticada e autorizada, em Guild de teste do proprietário.
4. Criar exatamente um Event sintético com nome identificável e sem PII, confirmar sua resposta e persistência no tenant correto.
5. Acompanhar logs do consumer e confirmar AuditLog para o mesmo `eventId`; verificar que a mensagem de teste não terminou na DLQ e que as filas retornaram ao estado de baseline aplicável.
6. Remover somente o Event sintético criado pelo próprio smoke, usando seu ID confirmado, e verificar a remoção.
7. Documentar evidências, limitações e gates. Reavaliar Release em etapa separada, sem promovê-lo automaticamente.

## Fora de escopo e proibições

- Não alterar código, variáveis, topologia, permissões, plano, serviço, health check ou deploy de Render/CloudAMQP.
- Não registrar Guild/User, alterar contas, enviar mensagens diretamente ao exchange/fila, ou usar publicação manual no CloudAMQP; isso contornaria `EventService`.
- Não enviar Discord, e-mail ou outra notificação externa; se o estado do canal não puder ser confirmado como desabilitado, parar antes do POST.
- Não acessar, registrar ou transmitir senha, JWT, Authorization header, webhook, URI de broker ou valores de configuração.
- Não consultar nem modificar dados fora da Guild da conta autorizada. Não apagar filas/mensagens nem executar SQL.
- Não repetir a publicação se o resultado for ambíguo; interromper e reportar em vez de criar Events duplicados.
- Não alegar que um smoke único prova SLA, confiabilidade durável, ausência de perda sob falha ou compatibilidade futura.

## Autorização e precondições obrigatórias

Antes de qualquer operação que altere produção, o proprietário deve autorizar explicitamente, nesta task, **a criação de um Event sintético e sua posterior exclusão**, identificando o serviço/ambiente e aceitando que a publicação assíncrona pode ocorrer mesmo se a resposta HTTP for ambígua. A autorização anterior para operar CloudAMQP não substitui esta autorização específica de mutação pela API.

Parar antes de mutar se qualquer item falhar:

- autorização explícita para o POST e para a exclusão do Event sintético;
- conta existente autenticada, role permitida e Guild de teste confirmada pelo proprietário;
- APIs e revisão/deploy corretos identificados sem expor credenciais;
- Discord confirmado desabilitado (somente estado, nunca valor de webhook);
- API e status do notification-service respondem conforme os endpoints existentes;
- consumer disponível no momento do teste e topologia principal/DLQ coerente;
- nenhuma mensagem pronta/em processamento preexistente que torne ambígua a atribuição do resultado; se houver mensagens, não purgar nem consumi-las, apenas parar e reportar;
- Event com marcador único ainda não existe na Guild;
- exclusão tenant-scoped do Event pode ser executada pela mesma conta.

Se o consumer estiver suspenso/indisponível, não fazer deploy, restart, keep-alive ou alteração de plano. A task é adiada e o Release permanece BLOQUEADO.

## Procedimento controlado

1. Registrar horário UTC, revisão/deploy, status HTTP dos health/status existentes, contagens de fila/DLQ/consumers e o estado não sensível de Discord. Não capturar secrets ou headers de autenticação.
2. Confirmar a identidade da sessão com `GET /users/me` e role/Guild esperadas; não incluir o token em evidências.
3. Escolher marcador irrepetível, por exemplo `SMOKE-RABBIT-<UTC>-<nonce>`, descrição explícita de teste e data futura válida. Usar somente `POST /events`.
4. Registrar status HTTP e `eventId` retornado. Esperado: HTTP 200, resposta válida e Event acessível por `GET /events/{eventId}` na mesma sessão.
5. Acompanhar por janela limitada de até 60 segundos os logs do notification-service, filtrando pelo `eventId`/marcador. Esperado: uma entrada AuditLog do Event correspondente. Não usar log “Published” isoladamente como prova de aceitação pelo broker.
6. Reconsultar, em modo somente leitura, fila principal e DLQ. Esperado: nenhuma mensagem desse smoke pendente ou morta. Mensagens preexistentes não devem ser consumidas, removidas ou atribuídas ao smoke.
7. Excluir somente o `eventId` retornado pelo POST e cuja resposta GET confirma o marcador exato; verificar HTTP 204 e que a leitura subsequente retorna 404. Nunca excluir por busca ampla ou por nome sem conferir o ID.
8. Confirmar que Discord continua desabilitado, registrar o estado final de fila/DLQ e encerrar. A remoção do Event não desfaz o AuditLog já emitido.

## Critérios de parada e falha

Parar sem novas mutações e preservar evidências sanitizadas se ocorrer: 401/403 inesperado; HTTP diferente de 200 no POST; resposta sem ID verificável; evento não persistido/tenant incorreto; AMQP 406; warning de publicação; ausência de AuditLog após 60 segundos; mensagem em DLQ; consumer ausente; estado de canal externo desconhecido/habilitado; ou falha de limpeza do Event.

Não repetir POST, não republicar, não remover mensagem/fila e não aplicar workaround. Se a exclusão falhar, deixar registrado o ID/marker sintético e pedir intervenção autorizada; não ampliar a limpeza. Qualquer acesso cross-tenant, vazamento ou envio a destino externo exige interrupção imediata e Security.

## Roteamento e gates

- **Planner:** manter o smoke restrito e definir evidências e parada segura.
- **Backend Developer (consultivo):** confirmar contrato e semântica dos endpoints; nenhuma mudança de backend prevista.
- **SRE/DevOps:** validar preflight de produção, serviços/deploy, broker e coleta de logs/contagens sem alterações operacionais.
- **Security:** revisar autenticação, tenant, exposição de dados e ausência de destino externo antes/depois.
- **QA:** verificar critérios e evidências; não confundir logs de tentativa com confirmação.
- **Auditor:** revisar plano, autorização, registro sanitizado e separação Task Verdict/Release Verdict após execução.
- **Skills:** `create-plan` nesta etapa; na execução, `security-review` e `audit-task`, além dos procedimentos QA/SRE do projeto. `test-backend` não é aplicável sem alteração de código.

Gates de execução: preflight SRE/DevOps → smoke autorizado → QA → Security → SRE/DevOps → Auditor. `finish-task` somente após Task Verdict APROVADO. Release Verdict continua separado e só pode ser reavaliado após Auditor confirmar a evidência ponta a ponta e verificar todos os bloqueadores globais conhecidos; um smoke aprovado não altera o veredito sozinho.

## Evidências a registrar

- autorização e limites concedidos (sem credenciais);
- ambiente/revisões e horário UTC;
- resultados HTTP sanitizados de `/`, `/users/me`, POST/GET/DELETE;
- `eventId` sintético e resultado de persistência/limpeza;
- evidência do AuditLog para o mesmo ID;
- estado pré/pós de consumers, ready/unacked, DLQ e ausência de 406;
- confirmação apenas do estado “Discord desabilitado”;
- QA, Security, SRE/DevOps, Auditor, Task Verdict e Release Verdict;
- limitações: best-effort, suspensão de serviço, sem publisher confirms/outbox/deduplicação persistente e sem garantia de entrega.

## Estado

Autorizada e em execução controlada. O preflight read-only está parcialmente concluído; nenhum Event foi criado e nenhuma mensagem foi publicada. A execução está pausada até renovar a leitura de consumers/filas no CloudAMQP; health, sessão Liderum e Discord foram confirmados. Até conclusão dos gates e reavaliação separada, Release Verdict permanece BLOQUEADO.
