# PRD — Atribuição acessível de Member a Team

Status: implementada e submetida aos gates; prioridade P1, Ciclo 3 — UX operacional.

## Problema e objetivo

Na rota `/teams`, a única forma visível de adicionar um Member disponível a uma Team é arrastá-lo até o card. A remoção já tem botão. O gesto de arrastar dificulta a operação por teclado e em telas de toque; a conclusão do Ciclo 3 exige que a composição de equipes seja operável sem depender desse gesto. A task deve oferecer uma alternativa simples sem remover o drag-and-drop existente.

## Requisitos

1. MARECHAL, GENERAL e MAJOR podem selecionar um Member disponível e uma Team da Guild atual por controles Angular Material já usados no projeto e confirmar a atribuição por botão acessível por teclado/toque.
2. CAPITÃO e SOLDADO podem consultar as Teams e Members permitidos, mas não veem nem acionam o novo controle de mutação. Perfil ainda não carregado também não pode mutar.
3. O fluxo usa `GroupService.addMemberToGroup(groupId, memberId)` e a autorização/tenant server-side existentes. Não solicita `guildId`, não muda contrato HTTP ou regra de negócio.
4. As opções vêm apenas das consultas atuais. Member já atribuído não é apresentado como disponível; Team inexistente ou dados não carregados não podem ser enviados. A seleção deve permanecer compreensível após filtro/paginação e atualização da lista.
5. Pending impede duplicação; sucesso é informado somente após confirmação HTTP e seguido de recarga, como o fluxo atual. Erro mantém contexto para nova decisão do usuário; falha de recarga posterior não é apresentada como falha da gravação nem dispara retry automático de POST.
6. Preservar o drag-and-drop, a remoção existente, transferência de líder, CRUD de Team, estados loading/empty/error, shell e estilo atual. O controle novo deve ter nome acessível, foco visível e mensagem de estado perceptível.
7. Verificar visualmente a rota autenticada em viewport desktop e estreito na execução. Registrar limitações se não houver ambiente/conta de teste; não afirmar validação que não ocorreu.

## Fora de escopo

Backend, migrations, JWT/RBAC/tenant, criação de Member, movimentação direta entre duas Teams, ordenação persistente, redesenho da página, responsividade avançada, design system, biblioteca nova, mudanças em outras áreas, E2E/Playwright e investigação do RabbitMQ AMQP 406 adiada pelo usuário.

## Aceite

- Administrador autorizado atribui Member disponível a Team por teclado e toque/clique sem arrastar, com feedback coerente; arrastar continua funcional.
- Não há envio com seleção incompleta, dados stale/loading, operação pendente, perfil sem permissão ou Member já atribuído.
- Falha HTTP e falha de recarga preservam as distinções existentes; nenhum sucesso falso nem mutação local não confirmada.
- Jasmine/Karma completo e build Angular passam; QA valida fluxo, acessibilidade básica e viewport proporcional; Security revisa RBAC/tenant; Auditor verifica escopo e evidências. Task Verdict e Release Verdict permanecem separados.

## Dependências e riscos

Sem nova dependência, API, ADR ou migration. Riscos: seleção ficar obsoleta após filtro/recarga, duplicar requisição entre botão e drag-and-drop, interpretar erro de recarga como falha da atribuição, ou expor a ação a role de leitura. O backend permanece a fonte de autorização. O bloqueador RabbitMQ preexistente continua no Release Verdict e não bloqueia a validação desta task independente.
