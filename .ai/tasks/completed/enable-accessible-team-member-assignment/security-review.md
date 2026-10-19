# Security Review — Atribuição acessível de Member a Team

## Task Security Verdict: APROVADO

## Release Verdict: BLOQUEADO

### Evidências da task

- O controle é apresentado apenas a MARECHAL, GENERAL e MAJOR, conforme as permissões existentes. O método chamado revalida role e estado antes de chamar o service; backend continua sendo a autoridade final.
- A mutação usa o endpoint existente `POST /teams/{teamId}/add-member/{memberId}`. Nenhum `guildId`, role ou identidade é enviado como autoridade pelo cliente.
- A task não altera autenticação, backend, `TenantService`, contrato, sessão ou persistência. Não foi identificado novo IDOR, privilege escalation ou exposição de dados.

### Achado global

- RabbitMQ AMQP 406 por divergência de argumentos da fila `notification.guild-event-created`: PREEXISTENTE_NAO_RELACIONADO. Continua bloqueando Release e teve investigação adiada pelo usuário; não reprova esta task.

Nenhum achado de segurança foi INTRODUZIDO_PELA_TASK ou AGRAVADO_PELA_TASK.
