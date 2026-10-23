# QA — `clarify-team-assignment-and-leadership-flow`

## Parecer

**Task QA: APROVADO COM LIMITAÇÕES** (a qualidade dos critérios testáveis está aprovada; o relatório abaixo explicita o que não foi observado visualmente).

## Evidências

- `npm test -- --watch=false --browsers=ChromeHeadless`: 107/107 SUCCESS, exit 0; Chrome Headless 155.0.0.0. A execução inicial no sandbox foi impedida de iniciar Chrome (`spawn EPERM`); a execução autorizada fora do sandbox passou.
- `npm run build`: SUCCESS, exit 0. Avisos de depreciação Sass e orçamentos CSS acima do limite em Dashboard/Members/Login, sem falha ou aviso relativo ao SCSS de Groups.
- `git diff --check`: exit 0, sem erro de whitespace.
- Specs `groups.component.spec.ts`: contexto dos selects, confirmação de servidor, erro sem mutação otimista, seleção preservada em falha, erro pós-mutação, estados de candidato, chamadas duplicadas e roles são cobertos. MARECHAL/GENERAL/MAJOR preservam as ações; perfil ausente/SOLDADO não recebem as mutações/candidatos.
- Template/SCSS: sequência membro → destino → ação; texto de sincronização associado à ação; formulário de liderança identifica grupo, líder atual e novo líder; foco `:focus-visible`; breakpoint <=540px empilha selects/ação e define largura integral. Material continua fornecendo controles nativamente focáveis/operáveis por teclado.
- Nenhuma mudança em TS de negócio, serviço, API, rota, regra de autorização ou tenant. Diff limitado a apresentação e testes de Groups.
- Os cinco avisos do Impeccable foram classificados individualmente em `execution.md`; todos são bindings Angular `[src]` reais e cada uma das 13 classes configuradas tem asset correspondente. Não é necessária correção.

## Limitações

- Não foi possível inspecionar visualmente `/teams` autenticado, nem usar credenciais reais ou dados de produção. Os testes de componente verificam texto e estado, não constituem screenshot/layout renderizado.
- Overflow em 390px/zoom e tabulação/foco conduzidos manualmente permanecem sem evidência. Breakpoints, foco visível, nomes acessíveis e guards foram avaliados em código/specs; isso não é auditoria WCAG completa.
- RBAC avaliado no comportamento visual do componente/specs; enforcement server-side e isolamento cross-tenant não foram exercitados por esta task (sem alteração de backend ou requests).

Essas lacunas visuais não reprovaram a task: o plano aceita limitação quando browser/sessão fixture não estão disponíveis, e os critérios verificáveis por teste/inspeção estática foram aprovados. O parecer não declara validação autenticada, produção ou conformidade WCAG.

## Regressões

Nenhuma regressão observada nos testes; sem alterações de operação existentes, contratos ou comportamento de negócio.
