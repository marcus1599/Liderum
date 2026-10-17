# Execution — add-authenticated-password-change

## Routing

- Domínios: backend, frontend, database, security, testing, UX, SRE, audit.
- Papéis: Backend Dev e Frontend Dev para implementação; QA e Security para gates; SRE/DevOps para PostgreSQL/deploy; Auditor após os gates.
- Skills: `test-backend`, `test-frontend`, `create-migration`, `security-review`, `ux-review`, `audit-task` e `finish-task`, aplicadas conforme os respectivos gates. Nenhum agente independente foi alegado.
- ADR-002 foi recomendado no plano, mas não criado porque a autorização recebida aprovou a execução da task, sem autorizar explicitamente a criação de ADR. ADR-001 não foi alterado.

## Implementação

- Migration V4 aditiva adiciona `users.credential_version BIGINT NOT NULL DEFAULT 0`; V1–V3 não foram alteradas.
- `PUT /users/me/password` usa o usuário autenticado resolvido por `TenantService`, lock persistido, refresh da entidade após o lock, confirmação da senha atual, BCrypt e incremento atômico da versão.
- JWT passou a carregar `uid` e `cv`, emitidos do mesmo principal autenticado; o filtro confere subject, id, versão e estado ACTIVE com os dados persistidos. Claims antigos/ausentes e conta recriada não autenticam; role e tenant continuam server-side.
- Frontend adiciona formulário autenticado `/account/password`, validação e confirmação local, estado de envio, mensagens seguras, bloqueio de repetição ambígua, limpeza da sessão após sucesso e link para todas as roles autenticadas. Nenhuma senha é persistida.
- Durante a validação direcionada, a corrida concorrente inicialmente aceitou duas mudanças. A causa foi a entidade já carregada no persistence context antes da aquisição do lock; o serviço passou a executar refresh sob lock. O cenário passou na repetição e no `clean verify`.

## Validações concluídas

| Comando | Resultado empírico |
| --- | --- |
| Backend direcionado: `-Dtest=PasswordChangeIntegrationTest,JwtFilterTest,FlywaySchemaIntegrationTest test` | 11 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit 0 |
| `backend/.\mvnw.cmd clean verify` | 81 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit 0 |
| `frontend/npm test -- --watch=false --browsers=ChromeHeadless` | 70/70 SUCCESS; exit 0 |
| `frontend/npm run build` | SUCCESS; exit 0 |
| PostgreSQL 18.6 descartável: `-Dtest=PostgreSqlFlywaySchemaCiIT,PostgreSqlFlywayUpgradeCiIT test` | 3 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit 0. Fresh V1–V4 e upgrades com User existente passaram. |
| `git diff --check` | sem erros de whitespace; Git emite apenas avisos de conversão LF→CRLF já observáveis nos arquivos modificados |

### Evidência PostgreSQL real

- Docker Engine 29.5.3; container exclusivo `codex-liderum-pwchange-pg18-20261015`, PostgreSQL 18.6 (`postgres:18.6`), banco local descartável sem volume. Containers anteriores do usuário foram apenas listados e não foram usados ou alterados.
- O banco começou sem tabelas públicas. O teste fresh executou com profile `prod`, Flyway aplicou V1–V4; `flyway_schema_history` registra quatro migrations bem-sucedidas e versão corrente 4. Foram verificadas sete tabelas do domínio, constraints/índices, e `credential_version` como `BIGINT NOT NULL DEFAULT 0`.
- A inicialização real do contexto Spring concluiu com Hibernate `ddl-auto=validate`; o teste fresh passou.
- O cenário V1→V4 preservou User legado (id, Guild, hash BCrypt e status) e autenticou a conta após upgrade. O cenário V3→V4 preservou Users ACTIVE/PENDING, manteve hashes/status/Guild e inicializou `credential_version=0`; contexto Spring/Hibernate iniciou e autenticação do User ACTIVE passou.
- O container temporário foi removido após confirmar seu ID/nome/imagem. Não houve conexão ou alteração de banco Render.
- Flyway 9.22.3 emitiu o aviso conhecido de que PostgreSQL 18 está acima da última versão declarada como testada (15). Os testes em 18.6 passaram; a lacuna formal permanece como dívida técnica separada, não causa de falha desta task.

Warnings do build Angular: `baseline-browser-mapping` desatualizado, deprecações Sass, import `MatTable` não usado preexistente e budgets SCSS ultrapassados em componentes existentes. Não foram corrigidos por estarem fora do escopo.

## Pendências de validação

- Nenhum gate técnico de backend/frontend ou PostgreSQL permanece pendente. O backend completo não foi repetido após a validação PostgreSQL porque nenhum código foi alterado depois do `clean verify` de 81 testes; o comando PostgreSQL recompilou o código e concluiu 3/3 testes.
- SRE/DevOps avaliou a estratégia de rollout sem executar deploy: a versão antiga da aplicação pode continuar aceitando JWTs legados enquanto ainda receber tráfego. O Release Verdict desta alteração permanece bloqueado até confirmar substituição/drain de todas as instâncias antigas; rollback para o código antigo poderia voltar a aceitar tais tokens, portanto requer correção forward ou avaliação explícita.
- Não houve inspeção manual autenticada de teclado/foco/contraste em browser desktop/handset. A revisão estática confirmou labels, `role=alert/status`, foco/teclado nativo de formulário, contraste via cores declaradas e breakpoint mobile; testes DOM cobrem estados e navegação. A limitação permanece documentada.
- Nenhuma alteração foi feita no Render; nenhum deploy, stage, commit ou push foi executado.
- Não houve inspeção visual autenticada manual em desktop/handset; responsividade e acessibilidade foram avaliadas por SCSS/template e cobertura DOM, não por inspeção visual completa.

## Estado

Task concluída após QA, Security, SRE/DevOps, Auditoria APROVADA e `finish-task`. Task Verdict APROVADO; Release Verdict permanece BLOQUEADO até execução/validação do rollout em ambiente real. Nenhum deploy, stage, commit ou push foi realizado.
