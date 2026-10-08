# PRD — Secure Staff User Activation Flow

**Classificação:** P0 — segurança/funcionalidade estrutural — LARGE.

## Problema

Hoje `POST /users` exige `password`. Isso obriga MARECHAL/GENERAL a conhecer a senha definitiva da pessoa provisionada e a transmiti-la por canal externo. O frontend também coleta essa senha. O fluxo contraria o princípio de identidade individual e não é adequado a uma Guild real.

## Objetivo

Permitir que um administrador autorizado crie uma conta na própria Guild sem definir a senha final. O usuário recebe um link copiável, define a senha por um token seguro e passa de `PENDING` para `ACTIVE` antes de poder autenticar.

## Lifecycle aprovado

1. MARECHAL ou GENERAL informa username, e-mail e uma role permitida pela hierarquia atual.
2. O backend deriva a Guild pelo `TenantService`, cria o `User` como `PENDING` e emite token criptograficamente aleatório.
3. Somente o hash do token é persistido. O token bruto é devolvido uma única vez ao administrador, que copia o link e o envia externamente.
4. O link expira em 24 horas. A tela pública recebe token e nova senha, sem pedir Guild ou role.
5. Uma ativação válida grava somente BCrypt, marca o token como usado e muda a conta para `ACTIVE` em uma transação.
6. Token inválido, expirado, usado ou revogado produz resposta genérica e não altera estado.
7. Regeneração autorizada para conta `PENDING` invalida tokens anteriores antes de emitir outro.
8. O primeiro MARECHAL do onboarding permanece `ACTIVE`; contas existentes serão migradas para `ACTIVE`.

## Modelo

- adicionar status explícito de conta: `PENDING`, `ACTIVE`, `DISABLED`;
- adicionar entidade/tabela `UserActivationToken`, ligada ao `User`, com hash único, expiração, uso/revogação e timestamps mínimos;
- permitir `password` nulo somente enquanto a conta estiver `PENDING`; `ACTIVE` exige hash BCrypt por regra de serviço;
- `DISABLED` entra no modelo durável, mas o endpoint completo de desativação fica fora desta task; qualquer conta nesse estado não autentica;
- manter User → uma Guild e o vínculo opcional User → Member; não criar N:N.

Uma entidade de token separada é preferível a colunas no `User`: suporta regeneração/invalidação, uso único e auditoria mínima sem misturar credencial temporária ao agregado de identidade. JWT não será usado como token de ativação, pois revogação e uso único exigiriam estado de qualquer modo.

## Segurança

- token bruto com ao menos 256 bits de entropia, gerado por CSPRNG; persistir hash SHA-256, nunca token bruto;
- não registrar token, senha, Authorization, e-mail ou payload;
- token de uso único, expiração de 24 horas e invalidação atômica dos anteriores na regeneração;
- ativação transacional e protegida contra replay/concorrência, por lock ou atualização condicional equivalente;
- mensagens uniformes não confirmam existência, estado ou Guild da conta;
- senha atende às validações existentes e é persistida exclusivamente com BCrypt;
- `CustomUserDetails` considera habilitado somente `ACTIVE`; falha de login permanece genérica;
- alta entropia torna enumeração impraticável. Rate limit específico é defesa em profundidade e só deve entrar se puder reutilizar a infraestrutura atual sem ampliar indevidamente a task;
- a página não persiste token/senha; deve remover o token da URL após capturá-lo quando tecnicamente simples e evitar dados sensíveis em snackbar/log;
- HTTPS é requisito operacional de produção.

## RBAC e tenant

- MARECHAL pode provisionar roles permitidas pela matriz atual, inclusive GENERAL/MARECHAL, respeitando a invariância de MARECHAL;
- GENERAL provisiona somente MAJOR, CAPITÃO e SOLDADO;
- regeneração segue a mesma hierarquia e só alcança `PENDING` da Guild autenticada;
- MAJOR, CAPITÃO e SOLDADO não provisionam nem regeneram contas;
- o endpoint público de ativação deriva o usuário pelo hash do token persistido; não recebe `guildId` nem role;
- acesso administrativo cross-Guild continua 404/bloqueado e não revela existência.

## Backend

- substituir o contrato administrativo de criação para não aceitar `password` nem `guildId`;
- devolver resposta específica contendo dados seguros da conta e o token/link somente na criação/regeneração;
- adicionar endpoint autenticado de regeneração e endpoint público de ativação;
- adicionar status às respostas de User, sem expor password/hash/token;
- nova migration Flyway incremental; V1 permanece imutável;
- onboarding, JWT de sessão, TenantService e hierarquia vigente permanecem arquiteturalmente inalterados.

## Frontend

- `/users`: remover senha do cadastro, exibir status e ação de copiar/regenerar link conforme RBAC;
- rota pública `/activate` com Reactive Form para senha e confirmação local;
- feedback genérico, loading e bloqueio de duplo submit;
- sucesso direciona ao login; não há login automático;
- não persistir senha/token e não usar `guildId` como autoridade.

## Fora de escopo

- provedor de e-mail, convite automático, confirmação de e-mail, recuperação/alteração de senha;
- fluxo completo de desativação/reativação ou transferência de ownership;
- User ↔ Member obrigatório, multi-Guild, billing, MFA e mudanças de JWT;
- novas roles, permissões dinâmicas, redesign, NgRx ou nova infraestrutura distribuída.

## Critérios de aceitação

- administrador nunca fornece nem conhece a senha final de usuários subsequentes;
- conta PENDING não autentica; após ativação válida autentica normalmente;
- token bruto não é persistido/exposto depois da resposta inicial, expira em 24h e funciona uma única vez;
- regeneração invalida o token anterior e respeita tenant/RBAC;
- ativação concorrente não produz dois sucessos nem estado parcial;
- frontend conclui provisionamento → cópia do link → ativação → login;
- onboarding do primeiro MARECHAL e usuários existentes permanecem funcionais;
- H2 e PostgreSQL/Flyway CI validam a migration; suites backend/frontend ficam verdes.
