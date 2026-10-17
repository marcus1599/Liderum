# Auditoria — add-authenticated-password-change

Data: 2026-10-15

## Escopo e rastreabilidade

O diff revisado corresponde ao PRD e ao plan: endpoint autenticado para troca da própria senha, validação BCrypt, revogação via identidade/versão persistida, migration V4 aditiva, formulário Angular, testes backend/frontend e evidências/governança da task. Não foram encontrados arquivos de produto sem relação com a task. V1–V3 e ADR-001 permanecem inalterados; ADR-002 foi recomendado, mas não criado sem autorização específica. Não houve mudança de dependência, deploy, stage, commit ou push.

## Evidências de validação

| Gate | Evidência | Resultado |
| --- | --- | --- |
| Backend direcionado | `-Dtest=PasswordChangeIntegrationTest,JwtFilterTest,FlywaySchemaIntegrationTest test`; 11 testes | 0 failures/errors/skipped; BUILD SUCCESS |
| Backend completo | `clean verify`; 81 testes | 0 failures/errors/skipped; BUILD SUCCESS; exit 0 |
| PostgreSQL 18.6 | `-Dtest=PostgreSqlFlywaySchemaCiIT,PostgreSqlFlywayUpgradeCiIT test`; 3 testes | 0 failures/errors/skipped; BUILD SUCCESS; exit 0 |
| Frontend | Jasmine/Karma ChromeHeadless | 70/70 SUCCESS; exit 0 |
| Build frontend | `npm run build` | SUCCESS; exit 0 |
| Whitespace | `git diff --check` | sem erros; somente avisos LF→CRLF do Git |

### Migration e banco

- Fresh em PostgreSQL 18.6 descartável começou sem tabelas; V1–V4 foram aplicadas e registradas com sucesso, versão corrente 4.
- Tabelas, constraints, índices e `users.credential_version BIGINT NOT NULL DEFAULT 0` foram verificados. Profile `prod`, Hibernate `ddl-auto=validate` e contexto Spring iniciaram.
- O cenário V1→V4 preservou e autenticou User preexistente; V3→V4 preservou Users ACTIVE/PENDING, BCrypt, Guild e status, atribuindo versão zero. A conta ACTIVE autenticou após migration e Hibernate validou o contexto.
- O container criado exclusivamente para a task foi removido após verificação de identidade. Nenhum banco Render ou container preexistente foi usado/alterado.
- Flyway 9.22.3 exibiu o warning conhecido sobre suporte oficialmente testado até PostgreSQL 15. O teste empírico em PostgreSQL 18.6 passou; upgrade do Flyway permanece dívida técnica separada.

## Segurança e riscos

- Target da operação é exclusivamente o principal persistido resolvido via `TenantService`; request não aceita identidade, Guild, role ou status como autoridade.
- Troca confirma senha atual via BCrypt, aplica limite de 72 bytes BCrypt, grava hash e incremento de versão em transação/lock; refresh pós-lock previne decisão a partir de entidade stale. A suíte cobre concorrência, tokens anteriores, claims, conta recriada/inativa e ausência de mutação indevida.
- `uid`/`cv` não são autoridade de tenant/role; ambas permanecem derivadas do User persistido. Respostas e erros do endpoint não retornam senhas/hash; UI não persiste senhas.
- Segurança da task: APROVADA. Nenhum secret real foi encontrado no escopo versionável; valores de teste são fictícios e credenciais temporárias do container ficaram somente no ambiente descartável.
- Release: BLOQUEADO até que o rollout substitua/drene instâncias antigas. Versões anteriores ainda podem aceitar JWT sem `uid`/`cv`; rollback para código antigo pode reabilitar essa aceitação. Não houve deploy e esta auditoria não declara produção validada.

## Limitações registradas

- Não houve sessão manual autenticada para inspeção visual desktop/handset. Revisão estática confirma formulário semanticamente rotulado, estados `alert/status`, controles nativos navegáveis por teclado e breakpoint mobile; testes DOM cobrem formulário/estados. Esta limitação não invalidou o comportamento automatizado nem alterou a autorização backend.
- Não houve smoke manual em Render. O plano exclui deploy desta autorização.

## Veredito

**Task Verdict: APROVADO.** Escopo do PRD cumprido; testes backend/frontend e migration PostgreSQL fresh/upgrade passaram; gates QA, Security e SRE/DevOps estão aprovados; sem scope creep ou desvio de ADR-001. Limitações UX/produção estão explícitas e não impedem validar esta alteração.

**Release Verdict: BLOQUEADO.** Condição pendente: rollout coordenado em ambiente de release, confirmar que nenhuma instância antiga atende tráfego e validar login/transição após JWT legado ser rejeitado. Não executar rollout sem autorização própria.

## Próximo passo

Executar `finish-task` e mover os artefatos para `tasks/completed/add-authenticated-password-change/`. O versionamento local e qualquer deploy permanecem ações separadas e não autorizadas nesta execução.
