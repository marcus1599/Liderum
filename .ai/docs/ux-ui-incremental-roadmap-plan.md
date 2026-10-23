# Plano incremental de UX/UI — pós-Ciclo 3

Status: execução sequencial. Tasks concluídas: `clarify-user-management-actions-and-language`, `clarify-team-assignment-and-leadership-flow` (Task Verdict APROVADO; commit local `8aa771a615c9904a534fe96de0baf61ad3d165a2`, sem push). Nenhuma task ativa; próxima elegível: `explain-empty-dashboard-analytics`. Contratos e condições dos estados foram confirmados no serviço Angular e nos endpoints tenant-scoped; iniciar apenas essa task após registrar seu PRD/plano.

## Base e limites

As seis fases do roadmap SaaS e o gate automatizado do Ciclo 3 estão concluídos. Este plano trata refinamentos identificados na auditoria UX/UI de 2026-10-23. A inspeção visual publicada foi parcial; achados de `/users`, `/teams` e ativação ainda precisam de confirmação visual antes de definir mudanças de layout. A task operacional `validate-production-rabbitmq-event-flow` foi encerrada administrativamente como `CLOSED_WITH_LIMITATIONS`; suas limitações e o Release Verdict bloqueado permanecem registrados, mas ela não bloqueia este backlog.

Preservar Angular standalone, Angular Material, rotas, RBAC, tenant server-side, contratos HTTP e comportamento funcional. Não iniciar redesign, design system, dependência nova ou alterações de backend por inferência visual. Cada task passa pelo Router e pelos gates proporcionais quando for ativada. Impeccable e `ux-review` orientam a experiência; `test-frontend` orienta a validação.

## Sequência recomendada

### 1. `clarify-user-management-actions-and-language` — A / P1 / MEDIUM

- **Objetivo:** tornar criação e administração de Users compreensíveis, com ação principal e operações por conta distinguíveis.
- **Evidência:** `users.component.html` concentra seleção de role, salvar, regenerar link, mudar status e remover em cada linha; exibe `Role` e estados `PENDING/ACTIVE/DISABLED`. A inspeção visual autenticada de `/users` não foi concluída.
- **Escopo:** inspecionar a tela em desktop e viewport estreita; mapear ações por status e role; propor hierarquia e rótulos em português; ajustar somente apresentação e texto seguros da UI; preservar confirmação, link de ativação e mensagens de erro úteis.
- **Fora:** API, auth, RBAC, regras de ativação, exclusão/desativação e refactor geral do componente.
- **Dependência:** sessão visual com papel MARECHAL ou GENERAL e exemplos de User PENDING, ACTIVE e DISABLED; se indisponíveis, usar fixtures locais e declarar a limitação.
- **Aceite:** todas as ações permitidas continuam encontráveis por teclado e toque; ação proibida não aparece; estado, consequência e feedback de cada operação são claros; formulário e linhas não exigem rolagem horizontal na largura estreita escolhida.
- **Validação:** specs de Users e RBAC visual, navegação por teclado, estados dos três status, ChromeHeadless e build Angular; comparação visual desktop/mobile.
- **Risco:** ocultar uma ação sensível ou alterar a interpretação de role/status. **Agents:** Planner, Frontend Dev, QA, Security consultivo para copy de autorização, Auditor. **Skills:** `ux-review`, Impeccable, `test-frontend`, `audit-task`, `finish-task`.

### 2. `clarify-team-assignment-and-leadership-flow` — A / P2 / SMALL–MEDIUM

- **Objetivo:** tornar a sequência de atribuir Member e trocar líder mais fácil de percorrer.
- **Evidência:** `/teams` oferece filtro, drag-and-drop, atribuição por seletores, criação de grupo e troca de líder. As alternativas acessíveis e o fluxo de líder já existem; não recriá-los.
- **Escopo:** após inspeção visual, ordenar títulos, instruções e controles existentes; explicitar quando se escolhe Member, Team ou líder; manter mensagens de sucesso/erro e estados de espera.
- **Fora:** backend, contrato Team, regras de líder, drag-and-drop novo ou remoção da alternativa por teclado/toque.
- **Dependência:** task 1 apenas para reaproveitar linguagem/ritmo visual, sem dependência funcional; fixtures com Team vazio e preenchido.
- **Aceite:** pessoa consegue concluir atribuição e troca de líder sem depender do gesto de arrastar; controles relevantes têm nomes e foco; confirmação do servidor precede mudança visual de líder; erros preservam contexto.
- **Validação:** specs de Groups, teclado/toque, RBAC, estados de erro, build e revisão visual desktop/mobile.
- **Risco:** regressão de seleção e de estado assíncrono. **Agents:** Planner, Frontend Dev, QA, Auditor; Security se fluxo de autorização for afetado. **Skills:** `ux-review`, Impeccable, `test-frontend`, `audit-task`, `finish-task`.

### 3. `explain-empty-dashboard-analytics` — A / P2 / SMALL

