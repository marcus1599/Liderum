# Plano — `clarify-user-management-actions-and-language`

## Classificação

Funcional UX/UI, P1, MEDIUM. Não requer ADR, backend, migration, dependência ou SRE.

## Roteamento

```text
Routing:
- Domínios: planning, frontend, testing, security, audit, documentation
- Agents: Planner → Frontend Dev → QA → Security (copy/visibilidade de autorização) → Auditor
- Skills: create-prd, create-plan, ux-review, Impeccable, test-frontend, security-review, audit-task
- Não selecionados: Arquiteto — sem decisão estrutural; Backend — contratos/RBAC preservados; SRE — sem operação/deploy; create-adr/create-migration/test-backend — não aplicáveis
- Gate: testes/frontend visual → QA → Security consultivo → Auditor; finish-task somente se Task Verdict APROVADO
```

## Escopo e arquivos prováveis

- `frontend/src/app/users/users.component.html`
- `frontend/src/app/users/users.component.scss`
- `frontend/src/app/users/users.component.ts`, somente para textos de apresentação/estado acessível que não alterem a operação
- `frontend/src/app/users/users.component.spec.ts`
- Artefatos desta task em `.ai/tasks/active/clarify-user-management-actions-and-language/`

## Etapas

1. Confirmar comportamento de rota, RBAC, serviços, template, SCSS e specs existentes.
2. Reorganizar a apresentação de cada usuário em identidade, cargo e ações de conta; localizar rótulos e estados internos apenas na UI.
3. Preservar todos os `*ngIf`, `canActOn`, confirmações, chamadas de serviço e mensagens seguras existentes; acrescentar contexto acessível e estado textual quando necessário.
4. Ampliar testes para matriz de papel/alvo/status, rótulos e ações permitidas, sem remover cenários existentes.
5. Validar com testes ChromeHeadless, build, `git diff --check`, detector Impeccable e inspeção local em 1280×800 e 390×844, teclado e zoom 200% quando o app puder ser iniciado.
6. Registrar execução, QA, Security e auditoria. O Release Verdict global RabbitMQ permanece bloqueado por evidência de produção ausente, sem relação com esta task.

## Dependências e limitação

Não há sessão autenticada com MARECHAL/GENERAL e exemplos reais dos três estados disponível nesta execução. As revisões usam fixtures locais; não alegar validação autenticada em produção.

## Riscos

- Ocultar ou exibir ação incompatível com RBAC/status.
- Quebrar o foco, a leitura por leitor de tela ou o layout estreito ao agrupar controles.
- Transformar ausência de dados em erro ou vice-versa.

## Conclusão verificável

O componente mantém operações e RBAC existentes, torna os controles contextualizados/localizados, passa testes e build, e recebe Task Verdict APROVADO em auditoria. `finish-task` é condicional a esse veredito; nenhum push é permitido.
