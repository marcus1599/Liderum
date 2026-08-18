# Plano — Corrigir dependência JUnit duplicada

**Classificação:** SMALL

## Problema e causa

O Maven reporta `org.junit.jupiter:junit-jupiter` duplicado: há duas declarações diretas e uma versão redundante no `dependencyManagement`. O `spring-boot-starter-test` já fornece JUnit Jupiter pelo gerenciamento do Spring Boot 3.2.0.

## Escopo

- Remover somente as declarações redundantes de JUnit em `backend/pom.xml`.
- Validar com Maven Wrapper e registrar auditoria.

## Fora do escopo

- Atualizar Spring Boot, JUnit, Mockito ou qualquer dependência.
- Alterar código Java, testes, frontend ou infraestrutura.

## Agentes

Planner, Backend Dev, QA e Auditor. Security não é necessário: não há alteração de autenticação, autorização, JWT, secrets, tenant ou dados.

## Etapas e testes

1. Remover redundâncias JUnit.
2. Executar `./mvnw.cmd clean verify`.
3. Confirmar ausência do warning e auditar diff/escopo.

## Critérios de conclusão

- Uma única fonte de gerenciamento de JUnit: Spring Boot Starter Test/BOM.
- Build completo aprovado, sem warning de JUnit duplicado.
- Apenas `backend/pom.xml` e artefatos da task alterados.
