# Plano — fix-production-rate-limit-and-cors-configuration

## Routing

- Domínios: `planning`, `backend`, `security`, `sre`, `testing`, `documentation`.
- Agents: Planner → Backend Developer + Security + SRE/DevOps (diagnóstico) → QA → Security → Auditor.
- Skills: `create-prd`, `create-plan`; na execução autorizada, `test-backend` → `security-review` → `audit-task` → `finish-task` somente com Task Verdict aprovado.
- Não selecionados: Frontend Developer (sem alteração de UI); Arquiteto (sem decisão estrutural até evidência exigir store distribuído); `create-adr` (não autorizado/não necessário no momento).

## Etapa 1 — Diagnóstico de rate limit, sem correção

1. Confirmar pelo log de startup que `RegistrationRateLimitFilter` integra o SecurityFilterChain; já há evidência de que ele integra.
2. Adicionar apenas se necessário uma instrumentação temporária, agregada/mascarada, para contar decisões e identificar a classe de origem sem registrar IP completo ou qualquer dado sensível. Remover antes da conclusão ou substituir por teste suficiente.
3. Executar smoke controlado e verificar: sequência, mesma instância, deploy/restart, valor efetivo de properties e número de Guilds criadas.
4. Validar a documentação Render para `X-Forwarded-For`; investigar se a plataforma fornece garantia de IP original e como ela trata header já enviado pelo cliente.
5. Definir a trust boundary antes de tocar em forwarded headers. Se a garantia não for suficiente, escalar alternativa proporcional em vez de aceitar header arbitrário.

## Etapa 2 — Correção condicional

1. Se a causa for `getRemoteAddr()` do proxy, adotar a menor integração Spring/Tomcat compatível com a trust boundary comprovada.
2. Se a causa for configuração, filtro, ordem ou reinício, corrigir somente o item comprovado.
3. Adicionar testes para limite, janela, bloqueio sem mutação e header/identificador confiável, sem depender de um IP real.
4. Não adotar Redis/distribuição sem evidência de múltiplas instâncias ou requisito adicional.

## Etapa 3 — CORS operacional

1. Origin HTTPS do deployment confirmada: `https://theliderum.vercel.app`.
2. `CORS_ALLOWED_ORIGINS=https://theliderum.vercel.app` foi configurada no Render, sem mudança de código.
3. Preflight positivo retornou `200` e `Access-Control-Allow-Origin` exato; origin distinta retornou `403`. Etapa concluída.

## Validação e gates

1. Testes backend direcionados para rate limiter/CORS e `./mvnw.cmd clean verify` com resultado final verificável se houver mudança de código/teste.
2. Smoke Render: cinco registros permitidos, sexto bloqueado com 429 e sem persistência; CORS positivo/negativo; login e `/users/me` como regressão.
3. QA → Security → SRE/DevOps → Auditoria.
4. Release só volta a aprovado se os dois achados forem resolvidos empiricamente.

## Dependências e bloqueadores

- CORS em produção foi configurado e validado positiva e negativamente.
- Para serviços públicos Render, a trust boundary aprovada é `CF-Connecting-IP`: a documentação Render afirma que Cloudflare o sobrescreve antes de encaminhar a requisição; `X-Forwarded-For` permanece fora da chave por ser cadeia falsificável.
- Falta somente deploy autorizado e smoke de rate limit em produção.
- Backlog separado preservado: `validate-flyway-migrations-against-postgresql-in-ci`.
