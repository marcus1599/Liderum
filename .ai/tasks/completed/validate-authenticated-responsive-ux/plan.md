# Plan — Validação de UX autenticada em viewports reais

## Classificação e routing

- Prioridade P1, Ciclo 3 — UX operacional; porte SMALL/MEDIUM, qualidade.
- Domínios: `planning`, `frontend`, `testing`, `audit`; Security apenas se a revisão encontrar exposição de tenant/RBAC; SRE não selecionado porque não há mudança operacional.
- Agents: Planner → Frontend Dev consultivo para correções concretas → QA → Security condicional → Auditor.
- Skills: `create-plan`, `ux-review`, `test-frontend` somente se houver alteração de UI, `security-review` condicional e `audit-task`/`finish-task` nos gates finais.
- PRD não é necessário neste momento: a task é uma validação de qualidade com escopo visual e critérios verificáveis, sem regra de negócio nova.

## Objetivo

Obter evidência manual autenticada do shell e das áreas principais do Angular em desktop e viewport estreito, validar navegação, responsividade, acessibilidade básica e estados operacionais, e corrigir apenas defeitos concretos que estejam dentro do escopo do Ciclo 3. A task deve fechar a lacuna registrada nas últimas tarefas sem iniciar redesign.

## Escopo

- Usar uma sessão autenticada disponível e verificar `/dashboard`, `/members`, `/teams`, `/events`, `/attendance`, `/users` para roles autorizadas, `/account/password` e logout.
- Viewports mínimos: desktop 1280×800 e estreito 390×844; registrar navegador e data da observação.
- Verificar sidebar/shell, rota ativa, abertura/fechamento do drawer, overflow horizontal, cards/tabelas/forms, foco e navegação por teclado, labels de controles, loading, vazio, erro, sucesso e estados 401/403 quando reproduzíveis com segurança.
- Confirmar que ações visuais respeitam o role carregado de `/users/me`, que dados não mostram `guildId` como autoridade e que mensagens não vazam detalhes internos ou dados de outra Guild.
- Se houver defeito concreto pequeno e reproduzível, planejar e aplicar a menor correção nos componentes/SCSS/testes afetados, preservando Angular standalone, Material, RxJS e o shell existente.
- Registrar cada achado como corrigido, encaminhado para task própria ou limitação de ambiente. Uma preferência visual sem problema observável não justifica alteração.

## Fora de escopo

- Redesign, novo design system, biblioteca de UI, NgRx, E2E/Playwright, alteração de backend/JWT/RBAC/tenant, Settings persistente, responsividade avançada, correção do RabbitMQ AMQP 406, correção de warnings Sass ou mudança de infraestrutura.
- Não declarar conformidade WCAG completa; a revisão é acessibilidade básica e evidência operacional.

## Procedimento

1. Preparar ambiente e confirmar conta/role de teste sem registrar credenciais ou dados sensíveis.
2. Inspecionar cada viewport seguindo a mesma ordem de rotas; registrar screenshots apenas se forem necessárias para documentar um defeito, sem incluir tokens, senhas ou dados de outra Guild.
3. Exercitar navegação por mouse/toque e teclado: foco visível, Escape/Tab, botões e selects, drawer e retorno entre rotas.
4. Reproduzir estados de lista vazia e erro com dados seguros quando possível; distinguir falha de API de ausência de dados.
5. Corrigir somente defeitos comprovados dentro do escopo e adicionar testes unitários de componente quando a correção alterar comportamento.
6. Executar a suíte Angular e build se houver código alterado; se a task for apenas observacional, registrar que não houve alteração e preservar as evidências existentes.
7. QA → Security condicional → Auditor. `finish-task` somente após Task Verdict APROVADO.

## Critérios de conclusão

- Há uma matriz de observação para as rotas e dois viewports, com resultado e limitações explícitos.
- Sidebar, rota ativa, conteúdo e ações principais não apresentam overflow, controles inacessíveis ou regressões de navegação nos viewports testados.
- Foco e nomes acessíveis são suficientes para os controles principais; loading, vazio, sucesso e erro permanecem distinguíveis.
- Nenhum dado de outra Guild, `guildId` fornecido pelo cliente ou detalhe sensível aparece na interface observada.
- Todos os defeitos corrigidos têm teste proporcional e a suíte/build passam; achados fora de escopo têm task ou pendência registrada.
- Task Verdict e Release Verdict são separados. O RabbitMQ AMQP 406 continua registrado como bloqueador global preexistente até orientação específica.

## Riscos e dependências

- Depende de uma conta autenticada e de dados seguros para observar as rotas; sem acesso, a task fica limitada a inspeção local e não pode declarar validação manual.
- A UI pode parecer correta em ChromeHeadless e ainda falhar em viewport real; screenshots não substituem teste de comportamento.
- Nenhuma migration, dependência, ADR ou mudança de backend é esperada.

Status: IMPLEMENTAÇÃO CONCLUÍDA; matriz autenticada verificada em 1280×800 e 390×844. Dois defeitos SCSS concretos (altura de campos Eventos em mobile e contraste de texto em Usuários) foram corrigidos localmente. Testes Angular 92/92 e build SUCCESS; QA/Auditor ainda devem registrar os gates. A página observada em produção não foi atualizada nesta task.
