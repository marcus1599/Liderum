# Plano — remove-production-demo-bootstrap-and-fix-cors

## Routing

- Domínios: `planning`, `backend`, `security`, `sre`, `testing`, `documentation`.
- Agents: Planner → Arquiteto (consultivo) → Backend Developer → QA → Security + SRE/DevOps → Auditor.
- Skills: `create-prd`, `create-plan`; na execução, `test-backend`, `security-review`, `audit-task` e `finish-task` se aprovado.
- Não selecionados: Frontend Developer — não há dependência real de alteração Angular; Database — não há schema/migration; `create-adr` — decisão de profile/configuração é local, reversível e não altera a arquitetura de tenancy/JWT.
- Gate: QA e Security devem aprovar antes de auditoria; Task Verdict e Release Verdict permanecem distintos.

## Classificação

P0, MEDIUM, segurança/infraestrutura. A task remove dois bloqueadores de release preexistentes relacionados à configuração operacional da aplicação.

## Decisões de planejamento

1. Usar `@Profile("dev")` para o `DataInitializer`; um feature flag adicional não é necessário, pois o único caso de uso aprovado é desenvolvimento explícito.
2. Remover a ativação implícita de `dev` em `application.properties`. Sem `SPRING_PROFILES_ACTIVE`, a aplicação não deve semear dados demo.
3. Manter Docker Compose explicitamente em `dev`, pois ele é ambiente de demonstração local; documentar que não é configuração de produção.
4. Configurar CORS com lista de origins individuais a partir de `CORS_ALLOWED_ORIGINS`, com valor dev explícito para `http://localhost:4200` e sem fallback permissivo em produção.
5. Quando a lista estiver vazia, não conceder headers CORS cross-origin. Same-origin não depende de CORS.
6. Integrar a configuração com Spring Security e remover/corrigir apenas o matcher de URL absoluta que tentava representar CORS. Não alterar permissões de autenticação.

## Etapas de execução

1. Backend Developer: restringir o bean de bootstrap ao profile `dev` e retirar `dev` implícito da configuração base.
2. Backend Developer: tornar as origins CORS uma lista configurável segura, aplicar o comportamento de lista vazia e assegurar preflight via SecurityConfig.
3. Backend Developer: ajustar Compose, `.env.example` e README somente para refletir as variáveis/profiles realmente utilizados.
4. Backend Developer: adicionar testes de contexto/profile e MockMvc CORS sem depender de serviços externos ou de estado de máquina.
5. Executar testes direcionados e `./mvnw.cmd clean verify`.
6. QA: verificar que os testes atravessam o contexto Spring real, que `dev` não foi reativado implicitamente e que CORS não produz falso positivo.
7. Security: classificar os achados preexistentes, confirmar ausência de wildcard/credenciais indevidas e preservar JWT/onboarding.
8. SRE/DevOps: revisar variáveis, profile no Compose e comportamento seguro em produção.
9. Auditor: comparar diff com este plano, confirmar ausência de Flyway/antiabuso/frontend e emitir Task/Release Verdict.

## Testes planejados

| Área | Cenário |
| --- | --- |
| Bootstrap | Sem profile `dev`, banco vazio permanece sem Guild/User/Member/Event demo. |
| Bootstrap | Profile `prod`, banco vazio permanece sem seeds. |
| Bootstrap | Profile `dev`, banco vazio recebe dados demo somente para desenvolvimento. |
| CORS | Origin configurada recebe resposta/preflight permitida. |
| CORS | Origin não configurada não recebe autorização CORS. |
| CORS | Duas origins configuradas por vírgula são reconhecidas individualmente. |
| CORS | Lista vazia em profile de produção não libera origin externa. |
| Regressão | Login/onboarding e demais suíte backend continuam aprovados. |

## Riscos e controles

- Desenvolvimento local pode deixar de ter H2/seeds automáticos: usar profile `dev` explicitamente e atualizar instrução apenas se necessário.
- Testes podem assumir seeds: cada teste deve declarar seus dados, não reintroduzir bootstrap fora de `dev`.
- A configuração CORS deve usar valores literais individuais, sem wildcard, e validar comportamento por MockMvc.

## Critérios de conclusão

- critérios do PRD satisfeitos e evidenciados por testes;
- `git diff --check` limpo;
- QA e Security com Task Verdict APROVADO;
- Release Verdict atualizado: bootstrap/CORS removido da lista de bloqueadores somente se ambos os riscos forem comprovadamente mitigados; Flyway e antiabuso permanecem independentes.
