# PRD — Proteção antiabuso do registro público de Guild

**Classificação:** P0 — segurança — MEDIUM.

## Contexto e problema

`POST /auth/register-guild` é público e chama diretamente o serviço transacional que cria uma `Guild` e o primeiro `MARECHAL`. A validação do payload, BCrypt, Flyway, tenancy server-side e RBAC já existem, mas não há controle de frequência no endpoint. Um agente externo pode disparar registros repetidos, consumindo recursos e criando dados indevidos.

Esse achado é `PREEXISTENTE_NAO_RELACIONADO` às tasks recentes, mas é o último bloqueador global conhecido do Release Verdict. O repositório não possui Redis, API Gateway, WAF ou biblioteca de rate limiting, e o Compose não configura proxy reverso confiável.

## Objetivo

Introduzir uma proteção server-side, mínima e demonstrável, exclusivamente para o registro público de Guild. Ela deve limitar tentativas por cliente, retornar `429 Too Many Requests` sem expor detalhes ou criar dados, preservar login e endpoints autenticados, e ter comportamento configurável por ambiente.

## Requisitos funcionais e de segurança

1. Aplicar o limite somente a `POST /auth/register-guild`, antes de o controller/serviço de onboarding tocar a persistência.
2. Usar uma janela fixa em memória, por endereço remoto observado pelo servidor, com política padrão proposta de **5 tentativas por 15 minutos**. O valor final deve ser configurável por properties para testes e ambientes.
3. Quando o limite for excedido, responder `429 Too Many Requests` com mensagem genérica, sem corpo de erro que revele dados do request, usuário, e-mail, senha, token ou estado interno. Incluir `Retry-After` apenas se for calculado de modo correto e testado.
4. Uma requisição bloqueada não pode criar Guild, User ou outro dado parcial.
5. O contador deve ser seguro sob concorrência; múltiplas tentativas simultâneas do mesmo cliente não podem ultrapassar o limite configurado.
6. Não confiar em `X-Forwarded-For`, `Forwarded` ou headers equivalentes enquanto não existir configuração explícita de proxy confiável. A fonte inicial é `HttpServletRequest.getRemoteAddr()`.
7. Conter crescimento de memória: remover entradas expiradas oportunisticamente e estabelecer limite documentado para chaves rastreadas. Sob saturação, falhar fechado para novas chaves com `429`, sem derrubar a aplicação.
8. O controle permanece habilitado por padrão. Ambiente de desenvolvimento pode alterar limite/janela por configuração explícita para facilitar testes manuais; não pode haver desabilitação implícita em default/produção.
9. `POST /auth/login`, CORS, JWT, `TenantService`, RBAC, contratos de onboarding e endpoints autenticados não devem mudar.
10. Não registrar payloads, credenciais, IPs completos ou identificadores de conta em logs de negócio. Métricas/observabilidade não fazem parte desta task.

## Escopo

- componente backend endpoint-scoped (filtro/interceptor/serviço, escolhido após revisão arquitetural proporcional) e configuração Spring correspondente;
- resposta HTTP 429 consistente e mínima;
- properties documentadas por ambiente, Docker Compose e `.env.example` somente se o código realmente as consumir;
- testes unitários e de integração/MockMvc da política, concorrência proporcional e não criação de dados;
- atualização de evidências de task, `state.md`/`handoff.md` somente ao término comprovado.

## Fora do escopo

- CAPTCHA, verificação de e-mail, frontend, billing, recuperação de senha e MFA;
- Redis, Kafka, API Gateway, WAF, proxy reverso, rate limiting global, observabilidade e CI/CD;
- alteração de JWT, RBAC, `TenantService`, multi-Guild, schema/Flyway ou contratos de registro;
- confiar em headers de encaminhamento sem infraestrutura de proxy confiável;
- mudanças de UI, redesign e refactors não relacionados.

## Critérios de aceitação

- até o limite configurado, um cliente pode usar o endpoint e o onboarding válido preserva o comportamento atual;
- a tentativa acima do limite retorna 429 verificável e não persiste Guild/User;
- outra origem remota observada possui quota independente;
- a quota volta a ser admitida após expirar a janela, de maneira determinística em teste;
- concorrência básica não permite mais admissões que o limite para a mesma chave;
- login e endpoint autenticado representativo não recebem 429 por esse mecanismo;
- nenhum header de proxy não confiável altera a chave usada;
- não há wildcard, secret, senha, JWT ou IP completo registrado pela nova lógica;
- `clean verify` permanece aprovado e os testes novos não dependem de relógio, rede, IP ou ambiente da máquina.

## Segurança e riscos

| Risco | Origem | Severidade | Tratamento |
| --- | --- | --- | --- |
| Criação automatizada massiva de Guilds/MARECHAIS | PREEXISTENTE_RELACIONADO | alta | Limite endpoint-scoped antes do onboarding. |
| Header `X-Forwarded-For` falsificado | PREEXISTENTE_RELACIONADO | alta se usado sem confiança | Ignorar headers até haver proxy confiável configurado. |
| Evasão por múltiplos IPs / botnet | Limitação inerente | média | Mitigação proporcional; CAPTCHA/WAF/e-mail ficam como evolução futura. |
| Limite em memória não compartilhado entre réplicas | Limitação operacional | média | Documentar; Redis/gateway só se escala real exigir. |
| Exaustão de memória por chaves únicas | Risco da solução | média | Expiração + capacidade máxima e falha fechada controlada. |
| Vazar dados em 429/logs | Risco da implementação | média | Mensagem genérica e proibição de log sensível. |

## Dependências

- Sem nova dependência proposta.
- Requer Spring MVC/Security já existentes e testes Spring/MockMvc já disponíveis.
- Sem migration, ADR ou mudança de infraestrutura nesta fase.
