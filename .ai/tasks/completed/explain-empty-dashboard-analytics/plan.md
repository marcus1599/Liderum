# Plano — `explain-empty-dashboard-analytics`

## Classificação e routing

UX/UI A / P2 / SMALL. O serviço calcula os agregados no cliente; não há necessidade de ADR, Backend, Arquiteto ou SRE se o escopo permanecer de apresentação.

```text
Routing:
- Domínios: planning, frontend, testing, audit, documentation
- Agents: Planner (planejamento concluído) → Frontend Dev → QA → Auditor
- Security consultivo somente se autorização, visibilidade ou informação sensível mudar
- Skills: ux-review, Impeccable, test-frontend, audit-task, finish-task
- Gate: specs/build/inspeção por fixture quando disponível → QA → Auditor → finish-task após Task Verdict APROVADO
```

## Evidências confirmadas

- `dashboard.service.ts` faz `forkJoin` de events, attendances e members; limita eventos recentes pelo filtro e deriva presença/agregados localmente.
- Backend lista Events, Attendances e Members tenant-scoped usando `TenantService`; não há endpoint agregado de dashboard.
- Estados reais e limitações de dedução estão registrados no PRD. `alerts=[]` não pode ser interpretado como sucesso de ausência de faltas quando não existem eventos/registros para avaliação.
- Loading, erro, ocultação do conteúdo e retry já são tratados; não reimplementar esses estados.

## Etapas

1. Fazer inventário de cada painel e das expressões/condições atuais; comparar com Direção B e com a tela renderizada em 1280×800 e 390×844 usando somente fixture local, caso exista.
2. Definir as condições de apresentação no componente sem mudar `DashboardService`, fórmula, endpoints ou filtro; preservar painéis que ainda têm informação válida.
3. Implementar somente copy/empty states necessários e pequenos ajustes locais comprovados pela inspeção. Não inventar eventos, membros ou registros.
4. Adicionar specs: sem eventos; evento(s) sem presença na janela; sem membros; membros com classes e presenças zero; dados mistos/preenchidos; loading/erro/retry; filtro preservado.
5. Executar suíte ChromeHeadless, build, revisão de acessibilidade básica, responsividade, RBAC/tenant por análise de escopo e regressão; registrar explicitamente a limitação visual se a fixture não estiver disponível.
6. Gate QA → Auditor; com aprovação, `finish-task`, atualizar state/handoff/roadmap, inspecionar/stage seletivo, solicitar autorização de escrita em `.git/` e criar commit local isolado. Sem push.

## Critérios de bloqueio e riscos

- Bloquear implementação de texto que exija inferir ausência global a partir da janela ou que conflite com a fórmula atual.
- Não afirmar alerta vazio confiável sem suficiente presença histórica nos eventos atuais.
- Se não houver fixture visual local, documentar ausência da evidência; seguir apenas se QA confirmar que os requisitos verificáveis por specs/análise bastam conforme governança.
- Não iniciar outra task enquanto esta permanecer ativa.
