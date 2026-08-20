# Plano — Remover JWT/Authorization header dos logs do JwtFilter

**Classificação:** MEDIUM — segurança

## Objetivo

Eliminar o registro de header `Authorization`, JWT Bearer e dados derivados sensíveis no `JwtFilter`, preservando o comportamento de autenticação.

## Evidência e risco

`backend/src/main/java/com/example/Liderum/Security/JwtFilter.java` imprime em saída padrão o header Authorization, o token JWT completo, username e roles. Um token válido em logs pode ser reutilizado durante sua validade, expondo sessão e dados associados.

O achado foi classificado como `PREEXISTENTE_NAO_RELACIONADO` à task de isolamento multi-tenant e bloqueia o Release Verdict até ser corrigido.

## Escopo

- Remover ou substituir logs que exponham credenciais, tokens ou dados sensíveis em `JwtFilter`.
- Adicionar/ajustar testes proporcionais que garantam a ausência de exposição de token sem mascarar a autenticação.
- Executar validação backend e Security Review.

## Fora do escopo

- Alterar protocolo JWT, claims, expiração, segredo, regras de autorização ou endpoints.
- Refatorar autenticação, introduzir dependências, modificar `TenantService` ou a task multi-tenant concluída.
- Alterar infraestrutura, Docker, CI/CD, schema ou frontend.

## Routing

- Domínios: `planning`, `backend`, `testing`, `security`, `audit`.
- Agents: Planner → Backend Dev → QA → Security → Auditor.
- Skills: `create-plan` → `test-backend` → `security-review` → `audit-task` → `finish-task` somente após Task Verdict aprovado.
- Não selecionados: Frontend Dev, Arquiteto, SRE/DevOps e `create-migration` — não há frontend, decisão estrutural, operação, schema ou migration no escopo.

## Critérios de aceitação

- Nenhum log do `JwtFilter` contém Authorization header, JWT, username ou roles derivados do token.
- Fluxo de autenticação permanece funcional e validado pelos testes relevantes.
- Nenhuma configuração de JWT ou autorização é enfraquecida.
- Security Review aprova a remediação; Release Verdict deixa de estar bloqueado por este achado.
