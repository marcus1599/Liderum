# PRD — Guild Onboarding UI

## Classificação

P1 — funcionalidade / segurança / qualidade — MEDIUM.

## Problema

O backend já permite criar uma Guild e o primeiro MARECHAL em uma transação pública, mas o fluxo não está acessível pelo Angular. Um visitante não consegue concluir o lifecycle SaaS pela interface.

## Objetivo

Disponibilizar uma rota pública Angular para criar uma Guild e seu primeiro MARECHAL, informar resultado claro e encaminhar o visitante ao login. O registro não autentica automaticamente.

## Contrato confirmado

- Endpoint: `POST /auth/register-guild`, público.
- Request: `guildName`, `serverName`, `username`, `email`, `password`.
- Validações backend: todos obrigatórios; Guild/servidor até 120 caracteres; username de 3 a 80; e-mail válido até 160; senha de 8 a 128.
- Resposta de sucesso: `201 Created` com `UserResponseDTO { id, username, email, guildRole }`; a senha não é retornada.
- O backend cria o User inicial com role `MARECHAL`; a UI não envia role ou `guildId`.

## Escopo

- Criar componente standalone público e rota `/register`.
- Adicionar serviço/contratos tipados de registro, reutilizando a infraestrutura HTTP existente.
- Usar Reactive Forms e Angular Material para os cinco campos reais e confirmação local de senha.
- Aplicar validações locais proporcionais ao contrato backend; backend continua autoridade final.
- Exibir loading, bloquear submit inválido/em andamento, mensagens seguras de sucesso/erro e navegação ao login após sucesso.
- Adicionar link bidirecional login ↔ registro.
- Preservar responsividade do layout de autenticação, reutilizando estilos/estrutura existentes apenas quando útil.
- Adicionar testes Jasmine/Karma comportamentais.

## Fora de escopo

- login automático, gestão de Users, RBAC visual, perfil/configuração de Guild, recuperação de senha, MFA, billing, e-mail, multi-Guild, wizard complexo, E2E, redesign geral e alterações de backend.
- Proteção antiabuso do endpoint público: bloqueador global existente, tratado em task própria.

## Segurança

- A senha existe apenas no controle do formulário durante o submit; não é persistida, registrada nem exibida após envio.
- Não enviar `guildId`, role ou qualquer dado de autoridade de tenant.
- Não expor mensagens internas retornadas por 5xx ou conflitos de integridade; usar mensagem genérica para 4xx e indisponibilidade para erro de rede/5xx.
- Não autenticar nem persistir JWT após o registro.

## Critérios de aceitação

- `/register` é pública e apresenta os cinco campos exigidos.
- Payload contém somente os campos aceitos pelo contrato; confirmação de senha não é enviada.
- Form inválido ou submit em andamento não envia requisição.
- Sucesso `201` mostra feedback e direciona ao login, sem criar sessão.
- Erros 4xx, rede e servidor mostram feedback seguro e permitem nova tentativa.
- O usuário recém-criado consegue seguir para o login usando as credenciais informadas.
- Testes Angular e build permanecem verdes.
