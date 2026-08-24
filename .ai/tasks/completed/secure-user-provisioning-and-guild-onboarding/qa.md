# QA — Provisionamento seguro de usuários e onboarding de Guild

**Veredito: APROVADO**

## Evidências

- A integração usa contexto Spring real, `MockMvc`, persistência H2, `UserRepository`, `PasswordEncoder` e `TenantService` real por meio do fluxo HTTP.
- Os 4 cenários cobrem registro de Guild/primeiro administrador, hash e ausência de senha na resposta, provisionamento por `MARECHAL`, bloqueio de role inferior e leitura cross-tenant.
- `UserServiceImplTest` cobre hash, Guild corrente, listagem filtrada e exclusão filtrada.
- `JwtFilterTest` confirma authorities derivadas do usuário persistido e ausência de exposição sensível.
- Não há estado externo, RabbitMQ ou segredo de máquina; os testes são transacionais e reproduzíveis.
- Suíte completa: 34 testes, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`.

## Lacunas não bloqueantes

- Não cobre ainda multi-Guild, convite/e-mail, rate limiting ou RBAC completo de Members/Teams/Events/Attendance; todos estão fora do PRD desta task.
