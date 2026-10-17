# Impeccable no Codex — integração de projeto

- Instalação local: `.agents/skills/impeccable/` (skill reconhecida pelo Codex). Não duplicar seu conteúdo em `.ai/skills/`.
- Uso inicial manual: invocar `$impeccable` no Codex para escolher um comando, ou `$impeccable critique <tela>` / `$impeccable audit <tela>` em uma task de interface aprovada. A skill não substitui `ux-review`, `test-frontend` ou os gates do Liderum.
- Hooks automáticos não foram instalados nesta etapa. Não ativar `.codex/hooks.json` sem decisão posterior; preservar quaisquer hooks preexistentes.
- Preservar Angular standalone, Angular Material, serviços, roteamento, guards, RBAC, tenant server-side e o escopo aprovado. A Impeccable fornece critérios de design, não autorização para alterar produto.
- `PRODUCT.md` e `DESIGN.md` não existiam no momento da instalação. Antes de executar `$impeccable init` ou `$impeccable document`, confirmar com o responsável quais informações de público, voz e identidade visual são decisões aprovadas; descrever padrões visuais observados no código sem inventar marca ou direção nova.

## Dúvidas pendentes para a inicialização manual

- Qual público prioritário e contexto de uso da Guild devem constar em `PRODUCT.md`, além do que já está documentado no roadmap?
- A identidade visual atual deve ser formalizada como direção de design, ou há uma decisão de marca ainda pendente?
- Quais telas existentes servem como referência canônica para `DESIGN.md`? Confirmar no frontend antes de documentar.
