# Ciclo 3 — Plano das tasks restantes de UX operacional

Data da inspeção: 2026-10-21. Status: concluído; as seis tasks deste plano foram executadas sequencialmente e o gate final foi aprovado.

## Base e limite

As seis fases do roadmap SaaS original, o Ciclo 1 e o Ciclo 2 estão concluídos. No Ciclo 3 já foram concluídos o shell autenticado, o feedback HTTP dos domínios, a visibilidade das ações por RBAC, a reassociação de líder, a atribuição acessível de Members a Teams e a inspeção autenticada em 1280×800 e 390×844. O Angular passou com 92/92 testes e build na última task. As correções de Events/mobile e contraste de Users foram versionadas, mas não há evidência pós-deploy dessas correções.

Este plano lista as lacunas restantes **comprovadas no código atual**. Não reabre tasks concluídas nem promete que não aparecerão novos defeitos durante a execução. Não há necessidade demonstrada de backend novo, migration, biblioteca de UI, design system, NgRx ou ADR para as propostas abaixo. `Settings` persistente, responsividade avançada e notificações como produto continuam adiados.

Routing comum: Planner → Frontend Dev → QA → Auditor; Security participa nas tasks que lidam com token de ativação, RBAC ou operação administrativa sensível. `ux-review` fornece os critérios de experiência; `test-frontend` fornece as práticas de teste. `create-prd` é proporcional para as tasks MEDIUM com comportamento funcional; `create-plan` deve detalhar cada task quando ela for ativada. `audit-task` e `finish-task` só são usados após implementação e gates aprovados. O Release Verdict global permanece separado e BLOQUEADO pelo RabbitMQ AMQP 406 preexistente, cuja investigação foi adiada pelo usuário.

## Sequência recomendada

| Ordem | Task proposta | Prioridade / porte | Dependência | Resultado verificável |
| --- | --- | --- | --- | --- |
| 1 | `make-staff-activation-link-handoff-reliable` | P0 / MEDIUM | Fluxo de ativação existente | CONCLUÍDA — fallback manual e cópia fiel. |
| 2 | `fix-attendance-event-selection-placeholder` | P1 / SMALL | Nenhuma | CONCLUÍDA — seleção nula sem `NaN`. |
| 3 | `stabilize-user-management-operational-states` | P1 / MEDIUM | Task 1 | CONCLUÍDA — erro/vazio/retry/ações pendentes. |
| 4 | `make-dashboard-data-states-trustworthy` | P1 / MEDIUM | Nenhuma | CONCLUÍDA — loading/erro/retry/filtro. |
| 5 | `confirm-destructive-domain-actions-consistently` | P2 / MEDIUM | Task 3 | CONCLUÍDA — confirmação de exclusão de Groups. |
| 6 | `validate-cycle-3-operational-flows` | P1 / MEDIUM; gate final | Tasks 1–5 | CONCLUÍDA — 103/103 e build SUCCESS. |

As prioridades não mudam a dependência: a task 6 é executada por último. Só uma task deve estar em `.ai/tasks/active/` por vez. As demais permanecem neste backlog até serem ativadas com PRD/plano proporcionais.

## 1. `make-staff-activation-link-handoff-reliable`

- **Problema comprovado:** `users.component.ts` chama `navigator.clipboard?.writeText(link).catch(() => undefined)` em criação e regeneração e informa “link copiado” mesmo se a API de clipboard não existir ou rejeitar. O administrador pode ficar sem o único link de ativação retornado pela resposta.
- **Objetivo/escopo:** apresentar ao administrador autorizado um resultado fiel da cópia, permitir obter o link sem depender exclusivamente do clipboard e manter o link disponível somente pelo tempo necessário ao repasse. Preservar o contrato atual de criação/regeneração, a regra de token único e o fluxo de ativação.
- **Segurança:** não persistir token/link em `localStorage`, logs ou analytics; não incluir `guildId` como autoridade; não exibir a outro perfil nem manter o link após sair da tela. Revisar se o fallback visual tem exposição proporcional e acessível. Security é gate obrigatório.
- **Aceite/testes:** cópia aprovada e rejeitada; clipboard indisponível; criação e regeneração; feedback e alternativa de cópia manual; nenhum aviso falso; token não persiste após navegação; nenhum dado sensível em log. Testes Jasmine/Karma e build Angular.
- **Routing:** Planner, Frontend Dev, QA, Security, Auditor. Skills `create-prd`, `create-plan`, `ux-review`, `test-frontend`, `security-review`, `audit-task`, `finish-task`.
- **Fora de escopo:** envio de e-mail, recuperação de senha, mudança do token/backend, login automático, refactor de Users.

