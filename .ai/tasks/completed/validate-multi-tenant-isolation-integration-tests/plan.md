# Plano — Validar isolamento multi-tenant por testes de integração

**Classificação:** MEDIUM — qualidade e segurança

## Objetivo

Comprovar, com integração real entre Spring, persistência H2, `SecurityContext` e `TenantService`, que dados vinculados a uma `Guild` não podem ser lidos, alterados ou removidos por usuário autenticado de outra guilda.

## Contexto e evidência

- `Guild` é o tenant do Liderum e `TenantService` resolve a guilda a partir do usuário autenticado.
- Services e repositories usam consultas filtradas por guilda.
- Os testes atuais de Attendance, Event, Member e Team usam mocks de `TenantService`; eles validam o contrato local dos services, não a fronteira real de isolamento.
- Multi-tenancy é um risco técnico explícito no `state.md`.

## Escopo

- Criar testes de integração backend com dados persistidos de, no mínimo, duas guildas e usuários autenticados distintos.
- Exercitar o fluxo real de resolução de tenant por `TenantService`.
- Validar, para os domínios cobertos, que listagem retorna somente dados da guilda atual e que acesso/mutação de identificador pertencente a outra guilda falha sem alterar o dado original.
- Cobrir referências cross-tenant relevantes em operações de criação, quando o service aceita IDs de entidades pertencentes à guilda.
- Registrar comandos, quantidade de testes e resultado final da suíte Maven.

## Fora do escopo

- Alterar implementação de produção de `TenantService`, services, repositories, controllers ou JWT.
- Alterar schema, migrations Flyway, contratos de API, frontend, RabbitMQ ou notification-service.
- Corrigir achados adicionais de autorização; eles devem ser registrados e escalados separadamente se não impedirem os testes.
- Criar PRD, ADR ou dependências novas.

## Routing

- Domínios: `planning`, `backend`, `testing`, `security`, `audit`.
- Agents: Planner → Backend Dev → QA + Security → Auditor.
- Skills: `create-plan` → `test-backend` + `security-review` → `audit-task` → `finish-task`, somente se aprovado.
- Não selecionados: Frontend Dev (nenhum arquivo frontend); Arquiteto (sem decisão estrutural); SRE/DevOps (sem alteração operacional); `create-migration` (sem schema); `create-prd` (requisito técnico delimitado); `create-adr` (sem decisão duradoura nova).

## Estratégia de implementação

1. Inspecionar fixtures, entidades e services para selecionar os domínios que aceitam identificadores tenant-scoped.
2. Criar fixtures isoladas de Guild A e Guild B, cada uma com usuário autenticável e dados de domínio próprios.
3. Executar operações pelo service real sob um `SecurityContext` de cada usuário, permitindo que `TenantService` consulte o repositório real.
4. Verificar listagem, busca por ID, atualização, exclusão e referências cross-tenant aplicáveis, sem mocks de `TenantService`.
5. Manter dados de teste isolados e reprodutíveis, sem depender de ambiente local, `.env` ou JWT real da máquina.

## Cenários mínimos de aceite

- Usuário da Guild A lista somente dados da Guild A.
- Usuário da Guild A não encontra recurso identificado da Guild B.
- Usuário da Guild A não atualiza nem exclui recurso da Guild B; o estado persistido da Guild B permanece inalterado.
- Operação que referencia entidade de outra guilda é rejeitada quando aplicável.
- Usuário sem guilda ou não autenticado mantém o comportamento de falha esperado de `TenantService`.
- `./mvnw.cmd clean verify` termina com resultado final verificável e sem dependência de segredo local.

## Riscos e escalonamento

- Se os testes revelarem acesso cross-tenant confirmado, interromper a implementação e escalar imediatamente para Security e sessão principal; não corrigir produção nesta task sem autorização.
- Se for necessário modificar schema, contrato HTTP, JWT ou arquitetura de tenant para testar, escalar ao Arquiteto/sessão principal.
- Falhas preexistentes fora do escopo devem ser registradas, não mascaradas por alteração de teste.

## Critérios de conclusão

- Testes de integração adicionados sem alteração de produção ou schema.
- QA aprova cenários de comportamento e evidência Maven.
- Security revisa explicitamente a superfície tenant/IDOR.
- Auditoria confirma escopo limitado a testes e documentação da task.
