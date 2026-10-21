# Liderum — Product Evolution Roadmap

> Roadmap pós-MVP. As seis fases do `roadmap.md` original estão concluídas e permanecem como histórico canônico; este documento não as reabre.

## Objetivo

Evoluir o Liderum de um SaaS demonstrável de portfólio para um produto que uma Guild real possa operar com identidades individuais, integridade de dados, UX administrativa clara e evolução proporcional à demanda real.

## Princípios permanentes

- uma pessoa usa um `User` próprio, uma credencial própria e uma role explícita; contas compartilhadas de Staff não são aceitas;
- `Guild` é o tenant, `User` é a identidade autenticável e `Member` é o personagem/membro operacional;
- `User` pertence a uma única `Guild`; o tenant é resolvido server-side via `TenantService`, conforme ADR-001;
- `guildId` não é fonte de autoridade no JWT ou no frontend;
- `User` e `Member` não serão fundidos e não haverá associação User ↔ Guild N:N nesta etapa;
- migrations futuras serão incrementais; a V1 não será editada;
- a solução mais simples que preserve segurança, isolamento e operabilidade deve prevalecer.

## Ciclo 1 — Identidade individual e ativação segura (CONCLUÍDO)

**Objetivo:** permitir que MARECHAL/GENERAL provisionem contas sem conhecer a senha definitiva do novo usuário.

**Entregável:** usuário criado como `PENDING`, link de ativação copiável pelo administrador, token aleatório de uso único armazenado somente como hash, definição de senha pelo próprio usuário e transição para `ACTIVE`.

Task concluída: `secure-staff-user-activation-flow` (Task Verdict APROVADO; QA, Security, SRE/DevOps e Auditor aprovados em 2026-10-07).

Decisões de produto:

- onboarding Guild + primeiro MARECHAL continua criando uma conta `ACTIVE` com senha definida pelo próprio fundador;
- usuários subsequentes são provisionados sem senha pelo administrador;
- primeira versão não envia e-mail: o administrador copia o link e o transmite externamente;
- validade inicial do link: 24 horas;
- regenerar um link invalida tokens anteriores ainda válidos;
- autenticação só é permitida para usuário `ACTIVE`;
- o link/token nunca contém `guildId`, role ou autoridade do cliente.

## Ciclo 2 — Integridade relacional do banco (CONCLUÍDO)

**Objetivo:** mover invariantes já assumidas pela aplicação para constraints versionadas.

**Entregável:** nova migration Flyway, validada em H2 e PostgreSQL 18, sem alteração da V1.

Task `enforce-relational-database-integrity`: V3 implementada e validada em H2 e PostgreSQL 18.6 fresh/upgrade, com Hibernate `validate` e suíte backend verde. Em 2026-10-13 a V3 foi aplicada em Render via Flyway; history V1/V2/V3, constraints, serviço live, health (200), CORS permitido/negado e smoke autenticado tenant-scoped foram verificados. A task `rollout-flyway-v3-to-render-production` foi aprovada pelos gates QA, Security, SRE/DevOps e Auditor; Task Verdict e Release Verdict APROVADOS. O status HTTP autenticado direto não ficou disponível em request logs, mas o login/guard completou `/users/me` e a UI de `/members` exibiu o estado vazio normal.

Itens candidatos:

- `UNIQUE (member_id, event_id)` em Attendance;
- `users.guild_id NOT NULL` e `users.guild_role NOT NULL` após compatibilização do lifecycle;
- `events.name NOT NULL`, `events.date NOT NULL` e `attendance.status NOT NULL`;
- revisão de índices tenant-scoped com evidência de consultas reais.

## Ciclo 3 — Consolidação de UX operacional (EM ANDAMENTO)

**Objetivo:** remover atrito nos fluxos já existentes, sem redesign ou novo framework de estado.

**Necessário:** tornar `/users` facilmente acessível para roles autorizadas, remover/tornar coerentes toggles legados do dashboard e padronizar loading/erro/vazio nas áreas principais.

**Útil:** feedback explícito do drag-and-drop de Team e tornar claro o fluxo de escolha/alteração de líder.

**Adiado:** Settings persistente sem requisitos concretos; responsividade avançada e design system próprio.

Primeira task concluída: `consolidate-authenticated-navigation-and-expose-users` (Task Verdict e Release Verdict APROVADOS). Dashboard e páginas de domínio compartilham o shell autenticado; o menu Users é exibido conforme perfil MARECHAL/GENERAL.
Segunda task concluída: `standardize-domain-http-feedback-states` (Task Verdict e Release Verdict APROVADOS, 2026-10-13). Members, Teams, Events e Attendance distinguem loading, vazio, erro, sucesso e falha de recarga após mutação. Feedback do drag-and-drop de Team foi incluído. Clareza da escolha/alteração de líder continua candidata separada; Ciclo 3 permanece em andamento.
Task concluída: `safely-deactivate-and-reactivate-staff-users` (2026-10-17). Desativação/reativação segura de Staff, revogação de sessão, proteção do último MARECHAL ACTIVE e bloqueio de 409 para líder de Team foram aprovados por QA, Security e Auditor. Release global permanece bloqueado por achado RabbitMQ preexistente, cuja investigação está adiada pelo usuário.

