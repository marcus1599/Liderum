# QA — RBAC e fronteiras tenant de User

**Veredito: APROVADO**

## Evidências

- `RbacUserTenantBoundariesIntegrationTest` usa Spring Security, `MockMvc`, JWT real, persistência H2, `TenantService`, repositories e services reais; não há mock que contorne autorização ou tenancy.
- Os 10 cenários comprovam criação/listagem/leitura por GENERAL, bloqueios contra GENERAL/MARECHAL, alteração e remoção abaixo de GENERAL, poderes superiores do MARECHAL, proteção do último MARECHAL, papéis inferiores, perfil próprio, Team, AdminController e cross-Guild.
- O teste do service cobre contrato explícito, hash, tenant e a proteção do último MARECHAL; a suíte anterior de isolamento multi-tenant continua cobrindo serviços de domínio.
- Testes direcionados: 19/19; suíte completa: 46/46; ambos com zero failures/errors/skips e exit code 0.
- Não há estado externo, RabbitMQ, banco local ou segredo de máquina. Cada cenário é transacional e isolado.

## Lacunas não bloqueantes

- Não há teste de concorrência com duas requisições simultâneas; o service usa transação e lock pessimista por Guild para serializar a regra do último MARECHAL. Teste concorrente dedicado pode ser acrescentado se surgir requisito de carga.
- Edição de credenciais, lifecycle de conta e frontend permanecem fora do PRD.
