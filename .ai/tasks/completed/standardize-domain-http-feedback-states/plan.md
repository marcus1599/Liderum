# Plan — standardize-domain-http-feedback-states

## Status, objetivo e routing

- Planejamento concluído em 2026-10-13; PRONTO PARA EXECUÇÃO após autorização do escopo. Nenhuma implementação iniciada.
- MEDIUM / P1 / qualidade e UX funcional, Ciclo 3. Escopo: estados HTTP dos fluxos existentes nas quatro áreas, conforme PRD e inventory.md.
- Domínios: planning, frontend, testing, security (exposição de mensagens), audit.
- Agents/responsabilidades: Planner e Frontend consultivo no planejamento, QA para critérios, Security proporcional a mensagens; execução futura Frontend Developer → QA → Security → Auditor.
- Skills usadas no planejamento: create-prd, create-plan, ux-review; test-frontend para estratégia e security-review para avaliação proporcional. Execução/gates: ux-review, test-frontend, security-review, audit-task e finish-task.
- Não selecionados: Backend executor (contratos apenas consultados), Arquiteto/create-adr (sem decisão estrutural), SRE (sem infraestrutura/deploy).

## Etapas e arquivos previstos

1. Reconfirmar Git e inventário antes de editar; ler PRD, padrões e estado corrente das quatro telas. Inspecionar UI desktop/estreita e registrar evidências reais.
2. Definir o mínimo de estado local para consulta e mutação, usando RxJS/finalize quando adequado. Pequeno helper puro de mensagem por status/operação pode residir em `frontend/src/app/shared/http-feedback.ts` com spec própria; sem serviço global ou framework de estado. Não criar componente genérico apenas para uniformizar quatro parágrafos.
3. Implementar Members e Events: feedback de mutações e exclusão, pending e proteção contra duplo submit, vazio/filtro e limpeza de erro; recarga separada de sucesso da operação. Preservar validações, payloads e datas existentes.
4. Implementar Teams: coordenar teams/members, tratar carga parcial, feedback CRUD e associação/remoção; remover atualização otimista não confirmada. Desabilitar gestos/ações conflitantes durante operação; manter formulários/filtros. Não refatorar subcomponentes não usados por Groups.
5. Implementar Attendance: prontidão dos dados essenciais, estado próprio dos alertas, seleção e respostas fora de ordem, pending de gravação e confirmação/erro/recarga. Preservar regras de faltas e Settings em memória. Não criar novo CRUD.
6. Ajustar somente templates/SCSS necessários para legibilidade, estados acessíveis, labels e viewport estreita. Manter shell, rotas, auth, contratos e RBAC atuais.
7. Implementar testes comportamentais focados; executar suíte/build; registrar resultados atuais em execution.md. QA e Security revisam os critérios do PRD, depois Auditor. Não confundir implementação com aprovação do executor.
8. Se Task Verdict APROVADO, aplicar finish-task e atualizar snapshot/handoff/roadmap proporcionalmente. Não declarar todo Ciclo 3 concluído sem avaliar itens restantes. Não fazer stage, commit ou push nesta execução futura.

Arquivos autorizados prováveis:

- `frontend/src/app/members/members.component.{ts,html,scss,spec.ts}`;
- `frontend/src/app/groups/groups.component.{ts,html,scss,spec.ts}`;
- `frontend/src/app/events/events.component.{ts,html,scss}` e nova `events.component.spec.ts`;
- `frontend/src/app/attendence/attendence.component.{ts,html,scss,spec.ts}` (preservar grafia/path);
- helper puro e spec acima, se usado; specs HTTP auxiliares somente quando necessários para verificar integração real de estado;
- artefatos da task e documentação global estritamente pertinente.

Services/DTOs, interceptors, AuthService, app.routes, shell e backend são referências; não há alteração funcional planejada nesses arquivos. Necessidade imprevista de alteração de contrato/autenticação/arquitetura exige escalonamento.

