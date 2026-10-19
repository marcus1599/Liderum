# QA — reassign-team-leader-before-staff-deactivation

**Veredito QA: APROVADO**

## Evidências

- A integração usa Spring Boot real, autenticação/JWT, `TenantService`, repositories, H2/Flyway e `MockMvc`; não é apenas teste de service/mock.
- Os cenários cobrem roles autorizadas e recusadas, autenticação ausente, candidatos reduzidos por Guild/status, Team/User cross-Guild, candidato PENDING/DISABLED, validação de payload, resposta com líder, ausência de mutação nos erros e corrida reassociação/desativação.
- O contrato da UI foi testado com `HttpTestingController`; componentes cobrem seleção, confirmação, sucesso, erro sem atualização otimista, controle visual por role e erro de leitura que não é apresentado como lista vazia.
- Backend completo: `.\mvnw.cmd clean verify` — 92 testes, 0 failures/errors/skipped, BUILD SUCCESS, exit code 0. Teste unitário direcionado: `.\mvnw.cmd -Dtest=TeamServiceImplTest test` — 6/6, BUILD SUCCESS.
- Frontend: `npm test -- --watch=false --browsers=ChromeHeadless` — 79/79 SUCCESS, exit code 0; `npm run build` — SUCCESS, exit code 0.
- Nenhum teste foi removido nem relaxado para mascarar falha. Não há dependência de serviço externo; a suíte é reproduzível com os recursos de teste do repositório. A suíte passou integralmente nesta validação final.

## Adequação e limites

- Listagem/consulta/mutação são demonstradas no escopo funcional da transferência e das barreiras tenant. Não há CRUD de Users novo.
- A UI apresenta loading, vazio, erro e confirmação; falha de HTTP não é confundida com vazio. Acessibilidade básica usa label/Material/controles nativos; não foi realizada inspeção manual em dispositivo real.
- Warnings de browser mapping, Sass/budgets Angular, Flyway/H2, open-in-view e Java agent não fizeram os gates falharem e não são alterações desta task.

Os critérios aprovados do PRD/plano foram satisfeitos; não foi observada regressão na suíte.