- **Objetivo:** explicar métricas e gráficos quando a Guild ainda não tem dados suficientes.
- **Evidência:** em viewport estreita, KPIs antecedem um gráfico amplo sem dados; o template já distingue loading/erro do carregamento geral. A semântica do agregado deve ser confirmada antes de mudar mensagens.
- **Escopo:** mapear os estados sem eventos, sem presenças e com dados; apresentar explicação ou orientação segura nos painéis afetados; ajustar somente hierarquia/ocupação necessária.
- **Fora:** fórmula de métricas, endpoint, nova visualização e novos dados fictícios.
- **Dependência:** confirmar o contrato dos dados e a condição real de cada gráfico; inspeção visual em duas larguras.
- **Aceite:** ausência de dados não parece erro nem gráfico quebrado; valores zero continuam honestos; com dados, gráficos e filtros existentes funcionam como antes.
- **Validação:** specs de Dashboard para vazio/erro/dados, revisão visual 1280×800 e 390×844 e build.
- **Risco:** mensagem falsa para zero legítimo. **Agents:** Planner, Frontend Dev, QA, Auditor. **Skills:** `ux-review`, Impeccable, `test-frontend`, `audit-task`, `finish-task`.

### 4. `harmonize-login-and-activation-presentation` — B / P2 / MEDIUM

- **Objetivo:** dar continuidade visual a login e ativação e reduzir movimento sem propósito.
- **Evidência:** login usa fundo ilustrado, partículas, borda e botão animados; ativação usa apresentação mais simples. A diferença precisa de comparação visual antes do ajuste.
- **Escopo:** preservar identidade escura/violeta e conteúdo real; alinhar superfície, tipografia e espaçamento; avaliar `prefers-reduced-motion` e foco/contraste; ajustar animação apenas onde competir com leitura/ação.
- **Fora:** fluxo de autenticação, token, formulários/validações de backend, novo asset ou redesign das demais páginas.
- **Dependência:** acesso visual a login e ativação com estados seguro/inválido; decisão de desenho após essa comparação.
- **Aceite:** continuidade reconhecível entre telas, formulário legível em móvel, foco perceptível e experiência utilizável com movimento reduzido; respostas e navegação preservadas.
- **Validação:** specs de login/ativação, teclado, movimento reduzido, largura móvel e build.
- **Risco:** regressão de contraste e de posicionamento com teclado virtual. **Agents:** Planner, Frontend Dev, QA, Auditor; Security se copy ou comportamento de token mudar. **Skills:** `ux-review`, Impeccable, `test-frontend`, `audit-task`, `finish-task`.

### 5. `normalize-domain-page-visual-rhythm` — B / P2 / MEDIUM

- **Objetivo:** alinhar títulos, espaçamentos e superfícies das áreas internas onde a auditoria visual provar divergência.
- **Escopo em duas etapas:** inventário comparativo de Dashboard, Members, Teams, Events, Attendance e Users; depois mudanças pequenas por superfície, reutilizando shell e Material existentes. Se o inventário não provar problema relevante, encerrar sem alteração.
- **Fora:** design system novo, reescrita CSS global, componentes compartilhados especulativos, Settings persistente e mudanças funcionais.
- **Dependência:** tasks 1–4 concluídas ou estabilizadas para evitar retrabalho visual; inspeção desktop/mobile de cada superfície incluída.
- **Aceite:** padrões comparáveis usam hierarquia coerente sem apagar necessidades de cada tela; navegação, estados e responsividade permanecem íntegros.
- **Validação:** comparação visual antes/depois, specs das superfícies tocadas, build e checagem de overflow/foco.
- **Risco:** regressão transversal por seletor CSS amplo. **Agents:** Planner, Frontend Dev, QA, Auditor. **Skills:** `ux-review`, Impeccable, `test-frontend`, `audit-task`, `finish-task`.

### 6. `document-reusable-ui-patterns-if-warranted` — C / P3 / CONDICIONAL

- **Gatilho:** somente se a task 5 demonstrar repetição real de tokens/soluções e custo concreto de manutenção.
- **Objetivo:** registrar padrões existentes e, se justificado em plano próprio, consolidar o mínimo compartilhável.
- **Aceite:** inventário cita duplicações com locais e benefício mensurável; documentação corresponde ao código; não cria abstração apenas por estética.
- **Validação:** revisão de CSS/componentes afetados, regressão visual proporcional e build se houver código. **Agents:** Planner, Frontend Dev, QA, Auditor; Arquiteto somente se a abstração for estrutural. **Skills:** `ux-review`, Impeccable, `test-frontend` se houver alteração.

## Ordem e governança

As tasks 1–4 podem ser planejadas individualmente; executar 1 antes de 5 para estabelecer o padrão administrativo, e 2–3 em sequência conforme disponibilidade de fixtures. A task 5 depende da evidência visual acumulada. A task 6 não é necessária para considerar este roadmap incremental concluído se o gatilho não ocorrer. Não ativar todas simultaneamente: mantenha somente uma task UX/UI em `tasks/active/`. A primeira ação para este backlog é planejar/ativar apenas a task 1, preservando o encerramento administrativo da iniciativa RabbitMQ sem atribuir-lhe um veredito novo.
