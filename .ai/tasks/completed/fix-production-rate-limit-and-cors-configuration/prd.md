# PRD — Corrigir rate limiting de produção e configurar CORS

**Classificação:** P0 — segurança / infraestrutura — MEDIUM.

## Contexto

Após a recuperação Flyway, o backend Render está live em PostgreSQL 18.6 e os fluxos mínimos de registro, login, perfil e endpoint tenant-scoped passaram. O smoke revelou dois achados independentes da recuperação:

1. o limite configurado por default como 5 registros por 15 minutos não retornou 429 após seis `POST /auth/register-guild` consecutivos; o filtro está presente no SecurityFilterChain e hoje usa `request.getRemoteAddr()`;
2. `CORS_ALLOWED_ORIGINS` não está configurada no Render. A preflight de uma origin não permitida foi corretamente rejeitada, mas a origin HTTPS real do frontend não foi identificada nem validada.

## Objetivo

Restabelecer uma proteção antiabuso comprovável para o registro público atrás do proxy Render e configurar/validar CORS em produção com a origin HTTPS exata do frontend, sem wildcard e sem alterar JWT, tenancy, RBAC ou contratos funcionais.

## Requisitos

### Rate limiting

1. Investigar e registrar o identificador real usado em produção sem logar IP completo, Authorization, JWT, senha, e-mail ou payload.
2. Confirmar, antes de confiar em qualquer header, o comportamento/garantia do Render e a cadeia de proxies.
3. Definir a trust boundary: o cliente não pode escolher livremente a chave do rate limiter via header falsificável.
4. Comprovar por teste e smoke que uma mesma origem recebe 429 após cinco registros no período configurado; nenhuma Guild/usuário pode ser criada pela requisição bloqueada.
5. Preservar limite configurável, janela, limites de memória e comportamento de testes existentes.

### CORS

1. A origin HTTPS real do frontend de produção foi confirmada pelo usuário: `https://theliderum.vercel.app`. O repositório só prova a URL do backend; esta confirmação operacional é a fonte para a configuração.
2. Configurar `CORS_ALLOWED_ORIGINS` no Render com a origin exata, sem path e sem wildcard.
3. Comprovar preflight permitido para essa origin, com `Access-Control-Allow-Origin` correspondente; origin não permitida deve continuar sem liberação.
4. Não alterar código se a implementação atual de `CorsConfig` já cumprir os critérios após a configuração.

## Fora do escopo

- Flyway, PostgreSQL schema, V1 ou dependências;
- JWT, RBAC, TenantService, frontend funcional e novos endpoints;
- rate limiter distribuído/Redis, CAPTCHA, e-mail, MFA ou redesign;
- confiar em `X-Forwarded-For` sem uma trust boundary documentada;
- logar dados sensíveis ou identificadores completos;
- CORS wildcard.

## Critérios de aceitação

- causa do smoke de rate limit confirmada por evidência ou bloqueio técnico explicitamente documentado;
- proteção de cinco requisições/15 minutos comprovada no ambiente Render após correção aprovada;
- origin HTTPS de frontend real identificada e CORS validado positiva e negativamente;
- testes backend proporcionais e smoke produção final verificáveis;
- Task/Release Verdicts atualizados sem misturar a recuperação Flyway;
- nenhuma credencial, token, IP completo ou payload sensível versionado.

## Riscos

| Risco | Mitigação |
| --- | --- |
| Header forjado pelo cliente | confiar somente em header cuja origem e sanitização pelo proxy sejam comprovadas; testar spoofing quando possível |
| Correção de proxy alterar URL/scheme globalmente | limitar a mudança ao mecanismo documentado, testar redirects e CORS |
| Origin incorreta | obter a URL do deployment real, não inferir do `environment.prod.ts` |
| Dados de smoke | usar identificadores descartáveis e evitar repetição além do necessário |
