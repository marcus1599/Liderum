# Auditoria — Rate limit e CORS

## Evidências

| Critério | Evidência | Resultado |
| --- | --- | --- |
| Escopo | Alterados apenas filtro, property de produção e teste de integração; CORS foi apenas configuração Render | aprovado |
| Trust boundary | `CF-Connecting-IP` autorizado para Render/Cloudflare; `X-Forwarded-For` não é usado | aprovado |
| Segurança | Nenhum segredo, token, IP completo ou payload foi registrado/versionado | aprovado |
| Testes | Direcionados: 8 verdes; suíte: 58 testes, 0 failures/errors/skipped, `BUILD SUCCESS` | aprovado |
| Produção | Deploy Render `live`; rate smoke `400,400,400,400,400,429`; CORS permitido/bloqueado comprovado | aprovado |
| Dependências/schema | Sem dependência, migration ou alteração de JWT/RBAC/tenant | aprovado |
| Scope creep | Não observado | aprovado |

## Task Verdict

**APROVADO**.

## Release Verdict

**APROVADO**.

## Pendências não bloqueantes

- Validar migrations Flyway contra PostgreSQL real no CI antes de novas mudanças de schema.
- Se o serviço passar a ter múltiplas instâncias, substituir o contador em memória por store distribuído em task separada.
