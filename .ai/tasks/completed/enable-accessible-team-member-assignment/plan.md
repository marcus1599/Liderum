# Plan — Atribuição acessível de Member a Team

## Classificação e routing

- Prioridade P1; porte MEDIUM; domínios `planning`, `frontend`, `testing`, `security`, `audit`.
- Agents: Planner → Frontend Dev → QA → Security → Auditor. Backend Dev somente se o contrato real divergir. Arquiteto e SRE/DevOps não selecionados: não há decisão estrutural nem mudança operacional.
- Skills: `create-prd`, `create-plan`, `ux-review` no planejamento; na execução, `ux-review`, `test-frontend`, `security-review`, `audit-task`, `finish-task` somente com Task Verdict APROVADO. Impeccable é apoio manual de UI, não autorização de redesign.
- Gate: QA e Security devem aprovar a task antes da auditoria; Release Verdict global continua separado.

## Evidência e padrões confirmados

- `frontend/src/app/groups/groups.component.html` lista Members disponíveis como `cdkDrag`; `dropToGroup()` é o único caminho visível de adição. A remoção já possui botão.
- `GroupService.addMemberToGroup()` usa `POST /teams/{teamId}/add-member/{memberId}`; `TeamController` restringe a mutação a MARECHAL/GENERAL/MAJOR. Nenhum `guildId` é fornecido pela UI.
- `GroupsComponent` já possui `availableMembers`, `groups`, `saving`, `loading`, `loaded`, `stale`, `canManageDomain()` e `mutate()` para feedback e recarga. `MatSelect` e botões Material já existem na tela; não é necessário novo padrão de estado.
- Specs existentes cobrem drag-and-drop, falha de API e RBAC, mas não há alternativa de adição por teclado.
- O roadmap pós-MVP marca navegação, feedback HTTP, clareza de liderança e RBAC visual como concluídos; a acessibilidade da composição de Team é gap concreto remanescente do Ciclo 3. Responsividade avançada segue adiada.

## Execução e evidências

1. Conferidos estados atuais da lista disponível, filtros, RBAC, mutações e testes de Groups.
2. Incluído controle de seleção Angular Material para Member disponível e Team, com botão de adição; drag-and-drop e remoção permanecem disponíveis.
3. O handler revalida role, estado carregado/não obsoleto, Member disponível e Team válida; seleção é reconciliada em recarga e limpa somente após confirmação HTTP. Erros continuam usando o feedback existente.
4. Adicionados testes de controle/label, RBAC, seleção incompleta/obsoleta/duplicada, sucesso, falha de POST e falha da recarga. Suíte completa: 92/92 em duas execuções consecutivas, exit 0.
5. `npm run build`: sucesso, exit 0. Não foi possível comprovar visualmente a tela com sessão autenticada em desktop e viewport estreito. O detector Impeccable apontou apenas avisos `broken-image` de imagens/classes preexistentes; nenhuma imagem foi adicionada nesta task.
6. QA, Security e Auditor documentados nos arquivos desta task; finish-task executado após Task Verdict aprovado. Release Verdict global permanece BLOQUEADO pelo achado RabbitMQ AMQP 406 preexistente, adiado pelo usuário.

## Arquivos prováveis

- `frontend/src/app/groups/groups.component.ts`
- `frontend/src/app/groups/groups.component.html`
- `frontend/src/app/groups/groups.component.scss` apenas se necessário para integrar o controle ao layout existente
- `frontend/src/app/groups/groups.component.spec.ts`
- Artefatos desta task em `.ai/tasks/active/`; `state.md`, `handoff.md` e roadmap apenas se a execução alterar o estado conhecido.

## Escalonamento e limites

- Se a API não preservar a composição ou um teste demonstrar bypass de RBAC/tenant, parar e envolver Backend/Security; não corrigir backend silenciosamente.
- Se o controle exigir mudança de contrato, biblioteca ou fluxo de mover Member entre Teams, parar e pedir nova decisão de escopo.
- Não usar resultado automatizado como substituto de inspeção visual manual. Sem commit/push nesta etapa de planejamento.

Status: IMPLEMENTADA; Task Verdict APROVADO; Release Verdict BLOQUEADO por achado preexistente não relacionado.
