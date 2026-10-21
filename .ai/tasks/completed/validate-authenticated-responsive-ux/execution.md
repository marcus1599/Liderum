# Execution — Validação de UX autenticada em viewports reais

## Routing

- Domínios: frontend, testing, audit; Security condicional caso apareça exposição de RBAC/tenant.
- Agents: Frontend Dev (inspeção/correções dentro do escopo) → QA → Security condicional → Auditor.
- Skills: `ux-review`; `test-frontend` somente se código de UI for alterado; `security-review` condicional; `audit-task`/`finish-task` após evidência suficiente.
- Impeccable foi usada apenas para contexto/adaptação responsiva, craft floor e detector; não houve redesign.

## Progresso e evidências

Data: 2026-10-19. Navegador: Codex in-app browser. Os screenshots disponíveis mediram 425×580 px; essa superfície não oferece ajuste de viewport nesta sessão. Portanto não foi declarada como 1280×800 nem como 390×844.

- `/dashboard`: abriu autenticado e apresentou KPIs/gráficos; screenshot estreito sem overflow horizontal visível. Conteúdo vertical usa rolagem.
- `/members`: navegação pela sidebar funcionou e fechou o drawer; loading terminou; formulário e lista com um registro ficaram disponíveis. Screenshot estreito mostrou campos e botão ocupando a largura disponível, sem overflow horizontal visível.
- `/teams`: loading terminou; membro disponível foi exibido, estado vazio de grupos foi comunicado e atribuição permaneceu desabilitada até haver grupo. Nenhuma mutação executada.
- `/events`: loading terminou e estado vazio “Nenhum evento cadastrado” foi apresentado, junto ao formulário. Nenhum evento criado.
- `/attendance`: loading terminou; UI explicou que não havia evento para registrar presença e que nenhum membro estava em risco.
- `/users`: tela administrativa foi acessível nesta sessão e identificou o escopo como Guild da sessão; formulário não foi submetido. Dados de conta vistos na tela não foram copiados para este registro.
- `/account/password`: campos de senha e instrução estavam presentes; formulário não foi submetido. Screenshot estreito mostrou conteúdo vertical rolável e rota ativa destacada no drawer.
- Drawer: links navegaram às rotas; o item ativo recebeu destaque visual; Tab mostrou foco visível no primeiro link; Escape fechou o drawer. Logout pelo menu redirecionou para `/login`.
- O menu Users estava disponível no perfil observado, consistente com a condição visual para MARECHAL/GENERAL no frontend; o role exato não foi registrado. Não foi possível testar outra role nem comprovar isolamento entre Guilds via UI.
- Foram vistos loading transitório e estados vazios/carregados. Não foram provocados erros de API, estados 401/403, sucesso de mutação, navegação por toque, nem cenários de outra Guild.
- Desktop 1280×800 e handset exato 390×844 não foram exercitados porque o navegador in-app não expôs controle de viewport. O viewport observado (425×580) não substitui nenhum deles.

### Retomada com Developer Mode / CDP

Data: 2026-10-21. O usuário aprovou explicitamente o uso do CDP nesta aba autenticada para definir viewports e inspecionar UX, sem leitura de credenciais, tokens ou tráfego sensível. O carregamento inicial persistente foi transitório; após aguardar a conclusão das chamadas, a navegação interna voltou a funcionar. A viewport foi restaurada ao final.

Matriz observada em viewports emulados (não dispositivo físico):

