# Security Review — Rate limit e CORS

## Task Security Verdict

**APROVADO**.

## Release Verdict

**APROVADO**.

## Checklist e evidências

| Item | Resultado |
| --- | --- |
| Trust boundary | `CF-Connecting-IP` só é usado quando a property explícita de produção está habilitada. A decisão foi autorizada pelo usuário para a borda pública Render/Cloudflare. |
| Header falsificável | `X-Forwarded-For` não é usado como chave. |
| Fallback | Sem a property ou sem header, aplica `request.getRemoteAddr()`. |
| Limite/mutação | Teste de integração comprova 429 após cinco tentativas e não altera o contrato do endpoint. |
| CORS | Origin exata Vercel autorizada; origin não permitida rejeitada; não há wildcard. |
| Segredos e logs | Nenhum segredo, IP completo, Authorization, JWT, senha, e-mail ou payload foi registrado ou versionado. |
| Dependências | Nenhuma dependência nova. |

## Achados

| Achado | Evidência | Origem | Impacto |
| --- | --- | --- | --- |
| Rate limiting inicialmente inefetivo na versão live | corrigido e comprovado no smoke final: `400,400,400,400,400,429` | PREEXISTENTE_RELACIONADO | resolvido |
| CORS de produção | resolvido e confirmado por preflight positivo/negativo | PREEXISTENTE_RELACIONADO | não bloqueia mais release |

## Encaminhamento

- Não adotar `X-Forwarded-For` como chave. A pendência de qualidade independente continua sendo validar migrations Flyway contra PostgreSQL real no CI.
