# Plan — add-authenticated-password-change

## Status e routing

Planejamento concluído em 2026-10-13; escopo e invalidação de JWT legado aprovados pelo usuário em 2026-10-15. ADR-002 recomendado, porém não criado por falta de autorização explícita para criar ADR. Execução iniciada; ver `execution.md`. Base `c74e528`; o working tree já continha os artefatos de planejamento antes da execução.

- Classificação: STRUCTURAL / P1 / esforço LARGE, funcionalidade e segurança fullstack.
- Objetivo, contratos e limites: `prd.md`; evidências e priorização: `inventory.md`.
- Domínios: planning, architecture, backend, frontend, security, database, testing, sre, audit.
- Responsabilidades selecionadas: Planner; Arquiteto e Security consultivos para revogação; Backend/Frontend para viabilidade e futura execução; QA para matriz de aceite; SRE para migration/deploy compatível; Auditor no gate final. Análise conduzida pela sessão principal seguindo esses papéis, sem alegar aprovações independentes ou gates executados.
- Skills de planejamento: create-prd, create-plan, ux-review, security-review; create-migration, test-backend e test-frontend consultadas para estratégia. Futura execução: essas skills conforme domínio, depois audit-task e finish-task. create-adr somente com autorização específica.
- Sem notification-service, MCP, agentes/skills ou workflow CI novo; job postgres-flyway existente já executa as classes a serem atualizadas.

## Decisão arquitetural proposta

Recomendar **ADR-002: revogação de JWT por identidade persistida e versão de credencial**. A decisão é duradoura: muda o contrato interno de emissão/validação, o comportamento de todas as sessões após troca e a transição de tokens legados. Não criar ADR sem autorização; nenhuma mudança em ADR-001.

| Alternativa | Benefício | Problema / decisão |
| --- | --- | --- |
| Atualizar BCrypt e limpar só navegador atual | Menos arquivos | Outros JWTs continuam válidos até expiração (padrão sete dias). Rejeitada para esta capacidade. |
| passwordChangedAt comparado ao iat | Sem contador | Precisão de tempo, tokens na mesma janela e clocks exigem cuidado adicional. Não preferida. |
| credential_version + uid conferidos no banco | Revogação imediata nas próximas requests; usa consulta já existente; impede reuso de username | Requer V4, novo claim e login após deploy; proposta escolhida para aprovação. |
| Blacklist/sessões persistidas/Redis/refresh tokens | Revogação por dispositivo | Infraestrutura e lifecycle desproporcionais ao requisito. Adiados. |

Não colocar hash de senha no JWT nem usá-lo como segredo derivado. Não confiar em role/Guild/versão enviada no corpo. Claim uid só verifica identidade persistida; não substitui TenantService. Delimitação de concorrência e compatibilidade está no PRD.

## Etapas após autorização

1. Reconfirmar Git, versões Flyway, ADR e PRD aprovados. Se ADR autorizado, registrar decisão antes do código. Manter mudanças preexistentes separadas.
2. Criar V4 incremental para credential_version, default zero e NOT NULL, com mapeamento coerente e valor inicial explícito em builders/novas contas. Sem alterar V1–V3, sem limpar banco e sem mudar senha/status/role/Guild de registros existentes.
3. Backend: DTO explícito, operação `PUT /users/me/password`, resolução do ator via TenantService, lock/releitura do User alvo, conferência de versão/status e senha atual, hash BCrypt + incremento atômico. Definir erros tipados/handler local que não exponha valores, incluindo validação e JSON malformado. Sem consolidação dos advices globais.
4. JWT: emissão com uid/cv do principal autenticado, validação de ambos e ACTIVE no filtro. Tratar usuário ausente/inativo/token revogado como falha de autenticação segura, sem 500. Configurar 401 de autenticação no boundary protegido, mantendo 403 de autorização. Não alterar endpoints públicos nem expiração/segredo. Documentar login único necessário após deploy e indisponibilidade de rollback para filtro antigo como recuperação segura de revogação.
5. Frontend após contrato backend: rota protegida e entrada no sidebar, formulário Material, serviço/DTO tipado, estados de envio, feedback local, logout e confirmação no login. Reutilizar auth guard/interceptor/estado existentes. Não decodificar uid/cv no Angular para conceder autoridade.
6. Implementar testes abaixo; executar direcionados, suítes e build. PostgreSQL real obrigatório pela migration; limitar criação/limpeza a ambiente descartável identificado. Não tocar Render nesta task de implementação.
7. Gates QA → Security → SRE/DevOps → Auditor. Registrar execution/reviews/audit com comandos, resultados e limitações reais. finish-task apenas com Task Verdict APROVADO; atualizar state/handoff/roadmap proporcionalmente.

