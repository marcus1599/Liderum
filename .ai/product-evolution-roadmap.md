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

## Ciclo 2 — Integridade relacional do banco (NEXT — NÃO INICIADO)

**Objetivo:** mover invariantes já assumidas pela aplicação para constraints versionadas.

**Entregável:** nova migration Flyway, validada em H2 e PostgreSQL 18, sem alteração da V1.

Itens candidatos:

- `UNIQUE (member_id, event_id)` em Attendance;
- `users.guild_id NOT NULL` e `users.guild_role NOT NULL` após compatibilização do lifecycle;
- `events.name NOT NULL`, `events.date NOT NULL` e `attendance.status NOT NULL`;
- revisão de índices tenant-scoped com evidência de consultas reais.

## Ciclo 3 — Consolidação de UX operacional (NEXT)

**Objetivo:** remover atrito nos fluxos já existentes, sem redesign ou novo framework de estado.

**Necessário:** tornar `/users` facilmente acessível para roles autorizadas, remover/tornar coerentes toggles legados do dashboard e padronizar loading/erro/vazio nas áreas principais.

**Útil:** feedback explícito do drag-and-drop de Team e tornar claro o fluxo de escolha/alteração de líder.

**Adiado:** Settings persistente sem requisitos concretos; responsividade avançada e design system próprio.

## Ciclo 4 — Decisão de produto para notificações (LATER)

O RabbitMQ e o notification-service continuam como demonstração técnica confiável (**opção A**) enquanto não houver requisito real de comunicação por Guild. O evento atual não carrega contexto tenant suficiente e o webhook é global.

Se notificações se tornarem feature real (**opção B**), criar task própria para:

- transportar `guildId` como contexto de roteamento, nunca como autoridade;
- resolver configuração de notificação server-side por Guild;
- guardar webhook/credencial como segredo e nunca expô-lo ao frontend;
- comprovar isolamento tenant, retries, idempotência e ausência de vazamento cross-Guild.

## Priorização de capacidades

### NOW

- nenhuma task ativa; Ciclo 1 concluído.

### NEXT

- planejar e implementar constraints relacionais da base via migration incremental, depois do inventário do schema e dos dados;
- alteração autenticada de senha;
- desativação segura de Staff, preservando ao menos um MARECHAL ativo;
- consolidação de navegação e feedback HTTP.

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
