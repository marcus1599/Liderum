# Plano — protect-public-guild-registration-against-abuse

## Routing

- Domínios: `planning`, `backend`, `security`, `testing`, `sre`, `audit`, `documentation`.
- Agents: Planner → Arquiteto (consultivo) → Backend Developer → QA → Security + SRE/DevOps → Auditor.
- Skills: `create-prd`, `create-plan`; na execução, `test-backend`, `security-review`, `audit-task`, `finish-task` somente se Task Verdict for APROVADO.
- Não selecionados: Frontend Developer (não há mudança de UI ou contrato); `create-migration` (sem schema); `create-adr` (decisão endpoint-scoped, reversível e sem mudança de tenancy/JWT); MCP (sem alteração de Agents/Skills/MCP).
- Gate: QA, Security e SRE aprovam antes de Auditoria. Task Verdict e Release Verdict permanecem separados.

## Classificação e objetivo

P0, MEDIUM, segurança. Remover o último bloqueador global conhecido de release protegendo `POST /auth/register-guild` contra volume repetido de registros, sem ampliar a superfície pública nem modificar o lifecycle Guild → primeiro MARECHAL.

## Decisões de planejamento

1. **Mecanismo:** implementar uma política local endpoint-scoped, sem dependência nova, baseada em contador concorrente por janela fixa. A implementação deve ficar antes do controller/onboarding; o Arquiteto valida se um `OncePerRequestFilter` restrito ao método/rota é o encaixe mais simples. Nenhuma regra global de rate limiting será introduzida.
2. **Política inicial proposta:** 5 admissões por 15 minutos, configuráveis por properties. Os valores são deliberadamente modestos para o onboarding público; a autorização de implementação confirma esta política, ou solicita ajuste dos valores.
3. **Identidade de cliente:** `request.getRemoteAddr()` é a fonte de verdade inicial. Não consumir `X-Forwarded-For`/`Forwarded`, pois o repositório não define rede de proxies confiáveis. Atrás de proxy, a quota poderá ser compartilhada pelo proxy — limitação operacional registrada, não uma razão para confiar em header falsificável.
4. **Recuperação e memória:** injetar tempo/relógio ou equivalente controlável para tornar a expiração testável; limpar entradas vencidas oportunisticamente e limitar o número de chaves. Quando não houver capacidade segura para uma nova chave, responder 429 em vez de crescer indefinidamente.
5. **Resposta:** 429 genérico e, se preciso, `Retry-After` derivado da janela real. Nenhum detalhe de chave/IP/payload em resposta ou log. O handler não pode revelar se username/e-mail/Guild já existe.
6. **Ambientes:** manter o controle habilitado por padrão, com properties de limite/janela/capacidade. `dev` pode ajustar explicitamente valores para fluxo manual; Compose/.env.example só mudam se essas properties forem de fato expostas. Não desabilitar automaticamente em prod/default.
7. **Escala:** a solução é por processo e é adequada ao estado atual de portfólio/single instance. Deploy com múltiplas réplicas exigirá controle compartilhado ou rate limiting no gateway/WAF; isto será uma pendência operacional, não uma dependência desta task.
8. **CAPTCHA/e-mail:** descartados agora por dependerem de UI/fornecedor/chaves e não serem necessários para remover o bloqueador com uma barreira demonstrável. Continuam alternativas futuras contra evasão distribuída.

## Etapas de execução

