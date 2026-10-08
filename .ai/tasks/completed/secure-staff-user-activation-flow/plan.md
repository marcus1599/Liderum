# Plano — secure-staff-user-activation-flow

## Routing

- Agents: Planner → Arquiteto → Security (consultivos) → Backend Developer → Frontend Developer → QA → Security → SRE/DevOps → Auditor.
- Skills no planejamento: `create-prd`, `create-plan`.
- Skills na execução futura: `create-migration`, `test-backend`, `test-frontend`, `security-review`, `audit-task`, `finish-task`.
- Task estrutural/LARGE: QA e Security são gates obrigatórios; Task Verdict e Release Verdict permanecem separados.

## Decisões arquiteturais

1. ADR-001 permanece integral: uma Guild por User e tenant server-side via `TenantService`; token de ativação não carrega autoridade tenant.
2. Usar `UserActivationToken` persistido separadamente, com somente hash SHA-256, expiração de 24h, `usedAt`/`revokedAt` e vínculo com User.
3. Token bruto aleatório (mínimo 32 bytes) é mostrado uma vez. O frontend monta o link com sua própria origin e `/activate`, evitando nova configuração de URL no backend.
4. `User.status` controla autenticação. Existentes e primeiro MARECHAL nascem/migram `ACTIVE`; Staff provisionado nasce `PENDING`; `DISABLED` não autentica, mas seu workflow administrativo fica para task posterior.
5. `password` pode ser nulo apenas no estado PENDING. A aplicação impede ACTIVE sem hash BCrypt; a migration preserva existentes e não inventa senha temporária.
6. Ativação e consumo do token são atômicos. Regeneração revoga tokens ativos do mesmo User na mesma transação.
7. Respostas inválidas são uniformes. Não usar JWT para ativação nem guardar token bruto, senha temporária ou `guildId` no cliente.
8. Não criar ADR novo: a decisão refina lifecycle de credencial sem alterar a fronteira arquitetural permanente do ADR-001. Reavaliar somente se surgir e-mail gerenciado, SSO ou multi-Guild.

## Etapas futuras de implementação

1. Confirmar diff limpo/atribuível e contratos atuais de User/Auth/Security antes de editar.
2. Criar migration V2 (ou próximo número livre confirmado) para status, nullable password e tabela/constraints/índices de activation token; validar ordem, FK e backfill `ACTIVE`.
3. Implementar enum, entidade, repository e serviço transacional de emissão, regeneração e consumo; usar CSPRNG, digest e comparação/lookup seguros.
4. Alterar criação administrativa para username/e-mail/role sem password, preservando matriz RBAC, TenantService e 404 cross-tenant.
5. Adicionar endpoint público de ativação e endpoint autenticado de regeneração. Atualizar SecurityConfig somente nos paths necessários.
6. Fazer `CustomUserDetails.isEnabled()` refletir `ACTIVE`; preservar mensagem genérica de autenticação.
7. Atualizar `/users` e seus contratos; criar `/activate` pública, sem guard, com senha/confirmação, loading e navegação para login.
8. Atualizar testes existentes que criam usuários administrativamente para ativar a conta pelo fluxo real ou usar fixtures internas explícitas, sem enfraquecer assertions.
9. Executar testes direcionados backend/frontend, suites completas, build Angular e validação PostgreSQL/Flyway proporcional.
10. QA → Security → SRE/DevOps → Auditor; somente então `finish-task`.

## Arquivos prováveis

- backend: `Entities/User.java`, novo enum/status e entidade token; repositories/services/controllers/DTOs de User/activation; `CustomUserDetails`; `SecurityConfig`; nova migration em `db/migration`;
- backend tests: serviço de token, autenticação/status, RBAC/tenant, MockMvc de ativação e migration/schema;
- frontend: `users/**`, novo `auth/activate/**`, rotas, models/services e specs;
- `.ai/tasks/active/secure-staff-user-activation-flow/**` e, após conclusão, state/handoff/roadmap de evolução.

## Testes obrigatórios

1. MARECHAL cria PENDING sem password e recebe token bruto uma única vez.
2. GENERAL cria MAJOR/CAPITÃO/SOLDADO PENDING.
3. GENERAL não cria GENERAL/MARECHAL; roles inferiores não criam usuários.
4. Payload com password, guildId ou role não autorizada não causa mass assignment.
5. Conta PENDING não autentica; usuário existente/primeiro MARECHAL ACTIVE continua autenticando.
6. Token persistido é hash, distinto do bruto, com entropia/tamanho previstos; resposta não contém password/hash.
7. Token válido ativa uma vez, grava BCrypt e permite login.
8. Token inválido, expirado, usado e revogado falham com resposta uniforme.
9. Regeneração invalida link anterior e o novo link funciona.
10. Regeneração cross-Guild é bloqueada sem enumeração.
11. Regeneração respeita MARECHAL/GENERAL e status PENDING.
12. Duas ativações concorrentes produzem no máximo um sucesso.
13. Falha durante ativação não deixa password/status/token parcialmente alterados.
14. Usuário DISABLED não autentica (invariante do modelo, ainda sem workflow de disable).
15. Migration faz backfill de usuários existentes como ACTIVE e preserva onboarding.
16. Banco vazio aplica todas as migrations e Hibernate `validate` passa em H2 e PostgreSQL 18 CI.
17. `/users` não pede/persiste senha e exibe status/link somente para ação autorizada.
18. `/activate` valida formulário, loading, duplo submit, sucesso e erros genéricos.
19. Rota de ativação é pública; token/senha não vão para localStorage/sessionStorage/logs.
20. Regressão completa de login, onboarding, RBAC, tenancy, frontend e notification/builds aplicáveis.

## Riscos e controles

- **Vazamento por URL/histórico/referrer:** HTTPS, token curto em validade, uso único, remoção da query após captura e ausência de analytics nessa rota.
- **Replay/concorrência:** lock ou consumo condicional transacional e teste concorrente.
- **Enumeração:** respostas e status uniformes; nenhum endpoint pesquisa por e-mail/username publicamente.
- **Inconsistência status/password:** backfill explícito e regras de serviço/testes de schema.
- **Scope creep:** e-mail, recovery, password change e disable UI ficam em tasks próprias.
- **Migração:** nunca editar V1; validar em PostgreSQL real via job existente.

## Gates de conclusão

- PRD e ADR-001 sem divergência; zero autoridade tenant proveniente do token/cliente.
- Todos os 20 grupos de cenários comprovados e suites completas verdes.
- Security aprova armazenamento, expiração, replay, enumeração, logs, RBAC e tenant.
- QA comprova fluxo demonstrável ponta a ponta sem senha administrativa.
- Auditor confirma ausência de e-mail provider, multi-Guild, mudança JWT ou refactor oportunista.

## Status

PLANEJADA — aguarda autorização explícita para implementação.
