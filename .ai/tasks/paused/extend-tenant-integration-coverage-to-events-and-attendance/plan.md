# Plano — extend-tenant-integration-coverage-to-events-and-attendance

## Routing

- Domínios: `planning`, `architecture`, `backend`, `testing`, `security`, `audit`.
- Agents: Planner → Arquiteto (consultivo) → Backend Developer → QA → Security → Auditor.
- Skills: `create-prd`, `create-plan`; na execução, `test-backend` → `security-review` → `audit-task` → `finish-task` somente com Task Verdict APROVADO.
- Não selecionados: Frontend Developer (nenhum artefato Angular); SRE/DevOps (sem alteração de ambiente/operação); `create-migration` (nenhuma mudança de schema prevista); `create-adr` (ADR-001 já cobre a tenancy e não há decisão duradoura nova); MCP (nenhuma mudança no sistema de agentes/MCP).
- Gate: QA e Security devem aprovar antes da Auditoria. Task Verdict e Release Verdict permanecem independentes.

## Classificação e objetivo

P1, MEDIUM, qualidade e segurança. Cobrir Event e Attendance com evidência de integração HTTP para isolamento por Guild equivalente à cobertura já concluída para Member e Team.

## Diagnóstico confirmado

- `EventServiceImpl` filtra listagem e recurso individual por `guildId` obtido de `TenantService`; a criação atribui a Guild corrente internamente e `EventRequestDTO` não recebe `guildId`.
- `AttendanceServiceImpl` filtra registros pela Guild do Event e, na criação, resolve `Member` e `Event` por `id + guildId` antes de persistir. O update atual altera apenas `status`; o request contém IDs de Member/Event, mas eles não são usados no update.
- `EventRepository` e `AttendanceRepository` expõem queries tenant-scoped coerentes com esses caminhos.
- Há testes unitários de Event e Attendance, mas todos mockam TenantService/repositórios; `MultiTenantIsolationIntegrationTest` integra apenas Member/Team e usa SecurityContext montado no teste, não HTTP/JWT.
- O repositório já tem padrão `@SpringBootTest + @AutoConfigureMockMvc + @Transactional`, tokens emitidos por login real e H2/Flyway para integrações HTTP.
- `EventServiceImpl.findEventInCurrentGuild` hoje lança `RuntimeException` quando não encontra Event. O handler genérico tende a traduzir isso em 500, diferente dos caminhos Attendance (`EntityNotFoundException` → 404). Isto não é evidência de acesso cross-tenant; será validado antes de qualquer mudança de produção.

## Decisões de planejamento

1. Criar uma classe dedicada, preferencialmente `EventAttendanceTenantIsolationIntegrationTest`, em vez de ampliar o teste antigo de Member/Team. Isso mantém fixtures, falhas e evidência por domínio legíveis.
2. Usar MockMvc com tokens reais, produzidos após onboarding/login, pois valida a cadeia completa mais adequadamente que apenas o service. Persistir Events, Members e Attendances das duas Guilds diretamente pelos repositórios é permitido para montar o estado inicial sem testar o endpoint de setup.
3. Usar `MARECHAL` de Guild A nas operações mutáveis. Assim, uma recusa não pode ser atribuída à role; ela evidencia a fronteira de tenant. Incluir um cenário curto de role sem permissão apenas se necessário para afirmar 403 versus 404 sem duplicar os testes RBAC existentes.
4. Esperar 404 para IDs e referências pertencentes a Guild B, preservando não enumeração. Caso Event devolva 500, não adaptar a expectativa para mascarar a falha: registrar o fato e escalar a correção de produção mínima como decisão separada, salvo autorização explícita para incluí-la nesta task.
5. Não criar migration: as tabelas, FKs e índices necessários já constam da V1; a task testa a aplicação sobre esse schema, não altera sua estrutura.

## Etapas de execução

1. Confirmar que não há task concorrente ou diff prévio e revisar PRD, ADR-001, handlers de exceção e configurações de teste antes de editar.
2. Backend Developer cria fixtures transacionais para Guild A/B, seus MARECHAIS autenticáveis, Events, Members e Attendances distintos. Usar nomes únicos por teste ou rollback transacional; limpar qualquer estado de segurança quando aplicável.
3. Obter JWTs pelo endpoint de login e executar via MockMvc:
   - Event: `GET /events`, `GET /events/{id}`, `PUT /events/{id}`, `DELETE /events/{id}`; criar Event em Guild A e confirmar sua Guild persistida.
   - Attendance: `GET /attendances`, `GET /attendances/{id}`, `PUT /attendances/{id}`, `DELETE /attendances/{id}`; `POST /attendances` com Member B/Event A e Member A/Event B.
