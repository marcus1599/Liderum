# Plano — `harmonize-login-and-activation-presentation`

## Classificação

MEDIUM / UX de autenticação no frontend, apresentação apenas. Sem alteração de fluxo, contrato, endpoint ou regra de negócio.

## Roteamento

- Planner: requisitos e task formados a partir da inspeção e do roadmap.
- Frontend Developer: implementação limitada aos componentes Login/Ativação e specs relacionadas (sessão principal).
- QA: revisão independente dos critérios, estados e regressões com evidências dos comandos e fixture; gate formal pendente.
- Auditor: revisão final independente do diff, evidências e gates após QA; gate formal pendente.
- Skills: Impeccable, `ux-review`, `test-frontend`, `audit-task` e, somente após gate aprovado, `finish-task`.
- Security não selecionado para execução: nenhum código de autenticação, token, endpoint, autorização ou mensagem de autorização será mudado; Auditor verifica essa fronteira.
- Arquiteto/SRE não selecionados: nenhuma decisão estrutural/infra ou deploy.

## Evidência e dependência

Fixture com mock local confirmou estados reais de Login (401) e Ativação (400) e composições desktop/mobile. A dependência visual do roadmap está satisfeita; a rejeição do token ocorre no POST, após formulário válido. Nenhum request alcançou backend real.

## Escopo e arquivos prováveis

- `frontend/src/app/auth/login.component.html|scss|ts|spec.ts`.
- `frontend/src/app/auth/activate.component.ts` e novos `activate.component.html|scss|spec.ts` se necessário para separar apresentação do componente comprimido inline.
- Artefatos de task e, ao encerrar, handoff/state/roadmap/checkpoint.
- Não tocar `AuthService`, `UserManagementService`, interceptors, rotas, backend, `environment.*`, tema global ou dependências além da restauração necessária das mudanças temporárias da fixture.

## Etapas

1. Registrar a investigação do fluxo e defeitos preexistentes sem editar auth.
2. Implementar alinhamento visual das duas telas, com alterações de movimento e acessibilidade dentro dos critérios.
3. Criar specs comportamentais para os estados de Login e Ativação usando services spies/HTTP testing, sem rede real.
4. Rodar testes direcionados, suíte, build e detector Impeccable; revisar semântica, contraste/foco e scope.
5. Inspecionar em 1280×800 e 390×844, com e sem `prefers-reduced-motion`, incluindo loading/erro.
6. QA e auditoria formal; com Task Verdict aprovado, `finish-task`, arquivo e commit seletivo. Parar antes de outra task se gate ou commit não passar.

## Riscos e controles

- Token: nenhuma mudança de query, `Location.replaceState`, payload ou validação; teste atualiza esses comportamentos.
- Loading global: não modificar componente raíz/interceptor; remover apenas duplicação local se sua ausência não reduzir feedback.
- Erros: preservar copy e resposta atuais; separar defeito semântico de erro genérico como pendência.
- Acessibilidade: assegurar labels, foco, mismatch associado e reduced motion; sem declarar WCAG completa.
- Segurança: valores fictícios foram interceptados no mock loopback, corpos nunca persistidos/logados; configuração temporária deve ser restaurada antes do commit.
- Working tree já contém alterações documentais de execução anteriores; staging apenas código e artefatos da task, sem incluir hunks/documentos independentes sem revisão de origem.

## Conclusão

Implementar somente após esta aprovação de escopo; esta task está ativa por autorização explícita do proprietário. Nenhuma alteração de produto fora do escopo foi aprovada.
