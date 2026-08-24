# Security Review — RBAC e fronteiras tenant de User

## Superfície analisada

- autenticação JWT e authorities persistidas;
- autorização de User, Team e AdminController;
- delegação MARECHAL/GENERAL e escalonamento vertical;
- IDOR e isolamento por Guild;
- DTOs, mass assignment, respostas, logs e secrets.

## Evidências

- `UserCreateRequestDTO` e `UserRoleUpdateRequestDTO` são contratos específicos, sem `guildId`; a validação de papel ocorre no `UserServiceImpl`, não apenas no controller.
- `TenantService.getCurrentUser()` resolve o ator persistido; criações usam a Guild desse ator e alvos por ID usam `findByIdAndGuildId`.
- GENERAL só passa no service quando papel atual e novo estão abaixo de GENERAL; MARECHAL mantém a autoridade aprovada.
- Mudança/remoção de MARECHAL preserva ao menos um por Guild sob transação e lock pessimista.
- `JwtFilter` mantém authorities do `UserDetails` persistido e nenhum `guildId` foi incluído no token.
- Integração HTTP comprovou `403` para escalonamento vertical e `404` para User de outra Guild; nenhuma mutação cross-Guild foi observada.
- Não há secret, token, senha ou credencial introduzido no diff ou retornado pelos contratos de User.

## Achados

| Achado | Evidência | Origem | Severidade | Tratamento |
|---|---|---|---|---|
| `TeamController` ainda escreve IDs de Team/Member em saída padrão | linha preexistente, não alterada pela task; não contém token, senha ou Authorization | PREEXISTENTE_RELACIONADO | baixa/informativa | avaliar política de logs na task de observabilidade; não bloqueia esta task |
| Bootstrap demo/CORS | pendência registrada no handoff | PREEXISTENTE_NAO_RELACIONADO | média | task P0 `remove-production-demo-bootstrap-and-fix-cors` |
| Migrations Flyway ausentes | pendência registrada no roadmap/state | PREEXISTENTE_NAO_RELACIONADO | média | task Fase 2 `baseline-flyway-and-production-database-schema` |
| Registro público sem antiabuso | pendência registrada na task anterior | PREEXISTENTE_NAO_RELACIONADO | média | task de proteção operacional antes de deploy público |

## Vereditos

**Task Security Verdict: APROVADO.** Não há vulnerabilidade introduzida ou agravada: RBAC e tenant boundary foram reforçados e testados.

**Release Verdict: BLOQUEADO.** Bootstrap/CORS, migrations e proteção antiabuso de registro continuam bloqueadores globais preexistentes para deploy público.
