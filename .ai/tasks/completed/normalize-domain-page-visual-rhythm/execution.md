# Execução — `normalize-domain-page-visual-rhythm`

## Ativação

Ativada em 2026-10-23 após confirmação da task anterior arquivada e commitada (`f2e9f68a9347efa38a2018237bfb01a3b9791bfe`). Fase atual: inventário/planejamento. Não foram feitas alterações de produto nesta task.

## Inventário estático — 2026-10-23

| Superfície | Evidência em código | O que não comprova |
|---|---|---|
| Dashboard | `.content` usa `padding: 20px`, reduzido a 10px e 6px em breakpoints; KPIs e painéis têm estilos próprios; templates distinguem estados vazios | Não comprova densidade percebida, alinhamento ou contraste renderizado |
| Members | Host margin `8px 16px`; cards com cabeçalho/content paddings próprios e ajustes móveis; formulário e lista em superfícies Material | Não comprova se ritmo de título/lista coincide com demais rotas |
| Teams | Host margin `8px 16px`; card Material com título; blocos de atribuição e liderança têm padding/separação locais e breakpoints | Não comprova hierarquia visual geral nem como grupos preenchidos se comparam |
| Events | Host margin `8px 16px`; cartões Material separados para formulário/lista e paddings locais | Não comprova espaçamento visual real entre seções, listas ou estados |
| Attendance | Host margin `8px 16px`; cartão Material principal, tabela e alertas em cartões internos; breakpoints próprios | Não comprova se aninhamento/superfícies ficam pesados em desktop/mobile |
| Users | `.users-page` tem `padding: 2rem`, `max-width: 1100px`, centralização; em ≤800px padding 1rem; linhas mudam para uma coluna | Não comprova se a margem externa contrasta com as demais páginas nem overflow efetivo |

Observação estática: o detector Impeccable retorna quatro alertas `broken-image` em `groups.component.html`. A expressão dinâmica `[src]="'assets/icons/classes/ClassIcon_' + member.classe + '.png'"` está presente no conteúdo e na prévia CDK; a evidência aponta limitação/falso positivo do detector que não reconhece binding Angular, não uma imagem sem fonte. Nenhuma alteração foi feita.

## Tentativa de inspeção local

Foi criada temporariamente uma fixture de mesmo origin e um mock loopback com apenas GET para perfil e listas vazias. O token sentinela era local de teste e não é credencial. Nenhuma requisição alcançou backend: a ponte do navegador rejeitou `http://127.0.0.1:4201/ux-roadmap-fixture.html` com conexão recusada mesmo após o `ng serve` reportar a URL de escuta. Não houve captura renderizada, leitura de estado visual, métrica de overflow ou validação de foco; screenshots/AX existentes de outras tasks não foram reutilizados como evidência desta inspeção.

Os processos Angular e mock foram encerrados, fixture removida, `environment.development.ts` restaurado exatamente para `http://localhost:8080`; `git diff` desse arquivo vazio. A checagem subsequente encontrou somente os documentos de governança já alterados e esta task.

## Bloqueio e retomada

Bloqueio técnico: navegador integrado sem conectividade para o servidor local do workspace, embora o servidor Angular compilasse e anunciasse `127.0.0.1:4201`. Evidência: erro `ERR_CONNECTION_REFUSED` da navegação local; nenhuma captura/AX foi obtida. Próxima ação: retomar quando houver browser/runtime que alcance localhost do workspace ou fixture renderizada fornecida em ambiente permitido; então capturar as seis rotas em 1280×800 e 390×844 com o mesmo mock local. Até lá, não inferir divergência nem modificar UI; a task permanece ativa e gates pendentes.

## Retomada e inventário renderizado — 2026-10-23

### Diagnóstico de conectividade

- No início desta retomada não havia processo Angular escutando em 4201; os processos anteriores haviam sido encerrados, então esse estado inicial não demonstra o motivo histórico da recusa. A tentativa de iniciar sem elevação encerrou antes de abrir a porta com `spawn EPERM` durante a criação do subprocesso esbuild: bloqueio do sandbox nessa invocação, não falha do projeto nem conflito de porta.
- Com execução aprovada fora do sandbox, `ng serve --host 127.0.0.1 --port 4201` compilou e permaneceu em watch mode. `Invoke-WebRequest` para `127.0.0.1:4201` e `localhost:4201` retornou 200 com título `Liderum`; `netstat` confirmou `127.0.0.1:4201` em LISTENING (PID 14456). O listener estava restrito a loopback.
- O navegador integrado alcançou a mesma URL e abriu `/login`, sem `ERR_CONNECTION_REFUSED`. A causa do erro anterior não pode ser determinada retrospectivamente porque checkpoint anterior não guardava PID, lifetime nem resposta HTTP. Nesta execução não havia servidor no diagnóstico inicial; depois, terminal e navegador acessaram corretamente.
- `localhost:8080` já tinha listener Java (PID 7072) em `0.0.0.0`; não foi acessado para evitar qualquer risco de tráfego ao backend. Mock temporário em `127.0.0.1:43128` não encaminhou requests e registrou somente método, caminho e status, nunca o corpo. A configuração dev apontou temporariamente ao mock, depois foi restaurada e seu diff verificado vazio. Nenhum serviço externo ou produção foi usado.

### Fixture e método

Foi usada a aplicação Angular real e suas rotas/componentes. Login local usou valores fictícios não armazenados em artefato; `/auth/login` respondeu um token de fixture e `/users/me` um perfil MARECHAL sintético. GET das listas responderam arrays vazios. Não houve dados reais nem mutações. A estratégia não foi mockup: foram observados os componentes renderizados e a árvore acessível do browser integrado.

