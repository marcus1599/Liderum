# QA — Atribuição acessível de Member a Team

## Verificações

- Testes de componente validam disponibilidade dos controles e label do Member para administradores, ocultação para perfil ausente/SOLDADO, seleção incompleta, estado stale, membro já associado, estado saving, submissão única, sucesso, preservação da seleção após falha HTTP e distinção entre gravação bem-sucedida e recarga falha.
- Os testes preexistentes de drag-and-drop, remoção, feedback de conflito, troca de líder e RBAC continuam na suíte.
- `npm test -- --watch=false --browsers=ChromeHeadless`: 92/92, exit 0 em duas execuções consecutivas.
- `npm run build`: SUCCESS, exit 0.

## UX e acessibilidade

O novo caminho utiliza `mat-select` com `mat-label` e um botão textual, então não depende de drag-and-drop e segue a navegação padrão de teclado do Angular Material. O botão tem área mínima de 48px. A lista usa somente Members e Teams obtidos pelas consultas atuais; `canAssignSelectedMember()` bloqueia estado incompleto, stale e associação inválida. O foco/contraste/layout não foram observados em browser autenticado nos viewports desktop e estreito; essa limitação não é apresentada como teste concluído.

O detector Impeccable reportou avisos `broken-image` associados a imagens de classe já existentes no template. Não são introduzidos pela task e não impedem avaliar o novo fluxo.

## Veredito

**QA: APROVADO** para o comportamento testado e build. A falta de inspeção visual autenticada permanece limitação não bloqueante, explicitamente registrada. Warnings pré-existentes de browser mapping e Sass não foram alterados.
