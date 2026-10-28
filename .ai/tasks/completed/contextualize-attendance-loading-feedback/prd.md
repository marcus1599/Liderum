# Feedback contextual para registro de presença

## Objetivo e contexto

Permitir registrar presença de uma lista grande sem interromper toda a aplicação a cada ação. A rota `/attendance` deve manter outras linhas utilizáveis enquanto um membro é salvo e mostrar o estado confirmado pelo servidor.

## Diagnóstico da implementação existente

- `LoadingInterceptor` incrementa um contador de requisições de módulo para toda chamada HTTP e usa `finalize` para ocultar o overlay de `AppComponent`. Sucesso, erro e cancelamento liberam o contador; requisições paralelas não ocultam o overlay prematuramente. O overlay cobre a janela inteira (`z-index: 9999`). Não há política por requisição.
- `AuthInterceptor` injeta JWT e expira sessão em 401; não controla loading. `AuthService.login` faz login e depois `/users/me`, que podem continuar sob overlay global por serem transição crítica.
- `AttendenceComponent.loadData` usa `forkJoin` para membros, eventos e presenças; `onEventChange` lê presenças; `loadAlerts` lê alertas. Há estados locais de loading/erro, mas o overlay global os encobre.
- `markAttendance` usa um booleano `saving` para todas as linhas, faz POST/PUT, atualiza a linha com a resposta e busca toda a lista novamente. Cada salvamento desabilita todos os rádios; a leitura posterior também aciona overlay. Leituras paralelas de atualização podem sobrescrever respostas de outras gravações. O rádio é selecionado pelo navegador antes da confirmação e revertido só em erro.
- O backend `AttendanceController` retorna `AttendanceResponseDTO` com id, memberId, eventId e status após salvar; valida tenant e RBAC no servidor. Essa resposta basta para confirmar a linha sem uma leitura global a cada gravação. Não há contrato de lote.

## Requisitos e critérios de aceite

1. Requisições da rota de presença usam feedback de seção ou de ação, sem overlay global. O login/restauração de sessão e outras requisições sem política explícita mantêm o comportamento global existente.
2. Após escolher evento, a tabela mostra apenas status confirmado. Ao marcar Presente, Faltou ou Justificou, a linha indica salvamento e mantém o status anterior até a resposta positiva; sem registro anterior, fica sem seleção até a confirmação.
3. Enquanto um membro salva, só os controles desse membro ficam indisponíveis. Repetições na mesma linha não enviam uma segunda requisição; linhas diferentes podem salvar simultaneamente.
4. Respostas positivas atualizam somente o registro correspondente, sem refazer toda a lista. O status e eventual id retornado pelo servidor são usados. Alerta de faltas é relido após o salvamento, sem bloquear a tabela.
5. Erro HTTP mantém o último status confirmado, mostra mensagem recuperável junto ao membro e permite nova tentativa. Erros de leitura permanecem distintos de lista vazia. 401 continua sob o comportamento de autenticação existente.
6. Seleção de evento e recarga completa ficam bloqueadas enquanto houver gravações em curso para evitar misturar contextos.
7. Feedback é textual e acessível por teclado/leitor de tela; respeita o visual escuro, Material violeta, Roboto e responsividade existentes.

## Estratégia e alternativas

Usar `HttpContextToken` com padrão global e opção local para as leituras e mutações desta rota; estado local por memberId; atualização pessimista com resposta do servidor. `finalize` libera o estado de cada linha inclusive em erro/cancelamento. A alternativa otimista exigiria rollback e poderia sugerir sucesso antes de o backend persistir. A recarga completa após cada gravação cria bloqueio e corrida, então será removida desta ação.

## Escopo

Interceptor e política de loading, serviços de leitura usados pela rota, serviço de presença, componente/template/SCSS de presença, testes correspondentes e registros da task. Preservar endpoints, payloads, RBAC, tenant, auth e demais telas. Não criar batch endpoint, dependência, store global, redesign nem alterar backend.

## Riscos, dependências e validação

- Concorrência entre duas linhas e retorno fora de ordem; teste com observables controlados.
- Falha de uma linha não deve desfazer sucesso de outra; teste de isolamento.
- A leitura de alertas pode falhar sem apagar status confirmado; manter feedback próprio.
- Verificar sucesso de Presente/Faltou, erro, loading, duplicação, simultaneidade, cancelamento, política de overlay e build Angular.
- Inspeção renderizada local em 1280×800 e 390×844 com fixture sintética para normal, em andamento, sucesso, erro e simultaneidade quando viável; registrar limites sem atribuir aprovação visual fictícia.
- QA e Auditor são gates; Security será consultado se a política afetar autenticação, dados sensíveis ou autorização. Release global RabbitMQ permanece independente e bloqueado.