1. Confirmar o diff inicial, o endpoint público, `SecurityConfig`, serviço de onboarding, exception handlers e properties antes de editar. Parar se houver proxy confiável ou mecanismo existente não documentado.
2. Backend Developer implementa o limitador endpoint-scoped e sua configuração, garantindo atomicidade por chave/janela, expiração e contenção de memória. Não modificar `GuildOnboardingServiceImpl`, JWT, RBAC ou schema.
3. Integrar a recusa 429 antes do controller, com resposta segura e mínima. Não registrar request body, senha, token, e-mail ou IP completo.
4. Adicionar properties base/dev/prod e Compose/.env.example apenas quando indispensáveis; documentar a limitação de instância/proxy sem declarar headers confiáveis.
5. Backend Developer cria testes determinísticos da política e MockMvc do endpoint. Usar relógio controlado/configuração de teste; não usar espera real nem IP da máquina.
6. Executar testes direcionados e `./mvnw.cmd clean verify`, aguardando resultado final verificável.
7. QA valida que o limite é anterior à persistência, não há falso positivo de teste/estado partilhado, login permanece livre e os casos de concorrência/expiração são estáveis.
8. Security revisa spoofing de IP, bypass, 429, mensagens/logs, DoS de memória e separa achados por origem. SRE revisa properties, comportamento em Compose e a limitação multi-instância/proxy.
9. Auditor compara o diff com este plano e PRD, confirma ausência de CAPTCHA/Redis/gateway/JWT/RBAC/tenancy/refactor e emite Task/Release Verdict. Executar `finish-task` somente se aprovado.

## Arquivos prováveis

- `backend/src/main/java/com/example/Liderum/Config/SecurityConfig.java` e um novo componente de rate limiting em pacote de segurança/configuração;
- `backend/src/main/java/com/example/Liderum/exceptions/**` apenas se a integração escolhida necessitar de resposta MVC consistente;
- `backend/src/main/resources/application.properties`, `application-dev.properties`, `application-prod.properties`;
- `docker-compose.yml` e `.env.example` somente se properties forem configuráveis no Compose;
- novos testes em `backend/src/test/java/com/example/Liderum/Security/**` ou pacote correspondente;
- artefatos da task em `.ai/tasks/active/protect-public-guild-registration-against-abuse/`.

## Testes planejados

| Área | Cenário |
| --- | --- |
| Política | Abaixo/no limite: admissões permitidas; primeira acima: 429. |
| Persistência | Requisição 429 não cria Guild nem User; onboarding válido dentro da quota continua atômico. |
| Janela | Expiração libera uma nova tentativa por tempo controlado, sem `sleep`. |
| Chaves | Duas origens remotas distintas recebem quotas independentes; header de encaminhamento não altera a chave. |
| Concorrência | Rajada básica da mesma chave não admite mais que o limite. |
| Regressão | Login público não é limitado por esta regra; endpoint autenticado representativo não recebe 429. |
| Resposta | 429 é genérico, sem segredo/credencial/payload, e `Retry-After` só é validado se implementado. |
| Ambiente | Defaults permanecem protetivos; overrides de teste/dev são explícitos e não dependem da máquina. |
| Final | Testes direcionados e `./mvnw.cmd clean verify`; `git diff --check`. |

## Riscos e controles

- **NAT/proxy:** vários usuários podem compartilhar quota quando o servidor só vê o proxy; registrar claramente e não aceitar headers falsificáveis.
- **Evasão distribuída:** rate limit por IP não elimina botnet; CAPTCHA, e-mail e WAF são defesas em camadas futuras.
- **Múltiplas instâncias:** quotas não são globais; não apresentar a proteção como distribuída.
- **Flakiness:** não usar relógio do sistema, `Thread.sleep` ou ordem não determinística em testes.
- **DoS de memória:** capacidade máxima e descarte de vencidos são requisitos, não otimização opcional.

## Critérios de conclusão

- Todos os critérios de aceite do PRD demonstrados por testes determinísticos.
- Sem nova dependência, migration, segredo, wildcard, confiança em header de proxy ou alteração do onboarding/JWT/RBAC/TenantService.
- QA, Security e SRE com Task Verdict APROVADO; auditoria sem scope creep.
- Release Verdict pode mudar para APROVADO somente se esta validação confirmar que não há outro bloqueador global conhecido. A pendência de PostgreSQL real continua risco operacional documentado, mas não bloqueador já aceito.

## Status

PRONTO PARA EXECUÇÃO mediante autorização explícita do usuário sobre a política proposta (5 tentativas por 15 minutos por endereço remoto observado, com configuração por ambiente).
