# Auditoria — RBAC e fronteiras tenant de User

## Escopo versus diff

| Item | Resultado |
|---|---|
| PRD e matriz RBAC | MARECHAL/GENERAL, papéis inferiores, perfil próprio, role explícita e último MARECHAL implementados conforme aprovado. |
| Tenant | User permanece resolvido por `TenantService`; consultas por ID usam Guild corrente; não há `guildId` no DTO ou JWT. |
| Team/Admin | Apenas os guards previstos foram adicionados e a expressão inválida foi corrigida. |
| Segurança | Security aprovou a task; não houve secret, credencial, senha ou token introduzido. |
| Testes | Integração de 10 cenários, testes direcionados 19/19 e suíte 46/46 aprovados. |
| Migrations/dependências | Nenhuma migration ou dependência nova; não necessária para o escopo. |
| Scope creep | Não identificado: frontend, credenciais, multi-Guild, CORS, bootstrap, Flyway e mensageria não foram alterados. |

## Vereditos

**Task Verdict: APROVADO.** Escopo entregue com evidência de comportamento, sem regressão ou vulnerabilidade introduzida/agravada.

**Release Verdict: BLOQUEADO.** Permanecem bootstrap demo/CORS, migrations Flyway e proteção antiabuso do registro público. São pendências globais preexistentes, não impeditivas da conclusão desta task.
