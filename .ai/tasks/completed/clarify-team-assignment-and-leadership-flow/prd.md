# PRD — Clarificar atribuição de membros e troca de liderança

## Objetivo

Facilitar a atribuição de um membro disponível a um grupo e a transferência de liderança em `/teams`, tornando as escolhas e a confirmação mais explícitas.

## Requisitos

- Explicar claramente a sequência membro → grupo → adicionar ao grupo.
- Explicar que trocar líder é uma operação distinta e identificar o grupo afetado e a nova pessoa escolhida.
- Preservar as alternativas atuais por teclado/toque e drag-and-drop.
- Aguardar confirmação do servidor antes de atualizar liderança ou atribuição na tela.
- Preservar loading, sucesso, vazio, erro, stale state e seleção em falha.
- Preservar RBAC, tenant server-side, rotas, serviços e contratos existentes.

## Direção

Direção B: SaaS moderno com identidade RPG discreta, mantendo a superfície escura/violeta e componentes Angular Material já usados. Não introduzir novos assets ou abstrações.

## Fora de escopo

Backend, contratos HTTP, regras de liderança, novo drag-and-drop, remoção da alternativa acessível, operações de grupos não relacionadas, novas dependências ou redesign global.

## Critérios de aceite

- O fluxo por seletores identifica quem será movido e para qual grupo antes de enviar.
- O formulário de liderança identifica grupo atual, líder atual quando conhecido e novo líder.
- A confirmação no cliente só atualiza o estado após resposta positiva do servidor; erros mantêm contexto e dados anteriores.
- Controls têm rótulos/nome acessível, ordem de teclado coerente e funcionam em largura estreita sem overflow.
- Testes, build e detector Impeccable passam; limitações visuais são registradas sem inferir validação autenticada.
