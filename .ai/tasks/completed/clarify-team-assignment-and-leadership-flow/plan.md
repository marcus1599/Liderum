# Plano — `clarify-team-assignment-and-leadership-flow`

## Classificação e routing

Funcional UX/UI, P2, SMALL–MEDIUM. Sem ADR, backend, migration ou SRE.

```text
Routing:
- Domínios: planning, frontend, testing, audit, documentation
- Agents: Planner → Frontend Dev → QA → Auditor
- Skills: create-prd, create-plan, ux-review, Impeccable, test-frontend, audit-task, finish-task
- Security consultivo somente se a revisão encontrar mudança de visibilidade/autorização; Backend, Arquiteto e SRE não selecionados porque contratos, arquitetura e operação ficam fora do escopo
- Gate: testes/build/diff/detector → QA → Security condicional → Auditor; finish-task após Task Verdict APROVADO
```

## Evidência e escopo

O template tem sequência acessível para atribuição e seção de troca de liderança, mas os títulos e instruções não explicitam com consistência a ordem/resultado. O layout combina painel de membros, seleção manual e drag-and-drop. Limitar alterações a `groups.component.html`, `groups.component.scss`, specs e, se imprescindível, textos de apresentação no TS.

Direção canônica: [[docs/liderum-visual-direction.md]]. Para esta superfície, modo Operate: clareza e previsibilidade do fluxo têm precedência; manter o tema escuro/violeta e Angular Material observados.

## Plano

1. Preservar estados, métodos, contratos e autorização atuais.
2. Tornar membro e grupo de destino passos visíveis e adicionar explicação curta sobre confirmação no servidor.
3. Na transferência, contextualizar o grupo, liderança atual quando disponível e seleção de substituto.
4. Testar sucesso, erro, seleção preservada, RBAC, teclado/toque e estados vazios/carregamento.
5. Executar suíte ChromeHeadless, build, `git diff --check`, detector Impeccable e inspeção desktop/390px caso navegador e sessão disponíveis.
6. Registrar QA, Security se aplicável, auditoria, resultado e hash do commit.

## Riscos e limitações

Risco de sugerir transferência antes de confirmação ou de alterar controles visíveis por role. Não há evidência de sessão autenticada disponível; specs e inspeção local não serão descritas como validação em produção.
