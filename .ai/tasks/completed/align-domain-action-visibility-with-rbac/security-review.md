# Security Review — Alinhar ações das áreas de domínio ao RBAC

## Task Security Verdict: APROVADO

## Release Verdict: BLOQUEADO

### Revisão da task

- Role é lida do `AuthService.profile$`, cujo perfil vem de `/users/me`; a task não decodifica JWT nem aceita role ou guildId do cliente como fonte de autoridade.
- Handlers de escrita verificam a role antes de chamar os serviços. Ocultar/guardar a UI é defesa de experiência, não substitui o backend; `@PreAuthorize` existente continua intacto.
- Nenhum endpoint, token, dado de outra Guild, segredo, dependência ou backend foi alterado.
- Consultas permanecem disponíveis apenas conforme os endpoints autenticados existentes. Não houve alteração de tenant boundary.

### Achados e origem

- `SecurityConfig` tem matcher `/api/members/**` enquanto o controller é `/members`: achado preexistente, informativo nesta task. Os métodos de escrita continuam com `@PreAuthorize`; não foi demonstrado bypass. A task não altera configuração de segurança.
- RabbitMQ AMQP 406: achado preexistente não relacionado, previamente registrado e adiado pelo usuário; continua bloqueando o veredito global de release.

Não há vulnerabilidade introduzida ou agravada pela task.
