# SRE/DevOps Review — Production Flyway Recovery

## Resultado operacional

- Novo PostgreSQL 18.6 free em Oregon criado após remoção autorizada do legado descartável.
- Serviço `Liderum` apontado ao banco novo; deploy final está `live`.
- Flyway aplicou V1; Hibernate `validate` e Tomcat iniciaram na porta 10000.
- O warning Flyway 9.22.3/PostgreSQL 18 permanece dívida técnica, sem impedir a execução comprovada.

## Riscos e pendências

- `DB_URL` precisa permanecer em formato JDBC; a tentativa inicial sem `jdbc:` falhou antes da migration e foi corrigida apenas na configuração do ambiente.
- O serviço não usa `healthCheckPath`; `/actuator/health` protegido não bloqueou o deploy.
- Rate limiting e CORS exigem task operacional/de segurança separada antes de liberar produção.
- Recomendar `validate-flyway-migrations-against-postgresql-in-ci` como task de qualidade separada.

## Veredito

**APROVADO** para a recuperação Flyway. O Release continua bloqueado pelos achados preexistentes registrados pela Security.