## Arquivos previstos

Backend, sob `backend/src/main/`:

- `java/com/example/Liderum/Controllers/UserController.java` (ou controller específico de senha sob `/users/me`, se necessário para isolar handlers), `Services/UserService.java`, `Services/Impl/UserServiceImpl.java` ou serviço dedicado pequeno para transação sensível;
- novo `dto/PasswordChangeRequestDTO.java` e exceções/handler estritamente específicos do fluxo;
- `Repository/UserRepository.java`, `Entities/User.java`;
- `Security/JwtUtil.java`, `Security/JwtFilter.java`, `Security/CustomUserDetails.java`, `Controllers/AuthController.java`, `Config/SecurityConfig.java`;
- `resources/db/migration/V4__add_user_credential_version.sql` (reconfirmar versão disponível).

Testes backend: nova `Security/PasswordChangeIntegrationTest.java`; cobertura unitária JWT/filtro quando útil; `Security/JwtFilterTest.java`, `config/FlywaySchemaIntegrationTest.java`, `config/PostgreSqlFlywaySchemaCiIT.java`, `config/PostgreSqlFlywayUpgradeCiIT.java`; fixtures que emitem JWT diretamente só se necessário para cumprir o novo contrato, nunca enfraquecer assertions existentes.

Frontend sob `frontend/src/app/`: novo componente standalone de senha em `auth/` (TS/HTML/SCSS/spec), `auth.models.ts`, `auth.service.ts` e specs; `app.routes.ts`, `shared/sidebar/sidebar.component.html` e testes existentes de navegação; login/template/spec apenas para confirmação após sucesso se necessário. `AuthInterceptor`/spec somente se adaptação comprovada do contrato exigir; preservar comportamento 401 já existente.

Documentação: artefatos da task, state/handoff/product-evolution-roadmap; ADR apenas se autorizado. Nenhuma dependência ou modificação de CI/Compose prevista.

## Matriz de testes

| Superfície | Cenários obrigatórios |
| --- | --- |
| HTTP e identidade | Login real → JWT → troca própria → login antigo falha/novo passa. Parametrizar roles ACTIVE. Duas Guilds: ator só altera a si mesmo; campos extras/ids/Guilds/roles forjados rejeitados e outras contas intactas. |
| Reautenticação | Senha atual errada, nova igual, campos ausentes, JSON inválido, limite 8 caracteres/72 bytes UTF-8 e multibyte; sem mutação parcial; erro seguro sem senha no corpo nem log. |
| Revogação | Dois tokens reais anteriores da mesma conta recusados após commit; novo token válido; token de outro User continua válido; uid/cv ausentes/malformados/divergentes, token expirado/assinatura inválida, User inexistente/PENDING/DISABLED recusados; mesma username recriada não aceita token antigo. |
| Corridas | Duas trocas concorrentes em transações independentes: só uma grava com credencial antiga, versão incrementa uma vez. Login concorrente nunca recebe versão nova validando senha antiga. Rollback preserva hash e versão. Evitar teste que passa por compartilhar a transação externa do teste. |
| Status HTTP | 401 real aciona expiração frontend; senha atual errada retorna 400 conhecido e mantém sessão; 403 por RBAC continua 403. Públicos/login/ativação permanecem funcionais. |
| Banco H2/PostgreSQL | Fresh V1→V4; upgrade dedicado V3 com User ACTIVE e PENDING existentes → V4, preservando hash/id/Guild/role/status. Manter também caminho V1 com User legado até a última versão. Verificar default/NOT NULL, versões history, novo login/troca após upgrade e Hibernate validate. |
| Angular | Validações e confirmação, payload exato, Authorization, loading e duplo submit; sucesso limpa sessão/campos e vai ao login com feedback; 400/401/403/0/5xx sem exposição de corpo bruto ou retry automático; nada sensível persistido; rota guardada, link para todas as roles, regressão Users/sidebar/login/ativação. |
| UX | Navegação e retorno, labels/autocomplete, teclado/foco, mensagens status/alert, Material/tema e desktop/handset. Capturar evidência; não apresentar testes unitários como inspeção visual. |

