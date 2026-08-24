# Roadmap SaaS — Liderum

## Objetivo final

Levar o Liderum de MVP de gestão de guildas de RPG a um SaaS seguro, funcional e demonstrável para portfólio Pleno, com operação reproduzível e decisões técnicas compreensíveis.

## Definição de SaaS concluído

O projeto estará concluído para este objetivo quando uma pessoa puder criar uma Guild e seu administrador, autenticar-se, operar exclusivamente os dados do seu tenant, administrar membros, equipes, eventos e presença pela interface, receber notificações de eventos e executar a aplicação localmente ou em demonstração com saúde, testes e documentação verificáveis.

Não significa billing, escala corporativa ou infraestrutura complexa.

## Fases

### Fase 1 — Fundação segura de identidade e tenancy

**Objetivo:** fechar o lifecycle `Guild → primeiro administrador → autenticação → operação isolada`.

**Entregável:** onboarding seguro, senha armazenada com hash, limites de User por Guild, RBAC inicial e testes HTTP/de integração correspondentes.

**Dependências:** nenhuma. Esta é a fase atual.

### Fase 2 — Persistência versionada e contratos de API

**Objetivo:** tornar schema e contratos evolutivos e reproduzíveis.

**Entregável:** baseline Flyway, constraints relevantes, profiles coerentes e respostas de erro padronizadas.

**Dependências:** estabilização do modelo User/Guild da Fase 1.

### Fase 3 — Fluxo funcional de Guild no frontend

**Objetivo:** expor o lifecycle de Guild e os fluxos de domínio pela interface Angular.

**Entregável:** onboarding, gestão de usuários e rotas para membros, equipes, eventos, presença e configurações, com feedback de erro/loading e responsividade básica.

**Dependências:** contratos estáveis das Fases 1 e 2.

### Fase 4 — Cobertura dos fluxos críticos e isolamento completo

**Objetivo:** provar comportamentos de autenticação, RBAC, tenancy e interface.

**Entregável:** cobertura útil de API/integração para Event e Attendance multi-tenant, autorização por endpoint e fluxos frontend críticos.

**Dependências:** Fases 1 a 3.

### Fase 5 — Mensageria confiável e observável

**Objetivo:** tornar as notificações de eventos confiáveis e demonstráveis sem sobre-engenharia.

**Entregável:** contrato de evento com contexto de Guild, política proporcional de retry/DLQ/idempotência, testes com RabbitMQ e diagnóstico operacional.

**Dependências:** Fases 1 e 2; contrato de domínio estabilizado.

### Fase 6 — Operação, CI e apresentação de portfólio

**Objetivo:** permitir execução, avaliação técnica e demonstração rápida do projeto.

**Entregável:** Compose completo, CI dos componentes, health checks e logs proporcionais, guia de execução, diagrama e roteiro de demonstração multi-tenant/mensageria.

**Dependências:** Fases 3 a 5.

## Backlog priorizado

| Prioridade | Task | Tipo | Esforço | Resultado observável |
| --- | --- | --- | --- | --- |
| P0 | `secure-user-provisioning-and-guild-onboarding` — concluída em 2026-08-24 | Segurança / funcionalidade | STRUCTURAL | Guild e primeiro administrador seguros e isolados |
| P0 | `enforce-rbac-and-user-tenant-boundaries` | Segurança | LARGE | Matriz de permissões aplicada e testada |
| P0 | `remove-production-demo-bootstrap-and-fix-cors` | Segurança / infraestrutura | MEDIUM | Bootstrap apenas em dev e CORS configurável correto |
| P1 | `baseline-flyway-and-production-database-schema` | Infraestrutura / qualidade | STRUCTURAL | Schema PostgreSQL criado por migrations |
| P1 | `complete-guild-onboarding-and-user-management-ui` | Funcionalidade | LARGE | Lifecycle SaaS acessível no Angular |
| P1 | `route-domain-areas-and-add-http-ux-feedback` | Funcionalidade / qualidade | MEDIUM | Áreas do domínio roteadas com feedback consistente |
| P1 | `extend-tenant-integration-coverage-to-events-and-attendance` | Qualidade / segurança | MEDIUM | IDOR cross-Guild bloqueado nos quatro domínios |
| P2 | `make-event-notifications-reliable-and-testable` | Infraestrutura / funcionalidade | LARGE | Retry/DLQ/idempotência e testes RabbitMQ |
| P2 | `establish-fullstack-ci-and-compose-demo-environment` | Infraestrutura | LARGE | CI dos três componentes e demo local completa |
| P2 | `add-proportional-observability-and-portfolio-documentation` | Qualidade / portfólio | MEDIUM | Health, logs, diagrama e roteiro de demonstração |

## Itens explicitamente adiados

- billing, cobrança e planos pagos;
- associação de um usuário a múltiplas Guilds;
- Kubernetes, service mesh e tracing distribuído;
- RAG, banco vetorial e nova infraestrutura de IA;
- expansão de microserviços sem necessidade demonstrada.

## Critérios finais de conclusão

- lifecycle seguro de Guild e usuário administrável pela interface;
- isolamento multi-tenant e RBAC comprovados em testes de integração;
- domínio principal completo e usável no frontend;
- migrations, ambientes e Compose reproduzíveis;
- notificações confiáveis e observáveis de forma proporcional;
- CI cobrindo componentes relevantes;
- README, diagrama e roteiro de demonstração suficientes para avaliação de portfólio;
- nenhum bloqueador de segurança conhecido para release.