## Matriz de validação

| Área | Evidência esperada |
| --- | --- |
| Quatro listas | Subject/HttpTestingController controla resposta pendente; DOM mostra loading e não vazio; sucesso com itens/sem itens; erro e retry bem-sucedido limpam erro; sem dados antigos acessíveis após 403. |
| Members/Events | Create/update/delete: sucesso e erro, formulário preservado, pending encerrado, duplo clique não duplica request, cancelamento não apaga dados durante submit, persistência confirmada + reload falho não gera falso erro de gravação. |
| Teams | teams falha; members falha após teams; vazios/filtro; CRUD; add/remove por gesto e botão; falha mantém composição anterior; sucesso com resposta Void e recarga; gesto duplicado bloqueado. |
| Attendance | Respostas essenciais em ordens diferentes; falha de cada dependência; nenhum evento/membro; evento sem presença; alertas falhos não viram lista vazia; troca rápida; criação/atualização; falha restaura radio; sucesso seguido de falha em refresh/alerta. |
| Mensagens | Casos 400/401/403/404/409/0/500; corpo com SQL/token fictício/HTML não exibido; 403 não chama logout; 401 preserva comportamento existente do interceptor. |
| Regressão | Suíte existente de login, ativação, onboarding, Users, rotas/sidebar continua verde; requests não recebem guildId como autoridade; nenhuma alteração de permissão. |
| UX | DOM real e UI desktop/estreita (por exemplo 1280 e 390 px): feedback legível, sem novo overflow, foco/teclado/retry, nomes acessíveis, status/alert anunciáveis. Registrar ambiente/limitações; não alegar validação visual a partir de teste unitário. |

Usar Jasmine/Karma, spies proporcionais nos services e Observables controlados para componentes; HttpTestingController quando necessário para integração de requests. Não fazer requests reais de specs nem substituir assertions úteis por simples criação. Members já importa HttpClientModule: assegurar isolamento HTTP explícito no TestBed. Events ganha spec porque não há arquivo dedicado. Não exigir duplicação de toda a matriz de status em cada tela quando helper cobrir tradução e testes de componente comprovarem integração.

Comandos em `frontend/`, após implementação:

```text
npm test -- --watch=false --browsers=ChromeHeadless
npm run build
```

Capturar total, failures/errors/skipped quando runner fornecer, resultado final e exit code; aguardar término. Repetir somente por evidência de flakiness ou mudança posterior. Executar `git diff --check`, `git status` e `git diff --stat`. Sem suíte backend/MCP obrigatória se esses domínios não forem alterados. Testes antigos não certificam código novo.

## Riscos, gates e limites

- Inventário registra exposição potencial de detalhes nos handlers backend (PREEXISTENTE_RELACIONADO); UI deve usar mensagens locais. Security avalia superfície real e separa Task/Release Verdict, sem absorver correção backend silenciosamente.
- RBAC visual incompleto e BARDO ausente no filtro Teams são preexistentes fora do escopo; recomendar backlog separado. Não expandir permissões nem confiar no cliente.
- Falha de rede após escrita pode ter resultado incerto; não reexecutar automaticamente e orientar recarga.
- Não limpar coleções de sucesso como solução para erro parcial; não exibir sucesso de UI antes de resposta; evitar resposta de seleção antiga sobrescrevendo a atual.
- QA valida comportamento e evidência; Security aprova mensagens/tenant/auth preservados; Auditor verifica PRD, diff e gates. Reprovação mantém task ativa; achado fora de escopo exige registro e decisão.

## Progresso

- [x] Contexto/Git, inventário, PRD e plano.
- [x] Autorização para implementar.
- [x] Implementação e testes atuais (execution.md: 65/65, build exit 0).
- [x] QA e Security (qa.md e security-review.md: APROVADOS).
- [x] Auditor (audit.md: Task Verdict APROVADO); finish-task é o passo final.
