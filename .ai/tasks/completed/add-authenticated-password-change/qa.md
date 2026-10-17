# QA — add-authenticated-password-change

## Veredito QA

**APROVADO.** O comportamento backend/frontend e os caminhos PostgreSQL fresh/upgrade foram validados. O veredito QA não implica que o rollout de produção foi executado.

## Evidências

- Backend direcionado: 11 testes, 0 failures/errors/skipped; BUILD SUCCESS.
- Suíte backend completa: 81 testes, 0 failures/errors/skipped; `clean verify`, BUILD SUCCESS, exit 0.
- Angular: 70/70 Jasmine/Karma ChromeHeadless; exit 0.
- Production build Angular: SUCCESS, exit 0.
- PostgreSQL 18.6 descartável: `-Dtest=PostgreSqlFlywaySchemaCiIT,PostgreSqlFlywayUpgradeCiIT test`, 3 testes, 0 failures/errors/skipped; BUILD SUCCESS, exit 0.
- Fresh: banco sem tabelas → V1–V4 → history versão 4, tabelas/constraints/índices e `credential_version` default 0/NOT NULL verificados; profile `prod`, Hibernate `validate` e contexto Spring iniciados.
- Upgrade: V1 com User legado e V3 com Users ACTIVE/PENDING avançaram até V4 preservando hash BCrypt, identidade, Guild e status; o ACTIVE autenticou após upgrade; Hibernate `validate` passou.
- Cobertura de integração verifica troca autenticada, todas as roles ACTIVE, senha atual incorreta/igual e limite BCrypt em bytes, token ausente/legado/divergente/inativo, revogação de sessões antigas, identidade recriada, payload com campos de autoridade forjados e concorrência.
- Testes de formulário verificam confirmação, payload mínimo, duplo submit, feedback seguro, erro 400, resultado de rede ambíguo e limpeza/encaminhamento para login.

## Limitações

- Não houve sessão manual autenticada para verificar contraste/foco/teclado em viewport desktop e handset. Inspeção estática e testes Angular não são apresentados como validação visual completa; a limitação fica registrada como evidência UX não manual.
- Não foi executado teste manual contra Render.

## Escopo e regressões

- `clean verify` cobre regressão de onboarding, ativação, RBAC, rate limit, isolamento e demais testes backend existentes.
- Nenhuma nova dependência, alteração frontend de tenant/role, ou endpoint de senha administrativa foi introduzido.
- O formulário usa o endpoint próprio do usuário e não envia confirmação, userId, guildId, role ou versão.

QA aprovado. O rollout em produção não foi executado e permanece fora desta task; sua segurança operacional é condição do Release Verdict, não da validação de comportamento desta task.
