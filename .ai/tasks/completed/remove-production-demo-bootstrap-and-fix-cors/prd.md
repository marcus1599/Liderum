# PRD — Remover bootstrap demo de produção e corrigir CORS

## Classificação

P0 — segurança / infraestrutura — MEDIUM.

## Contexto e problema

O `DataInitializer` é uma `@Configuration` sem restrição de profile. Em banco vazio ele cria automaticamente a Guild `Guilda Exemplo`, um usuário `admin` com papel `MARECHAL`, senha demo conhecida e dados mock de Member/Event. Embora a senha seja armazenada com BCrypt, esse comportamento é inseguro fora de desenvolvimento e conflita com o onboarding real da aplicação.

`application.properties` ativa `dev` por padrão e o Compose também fixa esse profile. Assim, uma execução sem profile explícito recebe o bootstrap. O CORS atual também passa duas origens separadas por vírgula como uma única string a `allowedOrigins`, logo não representa corretamente duas origens permitidas. A regra de `SecurityConfig` que contém uma URL absoluta de frontend não configura CORS.

## Objetivo

Impedir criação automática de dados e credenciais demo fora de desenvolvimento explicitamente selecionado e permitir CORS restritivo por origins configuráveis, preservando autenticação JWT e o fluxo de onboarding existente.

## Requisitos funcionais e de segurança

1. `DataInitializer` só pode executar com o profile Spring `dev` explicitamente ativo.
2. Execução sem profile ativo e execução com `prod` não podem criar Guild, User, Member ou Event demo.
3. O profile padrão não deve ativar `dev` implicitamente. Desenvolvimento local e Compose de demonstração devem selecionar `dev` de forma explícita.
4. As credenciais e dados demo permanecem apenas no código de bootstrap de desenvolvimento; não podem ser criados em produção/default. Nenhuma credencial demo deve ser publicada em documentação/configuração de produção.
5. CORS deve aceitar uma lista de origins individuais configurável por ambiente, preferencialmente por `CORS_ALLOWED_ORIGINS` (lista separada por vírgula).
6. `dev` deve continuar permitir `http://localhost:4200` por configuração explícita e pode permitir override por variável de ambiente.
7. Em `prod`, nenhuma origin pode ser aceita quando `CORS_ALLOWED_ORIGINS` estiver ausente/vazia; não usar wildcard e não habilitar credenciais para origens não permitidas.
8. A integração entre Spring Security e a configuração CORS deve permanecer correta para preflight, sem ampliar endpoints públicos além do necessário. A URL absoluta usada como `requestMatcher` não deve ser tratada como mecanismo de CORS.
9. JWT, RBAC, onboarding público `POST /auth/register-guild` e contratos do frontend não devem ser alterados.

## Escopo

- `DataInitializer`, profiles e propriedades Spring do backend;
- `CorsConfig` e a integração mínima necessária em `SecurityConfig`;
- Docker Compose e `.env.example` somente se necessários para tornar seleção de profile/origins explícita e segura;
- README somente se instruções de execução local precisarem mudar;
- testes de contexto/bootstrap e CORS.

## Fora do escopo

- rate limiting, CAPTCHA ou outra proteção antiabuso do registro público;
- Flyway, migrations ou alteração de schema;
- frontend e alteração de URLs do frontend, salvo dependência comprovada de configuração;
- mudança do fluxo JWT, RBAC, TenantService ou onboarding;
- alterar senha demo para torná-la "mais forte" em vez de restringir o bootstrap;
- deploy, CI/CD ou novos perfis além de `dev`/`prod` necessários à correção.

## Critérios de aceitação

- contexto com profile `dev` cria os dados demo somente quando as tabelas correspondentes estão vazias;
- contexto sem `dev` e contexto `prod` não criam qualquer dado demo;
- nenhum profile é ativado implicitamente pelo `application.properties`;
- uma origin individual configurada recebe os headers CORS esperados, e uma origin fora da lista não;
- múltiplas origins configuradas como lista são tratadas como valores distintos, não como uma string única;
- lista vazia em `prod` não libera CORS cross-origin;
- não existe `allowedOrigins("*")` nem equivalente permissivo com credenciais;
- autenticação existente e suíte backend permanecem aprovadas;
- Docker Compose permanece explicitamente em `dev` para demonstração local, sem representar configuração de produção.

## Dependências e arquivos prováveis

- `backend/src/main/java/com/example/Liderum/Config/DataInitializer.java`
- `backend/src/main/java/com/example/Liderum/Config/CorsConfig.java`
- `backend/src/main/java/com/example/Liderum/Config/SecurityConfig.java`
- `backend/src/main/resources/application.properties`
- `backend/src/main/resources/application-dev.properties`
- `backend/src/main/resources/application-prod.properties`
- `docker-compose.yml`, `.env.example` e `README.md`, se a inspeção de implementação confirmar necessidade;
- novos testes backend em `src/test/java`.

## Riscos

- retirar `dev` implícito pode mudar a experiência de desenvolvimento local; mitigar com instrução explícita e Compose já configurado;
- configuração CORS incorreta pode bloquear o frontend local; mitigar com teste de origin permitida e configuração dev explícita;
- o bootstrap atual influencia testes de contexto; mitigar com testes isolados e ajustes apenas nos testes que dependam indevidamente de seeds.

## Segurança

| Achado | Origem | Classificação | Tratamento nesta task |
| --- | --- | --- | --- |
| Guild/MARECHAL demo de credenciais conhecidas em execução não-dev | PREEXISTENTE_RELACIONADO | bloqueante para release | remover o comportamento de default/prod |
| CORS com lista malformada e origem efetivamente não configurável | PREEXISTENTE_RELACIONADO | alto | configurar lista por ambiente, restritiva |
| Registro público sem proteção antiabuso | PREEXISTENTE_NAO_RELACIONADO | bloqueante para release | manter como task separada |
| Flyway/migrations ausentes | PREEXISTENTE_NAO_RELACIONADO | bloqueante para release | manter como task separada |

## Testes esperados

- integração/contexto para ausência de bootstrap sem profile e com `prod`;
- integração/contexto para bootstrap somente com `dev`;
- MockMvc para origin permitida, não permitida, múltiplas origins e lista vazia;
- suíte backend completa após implementação.
