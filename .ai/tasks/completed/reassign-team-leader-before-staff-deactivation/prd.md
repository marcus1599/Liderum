# PRD — reassign-team-leader-before-staff-deactivation

**Status:** aprovado para execução pelo usuário em 2026-10-19. **Prioridade:** P1 do Ciclo 3.

## Problema e objetivo

O offboarding seguro bloqueia com 409 a desativação de quem lidera uma Team, mas não há caminho para transferir explicitamente essa liderança. Permitir que administradores de Team escolham outro User ACTIVE da mesma Guild e confirmem a transferência, preservando a barreira tenant e o status do líder anterior. O fluxo deve tornar visível quem lidera cada Team e permitir que, depois de reassociar todas as Teams, o administrador use separadamente a ação de desativação já existente em `/users`.

## Regras funcionais

1. Somente MARECHAL, GENERAL e MAJOR podem reassociar líder, seguindo o RBAC atual de mutações de Team. CAPITÃO e SOLDADO podem consultar Teams, mas não reassociar; anônimo não acessa.
2. A Team deve pertencer à Guild do ator. O novo líder deve ser User `ACTIVE` da mesma Guild. Não há requisito atual para restringir a liderança por `guildRole`: qualquer role ACTIVE é elegível. A UI não envia `guildId`, role nem status como autoridade.
3. A transferência é explícita, de uma Team por vez; não altera nome, membros, status ou papel de User, nem desativa automaticamente o líder antigo. O `leaderId` é obrigatório e não nulo no contrato de reassociação. Manter o líder padrão = criador e a semântica atual de update de nome. Ao criar Team com `leaderId` explícito, aplicar a mesma elegibilidade ACTIVE/mesma Guild para não deixar uma via de contorno.
4. Quando a operação retorna sucesso, a resposta e a próxima leitura indicam o novo líder por ID e nome. A ação repetida com o mesmo líder pode ser idempotente, sem efeitos adicionais. Falha não pode produzir transferência parcial.
5. O seletor mostra apenas dados mínimos de Users ACTIVE da própria Guild (ID e username), sem email, senha, token ou informação de outra Guild. MAJOR obtém esses candidatos sem receber acesso ao CRUD/listagem administrativa de `/users`.
6. A desativação de ex-líder só deixa de ter o bloqueio de liderança quando ele não lidera mais nenhuma Team. O 409 existente persiste caso ainda lidere outra Team.
7. Revalidar elegibilidade no servidor no momento da gravação, inclusive diante de mudança concorrente de status. Definir ordem de locks compatível com o fluxo de status antes da implementação e demonstrar ausência de atribuição a User inativo sob concorrência; não alterar a política do último MARECHAL.

## Contrato proposto

- `PUT /teams/{id}/leader` com DTO explícito `{ "leaderId": <id> }`; retorna `TeamResponseDTO` incluindo `leaderId` e `leaderName`. O update existente de nome continua separado. Não aceitar `guildId`, `status`, `role` ou campos inesperados como mecanismo de mass assignment.
- `GET /teams/leader-candidates`: lista reduzida `{id, username}` dos Users ACTIVE da Guild atual, autenticada e acessível somente a MARECHAL/GENERAL/MAJOR. Não substituir nem afrouxar `/users`.
- 401 sem sessão; 403 para role sem permissão; 404 seguro para Team ou User fora da Guild/inexistente; 409 para candidato que perdeu elegibilidade por status ou conflito concorrente. Validar o mapping efetivo dos handlers, sem expor detalhes internos. `leaderId` ausente/inválido deve gerar 4xx de validação.

## UX mínima

- Em `/teams`, mostrar líder atual e ação de troca apenas para roles autorizadas, com selector Material de candidatos ACTIVE, confirmação explícita e loading durante consulta e gravação.
- Erro/403/404/409 devem oferecer feedback seguro e útil, sem tratar falha HTTP como lista vazia. Após sucesso, atualizar a Team exibida e confirmar a transferência. Em falha, manter o líder visível anterior e permitir retry.
- Preservar shell, estilo, responsive básico, sem redesign. Controle com label, foco e operação por teclado. Se não houver outro candidato elegível, informar isso e impedir submit.
- O fluxo de desativação permanece em `/users`; pode orientar o administrador a reassociar todas as Teams antes de tentar novamente, sem encadear mutações automaticamente.

## Escopo

Contrato específico e validação backend de reassociação; consulta mínima de candidatos; resposta Team com ID do líder; UI mínima em `/teams`; testes backend HTTP/tenant/RBAC e frontend de contrato/estados; QA, Security e Auditoria.

## Fora de escopo

Mudança de role ou status; desativação automática; edição de User; criação de Team com novo fluxo de escolha na UI (a validação do `leaderId` existente na API está em escopo); reassociação em massa; remover exigência de 409; alteração de JWT/ADR-001; multi-Guild; schema/migration sem evidência; RabbitMQ; e-mail; redesign, design system, nova biblioteca ou store.

## Aceite e segurança

- Admin de Team transfere líder para User ACTIVE da própria Guild; a leitura mostra ID/nome novos; antigo líder só pode ser desativado após deixar de liderar todas as Teams.
- CAPITÃO/SOLDADO recebem 403; alvo Team/User cross-Guild recebe resposta sem enumeração e sem mutação; conta PENDING/DISABLED não é aceita, inclusive sob corrida com desativação.
- MAJOR vê somente candidatos mínimos e não ganha acesso administrativo a `/users`. Payload inesperado não muda Guild/role/status.
- Specs Angular verificam visibilidade RBAC, candidatos, confirmação, loading, sucesso, falhas e atualização sem optimistic write indevido. Testes backend direcionados e suíte completa; suíte Angular e build. QA → Security → Auditor; SRE somente se alguma mudança operacional comprovada surgir.
- Task Verdict independente do Release Verdict, que permanece BLOQUEADO pelo AMQP 406 preexistente e adiado.

## Dependências e decisões

Depende dos contratos de Team, `TenantService`, lifecycle ACTIVE/DISABLED e bloqueio 409 já implementados. O contrato de candidatos reduzidos evita expansão de `/users`. A relação User→Guild e JWT do ADR-001 permanecem. ADR novo não é necessário se o escopo permanecer neste desenho; conflito com a política de status/RBAC exige escalonamento antes de implementar.
