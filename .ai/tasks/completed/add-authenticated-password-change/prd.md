# PRD — add-authenticated-password-change

Status: ESCOPO APROVADO PARA EXECUÇÃO EM 2026-10-15. A proposta de invalidação de JWT legado foi aprovada junto com o escopo. ADR-002 foi recomendado, mas não criado: a autorização recebida não especificou a criação do ADR.

## Objetivo

Permitir que qualquer titular ACTIVE altere a própria senha após confirmar a senha atual, com persistência BCrypt e invalidação das sessões anteriores. Entregar o fluxo completo backend/Angular, acessível no shell autenticado.

Classificação STRUCTURAL (credenciais/JWT/schema), prioridade P1, esforço LARGE. Capacidade NEXT do roadmap pós-MVP. Contexto e priorização em `inventory.md`.

## Contrato proposto

- `PUT /users/me/password`, autenticado, disponível para todas as roles atuais de conta ACTIVE.
- Request explícito: `{ currentPassword, newPassword }`; confirmação de senha pertence apenas à UI. Não aceitar id/username/e-mail/Guild/role/status/versão de credencial para selecionar o alvo ou alterar autoridade. Campos extras nesse contrato devem ser rejeitados com 400; não alterar política global de JSON para conseguir isso.
- Alvo exclusivamente o User persistido do principal autenticado, resolvido por `TenantService`; revalidar identidade, Guild, status e versão sob lock na operação transacional.
- Sucesso: `204 No Content`. Não retornar senha, hash, novo JWT ou versão de credencial. Não autenticar novamente de forma automática.
- 400 com código seguro `CURRENT_PASSWORD_INVALID`, `PASSWORD_UNCHANGED` ou `INVALID_PASSWORD_REQUEST`, conforme o caso. Senha atual incorreta é falha da confirmação, não expiração da sessão; não devolver 401 nesse caso.
- 401 para token ausente, expirado, inválido ou revogado, ou principal inexistente/inativo. 403 permanece para usuário autenticado sem permissão em outros endpoints. Configurar o boundary necessário para essa distinção e cobri-lo por HTTP real.
- Falhas inesperadas do novo fluxo: 5xx com mensagem fixa sem SQL, stack, valores rejeitados ou corpo bruto. Erros de parsing desse endpoint também não podem repetir conteúdo sensível.

## Regras de senha e atomicidade

1. Verificar senha atual pelo `PasswordEncoder.matches` contra o hash persistido, dentro da transação que grava a alteração. Não comparar strings de hashes BCrypt.
2. Nova senha: não vazia, mínimo de 8 e máximo de 128 caracteres como referência dos fluxos atuais, adicionalmente no máximo 72 bytes UTF-8 para BCrypt. Não truncar, normalizar, fazer trim ou introduzir pre-hash. Testar ASCII e multibyte. Senha atual preserva compatibilidade de entrada do login existente, sem aplicar retroativamente o novo limite de bytes.
3. Recusar nova senha equivalente à atual segundo o encoder. Persistir exclusivamente BCrypt usando o bean existente, sem trocar algoritmo/custo nesta task.
4. Gravar hash e incrementar versão de credencial atomicamente. Erro/rollback deixa ambos inalterados. Duas trocas concorrentes com a mesma credencial antiga não podem ambas ter sucesso; usar lock de escrita no User alvo e revalidar após adquirir lock.
5. Password, hash e versão não entram em UserResponseDTO. Não logar request/DTO sensível, headers ou token; evitar geração automática de toString contendo senhas.
6. Sem mudança de role/status/Guild, sem desativar o último MARECHAL nem editar outro User; conta sem Guild falha fechada.

## Sessões e JWT — proposta arquitetural explícita

