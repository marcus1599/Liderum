# ADR-001 — User de Guild única e resolução server-side do tenant

**Status:** Aceito

**Data:** 2026-08-24

## Contexto

O Liderum é uma aplicação SaaS de gestão de guildas de RPG. O modelo atual possui `User.guild`, `User.guildRole` e `TenantService`, que carrega o usuário autenticado pelo subject e resolve a Guild persistida. Services de domínio usam essa Guild para filtrar recursos tenant-scoped.

O lifecycle de Guild e User ainda será implementado na task `secure-user-provisioning-and-guild-onboarding`. Hoje, o provisionamento de User não atribui Guild/role de forma segura e não é um boundary adequado para um SaaS. A decisão precisa preservar a fronteira multi-tenant sem duplicar a autoridade do tenant no JWT.

## Problema

O sistema necessita de uma fonte de verdade única para a Guild efetiva do usuário, onboarding atômico de Guild com seu administrador e regras claras para administração de usuários. Usar dados mutáveis ou duplicados no token pode criar divergência entre autorização emitida e vínculo persistido.

## Decisão

1. Cada `User` pertence a **uma única `Guild`** nesta fase do produto.
2. A Guild efetiva do request será resolvida server-side pelo `TenantService`, a partir do usuário autenticado persistido.
3. O JWT manterá o `username` como subject. `guildId` não será adicionado nem usado como fonte de autoridade no token.
4. O registro público de onboarding criará, em uma transação, uma Guild e seu primeiro User, com role `MARECHAL` e senha armazenada pelo `PasswordEncoder`.
5. Usuários subsequentes serão criados apenas por `MARECHAL` da Guild atual e associados à mesma Guild. CRUD genérico público de User não será oferecido.
6. Usuário sem Guild não pode acessar recursos tenant-scoped; o `TenantService` deve falhar explicitamente.

O formato e a validade do JWT não serão alterados por esta decisão. A task poderá avaliar a derivação de authorities a partir do `UserDetails` carregado do banco para evitar papel obsoleto em token, mas isso não altera a fonte de verdade do tenant.

## Consequências

### Autorização e isolamento multi-tenant

- Consultas e mutações de User e recursos de domínio devem usar a Guild atual resolvida pelo `TenantService`.
- Um identificador válido de outra Guild não deve revelar, alterar ou remover dados.
- `MARECHAL` administra usuários apenas da própria Guild; permissões completas dos demais domínios serão detalhadas em task posterior de RBAC.
- O endpoint público fica restrito ao onboarding e login; operações administrativas permanecem autenticadas e tenant-scoped.

### Backend

- O vínculo `User → Guild` existente passa a ser contrato explícito de tenancy.
- O onboarding requer boundary transacional para impedir Guild ou User órfãos.
- DTOs de onboarding e provisionamento devem impedir que solicitante escolha Guild arbitrária ou eleve privilégio indevidamente.
- Repositórios e services de User devem oferecer consultas filtradas por Guild, não `findAll` global para uso administrativo.

### Frontend futuro

- O Angular terá um fluxo de registro de Guild + primeiro administrador e uma área de gestão de usuários da Guild atual.
- A interface não escolhe `guildId` como mecanismo de autorização; o backend aplica o contexto autenticado.
- Uma eventual troca de Guild só poderá existir após revisão formal deste ADR e de seu modelo de membership.

### Testes

- Testes devem provar atomicidade do onboarding, hash de senha, login do primeiro `MARECHAL`, RBAC de User e bloqueio de IDOR cross-Guild.
- Testes de `TenantService` devem preservar o caso de usuário não autenticado ou sem Guild.
- Testes de JWT devem confirmar que o tenant continua vindo da persistência, não de `guildId` em claim.

## Alternativas descartadas

### 1. `guildId` como fonte de autoridade no JWT

**Benefícios:** evita uma consulta para descobrir Guild em cada fluxo e torna o contexto aparente no token.

**Problemas:** duplica o vínculo persistido, fica obsoleto se a associação mudar e permite que uma decisão de autorização seja guiada por claim em vez da fonte de verdade server-side. Também aumenta a superfície de testes e revogação.

**Motivo do descarte:** o `TenantService` já resolve o tenant pelo usuário persistido. O ganho não justifica a duplicação de autoridade.

### 2. Membership `User ↔ Guild` N:N

**Benefícios:** permitiria um usuário participar de múltiplas Guilds e alternar contexto.

**Problemas:** exige modelo de membership, papel por vínculo, seleção de Guild ativa, mudanças nos contratos, no frontend, em JWT/sessão e na estratégia de autorização.

**Motivo do descarte:** não há requisito de produto atual. A complexidade é prematura para o objetivo de portfólio.

### 3. CRUD público genérico de User

**Benefícios:** implementação aparentemente simples de cadastro.

**Problemas:** não define quem cria a Guild, permite usuários sem Guild e abre caminhos para provisionamento e escalonamento de privilégio fora do tenant.

**Motivo do descarte:** onboarding precisa ser um boundary explícito e transacional; administração de usuários é responsabilidade do `MARECHAL` tenant-scoped.

### 4. Guild criada separadamente do primeiro administrador

**Benefícios:** separa endpoints e pode permitir provisionamento posterior por operador.

**Problemas:** introduz estados intermediários de Guild sem administrador e necessidade de recuperação/limpeza se a segunda etapa falhar.

**Motivo do descarte:** o fluxo inicial deve ser atômico, simples e demonstrável; Guild e primeiro `MARECHAL` nascem juntos.

## Limitações conhecidas

- Não há suporte a multi-Guild, convite por e-mail, recuperação de senha, MFA ou limitação antiabuso de registro nesta decisão.
- A revisão abrangente de RBAC dos domínios Members, Teams, Events e Attendance permanece separada.
- A persistência versionada por Flyway será tratada na Fase 2 do roadmap.

## Revisão futura

Suporte a multi-Guild é uma possibilidade futura. Se esse requisito surgir, este ADR deve ser revisitado ou substituído por ADR posterior que defina membership, papel por Guild, troca de contexto e impacto em autenticação/autorização.

## ADRs relacionados

Nenhum.