Task concluída: `reassign-team-leader-before-staff-deactivation` (2026-10-19). A liderança de Team pode ser transferida explicitamente para User ACTIVE da mesma Guild por MARECHAL, GENERAL ou MAJOR, com candidatos mínimos, UX de confirmação e validação tenant-scoped. QA, Security e Auditor aprovaram; suíte backend (92), Angular (79/79) e build passaram. O Ciclo 3 permanece em andamento; a próxima prioridade deve ser reavaliada sem reabrir tarefas concluídas.

Task concluída: `enable-accessible-team-member-assignment` (2026-10-19). A composição de Teams tem alternativa por teclado/toque para adicionar Members, além do drag-and-drop, sem mudança de API/RBAC/backend. QA, Security e Auditor aprovaram; Angular 92/92 em duas execuções e build SUCCESS. A inspeção visual autenticada desktop/viewport estreito segue sem evidência. Ciclo 3 permanece em andamento enquanto as lacunas restantes são reavaliadas.

## Ciclo 4 — Decisão de produto para notificações (LATER)

O RabbitMQ e o notification-service continuam como demonstração técnica confiável (**opção A**) enquanto não houver requisito real de comunicação por Guild. O evento atual não carrega contexto tenant suficiente e o webhook é global.

Se notificações se tornarem feature real (**opção B**), criar task própria para:

- transportar `guildId` como contexto de roteamento, nunca como autoridade;
- resolver configuração de notificação server-side por Guild;
- guardar webhook/credencial como segredo e nunca expô-lo ao frontend;
- comprovar isolamento tenant, retries, idempotência e ausência de vazamento cross-Guild.

## Priorização de capacidades

### NOW

- `align-domain-action-visibility-with-rbac` concluída em 2026-10-19: as ações visuais de Members, Teams, Events e Attendance acompanham as permissões existentes, preservando consulta para roles de leitura. QA, Security e Auditor aprovados; Angular 86/86 e build SUCCESS.
- `enable-accessible-team-member-assignment` concluída em 2026-10-19: Members disponíveis podem ser atribuídos a Teams também por seletores acessíveis e botão, preservando o drag-and-drop. QA/Security/Auditor aprovados; Angular 92/92 em duas execuções e build SUCCESS. Inspeção visual autenticada permanece como limitação registrada.
- `validate-authenticated-responsive-ux` concluída (P1): sete rotas autenticadas revisadas em 1280×800 e 390×844; corrigidos flex-basis excessivo dos campos Events mobile e contraste da página Users. Angular 92/92, build SUCCESS, QA/Auditor aprovados. Sem deploy; Release permanece bloqueado pelo RabbitMQ AMQP 406.

### NEXT

- `make-staff-activation-link-handoff-reliable`, `fix-attendance-event-selection-placeholder`, `stabilize-user-management-operational-states`, `make-dashboard-data-states-trustworthy` e `confirm-destructive-domain-actions-consistently` concluídas com QA, Security e Auditor aprovados. Gate final `validate-cycle-3-operational-flows` aprovado; Angular 103/103 e build SUCCESS. Ciclo 3 encerrado no nível automatizado local;

### LATER

- recuperação de senha e confirmação/envio automático por e-mail;
- trilha de auditoria para ações administrativas sensíveis;
- configuração da Guild e timezone;
- transferência explícita de responsabilidade/ownership;
- associação opcional `User` → `Member` quando existir fluxo de “meu personagem”;
- cancelamento de evento;
- exportações e relatórios orientados por necessidade real;
- notificações tenant-aware, somente após decisão de produto.

### NOT NEEDED agora

- Kafka, Kubernetes, service mesh, CQRS ou event sourcing;
- banco por tenant, microserviço de autenticação/usuário ou Redis sem necessidade operacional;
- permissões dinâmicas, User ↔ Guild N:N, NgRx, billing e recorrência genérica;
- motor geral de notificações.

## Modelo conceitual

- `User.guildRole` representa autorização no produto. `SOLDADO` permanece como papel autenticado de menor privilégio por compatibilidade, não como prova de vínculo com um personagem.
- `Member.guildRole` descreve a posição operacional do membro/personagem. O uso do mesmo enum nos dois contextos é ambíguo, mas separá-lo agora seria refactor sem requisito comprovado; revisar quando as taxonomias divergirem de fato.
- `User.member` continua opcional. Vincular conta e personagem não é necessário para ativação e fica adiado até haver uma experiência concreta de perfil próprio.

## Critérios de evolução concluída

- cada Staff utiliza conta própria e define sua própria senha;
- contas pendentes, ativas e desativadas têm comportamento seguro e verificável;
- invariantes críticas existem também no banco;
- fluxos administrativos reais são encontráveis e dão feedback coerente;
- novas capacidades preservam ADR-001 e isolamento tenant;
- nenhuma infraestrutura é adicionada sem problema operacional comprovado.