| Rota | 1280×800 | 390×844 | Observação |
| --- | --- | --- | --- |
| `/dashboard` | Renderizou; documento 1280 px | Renderizou em cartões empilhados; documento 390 px | Indicadores e gráficos visíveis; rolagem vertical no mobile. |
| `/members` | Lista carregou após estado transitório; documento 1280 px | Formulário/lista renderizados; documento 390 px | Tabela tem rolagem horizontal interna; sem overflow horizontal do documento. Dados da conta não foram registrados. |
| `/teams` | Renderizou; documento 1280 px | Conteúdo empilhado e rolável; documento 390 px | Estado sem grupos; associação desabilitada até haver grupo. |
| `/events` | Renderizou; documento 1280 px | Renderizou; documento 390 px | Estado vazio claro. Antes da correção, cada `mat-form-field` tinha 180 px de altura no mobile. |
| `/attendance` | Renderizou; documento 1280 px | Renderizou; documento 390 px | Sem eventos para presença e aviso de faltas vazio; rolagem vertical. |
| `/users` | Rota e formulário renderizados; documento 1280 px | Rota e formulário renderizados; documento 390 px | Não exibe `guildId`; não foram feitas mutações nem copiados dados pessoais. Texto tinha contraste insuficiente. |
| `/account/password` | Formulário renderizado; documento 1280 px | Formulário renderizado; documento 390 px | Campos cabem na largura; formulário não submetido. |

- Em todas as rotas, o item ativo correspondeu à rota e a largura do documento não excedeu a viewport. No mobile, selecionar uma rota pelo drawer fecha o drawer. Enter abriu o drawer, Escape fechou e Tab moveu o foco ao próximo controle. Nenhum formulário foi submetido e nenhum dado foi alterado.
- Não foram provocados 401/403, erro de API ou sucesso de mutação, nem testada outra role/Guild; não seria seguro produzir esses cenários pela conta ativa sem fixtures apropriadas. Não foi usado dispositivo físico; as evidências são do navegador com viewport emulado.
- Baixo contraste em `/users`: texto herdava `rgb(29,27,30)` sobre superfície `rgb(46,49,57)`. A correção local define texto `#e8e6f0` e erro `#ffb3b3`; contrastes calculados com a superfície observada são 10,53:1 e 7,65:1.
- Correção local em Events: a regra base posterior definia `flex: 1 1 180px`; com o formulário em coluna, o basis virava altura de 180 px. Um override ao final do stylesheet, `.event-form mat-form-field { flex: 0 1 auto; }` em `max-width: 900px`, permite que a altura declarada de 47 px governe o campo. O site observado é produção e não foi atualizado nesta task; a correção local foi compilada, mas não foi apresentada como screenshot pós-deploy.

## Resultado da retomada

A inspeção autenticada nas duas viewports foi concluída. Foram feitas duas correções SCSS locais, sem alterar contratos ou comportamento de domínio. A página em produção não foi implantada após a correção; a revisão documenta esse limite e não afirma uma captura pós-deploy.

## Alterações

- `frontend/src/app/events/events.component.scss`: override do flex-basis responsivo para impedir campos com 180 px de altura em mobile.
- `frontend/src/app/users/users.component.scss`: cores legíveis para texto principal e erros no tema escuro.
- Testes: `npm test -- --watch=false --browsers=ChromeHeadless` — 92/92 SUCCESS, exit code 0.
- Build: `npm run build` — SUCCESS, exit code 0. Warnings de `baseline-browser-mapping`, Sass e budgets SCSS são preexistentes e fora do escopo.
- Detector Impeccable nos dois SCSS: `[]`, exit code 0.
- `git diff --check`: sem erros de whitespace.

## Bloqueio e próximo passo

A sessão autenticada permaneceu disponível durante a revisão; não foi feito logout para evitar interrompê-la. Erros/sucessos de mutações, outra role/Guild e dispositivo físico continuam como cenários não reproduzidos e estão registrados como limitações.

## Vereditos provisórios

- QA: APROVADO — matriz manual nos dois viewports; 92/92 testes e build SUCCESS.
- Security: NÃO ACIONADO — mudança exclusivamente SCSS, sem alteração de auth/RBAC/tenant.
- Task Verdict: APROVADO — scope restrito e validações verdes.
- Release Verdict: BLOQUEADO pelo achado preexistente RabbitMQ AMQP 406, sem investigação nesta task.
