# QA — `decouple-team-leadership-from-user-status`

## Verdict

**APROVADO COM LIMITAÇÕES**

## Evidências revisadas

- Backend: execução isolada de `mvnw clean verify` concluída com **95 testes, 0 falhas, 0 erros e 0 skips**.
- Backend focado: `TeamServiceImplTest` e `MemberServiceImplTest`, **11 testes, 0 falhas, 0 erros e 0 skips**.
- Frontend, conforme handoff do executor: suíte completa **122/122** antes do último ajuste pequeno; testes focados **26/26** após o ajuste, cobrindo a associação acessível entre o `mat-select` e seu `mat-hint`; build final passou.
- `git diff --check` passou.
- Revisão de diff, PRD, plano e specs confirmou cobertura da liderança por Member associado ao mesmo Team, sem dependência de conta, status ou papel; grupo recém-criado sem líder; rejeição de candidatos fora do Team/Guild; RBAC existente; limpeza da marcação ao mover/remover Member; e preservação da liderança ao desativar/excluir User vinculado.
- A migração V5 e seu backfill foram executados em H2 pelo teste de integração; o teste verifica correspondência válida do mesmo Team/Guild, casos não correspondentes, preservação dos valores legados e a nova restrição relacional.
- As specs da UI cobrem carregamento, erro com retry, grupo sem membros, ausência de alternativa, confirmação, erro de reassignment, texto da liderança tática temporária e associação acessível do seletor.
- O Impeccable reportou cinco avisos de `[src]` dinâmico em ícones de classe dos templates de drag preview. A revisão os classificou como usos dinâmicos já existentes, não como necessidade de trocar por caminhos estáticos; nenhuma nova violação decorrente desta alteração foi relatada.

## Limitações

- Não foi feita inspeção visual renderizada em navegador; a avaliação de UX/UI baseia-se em testes e análise estática.
- A migração não foi executada em PostgreSQL nesta validação; o suporte observado foi H2.
- A seleção de Member vinculado a User `PENDING` não tem caso específico. O caso `DISABLED` está coberto; o fluxo implementado consulta o Member e seu Team, sem consultar status ou associação de User.
- Os avisos dinâmicos de ícones não foram eliminados, pois não justificavam substituir imagens dinâmicas por caminhos estáticos.

Este parecer cobre QA da task e não declara inspeção visual completa, WCAG plena ou validação PostgreSQL.
