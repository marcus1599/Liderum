# Plan — rollout-authenticated-password-change-and-jwt-transition

Status: planejamento concluído; NÃO autorizado a executar deploy.

## Classificação e routing

- Prioridade: P0 para desbloquear o release da alteração de credenciais; porte STRUCTURAL operacional, sem implementação de feature.
- Domínios: planning, security, sre, testing, audit, documentation.
- Agents: Planner (plano/PRD); SRE/DevOps (readiness, estratégia e estabilidade); Security (JWT legado, revogação, secrets e rollback); Backend Dev consultivo apenas para interpretar V4/logs; QA (smoke); Auditor (escopo e evidências).
- Não selecionados: Frontend Dev (nenhuma mudança UI; apenas smoke de login/sessão), Arquiteto (nenhuma decisão arquitetural nova aprovada).
- Skills: `create-prd`, `create-plan`; na execução `security-review`, `test-backend` somente se houver mudança de código/configuração versionada ou evidência de regressão, `audit-task`, `finish-task` somente após Task Verdict APROVADO. Nenhuma migration ou alteração de produto prevista.

## Contexto e estado inicial confirmado

- A implementação de password change está concluída, com V4, JWT `uid`/`cv`, `PUT /users/me/password` e testes locais aprovados; detalhes em `tasks/completed/add-authenticated-password-change/`.
- Git local: `HEAD` e `origin/main` local coincidem na inspeção de planejamento; a consulta remota falhou por conexão, portanto publicação e CI atual não estão comprovados nesta sessão.
- Working tree contém somente alterações independentes de configuração Impeccable (`.agents/`, `.gitignore`, Frontend Dev, QA e documento de integração); não incluí-las nesta task.
- Nenhuma consulta Render, deploy, smoke de produção ou nova suíte foi executada neste planejamento. Release Verdict continua BLOQUEADO.

## Etapas de execução após autorização

1. **Preflight read-only:** verificar Git remoto/commit, CI backend e postgres-flyway na revisão alvo; conferir serviço Render, profile `prod`, conexão ao banco, estratégia de deploy, quantidade de instâncias, health, deploy atual e secrets apenas por nomes/presença. Identificar janela e conta de smoke autorizada. Não registrar valores.
2. **Banco e recuperação:** confirmar history atual e estado da V4; se V4 já estiver aplicada, não a reaplicar. Confirmar que há opção operacional segura de recuperação antes de deploy. Não executar backup/dump com dados reais sem autorização específica. Definir ponto de parada; rollback de código antigo não é opção segura para a garantia de revogação sem avaliação Security.
3. **Estratégia de transição:** escolher a menor estratégia suportada pelo Render real para eliminar tráfego da versão antiga, inclusive se isso exigir indisponibilidade breve. Registrar como a drenagem será comprovada. Se topologia/telemetria não permitir essa prova, parar para decisão do usuário.
4. **Deploy autorizado:** promover uma revisão exata uma vez; acompanhar logs seguros de Hikari → Flyway V4 → Hibernate validate → Spring/Tomcat → health/live. Parar diante de falha, loop ou estado ambíguo; não executar reparo manual no banco.
5. **Prova de drenagem:** conferir instâncias/revisões ativas e roteamento segundo evidência do Render. Somente após eliminar versão antiga executar teste de rejeição de token legado de conta de smoke, sem imprimir o token. Requests autenticados à versão antiga tornam Release Verdict BLOQUEADO.
6. **Smoke funcional:** com conta descartável autorizada, testar login, `/users/me`, rota tenant-scoped, mudança de senha própria, rejeição do JWT antigo, novo login com senha nova e continuação da sessão. Confirmar health e estabilidade após o teste. Não testar em conta real de terceiro, nem expor credenciais em URL/log/artefato.
7. **Gates:** QA → Security → SRE/DevOps → Auditor. Registrar `execution.md` e revisões com evidência, limitações e Task/Release Verdict separados. `finish-task` apenas após aprovação do Task Verdict; atualizar `state.md`/`handoff.md` e roadmap somente quando o estado real mudar.

## Testes e evidências esperados

- Pré-deploy: CI da revisão alvo, inclusive `clean verify` e PostgreSQL 18 fresh/upgrade; reexecutar localmente só se arquivos funcionais mudarem ou CI não fornecer evidência suficiente.
- Produção: logs de migration/validate/startup, Flyway history V4, health, deploy/revisão e ausência de instância antiga.
- HTTP: status de login, `/users/me`, leitura tenant-scoped, troca 204, JWT anterior 401, novo login/perfil 200; registrar somente códigos e identificadores não sensíveis.
- Segurança: nenhum token/header/senha em relatório, impossibilidade de reuso de JWT legado ou revogado na revisão nova, nenhum enfraquecimento de auth ou tenant.

## Stop conditions

- Revisão alvo/CI/serviço Render incertos; autorização de deploy ou conta de smoke ausente.
- History/migration inesperada; instâncias antigas não rastreáveis; health/startup instável.
- JWT legado ou revogado ainda aceito após a transição; smoke falha; logs expõem dados sensíveis.
- Necessidade de alterar código, esquema, segredo, arquitetura, banco ou comportamento fora do PRD.

## Próximo passo

Apresentar preflight e estratégia operacional concreta para aprovação explícita da janela e do deploy. Este plano não executa deploy, smoke, commit ou push.
