# PRD — `normalize-domain-page-visual-rhythm`

## Objetivo

Comparar o ritmo visual das áreas Dashboard, Members, Teams, Events, Attendance e Users, preservando Angular Material e a Direção B (SaaS moderno/profissional com identidade RPG discreta). Realizar apenas ajustes pequenos nas superfícies em que a evidência provar divergência relevante.

## Escopo

- Inventariar títulos, espaçamentos, superfícies e hierarquia das seis áreas.
- Inspecionar visualmente desktop e viewport móvel, usando somente dados e serviços locais isolados quando necessários.
- Documentar divergências com evidência por rota antes de qualquer mudança.
- Se não houver divergência relevante, encerrar sem alteração de produto.
- Se houver divergência demonstrada, ajustar somente CSS/apresentação da superfície afetada e preservar estados, navegação e comportamento.

## Fora de escopo

Mudanças de API, contratos, autenticação, RBAC, tenant, fórmulas/métricas, conteúdo funcional, redesign global, abstrações compartilhadas especulativas, alteração de Settings persistente ou dependência nova.

## Critérios de aceite

1. Inventário comparativo cobre as seis superfícies e registra evidência e limitação por viewport/estado.
2. Qualquer alteração implementada corresponde a uma divergência documentada, mantém Material/Direção B e não amplia o escopo.
3. Não há regressão em navegação, loading/erro/vazio, foco, responsividade ou RBAC visual.
4. Não se declaram estados, inspeções ou gates sem evidência observável.

## Validação

Comparação visual renderizada a 1280×800 e 390×844 para as rotas acessíveis com fixture local não produtiva; ChromeHeadless para specs afetadas; build Angular; overflow e foco; detector/revisão manual Impeccable; QA e Auditor. Sem credenciais ou serviços de produção. Se uma rota não puder ser autenticada com segurança, registrar o critério não comprovado e usar evidência estática apenas como tal.
