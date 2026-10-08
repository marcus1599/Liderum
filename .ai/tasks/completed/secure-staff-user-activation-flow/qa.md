# QA — secure-staff-user-activation-flow

## Evidências

- Backend direcionado: 13/13, sem failures/errors/skipped.
- Backend `clean verify`: 66/66, sem failures/errors/skipped, BUILD SUCCESS.
- Frontend: 35/35 SUCCESS em duas execuções; build de produção aprovado.
- Onboarding, login, users/me, RBAC, tenancy, PENDING/ACTIVE/DISABLED e ativação pública possuem cobertura executada.

## Bloqueadores

1. Não há evidência verificável de PostgreSQL real para V1→V2 em banco novo nem do upgrade com User existente; Docker Engine estava indisponível.
2. O endpoint autenticado de regeneração previsto no PRD/plan não foi encontrado, logo regeneração, revogação e replay por esse fluxo não estão demonstrados.

## Atualização final

- PostgreSQL 18.6 fresh: V1→V2 e Hibernate validate aprovados.
- Endpoint de regeneração implementado e UI disponível, mas sem suíte de cenários dedicada.
- Upgrade V1→V2 com User existente permanece sem evidência automatizada completa.

## Veredito

**BLOCKED** — falta o cenário de upgrade e a cobertura direcionada obrigatória da regeneração.

## Retomada de gates — 2026-10-07

### Evidências atuais

- `RbacUserTenantBoundariesIntegrationTest`: 14 testes executados, 0 failures, 0 errors, 0 skipped. Os novos cenários comprovam regeneração autorizada, hierarquia MARECHAL/GENERAL, bloqueio de roles inferiores, isolamento cross-Guild, estados ACTIVE/DISABLED, revogação do token anterior, funcionamento do novo token e rejeição de replay.
- `backend/.\mvnw.cmd clean verify`: 70 testes, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS`.
- `npm test -- --watch=false --browsers=ChromeHeadless`: 35/35 SUCCESS.
- `npm run build`: SUCCESS.
- `-DskipTests test-compile`: `BUILD SUCCESS`, incluindo o novo `PostgreSqlFlywayUpgradeCiIT`.
- O teste PostgreSQL automatizado foi adicionado ao job `postgres-flyway`, mas não foi executado nesta retomada. `docker version` e `docker info` confirmaram que o Docker Engine `desktop-linux` está indisponível; não há serviço PostgreSQL local na porta 5432.

### Avaliação

Os cenários de regeneração solicitados agora estão cobertos e passaram. O critério de upgrade V1 → User existente → V2 em PostgreSQL 18 permanece sem resultado empírico; compilação do teste e execução equivalente em H2 não substituem o PostgreSQL real.

## Veredito desta retomada

**REPROVADO PARA CONCLUSÃO / BLOQUEADO POR AMBIENTE** — não houve falha funcional observada nas suítes executadas, mas o gate de migração PostgreSQL exigido no PRD/plan não foi executado. A task permanece ativa.

## Veredito final — 2026-10-07

A pendência PostgreSQL foi resolvida nesta sessão. O Docker Engine respondeu com permissão elevada e os testes foram executados em PostgreSQL 18.6 descartável, sem volume, iniciando com o schema vazio.

- Execução combinada equivalente ao job `postgres-flyway`: `-Dtest=PostgreSqlFlywaySchemaCiIT,PostgreSqlFlywayUpgradeCiIT test` — 2 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`.
- O teste fresh aplicou V1/V2 e Hibernate `validate`; o teste de upgrade aplicou V1, inseriu Guild/User MARECHAL legado com senha BCrypt, aplicou V2, verificou preservação de ID/Guild/hash/status ACTIVE/FK/índice, iniciou contexto `prod` com Tomcat em porta efêmera e autenticou o User.
- O primeiro ensaio combinado falhou apenas porque o teste de upgrade tentava obter `AuthenticationConfiguration` com aplicação configurada como `WebApplicationType.NONE`. O teste foi ajustado para `SERVLET` e `server.port=0`; a execução combinada final passou. Nenhum código de produção foi alterado para contornar o ensaio.
- As 14 integrações RBAC/tenant, 70 testes backend totais e 35 testes frontend passaram; build Angular passou.

**QA: APROVADO.** O comportamento, as fronteiras tenant/RBAC, o fluxo de ativação/regeneração e a migration PostgreSQL fresh/upgrade têm evidências executadas. Warnings existentes de compatibilidade Flyway/PostgreSQL 18 e Sass não causaram falha e permanecem registrados como warnings/dívida técnica.

## Suplemento de QA final — 2026-10-07

- A revisão do PRD/plan constatou lacuna anterior para token inválido/expirado e consumo concorrente; esses cenários foram acrescentados sem remover assertions existentes.
- A execução direcionada final (`RbacUserTenantBoundariesIntegrationTest`, `UserActivationConcurrencyIntegrationTest`) passou: 16 testes, 0 failures/errors/skipped.
- A suíte backend limpa final passou: 72 testes, 0 failures/errors/skipped, `BUILD SUCCESS`, exit code 0.
- PostgreSQL 18.6 descartável: 2/2 testes (fresh e upgrade), Hibernate `validate`, inicialização `prod` e autenticação do usuário legado aprovados.
- Frontend permaneceu inalterado: evidência recente 35/35 em duas execuções e build de produção SUCCESS.
- Cenários de invalidez/expiração mantêm conta PENDING sem senha nem consumo do token; concorrência resulta em exatamente uma ativação bem-sucedida. Nenhum teste foi enfraquecido para obter aprovação.

**Veredito final de QA: APROVADO.**
