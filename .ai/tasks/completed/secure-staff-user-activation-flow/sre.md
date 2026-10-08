# SRE/DevOps — secure-staff-user-activation-flow

## Retomada — 2026-10-07

### Revisão operacional

- O workflow `postgres-flyway` usa serviço descartável `postgres:18.6`, credenciais exclusivas de CI e `DB_URL` local do runner; não acessa Render nem reutiliza segredos de produção.
- O comando do job inclui `PostgreSqlFlywaySchemaCiIT` (banco vazio com V1/V2 e Hibernate validate) e `PostgreSqlFlywayUpgradeCiIT` (aplica V1, insere Guild/User legado, aplica V2, valida preservação e inicializa contexto `prod`).
- O teste de upgrade cria um schema UUID isolado dentro do banco descartável e remove somente esse schema em `finally`.
- A alteração do workflow foi inspecionada estaticamente. `test-compile` e `clean verify` locais passaram, mas `clean verify` não seleciona classes `*CiIT` por padrão.

### Bloqueio operacional

- `docker version`/`docker info`: cliente Docker 29.5.3 disponível; Docker Engine do contexto `desktop-linux` indisponível (`dockerDesktopLinuxEngine` pipe ausente).
- Não foi encontrado serviço PostgreSQL local ativo na porta 5432.
- O job do GitHub Actions desta alteração não foi executado nem há resultado remoto verificável nesta retomada. Nenhum commit/push foi feito para dispará-lo.

## Veredito

**ESCALONAR / BLOQUEADO** — a configuração estática do job é apropriada e usa somente credenciais descartáveis, mas a integração PostgreSQL 18 não foi executada. Solicita-se disponibilizar o Docker Engine local ou um run autorizado do workflow; nenhuma operação em banco real deve ser usada como substituto.

## Veredito final — 2026-10-07

- Docker Desktop 4.77.0 / Engine 29.5.3 foi validado com permissão autorizada.
- O container descartável `postgres:18.6` iniciou saudável; o banco iniciou com zero tabelas no schema `public`. O teste usou credenciais exclusivas locais, sem volume persistente e sem conectar ao Render.
- O mesmo comando de seleção configurado em `backend.yml` executou os testes fresh-schema e upgrade; ambos passaram. Contexto Spring `prod`, Tomcat em porta aleatória e autenticação do User legado passaram.
- O container específico desta task foi encerrado e removido (`--rm`). Nenhum outro container foi parado ou modificado.
- O workflow foi revisado e seu comando correspondente foi executado localmente contra PostgreSQL 18.6. Não houve commit/push nem execução remota do GitHub Actions.

**SRE/DevOps: APROVADO.** A validação operacional reproduzível funcionou; o warning de suporte declarado pelo Flyway 9.22.3 continua dívida técnica, sem falha observada nos testes.

## Suplemento final — 2026-10-07

- A execução final em PostgreSQL 18.6 usou container novo, credenciais locais descartáveis, sem volume e schema vazio; a execução combinada dos dois testes terminou 2/2 com `BUILD SUCCESS`.
- Fresh V1/V2, upgrade com User legado, `flyway_schema_history`, Hibernate `validate`, startup `prod`/Tomcat e autenticação foram exercitados. O container exato foi removido ao final.
- A suíte `clean verify` após os últimos testes terminou 72/72, `BUILD SUCCESS`, exit code 0. Nenhuma mudança de configuração externa ou secret de produção foi necessária.
- O workflow PostgreSQL foi validado localmente com o mesmo seletor. Isso não afirma execução remota de GitHub Actions nesta retomada.

**SRE/DevOps: APROVADO.** Warning de compatibilidade declarado pelo Flyway permanece dívida técnica conhecida, sem falha observada no PG18.6 testado.
