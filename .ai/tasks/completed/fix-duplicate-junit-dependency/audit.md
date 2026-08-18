# Auditoria — Corrigir dependência JUnit duplicada

**Veredito:** APROVADO

## Evidências verificadas

- `backend/pom.xml` remove exclusivamente três declarações redundantes de `org.junit.jupiter:junit-jupiter`: uma com versão `5.10.0`, uma dependência direta de teste sem versão e uma entrada no `dependencyManagement` com versão `5.9.3`.
- A fonte de JUnit permanece sendo `spring-boot-starter-test` e o gerenciamento de versões do Spring Boot 3.2.0.
- Não houve alteração de código de produção ou de testes para esta task.
- `./mvnw.cmd clean verify` foi executado no diretório `backend/` e terminou com exit code `0`.
- Relatórios Surefire: 24 testes executados, 0 failures, 0 errors e 0 skipped.
- O warning original de dependência duplicada `org.junit.jupiter:junit-jupiter` não apareceu na execução validada; também não há dependência direta `junit-jupiter` em `backend/pom.xml`.
- `git diff --check` não reportou erro de whitespace. Os warnings vistos no comando são avisos do Git sobre normalização LF/CRLF, sem relação com a task.

## Escopo

O diff de `backend/pom.xml` está alinhado ao `plan.md`: somente remoção das redundâncias JUnit. O working tree contém alterações anteriores em arquivos de aplicação, testes, infraestrutura e artefatos `.ai`; elas não foram atribuídas a esta task e não configuram scope creep.

## Segurança e documentação

Não se aplica revisão de Security: a alteração não toca autenticação, autorização, JWT, secrets, tenant ou dados. O `plan.md` é proporcional à classificação SMALL; não é necessário PRD. Não há mudança relevante no snapshot técnico que exija atualização de `state.md`.

## Conclusão

Task apta para finalização sem alterações adicionais.
