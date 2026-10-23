# QA — `explain-empty-dashboard-analytics`

## Veredito

**APROVADO COM LIMITAÇÕES.** Os cenários funcionais de vazio, eventos sem presença, dados preenchidos com contagem zero e recuperação pelo botão de retry estão cobertos por specs; suíte e build foram executados com sucesso. A implementação permanece restrita à apresentação no Dashboard. A ausência de inspeção visual renderizada e de auditoria automatizada WCAG permanece registrada abaixo.

## Escopo e critérios verificados

- **Sem eventos na seleção:** o donut informa que não há eventos e a taxa apresenta `—`, em vez de transformar ausência de observações em taxa medida. A spec `explains when there are no events and avoids presenting zero observations as a measured rate` confirma esse estado.
- **Eventos sem registros de presença:** os nomes de eventos permanecem no gráfico de linha, com séries zero provenientes das métricas, e o texto distingue eventos selecionados sem registros. A spec `distinguishes events without attendance records from a measured zero rate` confirma essa distinção.
- **Membros e zeros legítimos:** classes e destaques permanecem renderizados com membros, inclusive destaque com zero presenças; a taxa é `null` quando o total de registros de presença é zero. A spec citada acima cobre classes e ranking zero; `keeps populated panels, zero-valued rankings, and filter behavior intact` cobre dados preenchidos, cálculo da taxa, alertas e filtro.
- **Alertas:** sem membros há orientação para adicionar membros. Com membros, sem evento ou sem qualquer registro de presença, o texto diz “Dados insuficientes”; com registros válidos e nenhum alerta retornado, não afirma que não houve faltas, apenas que nenhum alerta foi calculado para os eventos selecionados. A regra de alertas subjacente conta sequências de faltas nos eventos recentes recebidos e foi mantida.
- **Loading/erro/retry:** a spec `does not show dashboard content while metrics are loading or failed` verifica que o conteúdo não aparece durante loading/erro e que uma falha não vira empty state. A nova spec `retries loading metrics after a failed request` confirma a presença do botão, clica nele após falha e verifica nova chamada, recuperação do estado carregado e remoção do botão de retry.
- **Filtro:** valor 999 continua limitado a 50 antes da chamada; o estado preenchido confirma a chamada com 10. Sem alteração à regra/cálculo do filtro.
- **RBAC e tenant:** o diff limita-se ao componente, template, SCSS e spec do Dashboard; não toca em rotas, serviços HTTP, backend, auth ou RBAC. `/dashboard` permanece sob `authGuard` em `app.routes.ts`. O PRD registra os contratos existentes como listas tenant-scoped server-side por Guild, sem `guildId` ou seleção de tenant introduzidos no cliente. A interface não substitui a autorização do backend.
- **Acessibilidade básica:** novos vazios e notas usam `role="status"`; os ícones decorativos dos novos vazios têm `aria-hidden="true"`; o loading e erro existentes continuam como `status` e `alert`. A revisão foi estática, sem auditoria automatizada WCAG/contraste e sem teste por leitor de tela.
- **Responsividade:** SCSS mantém grid de três colunas, dois breakpoints intermediários (1024px e 768px) e uma coluna no mobile; KPIs ficam em duas colunas abaixo de 1024px e o conteúdo reduz padding em larguras pequenas. Não foi possível comprovar o layout renderizado nem ausência de overflow por screenshot.

## Execução e evidências

- `frontend`: `npm test -- --watch=false --browsers=ChromeHeadless` — **111/111 SUCCESS**, Chrome Headless 155.0.0.0, exit code 0, confirmado novamente após a spec de retry. A primeira tentativa dentro do sandbox foi impedida por `spawn EPERM`; as repetições autorizadas fora do sandbox terminaram com sucesso.
- `frontend`: `npm run build` — **SUCCESS**, exit code 0. Warnings não bloqueantes: deprecações Sass (`lighten` e regras aninhadas), aviso de browser mapping desatualizado e budgets SCSS excedidos (incluindo Dashboard). A primeira tentativa no sandbox encontrou `spawn EPERM`; a repetição autorizada fora do sandbox terminou com sucesso.
- `git diff --check` — exit code 0; apenas avisos de conversão LF/CRLF do Git, sem erro de whitespace reportado.
- Revisão de diff, template, componente, spec e `DashboardService`: confirma que as condições de exibição derivam de eventos, contagens de presença e destaques já recebidos; serviço, contrato, fórmulas de agregação, RBAC e tenant não foram alterados.

## Limitações e regressões

- Nenhum fixture visual local seguro foi encontrado. Não foram usados credenciais reais ou dados de produção; não houve inspeção autenticada/renderizada em 1280×800 ou 390×844. Portanto, alinhamento, contraste percebido, truncamento e overflow não têm comprovação visual.
- Não foi executado axe/WCAG nem teste com tecnologia assistiva; a acessibilidade reportada é revisão semântica básica estática, não conformidade formal.
- Não identifiquei regressão de filtro, conteúdo preenchido, RBAC ou isolamento no diff revisado.

## Parecer final

Os critérios funcionais de aceitação são atendidos pelas specs, pelo diff e pelo contrato confirmado no PRD. As limitações acima não impedem este gate conforme o plano da task, que permite seguir quando a fixture visual segura não existir desde que QA registre exatamente a evidência e a lacuna. **QA APROVADO COM LIMITAÇÕES**; não equivale a aprovação visual ou auditoria WCAG completa.
