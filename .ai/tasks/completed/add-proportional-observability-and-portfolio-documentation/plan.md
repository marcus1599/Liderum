# Plano — add-proportional-observability-and-portfolio-documentation

## Routing

- Domínios: `planning`, `sre`, `backend`, `messaging`, `security`, `testing`, `audit`, `documentation`.
- Agents: Planner → SRE/DevOps + Backend Developer → QA → Security → Auditor.
- Skills: `create-plan`; na execução, `test-backend`, `security-review`, `audit-task` e `finish-task` após Task Verdict APROVADO.
- Não selecionados: Frontend Developer — não há mudança de fluxo Angular prevista; Arquiteto/`create-adr` — não há decisão estrutural duradoura proposta; `create-prd` — task técnica/documental, sem regra de negócio nova; `create-migration` — schema fora do escopo.

## Classificação

P2, MEDIUM, qualidade/infraestrutura/documentação. É a task final recomendada da Fase 6 após Compose, CI, mensageria e fluxos de produto já validados.

## Objetivo

Entregar observabilidade operacional mínima e documentação de portfólio verificável, sem expor dados sensíveis nem introduzir uma plataforma externa de monitoramento.

## Diagnóstico confirmado

- O backend inclui Actuator, mas `/actuator/health` está protegido pela regra geral de autenticação; o endpoint raiz devolve 403 sem credencial.
- PostgreSQL e RabbitMQ já possuem healthchecks no Compose. O notification-service expõe `GET /` para status, sem endpoint Actuator explicitamente documentado.
- Backend e notification-service usam logs SLF4J; o publisher registra sucesso/falha de publicação e o consumer registra processamento, retries/DLQ e duplicatas.
- O README já descreve componentes, Compose, CI e URLs, mas não contém diagrama de arquitetura de portfólio, healthcheck operacional consolidado ou roteiro de demonstração.

## Escopo

1. Avaliar e, se seguro, expor somente endpoints mínimos de disponibilidade para backend e notification-service, sem liberar métricas, env, beans ou dados sensíveis.
2. Configurar healthchecks do Compose para backend, notification-service e frontend apenas quando os endpoints atuais/propostos permitirem validação real de readiness.
3. Padronizar logs operacionais mínimos e seguros para startup, publicação/consumo RabbitMQ, retry/DLQ e falhas relevantes; nunca incluir Authorization, JWT, senha, e-mail, payload completo ou IP.
4. Atualizar README com health/status, troubleshooting breve, limites locais e comandos de diagnóstico do Compose.
5. Criar documentação de portfólio versionável com diagrama Mermaid da arquitetura e roteiro curto de demonstração: onboarding, login, isolamento tenant, evento e consumo assíncrono.
6. Adicionar testes somente para comportamento novo de health/status ou logging observável quando proporcional.

## Fora de escopo

- Prometheus, Grafana, OpenTelemetry, tracing distribuído, ELK, alertas externos e dashboards hospedados;
- mudanças de JWT, RBAC, tenancy, CORS, schema, Flyway ou contratos funcionais;
- deploy/CD, Render/Vercel, novos serviços, frontend funcional, E2E browser e redesign;
- correção ampla de warnings Sass ou atualização geral de dependências.

## Estratégia técnica

### Health e readiness

- Preferir `GET /actuator/health` do backend com exposição e autorização estritamente limitadas a esse endpoint; confirmar com Security que health público não revela detalhes (`show-details=never`).
- Avaliar usar Actuator também no notification-service, já que a dependência existe; manter `GET /` somente se não causar ambiguidade, ou documentá-lo como status simples.
- Usar healthchecks HTTP no Compose somente depois de confirmar que os endpoints retornam 200 em processo pronto. PostgreSQL e RabbitMQ permanecem com seus checks nativos.
- Não usar endpoint autenticado ou raiz 403 como healthcheck.

### Logs

- Preservar os logs de evento já existentes e restringir novos campos a identificadores técnicos não sensíveis, como `eventId`, canal, tentativa e resultado.
- Não introduzir formato JSON/stack centralizado sem necessidade real.
- Registrar falhas de dependência com contexto técnico suficiente e sem payloads ou credenciais.

### Portfólio

- Criar um documento em `.ai/docs/` ou `docs/` conforme o padrão mais adequado encontrado na execução, com Mermaid e roteiro reproduzível baseado no Compose.
- O README deve referenciar o material sem duplicar o conteúdo inteiro.

## Testes e validações planejados

| Área | Evidência |
| --- | --- |
| Backend health | teste/contexto ou HTTP para `/actuator/health`, confirmando 200 e ausência de detalhes sensíveis. |
| Notification health | validação HTTP do endpoint escolhido, incluindo startup no Compose. |
| Compose | `docker compose config` e `docker compose up -d --build`; todos os healthchecks ficam saudáveis. |
| Mensageria | smoke de criação de evento e confirmação do consumo no notification-service. |
| Segurança | request sem credencial não acessa APIs de negócio, métricas ou endpoints sensíveis; logs revisados contra segredos/dados pessoais. |
| Regressão | `mvnw.cmd clean verify`, testes do notification-service e build/teste frontend apenas se arquivos correspondentes forem tocados. |
| Documentação | comandos, portas, diagrama e roteiro revisados contra o ambiente real. |

## Riscos e escalonamento

- Exposição de health revelar detalhes, dependências ou configuração: manter detalhes ocultos; se não for possível, parar e escalar Security.
- Healthcheck criar dependência circular ou falsa prontidão: usar somente endpoints que representem processo pronto e dependências essenciais.
- Necessidade de monitoramento externo ou credenciais: fora do escopo; registrar recomendação futura.
- Log novo puder incluir conteúdo sensível: parar a alteração e encaminhar para Security.

## Critérios de conclusão

- Disponibilidade dos componentes é verificável localmente por healthchecks proporcionais.
- Nenhum endpoint operacional novo expõe detalhes, métricas ou segredos desnecessários.
- Logs de mensageria permitem diagnosticar publicação, consumo, retry/DLQ e duplicata sem dados sensíveis.
- README e documentação de portfólio descrevem arquitetura, execução, troubleshooting e demonstração reproduzível.
- QA, Security, SRE/DevOps e Auditor aprovam; Task Verdict e Release Verdict permanecem separados.

## Autorizações necessárias antes da execução

Autorização para alterar configuração Spring/Compose, testes e documentação relacionados a observabilidade proporcional. Nenhuma alteração de produção externa, nova dependência ou plataforma de monitoramento está prevista.
