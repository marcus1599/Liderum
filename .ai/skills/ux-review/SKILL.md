---
name: ux-review
description: Revise UX de telas e fluxos Angular do Liderum com foco em consistência visual, navegação, feedback, acessibilidade básica, responsividade, RBAC e isolamento tenant; proponha a menor mudança coerente e não substitua test-frontend.
---
# UX Review — Liderum

**Consumidores:** Frontend Developer, QA e Planner quando houver interface ou fluxo de usuário.
**Objetivo:** identificar problemas concretos de experiência no frontend Angular do Liderum e orientar soluções coerentes com o produto existente, sem criar um design system paralelo nem ampliar o escopo.

## Contexto do frontend

Confirme os padrões atuais no código antes de recomendar mudanças. O frontend usa Angular 19, componentes standalone, Angular Material, RxJS, rotas explícitas, auth guard, serviços HTTP e Jasmine/Karma. Estilos são em geral SCSS por componente, com estilos globais e componentes compartilhados de layout/navegação. Esses fatos podem mudar: confirme-os na branch atual.

Reutilize componentes, tokens, padrões de formulário, feedback e navegação que já existam. Prefira Angular Material e os componentes compartilhados do projeto a HTML/CSS genérico quando houver equivalente adequado. Não imponha uniformidade artificial onde as telas têm necessidades diferentes.

## Fluxo obrigatório

1. Identifique a tela, rota e fluxo de usuário afetados.
2. Inspecione os componentes, templates, SCSS, componentes compartilhados, serviços, rotas/guards e testes relevantes.
3. Descreva o problema concreto de UX com evidência observável; se não houver problema demonstrado, não invente redesign.
4. Identifique padrões existentes no Liderum que se aplicam e diferenças justificáveis entre telas.
5. Proponha a menor alteração coerente, delimitando comportamento mantido, arquivos prováveis, estados afetados, riscos e fora de escopo.
6. Implemente somente depois que o escopo da task estiver aprovado; esta skill, por si só, não autoriza implementação.
7. Valide loading, success, empty e error states quando aplicáveis; diferencie falha de API de ausência real de dados.
8. Verifique responsividade nos tamanhos relevantes e acessibilidade básica.
9. Execute os testes apropriados e encaminhe práticas de teste para `test-frontend`.
10. Registre evidências, limitações, decisões e itens adiados; não declare validação visual que não foi observada.

## Critérios de revisão

### Consistência e arquitetura

- Preserve a identidade visual observável do Liderum: fundo escuro, acento lilás, tipografia/spacing existentes, cards, botões e padrões Material quando aplicáveis. Consulte o tema e a tela equivalente; não codifique esses exemplos como tokens globais novos.
- Mantenha responsabilidades nos componentes, serviços, rotas, guards e estado RxJS existentes; não introduza store global, nova biblioteca, abstração visual ou design system sem requisito aprovado.
- Preserve labels, termos de domínio e padrões já usados pelo produto, salvo se a task aprovar explicitamente uma mudança de conteúdo.

### Navegação, autorização e tenant

- Verifique destino, retorno, estado ativo, comportamento de voltar e acesso direto à rota.
- Elementos de navegação e ações devem refletir o RBAC conhecido; ocultar uma ação na UI é conveniência, não substitui autorização no backend.
- Não use `guildId` do cliente para escolher tenant, autorizar navegação ou filtrar dados como fonte de autoridade. O tenant é resolvido server-side conforme ADR-001.
- Não revele nomes, contagens, estado de existência ou qualquer dado de outra Guild em mensagens, listas, links, autocomplete ou estados de erro.

### Feedback e conteúdo

- Loading deve ser perceptível e não deixar a tela aparentar vazia ou travada; estados success devem confirmar a ação sem duplicar feedback.
- Empty state só deve aparecer após uma resposta bem-sucedida sem resultados, nunca como substituto silencioso de falha HTTP.
- Diferencie 401, 403 e 404 quando o contrato e a informação forem seguros e úteis. Para erros desconhecidos, use mensagem acionável e segura; não mostre stack trace, payload bruto, SQL, detalhes internos ou conteúdo sensível.
- Não transforme todo erro em mensagem genérica se a aplicação já consegue apresentar informação segura e específica, por exemplo falta de permissão ou recurso indisponível.
- Formulários devem indicar campos inválidos, preservar dados quando seguro e evitar submissões repetidas; não invente validação ou campo divergente do contrato backend.

### Acessibilidade e responsividade

- Controles devem ter nome acessível, foco visível e uso por teclado; ícones acionáveis precisam de label/tooltip acessível.
- Associe labels e mensagens de validação aos controles quando aplicável; feedback assíncrono importante deve ser perceptível por tecnologia assistiva (por exemplo `role="alert"`/`status` quando apropriado ao padrão existente).
- Preserve contraste legível, ordem de foco e semântica HTML; não use cor ou ícone como único indicador.
- Confira os breakpoints/padrões SCSS já usados, evitando overflow horizontal, controles inacessíveis ou mudanças que quebrem sidebar/layout em viewport menor.
- Não declare conformidade WCAG completa a partir desta revisão básica.

## Limites

- Não refatore telas ou estilos fora da área necessária à task.
- Não altere contrato/backend, autenticação, RBAC ou tenancy para resolver apenas uma preferência visual; reporte incompatibilidades e escale.
- Não remova informação útil nem assertion de teste para simplificar a interface.
- Não use screenshot/mockup como prova de comportamento de API, autorização ou estado real.
- Não introduza E2E ou dependências de UI; validação automatizada permanece sob `test-frontend` e o plano aprovado.

## Saída

Reporte:

- tela/fluxo e problema observado;
- padrões existentes consultados;
- proposta mínima e alternativas descartadas;
- impactos em navegação, RBAC/tenant e feedback;
- acessibilidade/responsividade revisadas;
- testes ou validações necessários (delegados a `test-frontend`);
- evidências, limitações e itens fora de escopo;
- status `PRONTO PARA PLANEJAR/EXECUTAR` ou `BLOQUEADO`.
