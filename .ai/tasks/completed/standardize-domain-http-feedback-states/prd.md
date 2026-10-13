# PRD — standardize-domain-http-feedback-states

Status: implementada e auditada; Task Verdict APROVADO em 2026-10-13. Ciclo 3, prioridade P1, porte MEDIUM.

## Objetivo e contexto

Permitir que uma Guild reconheça o andamento e o resultado das operações existentes em Members, Teams, Events e Attendance, sem confundir falha com ausência de dados ou sucesso de persistência. O inventário desta task registra os gaps reais. Navegação autenticada já foi consolidada; seis fases originais e Ciclos 1/2 permanecem concluídos.

## Escopo e requisitos

1. Separar estado de consulta inicial/recarga do estado de mutação. Loading deve permanecer perceptível até resolver as dependências relevantes e sempre encerrar em sucesso/erro. Evitar mensagens de sucesso antigas após nova tentativa.
2. Exibir vazio apenas após consulta bem-sucedida. Members diferencia cadastro vazio de filtro sem resultado; Teams diferencia ausência de equipes, membros disponíveis e filtro sem resultado. Events tem uma única mensagem vazia. Attendance diferencia ausência de eventos/membros, necessidade de selecionar evento e ausência de presença naquele evento; ausência de registro não impede registrar a primeira presença.
3. Erros de consulta ficam inline, com ação explícita de tentar novamente quando útil. Retry refaz somente leitura, nunca reenvia POST/PUT/DELETE automaticamente. Recuperação limpa erro anterior. Dados de última leitura podem permanecer visíveis quando ainda seguros, identificados como desatualizados; ações dependentes ficam desabilitadas até reconciliar. Em 401/403 não apresentar dados antigos como acessíveis.
4. Toda mutação já exposta tem pending, sucesso e erro: CRUD Members/Teams/Events; adicionar/remover membro de equipe por gesto/botão; criar/atualizar Attendance. Impedir duplicação da mesma operação e ações conflitantes enquanto pendente, inclusive por chamada do handler. Manter dados de formulário e seleção após falha; limpar/resetar apenas após confirmação. Preservar confirmações de exclusão existentes.
5. Confirmar sucesso somente após resposta HTTP bem-sucedida. Se a gravação passou e a recarga falhou, informar “operação concluída; não foi possível atualizar a lista” e permitir recarregar, sem sugerir repetir a gravação. Falha de transporte de mutação não prova que servidor não persistiu: orientar conferir/recarregar antes de repetir, sem retry automático.
6. Teams: adotar atualização após confirmação no lugar do push otimista sem rollback; falha deixa composição visual anterior. Caso POST/DELETE passe, mas GET posterior falhe, marcar necessidade de atualização. Falha de members não deve fingir lista de disponíveis vazia nem habilitar drag-and-drop com base incompleta. Não persistir ordenação nem adicionar seleção de líder.
7. Attendance: members/events/attendances são essenciais; alertas de faltas têm estado independente e falha não pode aparecer como “ninguém em risco”. Troca rápida de evento não permite resposta antiga sobrescrever seleção/feedback atuais. Gravação captura o par member/event e bloqueia concorrência conflitante de maneira simples; radio volta ao valor confirmado em falha. Falha na atualização dos alertas não converte presença salva em falha de gravação.
8. Feedback padrão inline por contexto com semântica alert/status, reaproveitando cards/botões Material e identidade atual. Não duplicar snackbar e mensagem inline para o mesmo resultado. Componente visual genérico ou store novo não é necessário; helper puro pequeno para mensagens é permitido pela duplicação entre quatro telas.
9. Acessibilidade básica: loading anunciado, nomes acessíveis nos controles afetados, texto além de cor/ícone, foco visível, retry acessível por teclado. Conferir desktop e viewport estreita sem quebrar shell/rolagem de tabelas. Acessibilidade completa do drag-and-drop e redesign ficam adiados.

## Política de erros e segurança

| Situação | Comportamento |
| --- | --- |
| 401 | Preservar expiração/redirecionamento do interceptor; não inventar logout local paralelo nem mostrar vazio. |
| 403 | Informar falta de permissão para a operação, sem encerrar sessão. |
| 404 | Recurso/dado solicitado não está disponível; não inferir se pertence a outra Guild nem tratar como lista vazia. |
| 400 | Orientar revisar os campos da operação; usar validações locais conhecidas, sem concatenar mensagens arbitrárias da API. |
| 409 | Informar conflito com estado atual e orientar recarga/revisão; não afirmar duplicidade ou FK específica apenas pelo status. |
| 0 / 5xx / desconhecido | Mensagem local de conexão/indisponibilidade apropriada à leitura ou mutação; nunca stack trace, SQL ou payload bruto. |

Não renderizar `message/error` arbitrários do backend. Erros específicos seguros devem ser construídos com status e contexto conhecido; não suprimir distinções úteis. Sem logs de payload, token, senha ou dados pessoais. Tenant continua server-side via TenantService, sem guildId como autoridade. Sem mudança de JWT, guard, sessão, permissões ou contratos.

## Fora de escopo

Backend, migrations, APIs novas, Users/auth/onboarding, dashboard/sidebar, Settings, escolha de líder, catálogo de classes, nova matriz/expansão de RBAC visual, redesign, design system, NgRx, dependências, CI, E2E/Playwright, infraestrutura, observabilidade e notificações. Achados desses domínios serão registrados/escalados.

## Aceite e testes

- As quatro áreas comprovam loading, sucesso, vazio real, erro e recuperação; nenhuma falha HTTP vira sucesso ou vazio silencioso.
- Mutações cobertas com respostas assíncronas controladas: bloqueio de repetição, preservação em falha, confirmação e falha da recarga posterior.
- Testes específicos de Teams (sem persistência visual falsa) e Attendance (dependências, alerta independente, resposta fora de ordem e radio em falha).
- Testes de status 401/403/404/400/409/0/500 e payload interno malicioso/sensível comprovam mensagens seguras; auth/rotas/Users continuam passando.
- Jasmine/Karma e build completos com resultado e exit code verificáveis; evidência visual desktop/estreito e teclado registrada com limitações.
- QA → Security → Auditor aprovam; finish-task apenas com Task Verdict APROVADO. Release Verdict separado.

## Dependências e riscos

Sem novas bibliotecas, backend ou ADR. Usar Angular/Material/RxJS existentes. Riscos principais: requisições concorrentes, feedback de recarga confundido com gravação, datasource/paginator afetados por renderização condicional e mensagens internas expostas. Histórico registra Release APROVADO; este planejamento não certifica um novo release nem executa gates finais.
