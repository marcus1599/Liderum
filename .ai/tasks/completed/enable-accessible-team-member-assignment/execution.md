# Execução — Atribuição acessível de Member a Team

## Implementação

- A rota existente `/teams` agora oferece selects Material para Member disponível e Team de destino, com botão “Adicionar ao grupo”. O drag-and-drop continua ativo.
- Atribuição usa `GroupService.addMemberToGroup()` e o endpoint existente. Nenhum contrato, código backend, tenant ou role foi alterado.
- A UI mostra o controle apenas para MARECHAL, GENERAL e MAJOR. O handler também valida autorização, carregamento, ausência de dados obsoletos, associação já existente e seleção válida.
- Seleção inválida é limpa após recarga; seleção válida é limpa somente após sucesso HTTP. Falha de gravação preserva a seleção; falha de recarga após gravação mantém o feedback de sucesso e informa o problema de atualização pelo helper existente.

## Arquivos

- `frontend/src/app/groups/groups.component.ts`
- `frontend/src/app/groups/groups.component.html`
- `frontend/src/app/groups/groups.component.scss`
- `frontend/src/app/groups/groups.component.spec.ts`

## Validação

- `npm test -- --watch=false --browsers=ChromeHeadless`: 92/92, exit 0. Executado duas vezes consecutivas após corrigir uma expectativa inicial do teste que selecionava o primeiro `mat-label` da tela em vez do label novo.
- `npm run build`: sucesso, exit 0.
- `git diff --check`: sem erros; Git exibiu apenas aviso de normalização LF/CRLF nos specs e componente TypeScript.
- Impeccable `detect --json` retornou exit 1 com avisos `broken-image` referentes a imagens de classe já existentes na tela. A alteração não introduziu imagens nem mudou esses caminhos.
- Inspeção visual autenticada desktop/viewport estreito não foi realizada nesta sessão. Os testes de componente verificam presença/ocultação, nomes dos controles, estados e handler; isso não substitui visualização real.

## Escopo e segurança

Somente o frontend da rota Teams e seus testes foram modificados. Sem nova biblioteca ou backend. O tenant continua derivado no servidor; nenhuma autoridade vem de `guildId` ou de role escolhida no cliente. Security e Auditor registram os limites e vereditos em seus arquivos próprios.
