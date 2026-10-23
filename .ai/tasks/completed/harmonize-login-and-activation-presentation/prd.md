# PRD — `harmonize-login-and-activation-presentation`

## Objetivo

Dar continuidade visual às telas públicas de login e ativação, preservar a identidade escura/violeta do Liderum e reduzir animação sem propósito, sem alterar o fluxo de autenticação.

## Contexto e problema observado

Inspeção renderizada local em iframe isolado com viewports CSS exatos de 1280×800 e 390×844 confirmou que `/login` tem composição central e superfície escura, enquanto `/activate` aparece sem composição, com título de baixo contraste e campos Material alinhados sem agrupamento/ritmo visual. O botão de login desabilitado não é perceptível na captura; o link “Criar uma Guild” mantém azul padrão e baixo contraste sobre a superfície escura. Login usa partículas, brilho/pulso contínuos, borda animada e entrada; não declara respeito a `prefers-reduced-motion`.

Estados de erro reais também foram renderizados com um mock HTTP local sem encaminhamento: 401 em `/auth/login` exibe “Usuário ou senha inválidos”; 400 em `/auth/activate` exibe “Link inválido ou expirado.”. O loading global mostra overlay e spinner; login também mostra spinner local, resultando em dois indicadores.

## Requisitos e comportamento mantido

- Harmonizar a composição, tipografia, espaçamento, campos, botões, links, foco e feedback das duas telas com Material e a Direção B canônica.
- Manter texto, rotas, validações, valores de request, token em memória, remoção do token da URL, respostas, snackbars e navegação existentes.
- Remover movimento decorativo contínuo e respeitar `prefers-reduced-motion` sem prejudicar foco, leitura ou operação.
- Tornar campos e estados relevantes identificáveis por teclado e tecnologia assistiva, sem alterar validações de negócio.
- Manter comportamento funcional existente no loading global; evitar duplicação de indicador na tela de login se isso puder ser feito sem tocar no interceptor compartilhado.
- Não adicionar dependências, endpoints, assets ou dados reais.

## Escopo

- Apenas apresentação de Login e Ativação e testes frontend diretamente relacionados.
- Estilo de estado inicial, loading já existente e feedback de erro/sucesso já implementado.
- Acessibilidade básica, teclado, reduced motion e composição responsiva.

## Fora do escopo

- Backend, endpoints, contratos, guards, RBAC, isolamento tenant, interceptor de autenticação/loading, geração/validação/expiração de token, validações e fluxo de autenticação.
- Corrigir o comportamento preexistente em que ausência de token permite preencher o formulário mas o submit termina sem feedback; registrar como defeito fora desta entrega.
- Alterar a mensagem única de erro da ativação por status HTTP, mesmo que hoje todo erro seja descrito como link inválido/expirado; mudança semântica será task separada.
- Redesign de outras rotas, troca de paleta global, novo asset ou dependência.

## Critérios de aceite

1. Login e ativação apresentam hierarquia, superfície, tipografia e espaçamento reconhecivelmente coerentes com Direção B/Material.
2. Os formulários permanecem legíveis, operáveis e sem overflow horizontal a 390×844 e 1280×800.
3. Campos possuem nomes acessíveis; teclado mantém ordem lógica e foco visível; incompatibilidade de senhas é percebida junto ao campo.
4. Estados de submissão/erro continuam funcionais, sem duplicação visual de spinner no login e sem alterar a resposta ou navegação.
5. Animação decorativa não compete com leitura; `prefers-reduced-motion: reduce` suprime movimento não essencial.
6. Não há mudança em autenticação, contrato, token, RBAC, tenant, backend ou dependências.

## Dependência visual

Satisfeita em 2026-10-23. Um fixture local temporário incorporou as rotas reais em iframes de CSS viewport exatos de 1280×800 e 390×844. A aplicação de desenvolvimento foi apontada temporariamente para um mock em `127.0.0.1:43127`, que só respondeu 401 para login e 400 para ativação, não encaminhou requisições nem registrou corpos. O token e as senhas foram valores fictícios; nenhuma credencial real ou produção foi usada. A fixture será removida após a inspeção final.

## Riscos e segurança

Risco principal é regressão de foco/contraste/layout em viewport estreita ou alteração involuntária de comportamento de autenticação. Revisão de Security não é necessária se diff permanecer estritamente visual/semântico e não alterar auth, token, request, endpoint ou mensagens de autorização. Auditor deve confirmar isso no diff final.

## Validação

- Specs de Login e nova spec de Ativação cobrindo renderização/labels, validação de formulário, mismatch, loading, erro HTTP e sucesso/navegação sem rede real.
- `npm test -- --watch=false --browsers=ChromeHeadless` e `npm run build`.
- Inspeção renderizada com fixture isolada em 1280×800 e 390×844: login/ativação, loading e erros reais do componente; teclado/foco e reduced motion.
- Impeccable manual review e `impeccable detect --json` sobre targets alterados.
- QA e Auditoria; registrar limitações sem alegar WCAG completa.
