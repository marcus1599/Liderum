# QA — rollout-authenticated-password-change-and-jwt-transition

Data: 2026-10-17. Veredito reavaliado: **APROVADO**, com limitação de cobertura de produção explicitamente aceita pelo usuário. A avaliação anterior era REPROVADO por evidência de aceite incompleta; não foi apagada da cronologia.

## Evidências aprovadas

- CI da revisão implantada: jobs backend, PostgreSQL Flyway e fullstack concluídos com sucesso; referências em `execution.md`.
- Render: revisão exata `live`, versão anterior `deactivated`, V4 `success=true`, `credential_version` com default zero/NOT NULL, JPA e Tomcat iniciados, health HTTP 200/UP.
- Smoke HTTP real: registro 201, login inicial 200, perfil 200, leitura tenant-scoped 200, troca de senha 204, JWT anterior 401, senha anterior 401, novo login e perfil 200. Script terminou exit code 0.

## Limitação de evidência e decisão

O PRD/plan exigem que, após a drenagem, um JWT legado **assinado** de conta de smoke seja rejeitado em produção. Não havia tal artefato antes da substituição e ele não pode ser reconstruído sem o segredo de produção ou a instância antiga. O teste de integração na mesma revisão prova a regra de código, mas não é o request HTTP em produção pedido. Um token com assinatura inválida não exercita esse caso. O usuário aceitou explicitamente como evidência equivalente: teste de integração assinado, CI da revisão exata, versão antiga desativada e revogação demonstrada em produção, observando que atualmente é o único usuário do projeto. O request legado direto **não foi executado**.

## Conclusão

O conjunto de testes automatizados da mesma revisão e o smoke operacional são suficientes para o gate proporcional desta task sob a decisão explícita do usuário. Não há falha observada de auth, e nenhum JWT/segredo de usuário real foi solicitado ou registrado. Prosseguir para Security e SRE/DevOps; o erro RabbitMQ preexistente pertence ao Release Verdict global, não a este gate de QA de auth.