Comandos previstos para a execução (resultados finais registrados em `execution.md`):

```text
backend/: .\mvnw.cmd "-Dtest=PasswordChangeIntegrationTest,JwtFilterTest,FlywaySchemaIntegrationTest" test
backend/: .\mvnw.cmd clean verify
backend/ com PostgreSQL 18 descartável: .\mvnw.cmd "-Dtest=PostgreSqlFlywaySchemaCiIT,PostgreSqlFlywayUpgradeCiIT" test
frontend/: npm test -- --watch=false --browsers=ChromeHeadless
frontend/: npm run build
repositório: git diff --check
```

Os nomes foram ajustados às classes efetivamente criadas e os comandos acima foram executados. Configuração PostgreSQL seguiu o job existente com credenciais exclusivamente descartáveis; nenhum teste apontou para produção. Os resultados e contagens empíricas desta task constam em `execution.md` e nos gates.

## Gates, riscos e aceite operacional

- QA exige comportamento completo, testes atuais, concorrência e regressão de sessão/tenancy.
- Security verifica revogação, identidade, senha, erros/logs, mass assignment e status de autenticação. Achados devem ter origem e Task/Release Verdict separados; não declarar todo o produto auditado.
- SRE verifica V4 fresh/upgrade PostgreSQL real, preservação de dados, default/validate, impacto de tokens legados e transição coordenada. Deploy não faz parte desta autorização; rollback para código que ignora cv reabilitaria tokens antigos e não deve ser descrito como seguro. Recuperação preferida é correção forward; qualquer estratégia operacional diferente exige avaliação própria.
- Auditor compara diff/PRD/ADR aprovado, evidência real e limites. Falha em PostgreSQL, vazamento ou sessões anteriores ainda válidas impede finalizar a task; rollout de produção permanece separado do Task Verdict.
- Escalar conflito com ADR, necessidade de dependência/infra, política de migração de tokens diferente, regressão fora do escopo ou risco que exija expandir endpoints. Sem reset de dados ou mudança silenciosa na política.
- ADR-002 não foi criado sem autorização explícita. Nenhum deploy, stage, commit ou push foi incluído na execução autorizada.

## Progresso

- [x] Contexto, Git, inventário, priorização, PRD e plano.
- [x] Aprovação do escopo e da política de sessões legadas.
- [x] Implementação backend/frontend e testes locais H2/Angular.
- [x] Validação PostgreSQL 18.6 fresh V1→V4 e upgrades V1→V4/V3→V4 com usuários existentes; Hibernate `validate`, startup e autenticação aprovados.
- [x] QA, Security e SRE/DevOps aprovados; Auditoria e finalização após o gate.
- [x] Auditoria Task Verdict APROVADO e `finish-task`; Release Verdict permanece BLOQUEADO até rollout operacional verificado.
