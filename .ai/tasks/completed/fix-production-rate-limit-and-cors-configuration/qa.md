# QA — Rate limit e CORS

## Veredito

**APROVADO**.

## Evidências

- O filtro continua limitado ao `POST /auth/register-guild`.
- Quando a confiança Cloudflare está habilitada, cinco proxies distintos com o mesmo `CF-Connecting-IP` compartilham a mesma quota e a sexta tentativa retorna `429`.
- `X-Forwarded-For` permanece fora da chave de quota.
- Sem a property de produção, o fallback é `request.getRemoteAddr()`.
- CORS de produção foi verificado externamente: origin Vercel permitida e origin distinta rejeitada.
- Testes direcionados: 8 testes, 0 failures, 0 errors, 0 skipped.
- `./mvnw.cmd clean verify`: 58 testes, 0 failures, 0 errors, 0 skipped e `BUILD SUCCESS`.
- Smoke Render após deploy: cinco requisições inválidas retornaram `400` e a sexta retornou `429`; não houve mutação de dados de domínio.

## Lacuna para conclusão

- Nenhuma lacuna de QA para o escopo aprovado.
