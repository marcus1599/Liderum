# PRD — Clarificar ações e linguagem na gestão de usuários

## Objetivo

Tornar a criação e a administração de usuários da Guild mais fáceis de compreender, preservando integralmente as regras atuais de autorização, tenant, ativação e operações HTTP.

## Problema e evidência

A rota autenticada `/users` concentra, em cada linha, seletor de cargo, gravação de cargo, regeneração de link, mudança de status e remoção. Os controles não possuem agrupamento ou rótulos contextuais suficientes, e a apresentação mistura `Role` e estados internos (`PENDING`, `ACTIVE`, `DISABLED`) com texto em português. Em viewport estreito, a linha é empilhada sem uma hierarquia explícita.

## Requisitos

- Apresentar nome, e-mail, cargo e estado de conta com rótulos claros em português, sem alterar enums, contratos ou valores enviados à API.
- Distinguir a mudança de cargo das ações de conta e apresentar o cargo atual antes da seleção de um novo cargo.
- Exibir somente as ações já autorizadas por `canActOn`, pelo papel da sessão e pelo status existente.
- Manter a regeneração de link somente para contas pendentes e a mudança de estado somente para contas ativas ou desativadas.
- Preservar as confirmações existentes, o fluxo de link de ativação, feedback seguro de erro e os estados de carregamento por ação.
- Garantir nomes acessíveis e feedback perceptível para ações por usuário, sem depender apenas de cor.
- Manter leitura e toque sem rolagem horizontal em largura estreita.

## Direção visual

Direção B: SaaS moderno com identidade RPG discreta. A identidade vem da linguagem de Guild e cargos já existente; não introduzir ícones, assets, design system ou ornamentos RPG novos. Reutilizar Angular Material e a superfície escura/violeta atual.

## Fora de escopo

- API, autenticação, guardas, RBAC, tenant server-side e regras de ativação, remoção ou status.
- Backend, dependências, componentes globais, redesign de shell, modal novo ou alteração de contrato.
- Inspeção autenticada com dados reais; usar fixtures locais e declarar a limitação.

## Critérios de aceite

- MARECHAL, GENERAL e papel não administrativo continuam vendo somente os controles previstos hoje.
- Uma pessoa identifica cargo atual, novo cargo, estado de conta e a consequência de cada ação sem interpretar enums internos.
- Controles repetidos possuem nome acessível contextual e ações pendentes dão feedback textual.
- Fluxos PENDING, ACTIVE e DISABLED, confirmação, erro, carregamento, vazio e retry preservam o comportamento atual.
- A tela passa testes Angular, build e revisão visual local em desktop e largura estreita; não há overflow horizontal introduzido.

## Riscos e segurança

O risco principal é esconder ou expor uma operação administrativa por erro de apresentação. A autorização backend e a resolução de tenant permanecem inalteradas. Security revisará a cópia e a visibilidade dos controles antes da auditoria.
