# Plano — `resolve-rabbitmq-amqp-406-queue-topology`

## Situação e prioridade

P0, infraestrutura/mensageria, esforço MEDIUM, risco operacional STRUCTURAL. O Release Verdict global está BLOQUEADO por AMQP 406 `PRECONDITION_FAILED` na fila `notification.guild-event-created`, observado antes e depois do rollout JWT. A entrega de notificações em produção não foi comprovada. Esta task não reabre o Ciclo 3 nem implementa o Ciclo 4 de notificações tenant-aware.

## Evidência disponível e lacuna

- `backend/.../Config/RabbitMQConfig.java` e `notification-service/.../config/RabbitMQConfig.java` declaram a fila durável com `x-dead-letter-exchange=liderum.events.dlx` e `x-dead-letter-routing-key=guild.event.created.dlq`, além de DLQ e bindings correspondentes.
- Ambos usam por padrão o nome `notification.guild-event-created`; propriedades de ambiente podem alterar nomes, routing key e broker. Os dois serviços aceitam `CLOUDAMQP_URL` ou `RABBITMQ_ADDRESSES`.
- A integração do notification-service passou com broker descartável, mas não prova compatibilidade com uma fila persistente já existente em produção.
- A comparação Git confirmou a transição em `5e6779c`: antes desse commit ambos os serviços declaravam a fila durável sem argumentos; nele, ambos passaram a solicitar `x-dead-letter-exchange` e `x-dead-letter-routing-key`. O inventário atual do broker confirma que a fila persistida ainda tem argumentos vazios, fechando a causa raiz.
- Render lista o backend `Liderum` live no commit `8c145419` (deploy de 2026-10-19) e o `notification-service` live no commit `bb8f6192` (deploy de 2026-09-15). Logs do backend em 2026-10-17 confirmam repetidamente `Existing queue 'notification.guild-event-created' declared with other arguments`; não informam os valores `current` e `received`.
- O inventário read-only do broker está registrado abaixo. Nenhuma URI ou credencial foi consultada ou exposta.

### Inventário read-only confirmado no broker em 2026-10-21

- Instância CloudAMQP `liderum`, engine **LavinMQ 2.10.2**, região AWS `ap-northeast-1` (Tokyo), vhost `lcqnaebs`.
- A API de leitura do broker confirma para `notification.guild-event-created`: `durable=true`, `arguments={}`, `effective_arguments=[]`, `consumers=0`, `messages=0`, `ready=0`, `unacked=0`.
- Exchanges `liderum.events` e `liderum.events.dlx` existem como direct/durable. Há um único binding no vhost: exchange `liderum.events` → fila principal, routing key `guild.event.created`.
- A lista contém apenas uma fila; `notification.guild-event-created.dlq` e o binding DLX→DLQ não existem.
- Nenhuma policy regular corresponde à fila. A operator policy `lcqnaebs-max-length` aplica somente `max-length=20000` e `max-length-bytes=1073741824`.
- Overview no instante da consulta: zero conexões, canais e consumers. Os logs Render anteriores mostram o 406 em `RabbitAdmin.initialize` / `declareQueues`.
- A configuração atual do Render confirma `notification-service` como `web_service` no plano gratuito, sem health check path configurado. A lista de eventos de serviço consultada não retornou eventos no período; isso não prova o estado do processo no instante da leitura. O broker sem consumers é fato; a suspensão por inatividade é uma explicação possível, não causa comprovada.
- A documentação oficial do Render informa que Web Services gratuitos podem suspender após 15 minutos sem tráfego HTTP/WebSocket, enquanto Background Workers executam continuamente e não possuem plano gratuito. Portanto, restaurar a topologia não garante consumo contínuo no plano atual.
- Valores de credenciais e URI não foram consultados nem registrados.

### Causa raiz e remediação recomendada

**Causa raiz confirmada:** a fila durável persistente foi criada originalmente sem argumentos. O commit `5e6779c` alterou as declarações de ambos os serviços para exigir argumentos DLX. O broker mantém a fila antiga sem argumentos, e a declaração atual recebe 406 por incompatibilidade. A topologia também ficou incompleta: sem DLQ nem binding de dead-letter.

