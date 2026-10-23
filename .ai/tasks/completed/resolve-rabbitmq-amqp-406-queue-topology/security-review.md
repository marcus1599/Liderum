# Security Review — `resolve-rabbitmq-amqp-406-queue-topology`

Data: 2026-10-21

## Superfície e evidências

- Nenhum código, configuração, variável ou deploy foi alterado nesta rodada. Nenhum secret foi obtido, impresso ou incluído nos artefatos.
- A mensagem atual contém `eventId`, `eventName` e `date`; o AuditLog do smoke registrou esses campos para um payload explicitamente sintético.
- A API Render disponível não expõe leitura das variáveis efetivas. Logs do intervalo do smoke não mostram `Discord notification sent` ou `skipped`; isso não comprova ausência de webhook nem a configuração efetiva.
- Uma única estratégia Discord configurada usa uma webhook compartilhada; não há `guildId` ou seleção de destino por Guild na mensagem. Se habilitada, Events de diferentes Guilds podem chegar ao mesmo destino externo.

## Achado

- **PREEXISTENTE_RELACIONADO — risco condicional, severidade moderada/alta conforme sensibilidade dos nomes de eventos.** A configuração efetiva de Discord/webhook é desconhecida, e o desenho atual não separa destinos por Guild. Vetor: Event criado em qualquer Guild → exchange → consumer → estratégia Discord habilitada → webhook global. Impacto: divulgação externa de nome/data/ID do Event a pessoas com acesso ao canal Discord compartilhado.
- Não há evidência de que a task tenha introduzido ou agravado o risco; não houve mudança de produto. A ocorrência de AuditLog é comprovada, mas canais restantes não.

## Vereditos

- **Task Security Verdict: BLOQUEADO** até confirmar, sem revelar valores, se Discord está habilitado e se webhook está configurado, ou obter decisão explícita de aceitação do destino global. A configuração impede validar o comportamento seguro do fluxo.
- **Release Verdict: BLOQUEADO.** Não declarar que Discord está desabilitado apenas pela ausência de log no smoke. Não inserir ou compartilhar valor de webhook em relatórios.

## Reavaliação após política best-effort — 2026-10-21

- A política best-effort não altera esta análise de segurança. Ela aceita atraso/perda operacional de notificações, mas não aceita exposição cross-Guild a um destino externo desconhecido.
- O Render MCP read-only confirmou metadados do serviço, mas não retorna variáveis de ambiente. Portanto `NOTIFICATION_CHANNELS_ENABLED` e a presença de `DISCORD_WEBHOOK_URL` continuam sem confirmação; não foram lidos nem expostos valores secretos.
- O payload possui `eventId`, `eventName` e `date`, sem `guildId` ou roteamento por tenant. O risco de divulgação em webhook global compartilhada permanece condicional e preexistente, não causado/agravado pela correção da fila.
- **Task Security Verdict permanece BLOQUEADO.** Próxima evidência suficiente: proprietário confirmar apenas `Discord habilitado? SIM/NÃO` e `webhook configurado? SIM/NÃO`, sem informar valor. Se ambos confirmarem Discord desativado, avaliar conforme o fluxo seguro efetivo; se ativo e webhook configurado, manter bloqueado até decisão/correção explícita de isolamento.
- **Release Verdict: BLOQUEADO** até resolver a condição de exposição, além das lacunas de QA.

## Reavaliação final após testes de integração — 2026-10-21

- As mudanças desta continuação são exclusivamente testes e fixtures sintéticas. Não houve edição de produção, acesso a serviços de produção, chamada Discord ou persistência de credencial local.
- As integrações usam broker RabbitMQ 3.13 descartável vinculado a localhost, e banco H2 descartável no teste backend. A única falha injetada é em memória na estratégia de AuditLog para comprovar retry/DLQ.
- A configuração de produção permanece sem alteração; vale a confirmação anterior do proprietário: Discord não habilitado e webhook configurada. Nenhum valor foi consultado ou registrado.
- Não surgiu achado introduzido ou agravado pela task. O risco latente do canal Discord global permanece se o canal for habilitado no futuro.
- **Task Security Verdict: APROVADO** para o diff test-only e configuração atual. Habilitar Discord exige nova Security Review. O Release Verdict é avaliado separadamente e permanece bloqueado conforme `qa.md`/`audit.md`.

## Confirmação do proprietário — 2026-10-21

- O proprietário confirmou explicitamente, sem compartilhar valores: **Discord não habilitado; webhook configurada** no Render.
- Como a estratégia Discord não está habilitada, a webhook configurada não é usada pelo fluxo atual. A condição necessária para exposição cross-Guild não está presente na configuração atual, conforme confirmação do proprietário.
- O risco latente permanece: se Discord for habilitado sem mudar o destino global/contrato de tenant, eventos de Guilds distintas poderão ser enviados à mesma webhook. Essa alteração de configuração deve exigir nova revisão Security e decisão sobre isolamento/destino.
- **Task Security Verdict: APROVADO para a configuração atual confirmada**, sem exposição de segredo e sem Discord habilitado. Não é uma aprovação permanente para habilitar o canal.
- **Release Verdict: BLOQUEADO** por QA ainda reprovado e auditoria pendente; a confirmação do canal não resolve as lacunas de testes.
