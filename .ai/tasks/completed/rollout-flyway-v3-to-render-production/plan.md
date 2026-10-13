# Plan — rollout-flyway-v3-to-render-production

## Classificação e routing

- Porte: LARGE; domínio principal SRE/DevOps, database/release; risco de alteração de schema produtivo.
- Agents: Planner (task); Backend Developer consultivo (revisão de V3/compatibilidade da revisão); SRE/DevOps (responsável operacional); Security (secrets, acesso/privacidade); QA (critérios e smoke); Auditor (gate final).
- Não selecionado: Arquiteto, pois V3 e a arquitetura de persistência já foram aprovadas e não há decisão nova; Frontend Developer, pois só se verifica CORS/smoke browser, sem alteração UI.
- Skills: `create-prd`, `create-plan`, `render-postgres`; na execução, `security-review`, `audit-task` e `finish-task` condicionada à auditoria aprovada. Não usar `create-migration` ou `test-backend` para modificar código: a migration/testes já foram implementados e aprovados.

## Pré-condições bloqueantes

1. Revisar `tasks/completed/enforce-relational-database-integrity/` e confirmar a revisão contendo V3 em Git remoto/branch efetivamente configurada no Render. No estado inspecionado em 2026-10-11, alterações V3 e `.ai` permaneciam locais; obter versionamento/push separado antes de iniciar.
2. Confirmar CI verde nessa revisão, incluindo `postgres-flyway` e `clean verify`.
3. Confirmar no Render identidade do Web Service, Postgres atual e vínculo de conexão. Preservar valores de ambiente; registrar apenas nomes/estado de presença, nunca valores.
4. Verificar no Dashboard plano, snapshot/backup disponível, retenção e procedimento possível de restauração. Se backup recuperável não estiver disponível, interromper e solicitar decisão; não assumir que free tier oferece retenção suficiente.
5. Usuário autoriza explicitamente o início do deploy em produção depois de apresentados alvo, backup, preflight e impacto operacional. A autorização atual é somente para planejar.

## Procedimento executável

### A. Readiness e freeze

- Registrar horário, revisão/commit alvo e estado atual do serviço.
- Conferir deploy anterior `live`, perfil `prod`, região, quantidade/estratégia de instâncias, auto-deploy e estratégia de health check diretamente no Render.
- Confirmar que a equipe não está publicando outra alteração de banco durante a janela.
- Verificar backup/snapshot de recuperação, horário, recurso-alvo e procedimento de restore no plano/console atual. Não criar/exportar dump com dados pessoais sem autorização própria.

### B. Preflight read-only imediatamente antes

- Conectar ao Postgres alvo somente em modo de consulta; não registrar URL, usuário ou senha.
- Confirmar que schema é `public`, histórico Flyway termina em V2 e todos os registros têm `success=true`.
- Comparar tabelas, constraints e índices com as migrations V1/V2 esperadas para detectar drift.
- Reexecutar consultas agregadas de `tasks/completed/enforce-relational-database-integrity/inventory.md`: contagem de tabelas; Users sem Guild/role; Event sem nome/data; Attendance sem status; duplicidade Member/Event; vínculos cross-Guild monitorados. Registrar somente rótulos e totais.
- **Gate stop:** qualquer drift, resultado diferente do esperado, tabela ausente, versão que não seja V2, linha incompatível ou relação/anomalia desconhecida interrompe o rollout. Não executar saneamento.

### C. Deploy

- Com confirmação explícita, implantar uma única vez a revisão aprovada no serviço correto.
- Não atualizar variáveis, banco, região ou plano.
- Aguardar logs/eventos finais. Ordem esperada: datasource conectado → Flyway valida/aplica V3 → Hibernate `validate` → EntityManagerFactory → Spring/Tomcat inicia → serviço `live`.
- Confirmar que não houve deploy simultâneo/concorrente e que não há restart loop, erro Flyway ou health check falho.

### D. Pós-deploy read-only e smoke

- `GET /actuator/health` deve responder 200; sem exposição de detalhes.
- Consulta read-only confirma Flyway V3 com `success=true`, cinco colunas `NOT NULL` e constraint unique `uk_attendance_member_event`.
- Confirmar que o Web Service segue `live` e que logs posteriores não mostram falhas repetidas de datasource, migration ou Hibernate.
- Smoke mínimo sem gravação de recursos: login usando conta de smoke autorizada, `GET /users/me`, `GET /members` no tenant autenticado e preflight CORS para `https://theliderum.vercel.app`; opcionalmente conferir origin não permitida sem `Access-Control-Allow-Origin`.
- Não criar Guild, User, Member, Event ou Attendance; não testar constraints por tentativas de escrita no banco produtivo.

### E. Falha / recuperação

- Falha de V3 ou startup: pausar, guardar trecho seguro do log e identificar estágio/SQLState sem secrets; não repetir às cegas, não executar Flyway `repair`, não editar history ou schema manualmente.
- V3 aplicada mas app não saudável: suspender novos deploys; avaliar rollback apenas do app à revisão anterior depois de confirmar compatibilidade com o schema V3. Não criar migration down nem remover constraints.
- Restore/overwrite do Postgres: ação destrutiva, fora da autorização de planejamento. Solicitar aprovação explícita e confirmar impacto/perda de gravações antes de qualquer operação.
- Se a aprovação de backup ou de recuperação não puder ser demonstrada, ficar bloqueado sem deploy.

### F. Gates e encerramento

- QA valida evidências pré/pós, sucesso dos smoke e ausência de dados escritos.
- Security confirma que nenhum segredo/PII foi exposto, o target e acesso foram restritos e as verificações tenant foram read-only.
- SRE/DevOps avalia estabilidade, logs, tempo de deploy, estratégia de recuperação e disponibilidade.
- Auditor compara PRD, autorização, revisão implantada, preflight, resultado Flyway, smoke e Git.
- Registrar Task Verdict e Release Verdict separadamente. `finish-task` somente após Task Verdict APROVADO; Release APROVADO apenas se todos critérios produtivos passarem.

## Critérios objetivos de parada

- Código V3 ainda não está no remoto/branch do Render, CI falhou ou target não é inequívoco.
- Backup/restore indisponível ou incerto; serviço/banco fora do estado esperado.
- Qualquer contagem de incompatibilidade positiva, drift ou erro de leitura.
- Deploy falha em Flyway, validação Hibernate, health ou smoke.
- Necessidade de alterar secrets/configuração, SQL manual, migration, plano/infra ou política de segurança.
- Solicitação de rollback do banco sem autorização adicional.

## Estado

Planejamento apenas. Nenhuma chamada Render, deploy, consulta a banco ou alteração de código foi realizada nesta task de rollout. O Release Verdict V3 permanece BLOQUEADO até execução autorizada e evidência completa.
