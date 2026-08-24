# Auditoria — Provisionamento seguro de usuários e onboarding de Guild

## Escopo versus diff

| Item | Resultado |
|---|---|
| PRD/ADR | Implementação segue onboarding Guild + primeiro `MARECHAL`, User único por Guild, TenantService e JWT sem `guildId`. |
| Backend | Alterações limitadas a DTOs, serviço transacional, UserService/repository, controllers, segurança, exceções e testes correspondentes. |
| Frontend | Nenhuma alteração. |
| Migrations/dependências | Nenhuma criada ou alterada; a ausência de migrations é pendência preexistente da Fase 2. |
| Scope creep | Não identificado. O handler 403/404 foi necessário para os contratos HTTP da própria task. |
| Secrets | Nenhum secret versionado; segredos de teste são fictícios e locais ao teste. |

## Evidências de qualidade

- Testes afetados: 8, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`.
- Suíte completa: 34, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`, exit code 0.
- Integração HTTP cobre registro, primeiro administrador, BCrypt, ausência de senha na resposta, provisionamento por `MARECHAL`, bloqueio de role inferior e leitura cross-tenant.
- Testes unitários cobrem UserService tenant-scoped e authorities persistidas no filtro JWT.
- Execução final foi feita em cópia temporária do working tree devido a lock de artefatos `target` pela IDE; o repositório não foi alterado por essa estratégia.

## Segurança

- Task Security Verdict: **APROVADO**.
- Não houve acesso cross-tenant nos cenários executados.
- Não houve senha em texto claro persistida por caminhos implementados nesta task, nem senha em resposta/log.
- `guildId` não foi introduzido no JWT.
- Release Verdict: **BLOQUEADO** por limitações globais preexistentes: bootstrap demo, ausência de migrations e falta de proteção antiabuso para registro público. Foram encaminhadas sem reprovar esta task.

## Desvios e limitações

- A task não implementa multi-Guild, frontend, e-mail, MFA, billing, migrations ou RBAC completo dos domínios, conforme PRD.
- O registro público ainda não possui rate limiting/CAPTCHA; permanece limitação conhecida para deploy público.
- A task não altera `ADR-001` silenciosamente.

## Vereditos

**Task Verdict: APROVADO.** Escopo cumprido, testes adequados, sem regressão ou vulnerabilidade introduzida/agravada.

**Release Verdict: BLOQUEADO.** O estado global ainda requer as tasks de bootstrap/CORS, migrations e proteção operacional antes de release público.
