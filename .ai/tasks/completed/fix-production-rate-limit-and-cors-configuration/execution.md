# Execução — Rate limit e CORS em produção

## Evidências coletadas em 2026-09-01

### CORS

- O Render foi configurado operacionalmente com `CORS_ALLOWED_ORIGINS=https://theliderum.vercel.app`.
- Preflight `OPTIONS /auth/register-guild` com essa origin retornou `200`, `Access-Control-Allow-Origin: https://theliderum.vercel.app`, métodos esperados e `Access-Control-Allow-Credentials: true`.
- A mesma preflight com origin não permitida retornou `403 Invalid CORS request`, sem header de liberação.
- Veredito: CORS é resolvido por configuração; nenhum código foi alterado.

### Rate limit

- O serviço Render `Liderum` está configurado com uma única instância Free. Não há variável de ambiente de rate limit no painel; portanto, o valor default do código deveria ser 5 tentativas por 15 minutos.
- Os logs de startup confirmam `RegistrationRateLimitFilter` no `SecurityFilterChain`.
- Seis `POST /auth/register-guild` consecutivos e deliberadamente inválidos retornaram `400,400,400,400,400,400`; não houve persistência de Guild ou User e não houve `429`.
- O filtro usa `request.getRemoteAddr()` como chave. A documentação atual do Render informa que aplicações públicas recebem o endereço do proxy no socket; a própria Render recomenda obter o IP do cliente por header encaminhado.
- Causa do comportamento: o limite por cliente não é comprovadamente aplicado em produção usando a chave atual. A troca proposta para `CF-Connecting-IP` foi recusada por revisão automática porque ainda exige uma trust boundary explicitamente aceitável para o código de produção.

### Correção local autorizada

- O usuário autorizou explicitamente `CF-Connecting-IP` como fronteira de confiança para o serviço público Render/Cloudflare.
- `RegistrationRateLimitFilter` agora usa esse header somente quando a property `liderum.registration.rate-limit.trust-cloudflare-client-ip` está habilitada; o valor default é `false` e `application-prod.properties` o habilita.
- Sem header ou fora de produção, o fallback continua sendo `request.getRemoteAddr()`.
- O novo cenário de integração usa cinco endereços de proxy distintos com o mesmo `CF-Connecting-IP`, exige `429` na sexta tentativa e confirma que `X-Forwarded-For` não altera a quota.
- Testes direcionados: `RegistrationRateLimitIntegrationTest` (4), `RegistrationRateLimiterTest` (2) e `CorsConfigurationIntegrationTest` (2), sem failures/errors.
- Suíte completa: `./mvnw.cmd clean verify` — 58 testes, 0 failures, 0 errors, 0 skipped e `BUILD SUCCESS`. O processo encerrou após o resultado; o wrapper de execução não devolveu uma linha separada de exit code.

### Deploy e smoke final

- Commit funcional implantado: `29f9ad3 fix(security): trust Cloudflare client IP for registration limit`.
- Deploy Render `dep-dabgnde8bjmc73ct05hg`: `live`. O novo contêiner iniciou com profile `prod`, conectou ao PostgreSQL, validou Flyway e iniciou o backend.
- Sonda final, com seis `POST /auth/register-guild` deliberadamente inválidos: `400,400,400,400,400,429`.
- Como o filtro executa antes da validação e todos os cinco primeiros requests eram inválidos, não houve criação de Guild/User; a sexta foi bloqueada antes de alcançar o controller.
- CORS revalidado após deploy: origin Vercel `200` com `Access-Control-Allow-Origin` exato; origin não permitida `403` sem header de liberação.

## Estado

- CORS: aprovado empiricamente.
- Rate limit: aprovado empiricamente em produção.
- Deploy, commit e push concluídos para o commit funcional; artefatos de governança ainda aguardam versionamento separado.
