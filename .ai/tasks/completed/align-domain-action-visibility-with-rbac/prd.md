# PRD — Alinhar ações das áreas de domínio ao RBAC

## Objetivo e contexto

No Ciclo 3, tornar a interface de Members, Teams, Events e Attendance coerente com a autorização já existente. Hoje os templates exibem formulários, botões de mutação e controles de presença para qualquer usuário autenticado; o backend restringe essas mutações a MARECHAL, GENERAL e MAJOR. Em Teams, apenas a troca de líder já consulta a role do perfil. A sidebar também oferece Members para todas as roles.

## Requisitos

1. Derivar a capacidade visual de administrar domínio de `AuthService.profile$`, alimentado por `GET /users/me`; não usar JWT decodificado, `guildId` do cliente ou role persistida separadamente.
2. Para MARECHAL, GENERAL e MAJOR, preservar os controles de criação, edição, exclusão, associação de Team, transferência de líder e registro de Attendance já existentes.
3. Para CAPITÃO e SOLDADO, apresentar Teams, Events e Attendance em modo de consulta, sem controles de mutação. Em Attendance, manter visualização dos dados já permitidos, sem radios acionáveis.
4. Em Members, preservar a consulta se o contrato real continuar permitindo-a; ocultar mutações para CAPITÃO/SOLDADO. Alinhar o link da sidebar à política efetiva do endpoint, sem supor que a regra `/api/members/**` cubra `/members/**`.
5. Não renderizar controles administrativos enquanto o perfil ainda não tiver sido carregado. Acesso direto às rotas deve respeitar a mesma apresentação.
6. Preservar loading, success, empty, error e tratamento de 401/403 já implementados. Um erro 403 não deve aparecer como lista vazia nem expor detalhe interno.
7. A UI apenas reflete permissões; o backend permanece como autoridade e suas regras não são alteradas nesta task.

## Escopo

- Templates/componentes de Members, Teams, Events e Attendance, e sidebar apenas se a inspeção do contrato exigir.
- Verificações locais nas ações de mutação para evitar disparo por invocação do componente enquanto a role não autoriza.
- Specs Jasmine/Karma proporcionais para papéis administrativos e de leitura, carregamento de perfil e regressões de feedback.

## Fora de escopo

- Alterar SecurityConfig, controllers, RBAC de backend, tenant, JWT ou API.
- Reestruturar rotas/guards, acrescentar role-guard global, novo store ou biblioteca.
- Redesign, design system, Settings, novas funcionalidades e diagnóstico do RabbitMQ AMQP 406 adiado.

## Critérios de aceite

- Perfis MARECHAL/GENERAL/MAJOR continuam executando fluxos administrativos existentes em cada área.
- CAPITÃO/SOLDADO não veem nem conseguem acionar pela UI as mutações dessas quatro áreas; continuam lendo dados autorizados.
- Estado de perfil ainda não resolvido não mostra ações administrativas.
- Não há regressão de feedback, navegação autenticada, Users nem troca de líder.
- `npm test -- --watch=false --browsers=ChromeHeadless` e `npm run build` terminam com sucesso verificável.
- QA valida a matriz visual; Security confirma que não há nova fonte de autoridade ou exposição cross-Guild; Auditor confirma escopo. Task Verdict e Release Verdict são independentes.

## Dependências e riscos

- Sem dependência nova, migration ou ADR. O contrato dos controllers é a referência: mutações de Members/Teams/Events/Attendance têm `@PreAuthorize` para MARECHAL/GENERAL/MAJOR; leituras de domínio permanecem autenticadas.
- Risco de esconder consulta junto com mutação, sobretudo em Teams/Attendance; exigir cenários de leitura por CAPITÃO/SOLDADO.
- Risco de divergência futura entre UI e backend; testes da matriz devem tornar o desvio observável, mas não substituem testes de autorização no backend.
- Release global segue BLOQUEADO pelo achado RabbitMQ AMQP 406 preexistente e adiado, independentemente do veredito desta task.
