# PRD — Mensageria confiável e testável

## Problema

A publicação de `GuildEventCreated` é best-effort: falhas AMQP são absorvidas pelo producer, não há política explícita de retry/DLQ/idempotência e a cobertura atual é predominantemente unitária. Isso pode perder notificações sem recuperação ou diagnóstico suficiente.

## Objetivo

Garantir entrega e processamento de notificações de eventos com comportamento de falha previsível, recuperação proporcional e evidência de integração RabbitMQ.

## Escopo

- definir contrato e metadados mínimos de entrega, preservando contexto de Guild;
- implementar política proporcional de retry e dead-letter queue;
- tornar o consumo idempotente para mensagens repetidas;
- preservar o isolamento tenant e a autorização existentes;
- adicionar testes de integração com RabbitMQ descartável e testes de regressão producer/consumer;
- documentar diagnóstico operacional e comportamento de falhas.

## Fora de escopo

Frontend, mudanças RBAC/JWT/tenancy, billing, novos canais de notificação, RabbitMQ gerenciado em produção, CI fullstack, observabilidade ampla e refactors não relacionados.

## Critérios de aceitação

1. Falha transitória de consumo/publicação segue retry limitado e observável.
2. Mensagem não processável termina em DLQ sem loop infinito.
3. Redelivery não duplica efeito de notificação.
4. Guild context permanece presente e não permite cross-tenant.
5. Testes de integração reproduzem sucesso, retry, DLQ e redelivery.
6. Suíte backend existente permanece verde.
