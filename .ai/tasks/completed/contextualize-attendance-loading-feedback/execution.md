# Execução — feedback contextual de presença

Data: 2026-10-27. Escopo: política de loading HTTP e rota `/attendance`, sem backend, contrato, RBAC, tenant ou autenticação alterados.

## Implementação

- `SHOW_GLOBAL_LOADING` usa `HttpContextToken` com padrão `true`. O interceptor preserva contador e `finalize` para requisições globais; apenas chamadas explicitamente marcadas como locais evitam o overlay.
- Serviços de Member, Event e Attendance aceitam contexto opcional nas leituras usadas por Presence. O componente passa contexto local em suas leituras e escritas. `DashboardService.getDashboardMetrics()` continua chamando `AttendanceService.getAttendances()` sem contexto e preserva loading global padrão. Demais consumidores também mantêm o padrão.
- Presença mantém `pendingMemberIds` e feedback por membro. A seleção confirmada é restaurada imediatamente após o clique e só muda com a resposta positiva do servidor. O retorno atualiza uma linha, sem recarregar a lista inteira. Erro preserva o último status confirmado e permite retry. Enquanto salva, apenas a linha afetada e a troca de evento ficam indisponíveis; linhas diferentes podem salvar em paralelo. A lista de alertas é relida separadamente.
- A tela usa texto discreto `Salvando...`, `Presença salva.` e mensagem de erro junto ao membro, com `role=status`/`alert`; os rádios mantêm rótulos acessíveis. O tema existente foi preservado.

## Validação

- `npx ng test --watch=false --browsers=ChromeHeadless`: 132/132 SUCCESS, exit 0 após o ajuste que preservou o comportamento do Dashboard. Testes cobrem contador global, erro/cancelamento, opt-out local, padrão global do serviço, sucesso de presente/falta, erro/retry, duplicação, simultaneidade por membro, RBAC e seleção de evento.
- `npm run build`: exit 0. A primeira tentativa no sandbox falhou com `spawn EPERM` ao iniciar esbuild; a repetição autorizada fora do sandbox compilou. Warnings Sass e budgets de Dashboard/Members preexistentes; novo warning não bloqueante de Attendance: 4,10 kB versus aviso em 4,00 kB (+102 bytes). Nenhum erro de compilação.
- `git diff --check`: sem erros de whitespace.
- Impeccable: leitura da skill e revisão manual do fluxo; detector nos HTML/SCSS alterados retornou `[]`. O comando `context` inicial usou o caminho inexistente `attendance/` em vez de `attendence/`; nenhum contexto de produto/design foi criado por esse comando. A revisão seguiu a direção canônica em `.ai/docs/liderum-visual-direction.md`.

## Inspeção renderizada

Fixture HTTP temporária fora do repositório, escutando só `127.0.0.1`, com 100 membros sintéticos, sessão local e resposta de escrita retardada; nenhuma chamada ao backend real. `ng serve` em `127.0.0.1:4201`. O `environment.development.ts` foi apontado temporariamente à fixture e restaurado; servidores e aba de inspeção foram encerrados.

| Viewport/estado | Observação |
| --- | --- |
| 1280×800, normal | Tabela e resumo de alertas renderizados no tema escuro; rádio confirmado de Membro 001 selecionado. |
| 1280×800, duas operações | Membros 002 e 003 mostraram `Salvando...` em suas linhas; só seus rádios desabilitaram. As outras linhas ficaram disponíveis; nenhum overlay global apareceu. |
| 1280×800, sucesso | Cada resposta confirmou a própria linha, com `Presença salva.` e rádio correspondente selecionado. |
| 390×844, normal/sucesso | Menu móvel fechado; tabela rolável internamente. `document.documentElement.scrollWidth=390` com viewport de 390; sem overflow horizontal da página. Feedback textual permaneceu legível na célula do membro. |
| 390×844, erro | Linha 004 mostrou pendência e depois mensagem de falha recuperável; rádios voltaram ao último valor confirmado e foram reabilitados. Demais linhas ficaram disponíveis. |

Capturas foram observadas no navegador integrado e não exportadas como artefatos. Foco visível e navegação por teclado não receberam teste manual completo; os controles nativos/rótulos e roles foram revisados por código e specs. `prefers-reduced-motion` não recebeu teste renderizado específico; a implementação não adiciona animação. Não houve leitor de tela, toque/zoom, WCAG completa, backend real nem teste multi-tenant executado nesta task. Essas limitações não equivalem a aprovação desses cenários.

## Escopo e pendências

Nenhuma dependência nova, migration ou alteração de endpoint/payload. Warning de orçamento CSS de Attendance (+102 bytes) é dívida pequena não bloqueante. Release global RabbitMQ permanece bloqueado por causa independente; esta task não o altera.
