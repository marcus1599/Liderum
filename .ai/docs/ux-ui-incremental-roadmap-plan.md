# Plano incremental de UX/UI — pós-Ciclo 3

Status do roadmap UX/UI incremental: **COMPLETED** em 2026-10-23. Todas as cinco tasks obrigatórias foram concluídas e seus commits confirmados: Users `16bb412d4e57f9d3fa7e84f79ef9613cab9a9c39`; Teams `8aa771a615c9904a534fe96de0baf61ad3d165a2` e registro de conclusão `c7a894ce8d5ae52f4d26b5bb16ec8779d43de8e8`; Dashboard `f63a34adef4fbe1dff5be93ab332e4cf66fd80b1`; Login/Ativação `f2e9f68a9347efa38a2018237bfb01a3b9791bfe`; ritmo visual `24d647a50205a49487c60374ad76172303382833`. A task 6 condicional está `NOT_APPLICABLE`: o inventário não demonstrou repetição de padrões com custo concreto de manutenção. Não há task ativa ou elegível. O Release global continua BLOQUEADO pelo achado RabbitMQ preexistente, independente deste roadmap.

## Base e limites

As seis fases do roadmap SaaS e o gate automatizado do Ciclo 3 estão concluídos. Este plano trata refinamentos identificados na auditoria UX/UI de 2026-10-23. A inspeção visual publicada foi parcial; achados de `/users`, `/teams` e ativação ainda precisam de confirmação visual antes de definir mudanças de layout. A task operacional `validate-production-rabbitmq-event-flow` foi encerrada administrativamente como `CLOSED_WITH_LIMITATIONS`; suas limitações e o Release Verdict bloqueado permanecem registrados, mas ela não bloqueia este backlog.

Preservar Angular standalone, Angular Material, rotas, RBAC, tenant server-side, contratos HTTP e comportamento funcional. Não iniciar redesign, design system, dependência nova ou alterações de backend por inferência visual. Cada task passa pelo Router e pelos gates proporcionais quando for ativada. Impeccable e `ux-review` orientam a experiência; `test-frontend` orienta a validação.

## Sequência recomendada

### 1. `clarify-user-management-actions-and-language` — A / P1 / MEDIUM

**Status: concluída.** Commit local `16bb412d4e57f9d3fa7e84f79ef9613cab9a9c39`; evidências em `tasks/completed/clarify-user-management-actions-and-language/`.

- **Objetivo:** tornar criação e administração de Users compreensíveis, com ação principal e operações por conta distinguíveis.
- **Evidência:** `users.component.html` concentra seleção de role, salvar, regenerar link, mudar status e remover em cada linha; exibe `Role` e estados `PENDING/ACTIVE/DISABLED`. A inspeção visual autenticada de `/users` não foi concluída.
- **Escopo:** inspecionar a tela em desktop e viewport estreita; mapear ações por status e role; propor hierarquia e rótulos em português; ajustar somente apresentação e texto seguros da UI; preservar confirmação, link de ativação e mensagens de erro úteis.
- **Fora:** API, auth, RBAC, regras de ativação, exclusão/desativação e refactor geral do componente.
- **Dependência:** sessão visual com papel MARECHAL ou GENERAL e exemplos de User PENDING, ACTIVE e DISABLED; se indisponíveis, usar fixtures locais e declarar a limitação.
- **Aceite:** todas as ações permitidas continuam encontráveis por teclado e toque; ação proibida não aparece; estado, consequência e feedback de cada operação são claros; formulário e linhas não exigem rolagem horizontal na largura estreita escolhida.
- **Validação:** specs de Users e RBAC visual, navegação por teclado, estados dos três status, ChromeHeadless e build Angular; comparação visual desktop/mobile.
- **Risco:** ocultar uma ação sensível ou alterar a interpretação de role/status. **Agents:** Planner, Frontend Dev, QA, Security consultivo para copy de autorização, Auditor. **Skills:** `ux-review`, Impeccable, `test-frontend`, `audit-task`, `finish-task`.

### 2. `clarify-team-assignment-and-leadership-flow` — A / P2 / SMALL–MEDIUM

**Status: concluída.** Task Verdict APROVADO; commits locais `8aa771a615c9904a534fe96de0baf61ad3d165a2` (implementação) e `c7a894ce8d5ae52f4d26b5bb16ec8779d43de8e8` (registro da conclusão); evidências em `tasks/completed/clarify-team-assignment-and-leadership-flow/`.

