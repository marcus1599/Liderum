# SRE/DevOps — `resolve-rabbitmq-amqp-406-queue-topology`

Data: 2026-10-21 (reavaliado após decisão do proprietário)

## Decisão operacional e estado

- O proprietário aprovou notificações assíncronas **best-effort** para MVP/demonstração: sem consumer 24/7, SLA, prazo máximo ou garantia fim a fim. O serviço gratuito pode suspender; notificações podem atrasar ou se perder. Worker pago, keep-alive e serviço adicional estão fora do escopo.
- Render MCP confirma `notification-service` como Web Service `free`, uma instância em Oregon, `not_suspended` no snapshot consultado, sem health-check path. Isso é apenas um snapshot e não promete disponibilidade contínua.
- A topologia corrigida permanece evidenciada: fila principal com DLX, DLQ e bindings. O smoke anterior provou que o consumer processou payload sintético pelo AuditLog. O broker teve snapshot posterior com zero consumers/conexões; sob a política aprovada isso é uma limitação operacional conhecida, não bloqueador isolado.
- Nenhum plano, tipo, deploy, variável, health check ou keep-alive foi alterado.

## Limitações operacionais registradas

- Suspensão/cold start pode atrasar notificações; o broker só pode reter mensagens que tenham sido efetivamente publicadas nele. A política não garante que o publisher tenha recebido confirmação do broker nem que processamento/efeitos externos terminem.
- A idempotência do dispatcher é mantida em memória (`ConcurrentHashMap`); reinício do processo remove o histórico e não há garantia de supressão durável.
- Condições para reavaliar infraestrutura: exigência de SLA/entrega, impacto de perda/atraso tornar-se inaceitável, crescimento de volume ou necessidade operacional de consumo contínuo. Qualquer worker pago/custo requer nova aprovação.

## Veredito

**SRE/DevOps: APROVADO COM LIMITAÇÕES** para o modelo operacional best-effort aprovado. A ausência de disponibilidade contínua não bloqueia esta task nem release isoladamente. Este veredito não substitui QA/Security e não aprova o Release Verdict global.

## Limitações restantes fora do gate operacional

- A integração isolada Docker foi executada e passou (1/1); ela não comprova o publisher real do backend nem os cenários retry/DLQ.
- A configuração efetiva de Discord/webhook continua indisponível para leitura segura pela integração Render usada. Security deve permanecer bloqueado até o proprietário confirmar apenas os estados (sem compartilhar valores) ou até configuração segura equivalente ser comprovada.
- A confirmação de health HTTP do serviço não demonstra consumo AMQP contínuo, o que é explicitamente não requerido nesta fase.

## Reavaliação após suíte de integração — 2026-10-21

- Os testes adicionais usam RabbitMQ local 3.13 em container descartável e H2, sem dependência de produção ou chamada externa. O container foi removido após a validação.
- Nenhuma alteração de configuração, plano, serviço, deploy, variável, healthcheck ou topologia persistente foi feita nesta continuação.
- As limitações operacionais existentes permanecem: Render Free pode suspender; best-effort aceita atraso/perda e não fornece SLA, publisher confirms, outbox ou dedupe persistente. A deduplicação atual é em memória.
- **SRE/DevOps: APROVADO COM LIMITAÇÕES**, veredito inalterado. Não é necessário teste de disponibilidade contínua conforme a decisão aprovada. Esta validação local não substitui um smoke de produção; Release permanece separado.