**Remediação mínima da incompatibilidade — executada:** após autorização operacional explícita e preflight confirmando zero mensagens e zero consumidores/conexões, foi excluída somente `notification.guild-event-created` no vhost `lcqnaebs`. Backend e notification-service recriaram fila, DLQ, argumentos e bindings compatíveis após serem acordados por GETs de status. Não foram alteradas exchanges, policies nem outros recursos.

**Decisão operacional ainda necessária:** definir se o consumer online sob Web Service gratuito, sujeito a suspensão por inatividade, atende ao contexto de demonstração. A opção de worker contínuo no Render não é gratuita. Não converter serviço, alterar plano, adicionar keepalive nem mudar o critério de Release sem autorização. Um consumer estava ativo no snapshot pós-correção, mas isso não prova disponibilidade contínua nem fluxo de mensagem ponta a ponta.

## Objetivo e aceite

Restabelecer declaração compatível e consumo confiável da fila de eventos no ambiente afetado, sem perda silenciosa de mensagens nem mudança indevida do contrato de eventos. O Release Verdict só poderá ser reavaliado após topologia e fluxo ponta a ponta verificados no broker real.

### Aditivo operacional aprovado pelo proprietário — 2026-10-21

Para a fase de demonstração/portfólio, o proprietário aprovou notificações assíncronas **best-effort**: não há requisito de consumer 24/7, SLA, prazo máximo ou entrega fim a fim garantida; o Web Service gratuito pode suspender, causando atraso, e falhas de publicação podem causar perda. Não será adicionado worker pago, keep-alive ou serviço adicional nesta task. Esta decisão resolve a pendência de escolha do modelo operacional, mas não dispensa testes, segurança, observabilidade de falhas ou gates.

Este aditivo registra a decisão, sem apagar o critério original de “sem perda silenciosa”. Ainda é necessário validar o comportamento do publisher e assegurar que logs não representem uma tentativa não confirmada como entrega confirmada. Se isso exigir alterar critério de aceite ou arquitetura, escalar antes de fazê-lo. A disponibilidade do consumer em tempo integral deixa de ser gate de release; os demais gates e as lacunas de fluxo/canais permanecem independentes.

## Routing

- Domínios: planning, architecture, backend, messaging, sre, testing, security, audit.
- Agents: Planner → SRE/DevOps + Backend Developer (diagnóstico); Arquiteto se houver mudança de topologia/contrato ou migração de fila; Backend Developer se código for necessário → QA → Security → SRE/DevOps → Auditor.
- Skills: `create-plan` agora; `test-backend` se código/configuração versionável mudar; `security-review` para acesso ao broker, segredos, payload e perdas; `audit-task` e `finish-task` somente após evidências e vereditos. `create-prd` não é necessário para corrigir compatibilidade operacional sem nova regra de produto; `create-adr` só mediante decisão duradoura e autorização.
- Não selecionado: Frontend Developer — não há alteração de UI planejada.

## Escopo

1. O log 406 e a causa por argumentos incompatíveis foram confirmados. O log não inclui os campos `received`/`current`; a leitura da fila confirma os argumentos atuais e o código define os solicitados.
2. O inventário **somente leitura** da fila, DLQ, exchanges, bindings, policies, contagens, consumidores e vhost está concluído. Não foram lidos secrets; portanto, não se verificou se ambos os serviços usam a mesma URL/vhost.
3. Causa classificada: fila persistente antiga sem argumentos DLX, incompatível com a declaração atual; topologia de DLQ também ausente. A fila e o código estão coerentes somente depois que o broker recriar a topologia.
4. **Concluída em 2026-10-21 após autorização específica:** preflight imediatamente antes confirmou a fila nomeada no vhost correto com zero mensagens totais/ready/unacked, zero consumers e zero conexões. Excluída somente a fila principal. As aplicações recriaram a topologia após chamadas GET de status, sem deploy/restart manual. Não repetir a exclusão.
5. Se houver alteração versionável, manter produtor e consumidor coerentes e adicionar regressão que exercite o cenário causador do 406 em broker descartável, incluindo redeclaração com estado persistente quando aplicável.
6. Validar localmente e depois no ambiente afetado: declaração sem 406, publicação de evento descartável, roteamento, consumo único esperado, retry/DLQ quando pertinente, métricas estáveis e ausência de perda conhecida. Não acionar webhook externo real no smoke. Demonstrar que o consumer está rodando sob o modelo operacional escolhido; uma conexão momentânea não prova disponibilidade contínua.

