# Security Review — secure-staff-user-activation-flow

## Controles verificados

- Token gerado por `SecureRandom` com 32 bytes, transportado em Base64 URL-safe e persistido somente como SHA-256.
- Expiração de 24 horas, uso único, revogação de tokens anteriores na emissão e BCrypt na ativação.
- Endpoint público de ativação não recebe guildId, role ou JWT; TenantService/JWT existentes permanecem inalterados.
- Contrato `AdminUserCreateRequestDTO` não aceita password, guildId, status ou authority arbitrária.
- Testes de RBAC, tenancy, login PENDING/DISABLED e ausência de token em storage frontend passaram.

## Achados

- **HIGH / bloqueante:** regeneração autenticada prevista no PRD/plan não está implementada; não é possível comprovar revogação controlada por tenant/RBAC.
- **HIGH / bloqueante de gate:** PostgreSQL V1→V2 e upgrade de instalação existente não foram validados nesta sessão; o workflow disponível cobre somente V1.
- **MEDIUM:** a rota frontend de ativação ainda deve ser revisada para remover o token da query string após captura, conforme o controle previsto no PRD.

## Atualização final

- Endpoint de regeneração agora deriva o tenant server-side, aceita somente PENDING e reutiliza revogação/hash/expiração existentes.
- URL de ativação é sanitizada após captura sem armazenamento persistente.
- Ainda faltam testes dedicados de regeneração (RBAC, cross-tenant, ACTIVE/DISABLED, token anterior/novo) e evidência de upgrade PostgreSQL.

## Veredito

**BLOCKED** — não há HIGH/CRITICAL novo confirmado, mas os controles obrigatórios ainda não estão comprovados por testes completos.

## Retomada de Security — 2026-10-07

### Evidências atuais

- A inspeção confirmou token com CSPRNG de 32 bytes, armazenamento SHA-256, validade de 24 horas, consumo único e persistência BCrypt da senha.
- A regeneração é autenticada; o alvo é obtido por ID dentro da Guild resolvida via `TenantService`, exige `PENDING` e segue as regras de hierarquia. Conta ACTIVE/DISABLED e alvo cross-Guild recebem resposta não enumerável; os testes direcionados passaram.
- As respostas de provisionamento/regeneração não expõem senha nem hash. O fluxo público de ativação não aceita Guild, role ou JWT como autoridade. O frontend mantém segredo em memória e remove o token da query string após a captura.
- `RbacUserTenantBoundariesIntegrationTest`: 14/14; backend completo: 70/70; frontend: 35/35 e build aprovado.
- Nenhum achado novo confirmado de privilege escalation, IDOR, vazamento de credencial ou bypass tenant foi identificado na inspeção e nos cenários executados.

### Limitação relacionada

O teste PostgreSQL 18 de fresh schema e upgrade com User legado foi incluído no workflow, porém não executado: Docker Engine indisponível e sem resultado remoto desta execução. Assim, a preservação/ativação da conta legada na migration ainda não está empiricamente confirmada em PostgreSQL.

## Vereditos desta retomada

- **Task Security Verdict: BLOQUEADO** — não por vulnerabilidade confirmada, mas pela evidência PostgreSQL relacionada ao lifecycle e à preservação do User legado ainda ausente.
- **Release Verdict: BLOQUEADO para o candidato desta task** até o job PostgreSQL 18 executar com sucesso. O estado live anterior não foi alterado nesta execução.

## Veredito final — 2026-10-07

- O teste PostgreSQL 18.6 fresh/upgrade foi executado com sucesso em container descartável sem volume. O User legado permaneceu associado à mesma Guild, senha BCrypt foi preservada, status foi backfilled para ACTIVE e a autenticação efetiva passou após inicialização `prod` com Hibernate `validate`.
- Os testes direcionados de regeneração demonstraram bloqueio cross-Guild e hierarquia, nenhuma mutação em estados recusados, revogação do token antigo, novo token válido, replay recusado e persistência apenas do hash.
- Nenhum secret de produção foi introduzido; o teste usa somente credenciais/JWT descartáveis de CI.

**Task Security Verdict: APROVADO. Release Verdict: APROVADO.** Não foram encontrados achados introduzidos/agravados bloqueantes nesta task. O warning Flyway 9.22.3 vs PostgreSQL 18.6 permanece dívida técnica conhecida, mas os cenários efetivamente testados passaram.

## Suplemento final — 2026-10-07

- Reteste dos controles após inclusão de token inválido/expirado e concorrência: 16 cenários direcionados e 72 testes da suíte backend passaram sem falhas.
- A conta continua sem mutação para token inválido/expirado; uso concorrente do mesmo token permite somente um vencedor. Regeneração mantém tenant/RBAC, revoga o token anterior e não altera alvos recusados.
- PostgreSQL 18.6 confirmou fresh schema e upgrade com User legado; migração não altera JWT/autoridade tenant e preserva hash BCrypt e vínculo User/Guild.
- Revisão não encontrou segredo de produção, exposição de senha/hash/token, mass assignment de Guild/role, bypass tenant ou elevação de privilégio introduzidos/agravados pela task.

**Task Security Verdict: APROVADO. Release Verdict: APROVADO.**
