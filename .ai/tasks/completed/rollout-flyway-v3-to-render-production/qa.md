# QA — Rollout Flyway V3 em Render

## Escopo verificado

Validação operacional do deploy da revisão aprovada que contém V3, startup Spring, schema migrado, health, CORS e smoke autenticado sem gravação de dados.

## Evidências

- Render deploy `dep-db76ikcs728c73dk48og` ficou `live`; os logs de startup registrados em `execution.md` mostram profile `prod`, conexão Hikari, Flyway validando/aplicando V3, inicialização do EntityManagerFactory e Tomcat na porta 10000.
- Consulta read-only confirmou V1, V2 e V3 com `success=true`, colunas obrigatórias `NOT NULL` e a unique `uk_attendance_member_event`.
- `GET /actuator/health`: 200.
- CORS: preflight da origin `https://theliderum.vercel.app` retornou 200 com `Access-Control-Allow-Origin` exato; origin não permitida retornou 403 sem ACAO.
- O usuário confirmou autenticação no frontend. A rota protegida `/members` carregou e apresentou estado vazio normal (0 itens). Pelo código, login só navega após `/users/me` completar com sucesso; o guard de `/members` também exige restauração bem-sucedida por `/users/me`. O request log/status HTTP direto não estava disponível, portanto esta evidência é do fluxo de aplicação e da UI, não captura de rede.
- Nenhum dado de domínio foi criado ou alterado como parte do smoke autenticado.
- A task de integridade que originou V3 teve `clean verify` aprovado com 74 testes; CI verde para revisão foi informado pelo usuário, não verificado independentemente nesta sessão.

## Veredito QA

**APROVADO**, com ressalva documental de que não houve status HTTP direto capturado para os requests autenticados. O fluxo protegido completo e o estado funcional tenant-scoped observado satisfazem o smoke mínimo sem expor credenciais nem criar dados.
