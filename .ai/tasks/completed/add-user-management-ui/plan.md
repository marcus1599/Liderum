# Plano — add-user-management-ui

## Routing

- Domínios: `planning`, `frontend`, `security`, `testing`, `audit`, `documentation`.
- Agents: Planner → Backend Developer (consultivo de contrato) → Frontend Developer → QA → Security → Auditor.
- Skills: `create-prd`, `create-plan`, `test-frontend`, `security-review`, `audit-task`, `finish-task`.
- Não selecionados: Arquiteto (ADR-001 e política RBAC já definem tenancy/autoridade); SRE/DevOps; `create-adr`; `create-migration`; `test-backend` salvo se o contrato real divergir.

## Estratégia

1. Confirmar DTOs e semântica HTTP do UserController sem modificar backend.
2. Criar `UserManagementService` e contratos para listagem, criação, role e remoção.
3. Criar página protegida `/users`, lista, empty/loading/error states e integração mínima com navegação autenticada.
4. Usar `AuthService.profile$` como contexto visual de papel; não persistir nem confiar nele como autoridade final.
5. Derivar as opções e ações visíveis da matriz aprovada: GENERAL só opera MAJOR/CAPITÃO/SOLDADO; MARECHAL possui superset.
6. Implementar criação e alteração de role por contratos explícitos, sem endpoint genérico/mass assignment; pedir confirmação para remoção.
7. Cobrir serviços, página, RBAC visual, 401/403/404, payloads e regressões de sessão; executar build e gates.

## Arquivos prováveis

- `frontend/src/app/users/user-management.models.ts`
- `frontend/src/app/users/user-management.service.ts` e spec
- `frontend/src/app/users/users.component.ts`, HTML, SCSS e spec
- dialog/component local de criação/role/remoção, somente se a página não puder permanecer simples
- `frontend/src/app/app.routes.ts`, navegação/sidebar e specs associados

## Testes planejados

- serviço: GET/POST/PUT role/DELETE com URLs e payloads exatos, sem `guildId`;
- rota: protegida e indisponível na navegação para papéis inferiores;
- página: loading, vazio, listagem e erro seguro;
- MARECHAL visualiza roles/operações superiores;
- GENERAL visualiza somente MAJOR/CAPITÃO/SOLDADO como alvos;
- papéis inferiores não veem gerenciamento;
- criação não persiste senha; alteração usa somente `{ role }`; remoção requer confirmação;
- 403/404 preservam contexto de erro e 401 delega à infraestrutura global existente;
- regressão da sessão, onboarding e suíte Angular completa.

## Riscos e controles

- UI RBAC pode divergir do backend: manter a matriz em funções pequenas testadas e não supor que ocultamento autoriza.
- Último MARECHAL é regra server-side: a UI não tenta reproduzir contagem/autoridade local; comunica erro seguro recebido.
- Necessidade de endpoint adicional, alteração de DTO ou resposta de erro estável: parar e escalar ao usuário, sem alterar backend.
- O bloqueador global de antiabuso do registro permanece fora do escopo e continua bloqueando Release.

## Gates

- QA: testes e build Angular verdes, sem regressão de sessão/onboarding.
- Security: RBAC visual não cria bypass, sem dados sensíveis/tenant no cliente.
- Auditor: verificar escopo, PRD, evidências, Task Verdict e Release Verdict separados.
- Finalizar somente após Task Verdict APROVADO.
