# Execução — reassign-team-leader-before-staff-deactivation

**Data:** 2026-10-19
**Task Security Verdict:** APROVADO
**Task Verdict:** APROVADO
**Release Verdict:** BLOQUEADO pelo achado global preexistente RabbitMQ AMQP 406, fora desta task.

## Implementação

- `TeamResponseDTO` agora identifica líder por `leaderId` e `leaderName`.
- `GET /teams/leader-candidates` retorna somente `id` e `username` de Users ACTIVE da Guild do ator; MARECHAL, GENERAL e MAJOR podem consultar. CAPITÃO/SOLDADO e anônimo são recusados pela autorização existente.
- `PUT /teams/{teamId}/leader` recebe DTO explícito com `leaderId` obrigatório, resolve Team e candidato via Guild obtida do `TenantService`, revalida ACTIVE sob lock e persiste somente a referência `leader`.
- `POST /teams` mantém o criador como líder quando `leaderId` não é enviado; quando fornecido, valida Guild e status ACTIVE. O líder padrão também é revalidado sob lock para fechar a corrida com desativação concorrente.
- Reassociação e mudança de status adquirem primeiro os locks ordenados dos MARECHALs e depois os recursos envolvidos. A integração exercita corrida entre reassociação e desativação; não altera a proteção do último MARECHAL.
- A UI `/teams` mostra o líder, carrega candidatos sob demanda, restringe o controle visual às roles permitidas, pede confirmação e só atualiza a liderança após resposta bem-sucedida. Falhas não viram estado vazio nem atualização otimista.
- Sem migration, dependência, alteração de JWT/ADR-001, backend fora do domínio Team, onboarding de usuário ou mudança automática de status.

## Validações empíricas

| Verificação | Resultado |
| --- | --- |
| `backend/.\mvnw.cmd -Dtest=TeamServiceImplTest test` | 6 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit 0 |
| `backend/.\mvnw.cmd clean verify` após a última mudança | 92 testes, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; exit 0 |
| `frontend/npm test -- --watch=false --browsers=ChromeHeadless` | 79/79 SUCCESS; exit 0 |
| `frontend/npm run build` | SUCCESS; exit 0 |
| `git diff --check` | sem erro de whitespace; Git reporta apenas avisos de conversão LF/CRLF para arquivos Java/TS |

Os testes frontend/build exibem warning não bloqueante de `baseline-browser-mapping` desatualizado e warnings Sass/budget preexistentes. Maven também reporta warnings de runtime (Flyway/H2, open-in-view e Java agent) não introduzidos por esta task.

## Cobertura exercitada

- Contrato HTTP, isolamento tenant, roles, usuários PENDING/DISABLED, referências cross-Guild, ausência de mutação em rejeições, payload extra sem mass assignment e concorrência com desativação.
- Contrato HTTP Angular, exibição/atualização após confirmação, tratamento de falha sem mutação otimista, visibilidade por role e falha na leitura de candidatos.
- Testes executados com Spring, `TenantService`, repositories, persistência H2/Flyway e `MockMvc`; não houve alteração de produção além do escopo aprovado.

## Limitações

- Lock/concurrency foi validado na infraestrutura de teste H2, não em PostgreSQL nesta task.
- Não foi feita inspeção visual manual desktop/handset; responsividade e acessibilidade foram verificadas por estrutura/markup e specs, não por sessão de navegador.
- Release continua BLOQUEADO pelo RabbitMQ AMQP 406 já adiado pelo usuário; isso não foi causado nem agravado pela tarefa.
