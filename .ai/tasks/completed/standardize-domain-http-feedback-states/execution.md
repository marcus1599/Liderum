# Execution — standardize-domain-http-feedback-states

2026-10-13. Escopo aprovado pelo usuário. Base Git `0467b9d`; antes da execução havia apenas `state.md`, `handoff.md` e os três artefatos de planejamento desta task no working tree.

## Implementação

- `MembersComponent`: leitura com estado de sucesso distinto de falha, empty de cadastro/filtro, retry de GET, pending e feedback para criação/edição/exclusão, bloqueio de dupla mutação e preservação do formulário em erro. Uma gravação bem-sucedida seguida de recarga falha é comunicada como tal.
- `GroupsComponent`: duas leituras necessárias (groups→members) com falha parcial visível, empty real/filtro, retry, pending e feedback em CRUD/add/remove. Drag-and-drop não altera a lista local antes de resposta do servidor; ordenação não persistida foi mantida sem mutação local. Ao falhar, dados anteriores só aparecem quando o estado permite e ações ficam bloqueadas.
- `EventsComponent`: leitura com empty único, retry, pending e feedback das mutações. Conversão de datetime-local aplicada somente ao payload copiado, preservando o formulário em falha.
- `AttendenceComponent`: `forkJoin` das três leituras essenciais, alerta independente com retry, seleção de evento protegida contra resposta fora de ordem, confirmação/falha da presença, restauração do radio anterior em falha e recarga após sucesso com erro próprio. Lista vazia de presença não impede a primeira marcação.
- `shared/http-feedback.ts`: helper puro com mensagens locais por status/contexto; corpo de erro arbitrário nunca é interpolado. Templates usam `role=status/alert`, botões de retry e estilos locais legíveis no layout escuro. Não há alteração de backend, service HTTP, auth, rota, RBAC, tenancy, dependências ou migration.

## Validação empírica

- `npm test -- --watch=false --browsers=ChromeHeadless` em `frontend/`: **65/65 SUCCESS**, exit code **0** na execução final após o último teste comportamental. Testes incluíram estados assíncronos, retry, duplo envio, falha da recarga após gravação, gesto em Team sem falso movimento, alerta separado, seleção fora de ordem, radio restaurado e mensagens que não reproduzem corpo sensível fictício.
- `npm run build` em `frontend/`: **SUCCESS**, exit code **0**, após os ajustes finais de SCSS. O primeiro build sandboxed falhou apenas em `spawn EPERM` do esbuild; repetido com permissão de execução e passou. Builds posteriores também passaram.
- `git diff --check`: exit code 0; avisos de normalização LF/CRLF sem erro de whitespace.
- Warnings Angular já conhecidos: deprecação Sass, budgets CSS e baseline-browser-mapping. O estilo adicional elevou o warning de budget de `members.component.scss` de 2,79 kB para 2,92 kB acima do limite de 4 kB; build segue aprovado. Não foram corrigidos fora do escopo.

## Limites

- Nenhuma sessão manual autenticada do frontend alterado foi realizada em navegador desktop/estreito. Specs rodaram em ChromeHeadless e verificaram DOM, estados e interações; responsividade foi revisada por templates/SCSS existentes e ausência de alteração estrutural do layout, sem alegação de inspeção visual manual ou WCAG completa.
- Não houve request real de produção nem backend reexecutado, pois só código/testes Angular foram alterados.
- O inventário registra handlers backend preexistentes que podem enviar mensagens internas e RBAC visual incompleto das áreas. O frontend desta task não exibe corpo bruto nem altera autoridade do backend; encaminhar cada tema em task própria se priorizado.