## 2. `fix-attendance-event-selection-placeholder`

- **Problema comprovado:** a opção placeholder usa `[value]="null"` no `<select>` nativo; o handler aplica `Number(eventId)` a valores não vazios. O valor textual `"null"` resulta em `NaN`, que não satisfaz a condição `selectedEventId === null` usada nos estados da tela.
- **Objetivo/escopo:** tratar a opção inicial/limpeza como `null` real, aceitar somente ID numérico válido e preservar a consulta/edição de presença para um evento selecionado.
- **Aceite/testes:** seleção inicial, evento válido, retorno ao placeholder, evento inválido e troca rápida não deixam `NaN`, tabela ou loading indevidos; não há mutação sem evento válido. Rodar specs direcionados, suíte Angular e build.
- **Routing:** Planner, Frontend Dev, QA, Auditor. Skills `create-plan`, `ux-review`, `test-frontend`, `audit-task`, `finish-task`. PRD dispensável por ser correção SMALL sem regra nova.
- **Fora de escopo:** alterar API/DTO de Attendance, redesenhar tabela ou mudar regra de presença.

## 3. `stabilize-user-management-operational-states`

- **Problema comprovado:** falha de `list()` preenche `errorMessage`, mas o template também mostra “Nenhum usuário encontrado” se `users` estiver vazio. O formulário não mostra mensagens de validação junto aos campos. `updateRole`, `removeUser` e `regenerateActivation` não têm estado de operação por usuário, diferentemente de `changeStatus`.
- **Objetivo/escopo:** distinguir falha de leitura de lista vazia real, oferecer retry, indicar campos inválidos e impedir ações repetidas enquanto a operação daquele usuário está pendente. Preservar a matriz MARECHAL/GENERAL e os contratos existentes.
- **Aceite/testes:** loading → sucesso vazio/dados/erro; erro não vira vazio; retry recupera a lista; campos inválidos têm orientação útil; ações concorrentes sobre a mesma linha ficam bloqueadas e são liberadas em sucesso/falha; 403 e 404 usam mensagens seguras; nenhuma ação aparece para role proibida. Suíte Angular e build.
- **Routing:** Planner, Frontend Dev, QA, Security consultivo para RBAC e dados, Auditor. Skills `create-prd`, `create-plan`, `ux-review`, `test-frontend`, `security-review` se superfície de autorização mudar, `audit-task`, `finish-task`.
- **Fora de escopo:** nova role, novo endpoint, edição de e-mail/username, mudança de senha, persistência de estado global ou lógica do token da task 1.

## 4. `make-dashboard-data-states-trustworthy`

- **Problema comprovado:** `DashboardComponent.loadDashboardData()` assina apenas o caso de sucesso. Valores iniciais zero e o texto “Nenhum destaque” aparecem também enquanto os três requests de métricas estão pendentes ou quando falham. `eventFilterCount` é enviado ao serviço sem validação no handler, embora o input mostre mínimo 1 e máximo 50.
- **Objetivo/escopo:** usar estados explícitos de loading, erro com retry e vazio real; impedir métrica enganosa como `0%` quando não existe denominador; validar o filtro antes da consulta. Reutilizar `DashboardService` e o padrão de feedback já usado nos domínios.
- **Aceite/testes:** requests pendentes não são apresentados como resultado vazio; erro de qualquer request tem feedback seguro e retry; sucesso sem dados é distinto de erro; com dados os gráficos/KPIs continuam corretos; filtro fora de 1–50 não dispara consulta inválida. Suíte Angular e build.
- **Routing:** Planner, Frontend Dev, QA, Auditor. Skills `create-prd` se o contrato de apresentação precisar de decisão, `create-plan`, `ux-review`, `test-frontend`, `audit-task`, `finish-task`.
- **Fora de escopo:** novos indicadores, backend de métricas, biblioteca de gráficos, redesign do Dashboard ou Settings.

