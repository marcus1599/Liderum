# Plano — feedback contextual de presença

Classificação: MEDIUM, frontend/UX. Autorização: pedido explícito do proprietário nesta conversa para uma task, implementação incremental, gates e commit local; sem push.

Routing:
- Domínios: `planning`, `frontend`, `testing`, `audit`.
- Agents: Planner → Frontend Dev → QA → Auditor. Security condicional se a implementação alterar comportamento de auth/autorização; Backend e SRE não selecionados, pois contratos e infraestrutura permanecem intactos.
- Skills: `create-prd`, `create-plan`, `ux-review`, Impeccable, `test-frontend`, `audit-task`, `finish-task`.
- Gate: testes e build aprovados, revisão visual/acessibilidade documentada, QA e Task Verdict APROVADO; release global avaliado separadamente.

## Etapas

1. Diagnóstico e auditoria UX em `prd.md`; conferir backend e testes antes de alterar código.
2. Adicionar política de loading por requisição com padrão global; aplicar modo local somente às chamadas da rota de presença.
3. Substituir `saving` global por pendências por membro, manter seleção confirmada até resposta, atualizar só a linha afetada e mostrar feedback local. Preservar alertas e seleção segura de evento.
4. Testar interceptor, múltiplas gravações, sucesso/erro/cancelamento, acessibilidade básica e regressões. Executar build e detector Impeccable.
5. Inspecionar UI renderizada em desktop/mobile com dados sintéticos; registrar observações e limites.
6. QA, Auditor, `finish-task`, arquivo em `completed/`, state/handoff e commit seletivo após vereditos aprovados.

## Arquivos previstos

`frontend/src/app/core/interceptors/LoadingInterceptor.ts`, novo spec e token de política; `frontend/src/app/services/{attendance,member,event}.service.ts`; `frontend/src/app/attendence/{attendence.component.ts,attendence.component.html,attendence.component.scss,attendence.component.spec.ts}`; artefatos desta task, `.ai/state.md` e `.ai/handoff.md`. O checkpoint do roadmap UX/UI concluído não será reaberto; apenas sua consistência será verificada.

## Critérios de conclusão

Critérios do PRD atendidos; sem alteração de contrato/backend/RBAC/tenant; nenhum teste obrigatório falhando; gates formais registrados; task arquivada; staging limitado aos arquivos da task; commit local verificado sem push.
