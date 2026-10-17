# Execução — safely-deactivate-and-reactivate-staff-users

## Decisão de escopo

- O usuário confirmou: bloquear desativação de User que lidera Team até que a liderança seja reassociada.
- Não há reassociação automática nem alteração da liderança nesta task; a resposta de conflito é 409 e não deve causar mutação parcial.
- A ocorrência preexistente de RabbitMQ permanece adiada e fora do escopo.

## Implementação atual

- Criado `PUT /users/{id}/status` com DTO explícito contendo somente `status`.
- Transições implementadas: `ACTIVE ↔ DISABLED`; `PENDING`, status repetido e reativação sem hash de senha são rejeitados.
- Operações tenant-scoped via usuário autenticado/TenantService; autoalteração e hierarquia são recusadas.
- Desativação incrementa `credentialVersion`; reativação mantém o hash existente e requer novo login para obter token válido.
- Proteção do último MARECHAL foi ajustada para contar somente usuários ACTIVE e serializar mudanças de role, exclusão e status bloqueando os MARECHAL da Guild.
- Usuário que lidera Team recebe conflito antes de alteração de estado.
- UI de `/users` recebeu ações de desativar/reativar com confirmação, bloqueio durante envio e feedback.

## Evidências de teste

- Backend direcionado: `RbacUserTenantBoundariesIntegrationTest` + `UserServiceImplTest`: 25 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS. A integração cobriu também duas desativações simultâneas concorrendo pela preservação do último MARECHAL ativo.
- Backend completo: `cd backend; .\\mvnw.cmd clean verify`: 86 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit code 0 (2026-10-17). A execução validou o estado final dos fontes e fixtures, incluindo usernames UUID para isolamento/repetibilidade do cenário concorrente.
- Frontend completo: `npm test -- --watch=false --browsers=ChromeHeadless`: 73/73 SUCCESS; exit code 0. O novo teste do conflito 409 observa a instância de Snackbar realmente injetada pelo componente.
- Frontend build: `npm run build`: SUCCESS; exit code 0. Permanecem warnings não bloqueantes de Sass, import Material não utilizado, budgets e `baseline-browser-mapping`; não houve alteração relacionada nesta task para corrigi-los.
- A tentativa inicial dos testes frontend encontrou EPERM de cache no sandbox e uma primeira expectativa de Snackbar ligada à instância errada; ambas foram resolvidas sem alterar comportamento de produção para facilitar teste. A execução completa final passou.

## Gates

- QA: APROVADO; veja `qa.md`.
- Security Review: APROVADO; achado RabbitMQ classificado como PREEXISTENTE_NAO_RELACIONADO e mantido como bloqueador global de release; veja `security-review.md`.
- SRE/DevOps: não aplicável; não houve mudança de infraestrutura, configuração operacional ou migration.
- Auditoria: APROVADO; veja `audit.md`.
- Finish-task: executada após aprovação do Task Verdict; artefatos movidos para `tasks/completed/safely-deactivate-and-reactivate-staff-users/`.
- Task Verdict: APROVADO.
- Release Verdict: BLOQUEADO pelo erro RabbitMQ AMQP 406 preexistente e explicitamente adiado pelo usuário; a task não o alterou nem agravou.