### Evidências comparativas observadas

| Superfície | Desktop 1280×800 | Mobile 390×844 | Achado/limite |
|---|---|---|---|
| Dashboard | KPIs em faixa, painéis analíticos em grade de três colunas; estados vazios contextualizados. | KPIs em duas colunas e painéis empilhados; rolagem vertical. | Estrutura de analytics própria; nenhuma divergência de superfície demonstrada. |
| Members | Formulário e lista separados em cartões escuros; tabela ocupa faixa larga. | Formulário empilhado; tabela preserva largura mínima de 600px dentro de `.table-wrapper { overflow-x: auto; }`. | Scroll horizontal observado é o scroll interno intencional da tabela, não evidência de overflow do documento; preservado. |
| Teams | Painel de membros e área de grupos/atribuição lado a lado. | Painéis empilhados; controles de atribuição em coluna e largura total. | Diferença estrutural justificada pelo fluxo e breakpoints; sem desvio material observado. |
| Events | Formulário horizontal em cartão e lista abaixo. | Formulário empilhado com campos/botões em largura total. | Padrões de superfície escura coerentes; sem desvio material observado. |
| Attendance | Card-raiz claro padrão do Material e título junto à borda, contrastando com shell escuro e cards internos escuros. | Mesmo card claro ocupa quase toda a altura disponível antes do painel de alertas escuro; título desalinhado com cartões internos. `.absence-alert-list` mantém `flex: 1 1 220px` após o layout trocar para coluna. | Divergência concreta de superfície/alinhamento e espaço vazio móvel; template raiz sem estilo local e crescimento flex preservado no breakpoint. Proposta mínima: aplicar ao card-raiz tons/paddings existentes no Dashboard/Members e zerar crescimento flex do painel de alertas em coluna. |
| Users | Página limitada a 1100px; hierarquia título, contexto, formulário e vazio. | Conteúdo empilhado com margem interna e sem scroll horizontal visível. | Densidade/hierarquia administrativas específicas; sem justificativa para uniformização adicional. |

Evidência renderizada observada: browser integrado nas seis rotas, nos dois viewports; AX confirmou títulos, campos, estados vazios, navegação e contagens. Capturas foram vistas nesta sessão, mas não exportadas como arquivos. Não havia dados de teste para estados populated/success/error; esses estados não são reivindicados como inspecionados visualmente nesta task. A navegação autenticada usou somente mock loopback.

### Alteração delimitada e gates pendentes

A única divergência material que justifica alteração é o card-raiz e o espaçamento móvel de Attendance. As diferenças de estrutura nas demais superfícies são explicadas pelo tipo de conteúdo, e o scroll da tabela Members é delimitado ao wrapper previsto. Implementar apenas ajuste SCSS scoped de superfície/padding do card e `flex-grow` do painel de alertas quando empilhado; não alterar conteúdo, Material, endpoints, RBAC, tenancy ou regras. Revalidar o mesmo estado vazio nos dois viewports, testes aplicáveis, build, acessibilidade básica, Impeccable, QA e Auditor. A task continua ativa até aprovação dos gates.

### Implementação e validação — 2026-10-23

- Alteração: somente `frontend/src/app/attendence/attendence.component.scss`. O card-raiz Attendance agora usa a superfície escura e cores de texto/título já presentes no tema; os paddings alinham título e conteúdo aos cards internos; em viewport empilhado o painel de alertas deixa de crescer para preencher altura vazia. Nenhuma alteração de template, TypeScript, HTTP, RBAC, tenant ou regra de negócio.
- Inspeção posterior da aplicação real, estado vazio com API mock isolada: Attendance 1280×800 e 390×844. A superfície agora combina com o shell escuro; título e conteúdo têm alinhamento interno; layout horizontal permanece no desktop, painéis empilham sem a grande faixa vazia no móvel. No móvel, o documento não excede 390px; foco por teclado no seletor de Evento exibe indicador visível e o controle tem nome acessível. O registro de observação desta sessão não inclui arquivos de captura exportados; não alegar que screenshots persistidas foram entregues.
- O inventário antes da mudança permanece válido para as outras cinco superfícies: Dashboard, Members, Teams, Events e Users em ambos os viewports. Elas não foram alteradas. Members conserva scroll horizontal restrito ao wrapper da tabela. Estados populated/success/error não foram renderizados por falta de dados/fixture apropriados; nenhuma conclusão visual é feita sobre eles.
- Testes Angular: `npm test -- --watch=false --browsers=ChromeHeadless` — 119/119 aprovados, ChromeHeadless 155.0.0.0, exit 0. Build: `npm run build` — sucesso, exit 0; avisos Sass deprecations e budgets de estilo são preexistentes nas áreas Dashboard/Members e estilos globais. `git diff --check` sem erros de whitespace.
- Impeccable: `detect --json --scope layout` no SCSS alterado retornou `[]`; revisão manual de hierarquia/superfície/responsividade concluída. A CLI instalada não implementa o comando `audit` (`Unknown command: "audit"`); não alegar um audit automatizado Impeccable. Sem auditoria WCAG completa, medição automatizada de contraste, teste de zoom/toque ou verificação visual de conteúdo populado. Security não é gate necessário: somente SCSS localizado foi modificado.
- QA independente: APROVADO COM LIMITAÇÕES; ver `qa.md`. A inspeção registrada comprova os estados vazios nos dois viewports, não estados populated/success/error nem WCAG completa.
- Auditoria independente: Task Verdict APROVADO após reavaliação com o pacote reconciliado; Release Verdict global permanece BLOQUEADO pelo achado RabbitMQ preexistente, não relacionado, sem reprovação desta task. Ver `audit.md`.
