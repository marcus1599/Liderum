# PRD — safely-deactivate-and-reactivate-staff-users

**Status:** aprovado para execução; política de líder de Team confirmada pelo usuário.

## Problema e objetivo

O lifecycle possui estados `PENDING`, `ACTIVE` e `DISABLED`, mas não há operação administrativa para desativar ou reativar uma conta. O objetivo é permitir offboarding reversível de Staff da própria Guild, preservando dados e evitando autenticação de conta suspensa, sem enfraquecer RBAC ou deixar uma Guild sem MARECHAL ativo.

## Requisitos propostos

1. MARECHAL pode alterar status de User da Guild, exceto o próprio. GENERAL pode alterar status somente de MAJOR, CAPITÃO e SOLDADO da Guild; não pode gerir a si, outro GENERAL ou MARECHAL. MAJOR/CAPITÃO/SOLDADO não gerem status.
2. Transições permitidas: `ACTIVE → DISABLED` e `DISABLED → ACTIVE`. PENDING fica sob fluxo existente de ativação/regeneração; não converter PENDING com endpoint de status. Transição repetida ou estado incompatível falha sem mutação.
3. Reativação exige hash de senha já definido; não define nem retorna senha. A pessoa deve autenticar novamente.
4. Desativação incrementa `credentialVersion` atomicamente. Token anterior deve continuar inválido após reativação; apenas novo login em conta ACTIVE cria sessão válida.
5. O último MARECHAL `ACTIVE` não pode ser desativado, removido ou rebaixado. A regra existente para role/exclusão passa a contar somente MARECHAL ACTIVE; proteger concorrência por Guild e revalidar estado após lock.
6. Ator e alvo são resolvidos server-side pela Guild do ator via `TenantService`. IDs de outra Guild não revelam existência nem alteram registros. Sem `guildId`, role ou senha no contrato de status.
7. Não permitir auto-desativação. Reativação de conta DISABLED segue RBAC da hierarquia vigente.
8. Recusar desativação de User que lidera Team com HTTP 409 seguro e sem alteração parcial, até reassociação em task separada. Não reatribuir líder automaticamente. A resposta não expõe dados de outra Guild.
9. Frontend `/users` apresenta ações de status conforme RBAC/estado, confirmação, loading e feedback; backend continua fonte final de autorização.

## Contrato proposto

- `PUT /users/{id}/status` com DTO explícito contendo apenas `status: ACTIVE | DISABLED`; retorna `UserResponseDTO` sem credenciais.
- 401 sem autenticação; 403 por role/self/último MARECHAL; 404 para alvo cross-Guild/não encontrado; conflito de estado ou liderança usa resposta 4xx segura (preferência 409, confirmar handler efetivo em testes). Validar rejeição de campos extras/mass assignment.

## Escopo

- Endpoint, DTO, service e consulta tenant-scoped;
- proteção do último MARECHAL ACTIVE em status, role e exclusão;
- revogação via credential version e testes HTTP/integração;
- ações/feedback mínimos em `/users` e testes Angular;
- QA, Security, Auditoria e atualização dos artefatos da task.

## Fora de escopo

- Reassociação/seleção de líder de Team e mudanças gerais em Team;
- recuperação/reset de senha, e-mail, MFA, mudança de username/e-mail;
- remoção de User substituída globalmente, RBAC de outros domínios, multi-Guild;
- alteração estrutural de schema, migration, JWT, ADR-001, RabbitMQ, CI/CD ou infraestrutura;
- redesign, nova biblioteca de UI ou estado global.

## Segurança

- User desativado não autentica por login ou Bearer; reativar não restaura sessão anterior.
- Preservar isolamento, authorities e tenant derivados do User persistido.
- Nenhum hash/senha/token/log sensível em payload ou resposta. Erros cross-Guild não enumeram conta.
- Nenhuma mudança de banco prevista; `UserStatus` e `credentialVersion` já existem.

## Aceitação

- Testes HTTP com duas Guilds cobrem RBAC, self, transições e cross-Guild sem mutação.
- Um único MARECHAL ACTIVE não pode ser removido/rebaixado/desativado mesmo se houver outro MARECHAL PENDING/DISABLED; com dois ACTIVE, transição permitida conforme hierarquia. Concorrência não deixa zero ACTIVE.
- JWT anterior recebe 401 após desativação e continua 401 depois de reativar; login novo funciona após reativação. User de outra conta não é afetado.
- User líder de Team: 409 até reassociação; sem mutação parcial.
- Testes backend direcionados e `mvnw.cmd clean verify`; testes Angular completos e build. QA → Security → Auditor; SRE condicional. Vereditos Task e Release independentes.

## Risco global conhecido

Erro RabbitMQ 406 preexistente foi adiado pelo usuário e não será tratado nesta task. A task não libera por si só o Release Verdict global.
