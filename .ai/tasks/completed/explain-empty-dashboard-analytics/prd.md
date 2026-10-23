# PRD — Explicar estados vazios das métricas do Dashboard

## Objetivo

Após um carregamento bem-sucedido, explicar quando os gráficos e indicadores de `/dashboard` ainda não têm dados suficientes, sem parecer erro nem esconder zeros legítimos.

## Problema e contrato confirmado

`DashboardService.getDashboardMetrics()` agrega no cliente as listas tenant-scoped de `GET /events`, `GET /attendances` e `GET /members`; não existe endpoint agregado do Dashboard. O componente já mantém loading/erro/retry separados e renderiza os painéis após sucesso.

- Sem eventos na janela selecionada pelo filtro: `presenceLine` não tem itens e os três itens de `presencePie` têm contagem zero. A distribuição por classe pode continuar preenchida se houver membros.
- Eventos na janela, mas nenhuma presença associada a eles: a linha mantém os nomes dos eventos com séries 0/0/0 e o total do gráfico de presença é zero. Isso significa ausência de registros na janela atual, não necessariamente ausência histórica.
- Sem membros: `classPie`, `highlights` e `alerts` ficam vazios. A distribuição não pode ser chamada de vazia quando os membros existentes já a preenchem.
- Com membros mas sem presenças recentes, `highlights` ainda lista até cinco pessoas com zero; não transformar isso em ausência de membros nem mudar o ranking nesta task.
- `alerts=[]` pode significar ausência de eventos/registros suficientes ou ausência real de sequência de faltas; não afirmar “nenhuma falta detectada” quando não há observações suficientes.

Os endpoints correspondem a listas (`Event[]`, `Attendance[]`, `Member[]`). Backend confirma `events` por `GuildId`, `attendances` por `Event.GuildId` e `members` por `GuildId` via `TenantService`. Nenhum dado real de produção foi consultado.

## Requisitos

- Explicar estados insuficientes de presença distinguindo janela sem eventos de eventos sem registros de presença na janela selecionada.
- Explicar ausência de membros nos painéis dependentes de membros, mantendo distribuição por classe quando houver dados válidos.
- Distinguir “dados insuficientes” de uma conclusão negativa válida no painel de alertas.
- Manter contagens e zeros verdadeiros; não substituir zero por valores fictícios nem esconder painéis que têm dados.
- Preservar estados de loading, erro e retry; empty state só aparece após resposta bem-sucedida.
- Preservar filtro de quantidade de eventos, cálculos, endpoints, RBAC, rotas e isolamento server-side por Guild.
- Usar cópia curta e componentes/estilo Material existentes, conforme Direção B e `ux-review`.

## Escopo

- Condições de apresentação derivadas do resultado já recebido pelo componente.
- Copy/estados vazios dos painéis de presença, distribuição por classe, alertas e destaques apenas quando a ausência estiver comprovada pelas coleções derivadas disponíveis.
- Ajustes locais e mínimos de hierarquia/ocupação para esses estados, se a inspeção renderizada demonstrar necessidade.
- Specs de Dashboard para vazio, dados parciais, zeros, dados preenchidos e separação entre loading/erro.

## Fora de escopo

Fórmulas/ranking, endpoint ou serviços/backend, nova visualização, novos dados fictícios, mudanças de RBAC/tenant, filtros, redesign global, novos assets/dependências e alterações a telas que não sejam Dashboard.

## Critérios de aceite

- Sem eventos e sem presenças na janela selecionada têm explicações distintas e verdadeiras.
- Gráfico de classes/destaques/alertas só recebe empty state correspondente quando não há membros; com membros e zero presenças, dados de classe e ranking zero permanecem visíveis.
- A mensagem de alertas não conclui ausência de faltas consecutivas quando não há dados suficientes para avaliar.
- Carregamento e falha de API não são apresentados como ausência de dados; retry existente continua funcionando.
- Com dados, gráficos, filtro e painéis preenchidos mantêm conteúdo e comportamento atuais.
- Specs e build passam; inspeção em 1280×800 e 390×844 é realizada se fixture local segura estiver disponível. Limitações renderizadas são registradas sem alegações visuais.

## Dependências, riscos e segurança

- Dependência de contrato/condições confirmada em `DashboardService`, services Angular, controllers/services e repositórios tenant-scoped.
- Inspecionar se há fixture local visual segura antes de alterar apresentação; não usar credenciais reais nem dados de produção.
- Risco principal: afirmar ausência global com base apenas na janela selecionada ou confundir zero válido com vazio.
- Sem impacto esperado em auth, autorização ou tenant; Security consultivo somente se o escopo real mudar.
- Nenhuma mudança operacional/observabilidade esperada.
