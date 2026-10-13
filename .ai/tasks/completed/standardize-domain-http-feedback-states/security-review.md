# Security Review — standardize-domain-http-feedback-states

## Superfície e achados

- Alteração da task: `httpFeedback` usa apenas status HTTP e string local estática de ação. Testes para 0/400/401/403/404/409/500 com corpo fictício contendo segredo/SQL comprovam que o texto bruto não chega à mensagem. Não há `error.error`/`error.message` nos componentes alterados, novo log ou armazenamento sensível. **INTRODUZIDO_PELA_TASK: nenhum achado bloqueante.**
- 401 continua tratado pelo `AuthInterceptor` existente; componentes não adicionaram logout paralelo. 403 informa falta de permissão sem logout. 404 é genérico e não revela existência/vínculo de outra Guild. Read 401/403 limpa ou oculta dados antigos nos fluxos afetados. Nenhum `guildId` adicionado ao frontend; TenantService/backend seguem como autoridade.
- Backend já possuía `ApiExceptionHandler` que monta 409 com root cause e 500 com mensagem da exceção, e `GlobalExceptionHandler` com mensagem de exceção. Evidência de código **PREEXISTENTE_RELACIONADO**; potencial exposição de detalhes na resposta HTTP. Esta task impede renderização do corpo dessas respostas nas quatro telas, então o achado não impede validar a alteração. Não foi reproduzida resposta de produção nem severidade crítica confirmada. Recomendada task separada para uniformizar respostas de erro e remover detalhes internos do backend. Não atribuir a esse achado bloqueio global novo sem avaliação própria.
- Controles de mutação em áreas de domínio já eram exibidos sem checagem visual completa de role. **PREEXISTENTE_NAO_RELACIONADO**; endpoints verificados têm `@PreAuthorize` para MARECHAL/GENERAL/MAJOR. Não há evidência de bypass nesta task; recomendar alinhamento visual posterior sem expandir este escopo.

## Vereditos

**Task Security Verdict: APROVADO.** Nenhuma elevação de privilégio, vazamento novo por mensagem, alteração de JWT/tenancy ou secret introduzido. Mensagens genéricas para falha de escrita não afirmam que a gravação foi revertida.

**Release Verdict: APROVADO** com as evidências globais conhecidas. Achados backend/visual são encaminhamentos preexistentes; esta revisão não revalidou produção nem certifica resposta dos handlers em produção.
