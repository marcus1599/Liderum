# PRD — Corrigir falha da baseline Flyway em PostgreSQL de produção

**Classificação:** P0 — incidente de produção / infraestrutura / qualidade — STRUCTURAL.

## Impacto e contexto

O Render estabelece conexão com PostgreSQL 18.4, mas a V1 é aplicada sobre schema legado sem histórico Flyway. A causa confirmada é SQLState `42P07`: `V1__baseline_schema.sql`, linha 1, `CREATE TABLE guilds (...)`, falha porque a relação já existe. Os erros posteriores de `EntityManagerFactory`, repositórios e segurança são cascata da migration não aplicada.

Evidência disponível:

```text
Successfully validated 1 migration
Current version of schema "public": << Empty Schema >>
Migrating schema "public" to version "1 - baseline schema"
Migration of schema "public" to version "1 - baseline schema" failed!
Changes successfully rolled back.
```

Os dados legados foram declarados descartáveis. A estratégia é reprovisionar banco novo/vazio, criado exclusivamente por Flyway; baseline/reconciliação do legado está descartada. A V1 passou apenas em H2; isso não constitui validação PostgreSQL suficiente.

## Objetivo

Identificar e reproduzir a causa SQL real da falha em PostgreSQL vazio, aplicar somente a menor correção comprovada e validar que Flyway V1, Hibernate `validate` e o contexto Spring sobem em PostgreSQL descartável sem manipular o banco de produção.

## Requisitos de investigação

1. Obter o trecho completo do log Render contendo a exceção Flyway/JDBC, com statement, SQLState, vendor code, mensagem PostgreSQL, linha/objeto da V1 e stack trace causal.
2. Revisar `V1__baseline_schema.sql` contra mappings JPA e dialect PostgreSQL, sem alterar SQL por tentativa.
3. Comparar explicitamente H2 e PostgreSQL para identity, tipos, nomes, constraints, FKs, ordem de criação e índices.
4. Reproduzir primeiro a falha em PostgreSQL descartável, preferencialmente Docker na versão 18 ou a mais próxima comprovadamente disponível; banco vazio → Flyway V1 → Hibernate validate → contexto Spring.
5. Verificar estado de produção somente por evidência de log/painel: schema inicialmente vazio e rollback informado não provam sozinhos o estado final. Não executar comandos SQL, reset, drop, repair, baseline, force ou alteração manual na produção.
6. Classificar o aviso Flyway 9.22.3/PostgreSQL 18.4 como warning, incompatibilidade relevante, possível fator ou causa comprovada com base em evidência. Não atualizar dependência apenas para suprimir warning.

## Requisitos de correção, condicionados à causa comprovada

1. Não editar V1 enquanto não for conhecido se ela já foi aplicada com sucesso em banco persistente relevante. O usuário/sessão deve confirmar essa condição antes de qualquer reescrita.
2. Se V1 nunca foi aplicada com sucesso em ambiente persistente oficial e a causa estiver em sua SQL, propor a menor correção; a aplicação depende de autorização explícita depois da investigação.
3. Se V1 já foi aplicada com sucesso em ambiente persistente relevante, não reescrevê-la; escalar estratégia de migration nova/compatibilidade.
4. Se a causa for versão/dependência Flyway, justificar atualização compatível com Spring Boot 3.2, registrar em `lib.md`, validar segurança/compatibilidade e não modificar outras dependências sem necessidade.
5. Preservar `ddl-auto=validate`, onboarding, RBAC, tenancy, rate limit e contrato de API.

## Escopo

- investigação e reprodução descartável PostgreSQL;
- possível correção mínima de migration ou dependência, somente após causa e autorização;
- teste automatizado PostgreSQL proporcional quando a infraestrutura de teste estiver definida e aprovada;
- validações backend, QA, Security, SRE e auditoria;
- documentação de incidente e estado operacional.

## Fora do escopo

- experimentação, limpeza ou alteração manual no banco Render/produção;
- `baselineOnMigrate`, `repair`, edição de `flyway_schema_history`, `clean`, drop/reset ou force migration;
- refatoração de entidades, novo schema funcional, frontend, RBAC/JWT, rate limit, RabbitMQ, CI completo ou migração de plataforma;
- upgrade especulativo de Flyway;
- retomar `extend-tenant-integration-coverage-to-events-and-attendance` antes de liberar o incidente.

## Critérios de aceitação

- causa SQL documentada com evidência concreta ou bloqueio formal por ausência da evidência externa;
- reprodução em PostgreSQL descartável, não apenas H2;
- após correção autorizada: V1 aplicada em banco vazio, `flyway_schema_history` com versão 1, Hibernate em `validate` e contexto Spring iniciado;
- `./mvnw.cmd clean verify` aprovado, com regressões de onboarding, RBAC, tenancy, rate limiting e Flyway preservadas;
- produção sem alteração manual e sem estado parcial conhecido não tratado;
- Release Verdict somente retorna a APROVADO se PostgreSQL for validado e não existir outro bloqueador conhecido.

## Riscos

| Risco | Tratamento |
| --- | --- |
| Alterar V1 aplicada | Bloqueio explícito até confirmar histórico persistente oficial. |
| Diagnóstico por tentativa | Exigir SQLState/statement ou reprodução que o produza. |
| H2 ocultar incompatibilidade | PostgreSQL descartável é gate obrigatório para correção. |
| Docker indisponível localmente | Registrar bloqueio operacional e usar alternativa descartável autorizada; nunca Render produção. |
| Upgrade Flyway breaking | Avaliação de Spring Boot, dependências e teste PostgreSQL antes de adoção. |
| Dados parciais em produção | Não manipular o banco; solicitar inspeção somente leitura/backup conforme canal operacional autorizado. |
