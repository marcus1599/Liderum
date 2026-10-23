# Plano — `normalize-domain-page-visual-rhythm`

## Classificação e roteamento

MEDIUM / UX frontend, inicialmente inventário. Uma task ativa por vez.

- Domínios: frontend, testing, audit, documentation.
- Agentes previstos: Planner (delimitar superfícies/evidências); Frontend Dev somente após inventário e plano de mudança específico; QA; Auditor.
- Skills: Impeccable, `ux-review`, `test-frontend`, `audit-task`, `finish-task` somente após aprovação.
- Security não selecionado: não tocar autenticação/autorização/RBAC; reavaliar caso o escopo mude.
- Não selecionados: Arquiteto, Backend Dev, SRE — sem decisão estrutural, alteração de backend ou infra prevista.

## Procedimento

1. Confirmar Direção B canônica, PRD e critérios. Mapear componentes, estilos, estados e rotas das seis superfícies.
2. Preparar inspeção local isolada sem backend real, credenciais ou dados reais; executar captura desktop/móvel em uma rodada comparativa.
3. Registrar achados por superfície, distinguir diferenças funcionais justificadas de inconsistência visual e encerrar sem código se não houver problema material.
4. Se houver achado relevante, apresentar a menor mudança proposta no execution/PRD antes de editar, limitado à apresentação da superfície comprovada.
5. Testar somente specs afetadas e suíte necessária, build, foco, overflow, estados e detector Impeccable; solicitar QA e Auditor independentes.
6. Só com Task Verdict APROVADO: finish-task, atualizar handoff/state/roadmap/checkpoint, staging seletivo, revisar diff e criar commit local isolado. Nunca push. Não iniciar a próxima task se commit ou gate falhar.

## Riscos e controles

- Estados vazios e hierarquias distintas podem justificar variação: não uniformizar sem evidência.
- CSS amplo pode regredir telas não relacionadas: preferir escopo por componente.
- Rotas protegidas não devem ser contornadas contra serviços reais; usar fixture isolada e mock sem forwarding ou declarar limitação.
- Working tree contém documentos globais de governança independentes; preservar e excluir do commit da task.
