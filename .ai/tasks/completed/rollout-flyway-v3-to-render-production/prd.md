# PRD — Rollout controlado da Flyway V3 em produção

## Objetivo

Aplicar em produção, exclusivamente pelo startup normal da aplicação e pelo Flyway, a migration V3 já implementada e validada em PostgreSQL 18.6 descartável, e comprovar que a aplicação permanece saudável. Este documento planeja uma operação futura; não autoriza nem executa deploy, alteração de variáveis ou acesso mutável ao banco.

## Estado verificado e limites de evidência

- O handoff registra que o Render ainda está na V2 e que a V3 passou em H2 e PostgreSQL 18.6 fresh/upgrade, com Hibernate `ddl-auto=validate`.
- A última consulta read-only registrada ao Render é de 2026-10-11: V1/V2 com sucesso, uma Guild e um User, tabelas de domínio vazias e zero violações candidatas. Esses dados são históricos e devem ser revalidados imediatamente antes do rollout.
- O código V3 está no working tree local, ainda não publicado no histórico observado. A execução depende de a revisão exata com V3 estar versionada, integrada à branch de deploy e aprovada nos checks CI; versionamento/push não pertencem a este PRD.
- `GET /actuator/health` está permitido publicamente e detalhes do health estão desabilitados. O endpoint não substitui verificação de startup, Flyway ou smoke funcional.
- A aplicação usa `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` e `CORS_ALLOWED_ORIGINS`. Nenhum valor secreto deve ser lido ou registrado como parte do relatório.
- Não foi encontrado `render.yaml`; o plano não introduz Blueprint nem altera a topologia Render.

## Requisitos operacionais

1. Antes de deploy, identificar pelo painel/integração Render o Web Service de produção, o Postgres associado, região/plano, branch de deploy, estratégia de auto-deploy, instâncias em execução e estado atual do último deploy. Confirmar que DB_URL do serviço aponta ao banco esperado sem exibir ou registrar o valor.
2. Confirmar no controle Render qual backup/snapshot/restore point está disponível para o plano atual e como restaurá-lo. Se não houver recuperação verificável, ou se a operação de restore exigir sobrescrever/recriar recurso, parar e pedir decisão explícita. Não assumir retenção ou PITR pelo plano.
3. Confirmar que a revisão contendo V3 está no repositório remoto/branch de deploy e que CI do backend, incluindo `postgres-flyway`, terminou com sucesso para essa revisão. Não disparar push como parte deste plano.
4. Imediatamente antes do deploy, executar somente consultas read-only no banco-alvo: confirmar Flyway V2 com `success=true`, schema esperado sem drift e contagens agregadas das violações candidatas a V3 iguais a zero (Users sem Guild/role, Event sem name/date, Attendance sem status e duplicata de `(member_id,event_id)`). Registrar apenas números e metadados, nunca dados pessoais.
5. Se houver qualquer divergência, versão inesperada, contagem positiva, backup não verificável ou alvo ambíguo, interromper. Não limpar/alterar dados, não editar histórico Flyway e não adaptar V3 silenciosamente.
6. Obter confirmação operacional final do usuário antes de iniciar o deploy produtivo. Confirmar janela adequada e canal para acompanhar logs/estado.
7. Disparar uma única implantação da revisão aprovada no Web Service correto, preservando configuração e secrets existentes. Não trocar `DB_URL`, credenciais, profile, CORS ou JWT nesta operação.
8. Deixar o startup normal executar Flyway. Não executar SQL DDL manual, `baseline`, `repair`, scripts auxiliares de migração ou endpoint de registro para smoke.
9. Acompanhar deploy/events/logs sem copiar secrets ou PII. Confirmar datasource conectado, Flyway V3 aplicada com sucesso, Hibernate `validate`, EntityManagerFactory, Spring/Tomcat e estado `live`; detectar restart loop, health check falho ou erros de conexão.
10. Após o deploy, fazer verificações somente de leitura: `/actuator/health` 200; `flyway_schema_history` registra V3 com `success=true`; as colunas previstas estão `NOT NULL`; a unique `uk_attendance_member_event` existe; serviço continua live.
11. Executar smoke sem criar dados de domínio: login com conta de smoke autorizada, `GET /users/me` autenticado, `GET /members` tenant-scoped e preflight CORS da origin de produção. Não enviar `POST /auth/register-guild`, nem tentar gravar valores inválidos/duplicados no banco produtivo.
12. Registrar duração, resultado e eventos relevantes sem expor conteúdo sensível; atualizar state/handoff e encerrar somente após QA, Security, SRE/DevOps e Auditor.

