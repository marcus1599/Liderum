# PRD — rollout-authenticated-password-change-and-jwt-transition

Status: PLANEJADO; execução e deploy dependem de autorização específica.

## Problema e objetivo

A task `add-authenticated-password-change` foi aprovada e concluída, mas seu Release Verdict segue BLOQUEADO. O backend novo emite JWT com `uid`/`cv` e rejeita tokens legados ou revogados; instâncias antigas ainda atendendo tráfego podem aceitar esses tokens. O objetivo desta task operacional é promover a revisão já aprovada, comprovar que nenhuma instância antiga continua atendendo requests e validar em produção a transição de sessão, a V4 e o fluxo de troca de senha, sem mudar o contrato ou o código.

## Requisitos

1. Identificar serviço Render, branch/revisão efetivamente implantada, instâncias, estratégia de deploy, health e configuração de produção antes de agir. Conferir Git remoto e CI dessa revisão; o estado local de `origin/main` não basta.
2. Preservar banco, secrets, configurações de auth/CORS e dados existentes. Não expor valores de ambiente, senhas, JWTs ou headers no registro da task.
3. Confirmar histórico Flyway pré-deploy e caminho V3→V4 esperado; observar V4 aplicada com sucesso, `users.credential_version` com default zero/NOT NULL e Hibernate `validate`, sem SQL manual.
4. Definir janela/estratégia coordenada de substituição. Não declarar revogação efetiva enquanto versão antiga atender tráfego. Se o Render não permitir comprovar drenagem, parar e escalar; não improvisar mudança de infraestrutura.
5. Comunicar a reautenticação necessária: tokens anteriores sem `uid`/`cv` devem deixar de funcionar após a substituição completa. Sem perda ou reset de senha.
6. Validar health/startup, login, `/users/me`, rota tenant-scoped, troca da própria senha e revogação da sessão anterior usando exclusivamente conta de smoke autorizada. Credenciais/tokens de smoke permanecem fora do repositório e dos logs.
7. Confirmar que token legado de teste é rejeitado após a drenagem, que novo login gera sessão funcional e que troca de senha invalida o token anterior. Não usar contas de terceiros para prova.
8. Não descrever rollback para código antigo como recuperação segura: ele pode reaceitar tokens revogados. Qualquer rollback exige nova avaliação Security e decisão explícita; preferir correção forward.
9. Separar Task Verdict de Release Verdict. Aprovação de release depende de V4, startup estável, drenagem comprovada e smoke completo, sem novo bloqueador conhecido.

## Fora de escopo

Não alterar JWT, schema/migrations, backend, frontend, RBAC, tenancy, CORS, rate limiting, bibliotecas ou CI. Não criar ADR-002 sem autorização separada. Não executar reset de banco, Flyway `repair`, edição de history, rotação de segredo ou limpeza manual de sessões. Não misturar a configuração pendente da Impeccable nem iniciar a próxima capacidade do Ciclo 3.

## Aceite verificável

- Revisão exata implantada identificada e CI correspondente aprovado.
- Flyway V4 registrada `success=true`; Hibernate `validate` e health/startup aprovados.
- Nenhuma instância da versão anterior recebe tráfego após a transição, com evidência da plataforma adequada à topologia real.
- JWT legado sintético/autorizado rejeitado, login novo e `/users/me` funcionais; rota tenant-scoped permanece isolada; troca de senha da conta de smoke retorna sucesso e invalida JWT anterior.
- Sem secrets ou dados sensíveis em logs/artefatos; QA, Security, SRE/DevOps e Auditor aprovam os gates proporcionais.

## Dependências e riscos

- Autorização explícita de deploy, janela, conta descartável e mutações de smoke em produção.
- Acesso de leitura ao Render/GitHub para preflight e pós-deploy. Falta de evidência de instâncias drenadas, CI, V4 ou smoke bloqueia Release Verdict.
- Transição pode exigir novo login de todos os usuários. Rollback inseguro para revogação e falha de migration/startup exigem parada e escalonamento.
