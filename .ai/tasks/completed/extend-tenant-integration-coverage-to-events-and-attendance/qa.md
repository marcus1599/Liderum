# QA — Event e Attendance Tenant Integration

## Veredito

**APROVADO**

## Evidências

- A classe dedicada percorre HTTP, filtro JWT, autorização Spring, `TenantService`, services, repositories, H2 e baseline Flyway. Tokens são obtidos pelo login real de usuários criados pelo onboarding.
- As oito verificações cobrem listagem, leitura, atualização, exclusão, criação tenant-scoped de Event, listagem/leitura/atualização/exclusão de Attendance, referências cruzadas Member/Event, ausência de mutação e a separação entre 403 por RBAC e 404 por recurso de outra Guild.
- Fixtures são `@Transactional`, criadas por cenário e não dependem de banco externo, credencial local, seed dev ou RabbitMQ. O publisher é mockado exclusivamente para evitar efeito de rede externo; nenhuma fronteira de segurança ou persistência foi simulada.
- Não há assertion removida para mascarar falha: o retorno 500 inicial de Event foi investigado, corrigido de forma mínima e então validado como 404.
- Teste dirigido: 8 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`.
- Suíte completa: 66 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`.

## Regressões observadas

- Nenhuma.