## Fora de escopo

Frontend, JWT, RBAC, tenancy, Flyway, novo broker, canais de notificação, notifications tenant-aware, mudança de payload, refactor geral de mensageria, limpeza indiscriminada de filas e alterações em outros recursos do Render/CloudAMQP.

## Etapas e gates

1. **Diagnóstico:** SRE/DevOps e Backend apresentam evidência do argumento divergente e inventário do broker. Sem isso, nenhuma correção operacional ou de código deve ser escolhida.
2. **Decisão:** classificar risco de perda e selecionar correção mínima; escalar ao Arquiteto e usuário se houver contrato novo, migração de fila, descarte, custo ou indisponibilidade relevante.
3. **Implementação autorizada:** ajustar somente configuração/código necessário, se necessário. Mudança no broker/deploy constitui etapa operacional separada com revisão prévia.
4. **QA:** executar testes do backend/notification-service pertinentes, integração RabbitMQ real descartável e suítes proporcionais; capturar comando, contagens e exit codes. Não inferir sucesso por ausência de logs.
5. **Security:** validar ausência de credenciais/payloads sensíveis em evidências, vhost/permissões e integridade do evento; distinguir Task Verdict de Release Verdict.
6. **SRE/DevOps:** registrar antes/depois da topologia, deploy/restart se autorizado, profundidade da fila/DLQ, consumer ativo e smoke ponta a ponta. Ter rollback que não restaure declaração incompatível nem perca mensagens.
7. **Auditor:** comparar plano, diff e evidências. `finish-task` apenas com Task Verdict APROVADO. Release Verdict pode voltar a APROVADO somente se fluxo real e ausência de outros bloqueadores forem comprovados.

## Riscos e dependências

- Dependência: acesso read-only ao broker e aos logs/revisões dos serviços; acesso de escrita apenas depois de decisão e autorização específicas.
- Mensagens existentes podem ter valor: não presumir que dados de teste da aplicação tornam a fila descartável.
- Uma fila vazia criada do zero pode passar enquanto a fila persistente antiga continua incompatível; o teste de regressão precisa refletir o estado diagnosticado.
- O publisher captura `AmqpException` e registra falha, sem garantir entrega; health HTTP do backend não prova mensageria.
- Se não houver consumidor de produção implantado ou se notificações forem apenas demonstração técnica, registrar a topologia real e reavaliar com o usuário o critério de release, sem alterar o veredito por suposição.
- O plano `free` de Web Service não é um runtime contínuo para consumer: suspensão por inatividade é documentada e Background Worker não tem plano grátis. Keepalive artificial não é a solução assumida. Confirmar com o usuário se a disponibilidade contínua é requisito e qual custo/modelo operacional aceita antes de alterar o serviço.

## Estado

Correção operacional executada após autorização específica: fila antiga excluída sob preflight vazio; backend e consumer recriaram fila/DLQ/argumentos/bindings, e ambos estabeleceram conexão. Backend iniciou e health respondeu UP; nenhum 406 foi observado após a recriação. Smoke sintético comprovou entrega direta ao listener/dispatcher (AuditLog registrado) e publicação via exchange com fila principal/DLQ sem mensagens pendentes ao final. Não exercitou EventService/RabbitTemplate do backend, nem comprovou uma segunda execução do listener para a publicação via exchange. A task continua ativa para gates e decisão sobre disponibilidade: o consumer permanece em Web Service gratuito sujeito a idle sleep; não converter para worker nem alterar plano sem decisão do usuário. Release permanece BLOQUEADO até os gates, o fluxo funcional aplicável e o critério de disponibilidade serem validados.
