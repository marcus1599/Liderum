# PRD — User Management UI

## Classificação

P1 — funcionalidade / segurança / qualidade — LARGE.

## Problema

O backend já expõe administração tenant-scoped de Users e aplica a matriz RBAC de MARECHAL e GENERAL, porém o Angular não possui rota, serviço ou interface para listar, criar, alterar role ou remover usuários da própria Guild.

## Objetivo

Disponibilizar uma área autenticada de gestão de Users para MARECHAL e GENERAL, consumindo estritamente os contratos backend existentes e refletindo visualmente as permissões sem substituir a autorização server-side.

## Contratos backend confirmados

- `GET /users`: lista Users da Guild corrente; MARECHAL/GENERAL.
- `GET /users/{id}`: consulta User da Guild; MARECHAL/GENERAL.
- `POST /users`: cria User na Guild corrente com `username`, `email`, `password`, `role`; MARECHAL/GENERAL.
- `PUT /users/{id}/role`: altera somente `{ role }`; MARECHAL/GENERAL.
- `DELETE /users/{id}`: remove User da Guild; MARECHAL/GENERAL.
- `GET /users/me`: perfil próprio autenticado para todos os papéis.
- O backend resolve tenant por `TenantService`; não recebe `guildId` do cliente.

## Regras RBAC a refletir na UI

- MARECHAL pode criar/promover qualquer role, inclusive GENERAL e MARECHAL; pode alterar/remover GENERAL e outro MARECHAL, respeitando a invariável backend do último MARECHAL.
- GENERAL pode criar, alterar role e remover apenas MAJOR, CAPITÃO e SOLDADO.
- GENERAL nunca deve receber opções/ações sobre GENERAL ou MARECHAL.
- MAJOR, CAPITÃO e SOLDADO não acessam administração de Users.
- As permissões de UI são apoio de UX; 403 do backend continua definitivo e deve ser tratado sem vazar detalhes.

## Escopo

- Criar rota protegida `/users`, serviço e DTOs TypeScript tipados.
- Criar lista de Users da Guild com ações condicionadas ao perfil em memória fornecido por `/users/me`.
- Criar formulário/dialog de criação com username, e-mail, senha e roles permitidas ao ator atual.
- Criar contrato explícito para mudança de role e confirmação proporcional antes de remoção.
- Exibir loading, estados vazios, feedback de sucesso/erro e tratamento visual seguro de 401/403/404.
- Integrar acesso à área pela navegação autenticada apenas quando o papel permitir.
- Adicionar testes Jasmine/Karma comportamentais e de serviço.

## Fora de escopo

- Alterar username, e-mail, senha, perfil próprio, recuperação de senha, MFA, multi-Guild, novo RBAC backend, mudança de JWT/TenantService, backend, onboarding, billing, E2E, redesign geral e proteção antiabuso.

## Segurança

- Nunca enviar `guildId`, role de ator, token ou dados de tenant como autoridade.
- Senha só existe no formulário de criação e no payload POST; não persistir, registrar nem renderizar em lista.
- Não confiar apenas no ocultamento de ações; 403 do backend será a barreira definitiva.
- Não expor detalhes internos de erros de validação, conflito ou servidor.

## Critérios de aceitação

- MARECHAL e GENERAL autenticados podem acessar `/users`; papéis inferiores são redirecionados ou recebem UI segura sem ações.
- Lista, criação, alteração de role e remoção usam os contratos existentes, sem `guildId` no payload.
- GENERAL não visualiza nem consegue acionar operações proibidas sobre GENERAL/MARECHAL.
- MARECHAL pode acionar as operações superiores permitidas; erro do último MARECHAL é apresentado sem mascarar regra backend.
- 401 limpa sessão pela infraestrutura existente; 403/404 permanecem distinguíveis e não expõem detalhes.
- Testes e build Angular permanecem verdes.
