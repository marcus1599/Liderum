# Security Review — safely-deactivate-and-reactivate-staff-users

**Task Security Verdict: APROVADO**

**Release Verdict: BLOQUEADO**

## Revisão

- O ator e a Guild são obtidos server-side através de `TenantService`; o DTO de status contém somente `status`, sem `guildId`, role ou credenciais.
- Endpoint limitado a MARECHAL/GENERAL, com hierarquia e proibição de autoalteração verificadas no service. MAJOR, CAPITÃO e SOLDADO não recebem gerenciamento de status.
- Alvos são carregados tenant-scoped; tentativa cross-Guild não revela existência e não causa mutação.
- Desativação incrementa `credentialVersion`; token anterior continua recusado após reativação. A senha hash é preservada e não retornada, e a pessoa precisa autenticar novamente.
- O último MARECHAL ACTIVE é protegido com serialização por Guild e revalidação do estado. A integração concorrente comprovou que duas operações simultâneas não deixam a Guild sem autoridade ativa.
- User líder de Team recebe 409 genérico e seguro antes de qualquer mudança de status; não há reatribuição implícita nem alteração parcial.
- Nenhum secret/token real foi adicionado; nenhum JWT, senha ou hash é exposto pelo contrato de status.

## Achado preexistente não relacionado

O erro RabbitMQ AMQP 406 na fila de notificações já era conhecido antes desta task e foi explicitamente adiado pelo usuário. Não foi introduzido nem agravado pelas alterações de lifecycle de User. Ele continua bloqueando o Release Verdict global, sem reprovar esta task independente.

## Conclusão

Não foi identificada vulnerabilidade introduzida, agravada ou relacionada que impeça avaliar a alteração. Task Security Verdict APROVADO; Release Verdict permanece BLOQUEADO exclusivamente pelo achado preexistente RabbitMQ registrado no handoff.