- Adicionar `users.credential_version BIGINT NOT NULL DEFAULT 0`, sem índice novo. Versão só incrementa nesta troca, não a cada login nem a cada gravação genérica de User.
- JWT novo mantém subject=username, assinatura/segredo, expiração e formato externo do login; acrescenta `uid` e `cv`, emitidos exclusivamente pelo backend a partir do mesmo principal cuja senha foi verificada. `uid` identifica a conta persistida; `cv` é sua versão de credencial. Nenhum guildId ou hash/senha no token.
- A cada request autenticada, comparar uid/cv com User carregado do banco e conferir ACTIVE. As authorities continuam derivadas do banco e Guild via TenantService. O claim não dispensa consulta nem concede permissão.
- uid impede reutilização de token após exclusão e recriação de conta com o mesmo username e versão inicial zero. Claims ausentes, malformados ou divergentes não autenticam.
- Após commit da troca, JWTs emitidos com a versão anterior são rejeitados em requests seguintes, inclusive o usado na troca. Requests que já passaram pela autenticação antes do commit não são canceladas retroativamente; a própria troca revalida sob lock para impedir alterações concorrentes com credencial obsoleta.
- Login concorrente com troca não pode emitir versão nova a partir de senha antiga: usar snapshot do principal efetivamente autenticado, nunca buscar apenas a versão mais recente depois da validação de senha.
- Política de implantação proposta: tokens legados sem uid/cv deixam de autenticar; usuários precisarão fazer login uma vez após o deploy. Nenhuma senha/dado de conta é perdida. Não aceitar claim ausente como versão zero. Requer aprovação explícita desta consequência junto do escopo.
- Não criar refresh token, blacklist, Redis, tabela de sessões, rotação de JWT_SECRET ou gestão de dispositivos.

Essa decisão complementa ADR-001: preserva User de Guild única, subject username e tenant server-side. A frase do ADR sobre não alterar o JWT descreve o escopo daquela decisão; esta evolução de revogação será explícita e não uma edição silenciosa. Recomenda-se ADR-002 específico antes da implementação; não criado neste planejamento.

## UX

- Rota filha protegida `/account/password`, com link “Alterar senha” disponível a todo usuário autenticado no sidebar compartilhado, independente do link administrativo Users. Não criar Settings genérico.
- Reactive Forms + Material e tema existente; campos “Senha atual”, “Nova senha”, “Confirmar nova senha”, autocomplete adequado (`current-password`/`new-password`), labels e erros de campo. Confirmação não é enviada à API.
- Botão desabilitado em formulário inválido/pending; bloqueio também no handler. Não repetir PUT automaticamente. Loading anunciado e campos protegidos contra submit concorrente.
- Sucesso: limpar formulário, remover token/perfil local, navegar para login e exibir confirmação não sensível de que é necessário entrar com a nova senha. Sem guardar senha/query params sensíveis nem login automático.
- 400 conhecido: feedback específico e seguro; limpar senha atual inválida, manter nova senha apenas na memória do formulário enquanto a tela estiver aberta. 401 usa expiração existente; 403 informa restrição sem logout; demais falhas usam texto local.
- Rede/timeout/5xx podem ocorrer após persistência: não afirmar que senha antiga continua válida nem repetir a operação. Informar que não foi possível confirmar, orientar novo login e oferecer ação “Ir para login” que limpa sessão/campos.
- Limpar dados sensíveis ao sair/destruir a tela. Nunca persistir senhas em localStorage/sessionStorage, URLs ou logs. Não prometer apagar preenchimento mantido pelo gerenciador de senhas do navegador.
- Verificar teclado, foco, alert/status, contraste e larguras desktop/handset. Registrar evidência real e limites.

## Fora de escopo

Reset por administrador, esqueci minha senha, e-mail/MFA, alteração de username/e-mail, desativação/reativação de Staff, novo RBAC, multi-Guild, líder de Team, handlers globais de domínio, novo rate limiter/infra, dependências, redesign, E2E, troca de algoritmo ou mudança global de política de senha. Limitação atual: não adiciona proteção específica contra tentativas de confirmação de senha; requer sessão válida, não recebe alvo arbitrário. Security deve avaliar risco concreto; se bloqueante, escalar sem adicionar infraestrutura silenciosamente.

## Aceite

- Troca própria funciona para cada role ACTIVE; senha anterior não autentica, nova autentica; apenas hash/versão do próprio User mudam.
- Todos os JWTs anteriores da conta são recusados após a troca; sessões de outros usuários/Guilds permanecem válidas; token de conta excluída não autentica uma conta recriada com mesmo username.
- Ausência/invalidade/revogação do token, PENDING/DISABLED, senha incorreta, confirmação inválida, limite em bytes, campos extras e concorrência não permitem mutação indevida.
- Erro/log não expõe senhas, hash, Authorization ou JWT. Handler real do novo endpoint comprovado.
- V4 aplica em H2 e PostgreSQL 18 fresh e upgrade com User existente preservado; Hibernate validate passa; V1–V3 intactas.
- Suítes backend/frontend e build passam; UX verificada; QA → Security → SRE/DevOps → Auditor; finish-task somente após Task Verdict APROVADO, com Release Verdict separado.
