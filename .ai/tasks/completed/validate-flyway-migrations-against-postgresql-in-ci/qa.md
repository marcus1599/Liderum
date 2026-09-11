# QA — PostgreSQL Flyway no CI

## Veredito

**APROVADO**

## Evidências

- Workflow `Build Backend #15` executou os dois jobs previstos: `build` e `postgres-flyway`, ambos concluídos com sucesso.
- O job PostgreSQL usou service container PostgreSQL 18.6 descartável e executou `PostgreSqlFlywaySchemaCiIT` com profile `prod`.
- A suíte H2 local permaneceu verde: 66 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`, exit code 0.
- O teste PostgreSQL verifica history Flyway V1, seis tabelas, pelo menos oito FKs e os seis índices tenant/join definidos na baseline. O contexto Spring iniciou com `ddl-auto=validate`.
- O teste `*IT` não altera a suíte local padrão nem depende de dados externos, Render ou credenciais reais.

## Observações

- Não foi possível repetir PostgreSQL localmente porque o Docker daemon desta máquina permaneceu inacessível; o run remoto forneceu a evidência equivalente no ambiente de CI.
- Warnings de depreciação de Node.js 20 e `setup-java@v4` não são falhas desta task.