## Política de falha e recuperação

- Se Flyway falhar ou o serviço não iniciar, interromper novas tentativas e preservar logs/estado observados. Não usar `repair`, não modificar V1/V2/V3 e não executar DDL/DML manual.
- Se V3 tiver sido aplicada mas o app não ficar saudável, avaliar retorno do Web Service à revisão anterior somente após confirmar compatibilidade dessa revisão com o schema V3. Não remover/reverter constraints automaticamente.
- Restaurar snapshot, sobrescrever banco, fazer failover ou executar qualquer ação que possa descartar gravações exige nova confirmação explícita. Se a restauração for necessária, parar e escalar ao usuário; não executar como rollback automático.
- Não prometer ausência de downtime: observar a estratégia real do serviço e comunicar impacto se houver restart/indisponibilidade.

## Segurança e privacidade

- Preservar segredo JWT, credenciais do datasource e lista de CORS; nunca imprimir valores, tokens ou payloads.
- Usar Render internal connection do mesmo workspace/região para o app, sem revelar URLs/credenciais; consultas pré/pós-deploy devem ser read-only.
- Smoke autenticado usa apenas conta existente autorizada e endpoints sem mutação.
- Isolamento tenant permanece server-side; nenhum `guildId` é fornecido como autoridade.

## Escopo

- Preflight read-only e verificação de recuperação;
- deploy do commit já aprovado que contém V3;
- observação do startup e confirmação read-only do schema;
- smoke mínimo de disponibilidade, autenticação, tenant e CORS;
- atualização dos registros operacionais e gates.

## Fora de escopo

- Implementar/modificar migration ou código;
- commit, push ou alteração de branch;
- qualquer mudança manual de schema/dados;
- rotação/troca de secrets ou configuração do Render não necessária ao alvo;
- novo banco, migração de dados, mudança de plano/região ou redesign de rollback;
- corrigir warning Flyway 9.22.3/PostgreSQL 18 (dívida técnica separada);
- iniciar outras tasks, incluindo Event/Attendance ou PostgreSQL CI.

## Critérios de aceite

- Revisão exata contendo V3 integrada à branch de deploy; CI relevante aprovado.
- Banco de produção identificado sem ambiguidade; estado antes do deploy confirmado como V2 e sem drift; todas as contagens candidatas a V3 = 0.
- Recuperação por snapshot/backup confirmada no plano vigente e procedimento conhecido; usuário autorizou a operação final.
- Deploy da revisão correta `live`, sem falha/restart loop; Flyway history V3 success; Hibernate `validate` e Spring startup concluídos.
- Health e smoke read-only aprovados; login, `/users/me`, endpoint tenant-scoped e CORS corretos; sem regressão operacional observada na janela acordada.
- QA, Security, SRE/DevOps e Auditor aprovam. Task Verdict e Release Verdict são registrados separadamente.

## Dependências e classificação

- Tipo: operação/infraestrutura, risco alto, porte LARGE.
- Dependências: task de integridade relacional concluída; revision V3 commitada/integrada e CI verde; Render access; backup/restore confirmados; autorização operacional explícita.
- Sem dependência nova, migration adicional ou ADR. A arquitetura e V3 já foram decididas/validadas.
- Até conclusão do rollout: Release Verdict desta migration permanece BLOQUEADO; não confundir com o Task Verdict aprovado da implementação.
