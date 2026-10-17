# SRE/DevOps Review — add-authenticated-password-change

## Veredito SRE/DevOps

**APROVADO para conclusão da task; rollout de produção não executado.** A validação PostgreSQL real está aprovada. A promoção da alteração continua sujeita ao Release Verdict e ao procedimento de rollout abaixo.

## PostgreSQL real

- Docker Engine 29.5.3 iniciou um container isolado `postgres:18.6`, sem volume e com porta localhost dinâmica. O banco descartável começou sem tabelas públicas.
- Comando: `backend/ .\mvnw.cmd "-Dtest=PostgreSqlFlywaySchemaCiIT,PostgreSqlFlywayUpgradeCiIT" test`.
- Resultado: 3 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit code 0.
- Fresh: profile `prod`, Flyway aplicou V1–V4, history registrou quatro migrations successful/current version 4, sete tabelas de domínio, constraints/índices, `credential_version BIGINT NOT NULL DEFAULT 0`, Hibernate `validate` e Spring context aprovados.
- Upgrade V1→V4: User preexistente preservou id, Guild, status e hash BCrypt e autenticou após o upgrade.
- Upgrade V3→V4: Users ACTIVE e PENDING preservados, `credential_version=0`, status/hash/Guild intactos; Hibernate `validate` e contexto Spring passaram, e User ACTIVE autenticou.
- O container criado para a task foi removido após verificar identidade. Nenhum container PostgreSQL anterior foi iniciado, removido ou reutilizado; Render não foi acessado.

## Avaliação operacional de rollout

- V1–V3 permanecem imutáveis; V4 é aditiva e compatível com Users existentes por default zero.
- Tokens emitidos antes do deploy não contêm `uid`/`cv` e serão recusados por todas as instâncias atualizadas. Usuários terão que autenticar novamente; não há perda de senha ou dados.
- Uma instância antiga ainda servindo tráfego continua aceitando JWT legado. Portanto, o rollout deve evitar considerar a revogação efetiva enquanto instâncias antigas recebem requests: usar substituição coordenada/indisponibilidade breve ou verificar drenagem completa de versões antigas antes de declarar a transição concluída.
- Rollback para código antigo pode voltar a aceitar tokens que a versão nova rejeita. Preferir correção forward; rollback requer avaliação de segurança explícita e comunicação de que a garantia de revogação deixa de valer.
- Monitorar startup Flyway V4, Hibernate `validate`, disponibilidade e login após deploy. Não executar alteração manual de schema, editar history ou reiniciar sessões por mecanismo alternativo.
- Nenhum deploy, mudança de variável, ação Render ou smoke de produção foi realizado nesta autorização.

## Riscos e dívida técnica

- Flyway 9.22.3 declara suporte testado até PostgreSQL 15 e emitiu warning com PostgreSQL 18.6; os cenários reais da task passaram. Registrar avaliação de upgrade em task separada, sem elevar isso a falha atual.
- Inspeção visual manual autenticada em desktop/handset não foi realizada; a limitação está registrada em QA/execution.

## Conclusão

O gate SRE/DevOps aprova a migration e o preparo operacional para a task. **Release Verdict: BLOQUEADO** até executar rollout que drene/substitua as instâncias antigas e validar a transição em produção; essa atividade não foi autorizada nem realizada aqui.