- **Objetivo:** tornar a sequência de atribuir Member e trocar líder mais fácil de percorrer.
- **Evidência:** `/teams` oferece filtro, drag-and-drop, atribuição por seletores, criação de grupo e troca de líder. As alternativas acessíveis e o fluxo de líder já existem; não recriá-los.
- **Escopo:** após inspeção visual, ordenar títulos, instruções e controles existentes; explicitar quando se escolhe Member, Team ou líder; manter mensagens de sucesso/erro e estados de espera.
- **Fora:** backend, contrato Team, regras de líder, drag-and-drop novo ou remoção da alternativa por teclado/toque.
- **Dependência:** task 1 apenas para reaproveitar linguagem/ritmo visual, sem dependência funcional; fixtures com Team vazio e preenchido.
- **Aceite:** pessoa consegue concluir atribuição e troca de líder sem depender do gesto de arrastar; controles relevantes têm nomes e foco; confirmação do servidor precede mudança visual de líder; erros preservam contexto.
- **Validação:** specs de Groups, teclado/toque, RBAC, estados de erro, build e revisão visual desktop/mobile.
- **Risco:** regressão de seleção e de estado assíncrono. **Agents:** Planner, Frontend Dev, QA, Auditor; Security se fluxo de autorização for afetado. **Skills:** `ux-review`, Impeccable, `test-frontend`, `audit-task`, `finish-task`.

### 3. `explain-empty-dashboard-analytics` — A / P2 / SMALL

**Status: concluída em 2026-10-23.** QA APROVADO COM LIMITAÇÕES; Task Verdict APROVADO; Angular 111/111 e build SUCCESS. Inspeção autenticada/renderizada em 1280×800 e 390×844 não realizada por ausência de fixture local segura; WCAG completa não reivindicada. Commit local `f63a34adef4fbe1dff5be93ab332e4cf66fd80b1`.

- **Objetivo:** explicar métricas e gráficos quando a Guild ainda não tem dados suficientes.
- **Evidência:** em viewport estreita, KPIs antecedem um gráfico amplo sem dados; o template já distingue loading/erro do carregamento geral. A semântica do agregado deve ser confirmada antes de mudar mensagens.
- **Escopo:** mapear os estados sem eventos, sem presenças e com dados; apresentar explicação ou orientação segura nos painéis afetados; ajustar somente hierarquia/ocupação necessária.
- **Fora:** fórmula de métricas, endpoint, nova visualização e novos dados fictícios.
- **Dependência:** confirmar o contrato dos dados e a condição real de cada gráfico; inspeção visual em duas larguras.
- **Aceite:** ausência de dados não parece erro nem gráfico quebrado; valores zero continuam honestos; com dados, gráficos e filtros existentes funcionam como antes.
- **Validação:** specs de Dashboard para vazio/erro/dados, revisão visual 1280×800 e 390×844 e build.
- **Risco:** mensagem falsa para zero legítimo. **Agents:** Planner, Frontend Dev, QA, Auditor. **Skills:** `ux-review`, Impeccable, `test-frontend`, `audit-task`, `finish-task`.

### 4. `harmonize-login-and-activation-presentation` — B / P2 / MEDIUM

**Status: concluída em 2026-10-23.** QA APROVADO COM LIMITAÇÕES; Task Verdict APROVADO, Release Verdict global bloqueado por RabbitMQ preexistente e não relacionado. Testes 119/119, build exit 0, Impeccable `[]`, inspeção renderizada em quatro viewports sem overflow. Commit local isolado `f2e9f68a9347efa38a2018237bfb01a3b9791bfe`.

