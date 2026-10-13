# Security Review — Integridade relacional

## Achados

| Achado | Evidência/origem | Classificação | Efeito |
| --- | --- | --- | --- |
| `users.guild_id` e `users.guild_role` agora obrigatórios | V3 e mapping User; compatibilidade conferida por contagens read-only de produção | INTRODUZIDO_PELA_TASK | Impede User autenticável sem vínculo/role; nenhuma violação atual encontrada |
| Integridade cross-Guild não é imposta por FK composta | Serviços resolvem referências por Guild e testes existentes cobrem Event/Attendance; lacuna estrutural anterior | PREEXISTENTE_RELACIONADO | V3 não amplia nem enfraquece a proteção da aplicação; FKs compostas seguem fora deste escopo |
| Duplo índice UNIQUE em token_hash | Schema de produção contém índice nomeado e unique inline pela V2 | PREEXISTENTE_NAO_RELACIONADO | Redundância de armazenamento/manutenção; sem alteração nesta task |

## Avaliação

- Nenhum segredo, token ou dado pessoal foi consultado no inventário.
- A presença duplicada fica impedida por chave única; colunas essenciais não aceitam nulo.
- `users.password` permanece nullable para activation PENDING; nenhuma regra JWT, RBAC ou TenantService foi alterada.
- PostgreSQL 18.6 fresh/upgrade passou, incluindo preservação do User legado e Hibernate `validate`. Render segue na V2; não houve alteração de produção.

## Vereditos

- **Task Security Verdict: APROVADO**; a diff não introduz risco de autorização ou vazamento, e os cenários PG18 fresh/upgrade passaram.
- **Release Verdict: BLOQUEADO para publicar/aplicar V3 em produção** até preflight imediatamente anterior, plano operacional e autorização de rollout. A produção atual segue saudável e inalterada na V2; isso não reprova a task independente.
