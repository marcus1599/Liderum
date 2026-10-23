# Execução — `explain-empty-dashboard-analytics`

## Escopo implementado

- Dashboard agora distingue, nos painéis de presença, ausência de eventos selecionados e eventos sem registros de presença.
- Taxa de presença sem observações é exibida como `—`; contagens legítimas continuam em zero.
- Painéis de classe, alertas e destaques explicam ausência de membros. Com membros e presenças zero, classe e ranking permanecem visíveis.
- Alertas sem observações suficientes não afirmam ausência de faltas consecutivas; quando existe dado, a mensagem fica limitada aos eventos selecionados.
- Estados assíncronos e filtro existentes foram preservados. Não foram alterados DashboardService, endpoints, RBAC, regras de tenant, cálculos de métricas ou backend.

## Validações

| Verificação | Resultado | Evidência/limitação |
| --- | --- | --- |
| Testes Angular | APROVADO | `npm test -- --watch=false --browsers=ChromeHeadless`: 111/111 em Chrome Headless 155.0.0.0, incluindo recuperação após retry. |
| Build Angular | APROVADO | `npm run build`: SUCCESS. Permanecem warnings Sass de depreciação e budgets SCSS existentes (Dashboard, Members e Login). |
| Comportamento | APROVADO | Specs cobrem loading/erro/retry, ausência de eventos, eventos sem presenças, zero legítimo, membros/dados preenchidos, destaques com zero e filtro. |
| Acessibilidade básica | APROVADO COM LIMITAÇÕES | QA confirmou mensagens com `role="status"`, ícones decorativos `aria-hidden="true"` e contraste pelo token secundário do tema; não houve auditoria WCAG completa ou teste com leitor de tela. |
| Responsividade | Revisão estática | Mantidos breakpoints existentes (1024/768/480 px), grid responsivo e layout de cards; não houve render autenticado para aferir overflow visual. |
| RBAC/tenant | Revisão de escopo | Sem alterações em navegação, guards, services HTTP, endpoints ou filtros de tenant; listas seguem os serviços existentes tenant-scoped. Não é teste de autorização novo. |
| Inspeção renderizada | NÃO REALIZADA | Nenhuma fixture local segura de Dashboard foi encontrada. Não foram usadas credenciais reais nem dados de produção. As condições foram verificadas por specs/renderização Angular de componentes sob métricas mockadas, não por screenshot autenticada em 1280×800/390×844. |
| Detector Impeccable | Executado | Apontou duas bordas coloridas preexistentes nos cards KPI (`dashboard.component.scss`); não relacionadas ao escopo de empty states e não alteradas. |
| QA | APROVADO COM LIMITAÇÕES | Parecer formal em `qa.md`; confirmou specs incluindo clique/recovery de retry e 111/111. Permanecem limitações visuais/WCAG. |
| Auditor | APROVADO | Task Verdict APROVADO em `audit.md`; Release Verdict global BLOQUEADO por achado RabbitMQ preexistente, não relacionado. Auditor tentou reproduzir build/testes, mas `spawn EPERM` do sandbox impediu; utilizou evidência QA atual e registrou essa limitação. |

## Revisão de critérios

- Sem eventos e eventos sem presenças têm mensagens distintas e verdadeiras.
- Sem membros, painéis dependentes explicam a ausência; com membros, dados de classes e rankings zero permanecem.
- A ausência de alertas é descrita apenas para a seleção atual e, sem eventos/registros, como dados insuficientes.
- Loading/erro/retry continuam separados do sucesso vazio; os testes Angular confirmam o contrato atual.
- Gráficos e filtro continuam conectados às mesmas métricas e fluxo existentes quando há dados.

## Limitações

A inspeção visual autenticada em 1280×800 e 390×844 não foi comprovada por ausência de fixture de teste visual local. A validação deste momento é baseada em testes de componente com métricas controladas, build e revisão estática do SCSS responsivo/semântica. Nenhuma alegação de validação visual é feita.