## 5. `confirm-destructive-domain-actions-consistently`

- **Problema comprovado:** Users, Members e Events usam `window.confirm`/`confirm`; `GroupsComponent.deleteGroup()` executa exclusão diretamente. Os fluxos destrutivos têm confirmação e interação diferentes.
- **Objetivo/escopo:** exigir confirmação compreensível antes de excluir User, Member, Event ou Team, com identidade do alvo, cancelar sem request e bloquear repetição enquanto salva. Usar Angular Material e o padrão visual existente; criar componente compartilhado apenas se a repetição real compensar. Confirmar o comportamento real do backend para dependências antes de escrever o texto da confirmação.
- **Aceite/testes:** confirmar e cancelar em cada domínio; Team não é excluída por clique acidental; teclado/foco e nome acessível funcionam; erro mantém a tela e informa a falha sem vazar detalhes; RBAC visual permanece. Suíte Angular e build.
- **Routing:** Planner, Frontend Dev, QA, Security consultivo se a apresentação de operações de User mudar, Auditor. Skills `create-prd`, `create-plan`, `ux-review`, `test-frontend`, `security-review` condicional, `audit-task`, `finish-task`.
- **Fora de escopo:** undo, lixeira/soft delete, mudança de cascade ou endpoints, fluxo genérico de diálogos para todo o produto.

## 6. `validate-cycle-3-operational-flows`

- **Lacuna comprovada:** a revisão responsiva recente observou navegação e renderização, mas não executou mutações, cenários 401/403, outra role/Guild ou aparelho físico. As correções visuais ainda não têm observação pós-deploy registrada.
- **Objetivo/escopo:** fechar o Ciclo 3 com matriz de uso real em ambiente seguro e dados descartáveis: criação/ativação de Staff, seleção e registro de presença, feedback de Users/Dashboard, confirmação/cancelamento de exclusões, role de leitura e role administrativa, desktop e viewport estreito. Verificar o commit implantado antes de atribuir achados à versão. Registrar limites sem inventar evidências.
- **Aceite/testes:** roteiro e resultados por fluxo/role/viewport; nenhuma credencial/token incluído em evidência; estados 401/403/404 observados de modo seguro; regressões abrem task separada ou impedem o fechamento; QA e Auditor aprovam o fechamento do Ciclo 3. Testes automatizados existentes são preservados; novos testes só para defeitos comprovados.
- **Routing:** Planner, Frontend Dev consultivo, QA principal, Security para fronteiras RBAC/tenant, Auditor; SRE apenas se a implantação/configuração for necessária. Skills `create-plan`, `ux-review`, `test-frontend` se houver mudança de UI, `security-review` para achados de autorização, `audit-task`, `finish-task`.
- **Fora de escopo:** testes destrutivos em dados reais de produção, ampliar cobertura por meta numérica, E2E/Playwright obrigatório, correção do RabbitMQ ou nova funcionalidade.

## Critério para encerrar o Ciclo 3

As seis tasks propostas estão concluídas ou uma delas foi descartada com justificativa baseada em evidência; os fluxos operacionais críticos podem ser usados sem feedback falso, seleção inválida silenciosa ou exclusão acidental; as roles e fronteiras de Guild continuam respeitadas; Angular e build passam; QA/Auditor registram limitações e vereditos. A conclusão do Ciclo 3 não altera automaticamente o Release Verdict global: o RabbitMQ AMQP 406 continua pendente até tratamento próprio.
