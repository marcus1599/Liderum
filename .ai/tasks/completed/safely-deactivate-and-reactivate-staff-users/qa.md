# QA — safely-deactivate-and-reactivate-staff-users

**Veredito: APROVADO**

## Evidências

- O fluxo é validado por testes HTTP/MockMvc com contexto Spring, Security/JWT, `TenantService`, services, repositories, persistência JPA e H2/Flyway reais; não são apenas mocks de service.
- Cenários cobrem ACTIVE→DISABLED→ACTIVE, JWT antigo recusado antes e depois da reativação, novo login válido, hash de senha preservado, transições inválidas/repetidas, hierarquia, autoalteração, usuário de outra Guild, último MARECHAL ativo, Team leader com 409 sem mutação parcial e concorrência entre duas desativações.
- O cenário concorrente confirma um sucesso e uma recusa 403, mantendo exatamente um MARECHAL ativo. Fixtures não transacionais usam sufixos UUID para evitar colisões entre execuções.
- Frontend mantém confirmação, busy state, feedback de sucesso/conflito e controles visuais por role; o backend permanece responsável pela autorização.
- Nenhuma dependência de serviço externo foi introduzida. A suíte usa banco H2 descartável e valores exclusivamente de teste.

## Validação executada

- Backend direcionado: 25 testes; 0 failures, 0 errors, 0 skipped; BUILD SUCCESS.
- Backend completo: `cd backend; .\\mvnw.cmd clean verify`; 86 testes; 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit code 0.
- Frontend completo: `npm test -- --watch=false --browsers=ChromeHeadless`; 73/73; exit code 0.
- Frontend: `npm run build`; SUCCESS; exit code 0.
- A execução frontend completa final passou; warnings de build registrados são não bloqueantes e não introduzidos por esta task.

## Conclusão

Os critérios testáveis do PRD foram atendidos, as alterações não dependem de estado externo e os testes são repetíveis. Não foi observado falso positivo ou regressão nos cenários validados. QA APROVADO.
