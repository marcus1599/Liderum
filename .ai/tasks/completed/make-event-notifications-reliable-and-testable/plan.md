# Plano — make-event-notifications-reliable-and-testable

## Routing

- Domínios: `planning`, `architecture`, `messaging`, `backend`, `testing`, `security`, `sre`, `audit`.
- Agents: Planner → Arquiteto → Backend Developer → QA → Security → SRE/DevOps → Auditor.
- Skills: `create-prd`, `create-plan`, `test-backend`, `security-review`, `audit-task`, `finish-task`.
- Não selecionados: Frontend Developer — nenhum fluxo Angular; `create-migration` — sem mudança de schema prevista; `create-adr` — somente se surgir decisão duradoura, mediante autorização.

## Classificação

P2, LARGE, infraestrutura/funcionalidade. Task da Fase 5 e próxima prioridade após as fundações, persistência, frontend e CI PostgreSQL.

## Objetivo e escopo

Evoluir o fluxo RabbitMQ de eventos de guild para entrega confiável e testável, com retry limitado, DLQ, idempotência e integração verificável, sem alterar contratos de autenticação, RBAC ou tenancy.

## Diagnóstico

- `GuildEventCreatedPublisher` publica de forma síncrona e apenas registra falha AMQP.
- O listener não explicita retry, DLQ ou política de erro.
- Dispatcher executa estratégias e captura falhas individualmente.
- Há testes unitários, mas não prova integrada com broker real.
- Compose possui RabbitMQ descartável para desenvolvimento local.

## Estratégia

1. Arquiteto define política mínima de retry/DLQ e chave de idempotência.
2. Backend implementa configuração producer/consumer e tratamento de falhas conforme decisão.
3. Testes usam broker descartável, sem dependências externas ou credenciais reais.
4. QA valida sucesso, retry, DLQ, redelivery e regressão.
5. Security verifica tenant context, replay e exposição de dados.
6. SRE valida operação local, readiness e diagnóstico.

## Testes planejados

- publicação e consumo bem-sucedidos;
- falha transitória com número limitado de tentativas;
- mensagem inválida na DLQ;
- redelivery sem duplicação de efeito;
- broker indisponível sem perda silenciosa não documentada;
- contexto Guild preservado;
- suíte `clean verify` e integração RabbitMQ descartável.

## Riscos e escalonamento

- decisão que altere semântica pública do evento ou exija nova infraestrutura deve ser escalada ao Arquiteto;
- não introduzir retry infinito, duplicação de notificações ou fallback inseguro;
- se a integração exigir dependência nova, revisar escopo antes de prosseguir;
- falha cross-tenant bloqueia a task e aciona Security.

## Dependências

RabbitMQ local/CI descartável e contratos atuais de evento. Nenhuma migration prevista. ADR não criado; avaliar somente se a política escolhida for estrutural e duradoura.

## Gates

Task Verdict e Release Verdict serão separados. QA, Security e SRE devem aprovar antes de Auditoria; `finish-task` somente com Task Verdict APROVADO.
