# QA — Validação de UX autenticada em viewports reais

Data: 2026-10-21

## Evidências

- Revisão autenticada das rotas `/dashboard`, `/members`, `/teams`, `/events`, `/attendance`, `/users` e `/account/password` em viewports emulados de 1280×800 e 390×844.
- Navegação interna e item ativo corresponderam à rota; drawer abriu, fechou após navegação e respondeu a Enter/Escape. Tab moveu o foco para o próximo controle.
- Em todas as rotas, `document.documentElement.scrollWidth` permaneceu igual à viewport. A tabela de Members usa rolagem horizontal interna em vez de alargar a página.
- Foram observados estados vazios de grupos, eventos e attendance; loading transitório em Members; formulários sem submissão.
- A inspeção encontrou dois defeitos verificáveis e as menores correções ficaram restritas a SCSS: flex-basis dos campos de Events em layout de coluna e contraste da tipografia/erro de Users.
- `npm test -- --watch=false --browsers=ChromeHeadless`: 92/92 SUCCESS, exit code 0.
- `npm run build`: SUCCESS, exit code 0.
- Detector Impeccable para os SCSS alterados: `[]`, exit code 0.

## Limitações

- Não foram produzidas mutações nem estados de erro/sucesso, 401/403, outra role/Guild ou dispositivo físico. A conta de produção foi usada somente para leitura/navegação.
- O browser observava a implantação existente; as duas correções locais não foram implantadas e, portanto, não há screenshot pós-deploy. A causa CSS foi confirmada no DOM/stylesheet e as alterações passaram build e testes.
- Warnings de browser mapping, Sass e budget de estilos são anteriores e não bloqueiam esta task.

## Veredito QA

**APROVADO.** A matriz planejada foi executada nos dois tamanhos; a navegação, estados vazios e ausência de overflow horizontal foram observados. As correções mínimas estão dentro do escopo e a suíte/build passam. As limitações acima são registradas sem alegar estados ou dispositivo que não foram testados.