4. Após cada recusa, consultar repositórios para verificar ausência de novo Attendance, status imutável e recurso de Guild B ainda existente.
5. Se algum cenário cruzado permitir leitura, listagem, atualização, exclusão ou criação, interromper a execução, preservar a evidência, acionar Security e não corrigir produção nesta task.
6. Se houver bloqueio sem acesso, como status 500 incompatível para Event, registrar no `execution.md`, classificar a origem e solicitar decisão antes de alterar produção. Não trocar a expectativa por 500 para fazer a task parecer aprovada.
7. Executar primeiro o teste dirigido, depois `./mvnw.cmd clean verify`; registrar contagens, failures/errors/skipped, BUILD e exit code verificáveis.
8. QA avalia a pilha realmente atravessada, isolamento das fixtures, ausência de falsos positivos, idempotência e preservação de dados. Security avalia IDOR, enumeração, referências cross-Guild e origem de qualquer achado. Auditor compara diff, PRD e evidências; finalizar somente se aprovado.

## Arquivos previstos

- `backend/src/test/java/com/example/Liderum/Security/EventAttendanceTenantIsolationIntegrationTest.java` (ou pacote `Tenancy`, se o padrão final se mostrar mais coerente);
- artefatos em `.ai/tasks/active/extend-tenant-integration-coverage-to-events-and-attendance/`;
- `state.md` e `handoff.md` somente após finalização aprovada.

Não há arquivo de produção, migration, configuração ou dependência previsto. Qualquer necessidade desses arquivos exige reavaliação de escopo.

## Matriz de testes planejados

| Domínio | Cenário | Evidência esperada |
| --- | --- | --- |
| Event | Listagem por Guild | JWT Guild A recebe apenas Event A. |
| Event | Leitura cross-Guild | `GET` de Event B por Guild A é bloqueado, preferencialmente 404. |
| Event | Update/delete cross-Guild | Operações por MARECHAL A são bloqueadas; Event B não muda e continua persistido. |
| Event | Criação | `POST` A cria Event associado à Guild A sem campo `guildId` externo. |
| Attendance | Listagem por Guild | JWT Guild A recebe apenas Attendance do Event A. |
| Attendance | Leitura cross-Guild | `GET` de Attendance B por Guild A é bloqueado, preferencialmente 404. |
| Attendance | Update/delete cross-Guild | Operações são bloqueadas; status e registro B preservados. |
| Attendance | Member cross-Guild | `POST` com Member B e Event A é bloqueado, sem novo registro. |
| Attendance | Event cross-Guild | `POST` com Member A e Event B é bloqueado, sem novo registro. |
| Segurança HTTP | 403 versus 404 | Role insuficiente recebe 403 antes do serviço; ID de outra Guild não é exposto como autorização concedida. |
| Regressão | Suite completa | `./mvnw.cmd clean verify` finaliza sem falhas novas. |

## Riscos e escalonamento

- **Acesso cross-Guild comprovado:** achado de segurança potencialmente alto; parar, manter task ativa, registrar origem e acionar Security/sessão principal.
- **Status HTTP 500 sem acesso:** defeito preexistente de mapeamento de erro possível; não incluí-lo silenciosamente. Solicitar autorização se a correção mínima for necessária para o aceite de 404.
- **Fixture poluída por profile dev:** não depender do seed; identificar os recursos pelos IDs criados na própria transação. Se o profile de teste não puder ser determinístico, escalar antes de mudar configuração ampla.
- **RabbitMQ durante criação de Event:** o publisher real pode estar presente; para evitar acoplamento operacional sem alterar produção, confirmar o comportamento do contexto atual antes de escrever o teste. Se a publicação impedir uma integração isolada, escalar em vez de mockar a camada de tenant/persistência.

## Critérios de conclusão

- Todas as operações e referências cross-Guild previstas estão cobertas por integração HTTP real e não apresentam bypass.
- Não há mutação parcial após tentativas bloqueadas.
- A diferença entre 403 por RBAC e 404 por escopo de tenant está evidenciada; qualquer 500 inesperado está escalado, não mascarado.
- Teste dirigido e suíte Maven completa possuem resultados finais verificáveis.
- QA e Security aprovam; Auditor confirma que o diff contém somente testes/documentação e nenhum scope creep.
- Status final: **PRONTO PARA EXECUÇÃO**, condicionado à autorização explícita para implementar os testes e ao escalonamento obrigatório se revelar acesso cross-Guild ou exigir correção de produção.