- **Objetivo:** dar continuidade visual a login e ativação e reduzir movimento sem propósito.
- **Evidência:** login usa fundo ilustrado, partículas, borda e botão animados; ativação usa apresentação mais simples. A diferença precisa de comparação visual antes do ajuste.
- **Escopo:** preservar identidade escura/violeta e conteúdo real; alinhar superfície, tipografia e espaçamento; avaliar `prefers-reduced-motion` e foco/contraste; ajustar animação apenas onde competir com leitura/ação.
- **Fora:** fluxo de autenticação, token, formulários/validações de backend, novo asset ou redesign das demais páginas.
- **Dependência:** acesso visual a login e ativação com estados seguro/inválido; decisão de desenho após essa comparação. Satisfeita em 2026-10-23 usando fixture local com rotas reais em iframes 1280×800/390×844 e mock loopback que respondeu 401/400 sem encaminhar requests nem registrar corpos. A rejeição de token inválido é posterior ao submit; nenhuma credencial real foi usada.
- **Aceite:** continuidade reconhecível entre telas, formulário legível em móvel, foco perceptível e experiência utilizável com movimento reduzido; respostas e navegação preservadas.
- **Validação:** specs de login/ativação, teclado, movimento reduzido, largura móvel e build.
- **Risco:** regressão de contraste e de posicionamento com teclado virtual. **Agents:** Planner, Frontend Dev, QA, Auditor; Security se copy ou comportamento de token mudar. **Skills:** `ux-review`, Impeccable, `test-frontend`, `audit-task`, `finish-task`.

### 5. `normalize-domain-page-visual-rhythm` — B / P2 / MEDIUM

**Status: concluída em 2026-10-23.** Inventário renderizado das seis superfícies em 1280×800 e 390×844. Divergência encontrada somente no card-raiz/ritmo mobile de Attendance; corrigida por CSS scoped. Angular 119/119, build SUCCESS com avisos preexistentes, Impeccable layout `[]`, QA APROVADO COM LIMITAÇÕES e Auditor Task Verdict APROVADO. Capturas não exportadas; somente estados vazios visualizados; WCAG completa, toque/zoom e estados populados/loading/erro/sucesso não comprovados. Commit `24d647a50205a49487c60374ad76172303382833`, sem push.

- **Objetivo:** alinhar títulos, espaçamentos e superfícies das áreas internas onde a auditoria visual provar divergência.
- **Escopo em duas etapas:** inventário comparativo de Dashboard, Members, Teams, Events, Attendance e Users; depois mudanças pequenas por superfície, reutilizando shell e Material existentes. Se o inventário não provar problema relevante, encerrar sem alteração.
- **Fora:** design system novo, reescrita CSS global, componentes compartilhados especulativos, Settings persistente e mudanças funcionais.
- **Dependência:** tasks 1–4 concluídas ou estabilizadas para evitar retrabalho visual; inspeção desktop/mobile de cada superfície incluída.
- **Aceite:** padrões comparáveis usam hierarquia coerente sem apagar necessidades de cada tela; navegação, estados e responsividade permanecem íntegros.
- **Validação:** comparação visual antes/depois, specs das superfícies tocadas, build e checagem de overflow/foco.
- **Risco:** regressão transversal por seletor CSS amplo. **Agents:** Planner, Frontend Dev, QA, Auditor. **Skills:** `ux-review`, Impeccable, `test-frontend`, `audit-task`, `finish-task`.

### 6. `document-reusable-ui-patterns-if-warranted` — C / P3 / CONDICIONAL

**Status: NOT_APPLICABLE.** A task 5 não demonstrou repetição real de padrões com custo concreto de manutenção, portanto o gatilho definido não ocorreu. Não foi iniciada nem implementada; permanece dispensada nesta execução do roadmap.

- **Gatilho:** somente se a task 5 demonstrar repetição real de tokens/soluções e custo concreto de manutenção.
- **Objetivo:** registrar padrões existentes e, se justificado em plano próprio, consolidar o mínimo compartilhável.
- **Aceite:** inventário cita duplicações com locais e benefício mensurável; documentação corresponde ao código; não cria abstração apenas por estética.
- **Validação:** revisão de CSS/componentes afetados, regressão visual proporcional e build se houver código. **Agents:** Planner, Frontend Dev, QA, Auditor; Arquiteto somente se a abstração for estrutural. **Skills:** `ux-review`, Impeccable, `test-frontend` se houver alteração.

## Ordem e governança

Tasks obrigatórias 1–5 concluídas. A task 6 não é necessária se seu gatilho não ocorrer; foi avaliada como `NOT_APPLICABLE`. O roadmap está `COMPLETED`, não há task em `tasks/active/` e não há próxima task elegível. Em retomadas, reconciliar `.ai/roadmap-execution-state.json` com Git e filesystem; se o estado for concluído, informar que não há pendências e não reativar tasks nem iniciar outro roadmap. Novo roadmap requer autorização explícita do proprietário. O encerramento administrativo RabbitMQ e o bloqueio de Release permanecem independentes e inalterados.
