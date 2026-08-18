# Plano de Execução — Correção de Vulnerabilidade: Segredo JWT Exposto

> **Tarefa:** Correção e rotação do segredo JWT exposto nos arquivos de configuração do Liderum.
> **Classificação:** SMALL (Segurança & Configuração)
> **Status:** Validada localmente; ainda não commitada

---

## 1. Resumo
Removido o segredo JWT comprometido dos arquivos `docker-compose.yml` e `application-dev.properties`. A configuração exige `JWT_SECRET` explicitamente, sem fallback versionado, e disponibiliza apenas um template `.env.example`.

---

## 2. Agentes Envolvidos

| Agente | Necessário? | Papel nesta tarefa |
| --- | --- | --- |
| **Planner** | Sim | Planejamento, delimitação de escopo e critérios de conclusão |
| **Arquiteto** | Não | Decisão trivial de configuração/segurança; nenhum ADR necessário |
| **Backend Dev** | Sim | Ajuste dos arquivos de propriedade (`application.properties`, `application-dev.properties`) |
| **Frontend Dev** | Não | Alteração limitada à segurança do backend e infra |
| **Security** | Sim | Validação das diretrizes de rotação, injeção de segredos e checklist pós-correção |
| **SRE/DevOps** | Sim | Ajuste do `docker-compose.yml`, `.gitignore` e criação do `.env.example` |
| **QA** | Sim | Validação da execução dos testes backend (`./mvnw clean verify`) |
| **Auditor** | Sim | Auditoria final do `git diff`, escopo e sincronização do `state.md` |

---

## 3. Escopo Executado

1. [x] Removido o segredo JWT comprometido de `docker-compose.yml` e `backend/src/main/resources/application-dev.properties`.
2. [x] Configurada exigência explícita de `JWT_SECRET` em `application.properties` e `docker-compose.yml`, sem fallback versionado.
3. [x] Injetada a variável de ambiente `${JWT_SECRET}`.
4. [x] Adicionado `.env` e `*.env` em `.gitignore` (raiz e backend).
5. [x] Criado `.env.example` na raiz do projeto.
6. [x] Atualizado `.ai/state.md` registrando a resolução da vulnerabilidade.
7. [x] Executada a suíte backend `./mvnw.cmd clean verify`: 24 testes, 0 failures, 0 errors, `BUILD SUCCESS`.
8. [x] Validada a configuração fail-fast no diff e executados `git diff --check` e `git status` após a consolidação.
10. [x] Nenhum commit ou push realizado.

## Registro de consolidação (2026-08-16)

* A cópia incorreta em `tasks/active/plan.md` foi removida; este é o único registro da task JWT.
* Os ajustes em quatro testes de serviço foram classificados como mudança independente de multi-tenancy e registrados em `tasks/active/align-service-tests-with-multi-tenancy/`.
* O contexto Spring recebe `jwt.secret` exclusivamente em `LiderumApplicationTests`, por meio de propriedade fictícia de teste; a configuração normal continua exigindo `JWT_SECRET`.
* A validação Maven foi executada com sucesso usando JDK 21: 24 testes, 0 failures, 0 errors.
