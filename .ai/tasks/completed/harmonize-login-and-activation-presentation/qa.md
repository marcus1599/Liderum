# QA — `harmonize-login-and-activation-presentation`

## Parecer formal — 2026-10-23

**Veredito: APROVADO COM LIMITAÇÕES.**

| Verificação | Evidência | Resultado |
|---|---|---|
| Critérios visuais e responsividade | Inspeção renderizada de Login e Ativação em 1280×800 e 390×844; `scrollWidth` e `clientWidth` coincidem, assim como `scrollHeight` e `clientHeight` | Aprovado, sem overflow |
| Estados observáveis | Componentes reais com mock loopback sem encaminhamento: Login 401, Ativação 400, mismatch e loading | Aprovado; copy e controles observados |
| Contraste dos rótulos | Após ajuste dos tokens Material locais, `mat-label` computa `rgb(216, 208, 226)` sobre superfície escura | Aprovado nesta verificação básica |
| Acessibilidade básica | Campos nomeados; mismatch associado por `aria-describedby` e anunciado via `role="alert"`; ordem de teclado e foco visível registrados; `prefers-reduced-motion` verificado | Aprovado com escopo limitado; não é auditoria WCAG completa |
| Suíte Angular | `npm test -- --watch=false --browsers=ChromeHeadless` | 119/119 SUCCESS |
| Build | `npm run build` | Exit 0; avisos Sass e budgets excedidos são preexistentes em Dashboard/Members |
| Diff e detector visual | `git diff --check`; Impeccable `detect --json` nos templates | Sem erros; `[]` |
| Fronteira funcional | Revisão do diff de autenticação, rotas, payloads, serviços e contratos | Nenhuma alteração encontrada |

Limitações: não houve teste contra backend real, intencionalmente; não foi realizada auditoria WCAG completa. Permanecem fora desta task os defeitos preexistentes documentados de token ausente sem feedback e erro genérico para falhas HTTP de ativação.
