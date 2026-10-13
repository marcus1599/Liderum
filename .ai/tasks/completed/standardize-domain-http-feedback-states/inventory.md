# Inventário — estados HTTP do domínio

Inspeção estática em 2026-10-13, base Git `0467b9d`. Working tree inicialmente limpo. Nenhuma suíte ou validação visual executada nesta etapa de planejamento; os 40 testes verdes/build anteriores são evidência da task de navegação, não desta task.

## Telas e evidências

| Rota / componente | Estado encontrado | Correção delimitada |
| --- | --- | --- |
| `/members` / `members.component.*` | Listagem tem loading, erro 403/404 e vazio condicionado; create/update/delete só tratam sucesso. Submit não bloqueia requisição duplicada. Delete não confirma sucesso visualmente. Filtro sem estado próprio de resultado vazio. | Separar consulta e mutação; erro/sucesso em cada ação; preservar formulário em falha; distinguir lista vazia de filtro sem resultado. |
| `/teams` / `groups.component.*` | GET teams seguido de members sem callbacks de erro ou loading. CRUD e associação/remoção sem feedback. `dropToGroup` faz push e remove disponível antes de confirmar POST, sem rollback. | Estado das duas consultas, falha parcial explícita, bloqueio temporário das ações dependentes; associação visual confirmada pelo servidor; feedback de CRUD/add/remove. |
| `/events` / `events.component.*` | Loading e erro de listagem existem; erro não é limpo no início de nova consulta. Template tem segundo vazio incondicional por length, inclusive durante loading/erro. Mutações só tratam sucesso. | Estados mutuamente coerentes, limpeza de erro ao recuperar, feedback e pending de mutações; manter conversão datetime-local atual. |
| `/attendance` / `attendence.component.*` | Members, events, attendances e alertas consultados separadamente. Apenas attendances encerra loading no sucesso; falha de qualquer consulta encerra loading mesmo com outras pendentes. Alertas não tratam erro e exibem “Nenhum membro em risco” antes de comprovar resposta. Troca de evento e gravações possuem subscriptions sem tratamento de erro. | Coordenar dependências essenciais, estado separado dos alertas, seleção sem resposta obsoleta, feedback por gravação e restauração do radio em falha. |

## Contratos existentes (preservar)

- Members: GET/POST `/members`, PUT/DELETE `/members/{id}`; listas/objeto Member e DELETE sem corpo.
- Teams: GET/POST `/teams`, PUT/DELETE `/teams/{id}`; create/update usam `{name, leaderId?}`, resposta de Team; POST `/{id}/add-member/{memberId}` e DELETE `/{id}/remove-member/{memberId}` retornam corpo vazio. Esta task não cria seleção de líder.
- Events: GET/POST `/events`, PUT/DELETE `/events/{id}`; `{name,date,description?}`, resposta Event; DELETE sem corpo.
- Attendance: GET/POST `/attendances`, PUT `/{id}`; `{memberId,eventId,status}`, resposta Attendance; GET `/attendances/consecutive-absences?threshold=...` retorna IDs. Não adicionar interface de exclusão inexistente.
- Controllers atuais restringem mutações desses domínios a MARECHAL/GENERAL/MAJOR; leitura continua autenticada. Backend é autoridade. Não inferir permissão a partir de erro ou de Guild enviada pelo cliente.

## Padrões reutilizáveis

- Angular standalone, FormsModule, Material cards/inputs/buttons, RxJS e estado local; rotas sob AuthenticatedLayout e authGuard.
- Members/Events já usam texto inline com `role=alert/status`; Users separa isLoading/isSaving e usa mensagens locais por status. Reutilizar linguagem visual escura/lilás e SCSS local; breakpoints existentes nas quatro áreas.
- AuthInterceptor trata 401 protegido com `expireSession`; LoadingInterceptor contabiliza requests globais. Preservar ambos: loading global não substitui prontidão de dados por tela.
- Specs de Members/Groups/Attendance inspecionados só comprovam criação; Events não tem spec própria. Acrescentar testes comportamentais dos estados, sem campanha geral de cobertura.

## Achados e limites

- `ApiExceptionHandler` monta 409 com `ex.getRootCause().getMessage()` e 500 com `ex.getMessage()`; `GlobalExceptionHandler` também inclui mensagem de exceção. Há formatos de validação diferentes em três advices. Evidência estática confirmada; resposta efetiva de cada handler e conteúdo sensível em produção não reproduzidos aqui. Origem PREEXISTENTE_RELACIONADO; risco potencial de divulgação se a UI passar a renderizar corpo bruto. Mitigação desta task: mensagens locais por operação/status, sem corpo bruto; recomendar análise backend separada. Não afirmar exploração nem novo bloqueio global sem evidência proporcional.
- Controles administrativos de domínio aparecem sem checagem de role nos componentes inspecionados. PREEXISTENTE_NAO_RELACIONADO à padronização HTTP; backend contém PreAuthorize. Registrar candidato separado para alinhamento visual, sem concluir bypass de autorização. Testar 403 sem logout ou falso sucesso.
- Groups omite BARDO no filtro local; correção de catálogo fica fora desta task.
- Não houve inspeção de browser nesta rodada. Evidências visuais desktop/mobile serão produzidas durante execução/QA; não se declara conformidade WCAG completa.
