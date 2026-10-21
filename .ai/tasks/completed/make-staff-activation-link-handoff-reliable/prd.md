# PRD — Entrega confiável do link de ativação

## Objetivo

Garantir que MARECHAL/GENERAL obtenham um link de ativação utilizável quando criam ou regeneram um usuário PENDING, mesmo quando a API Clipboard não estiver disponível ou falhar.

## Problema

`UsersComponent` tenta copiar o link e sempre exibe “copiado”, ignorando rejeições do clipboard. Isso pode deixar o administrador sem uma forma confiável de entregar o link.

## Requisitos

- Exibir o link de ativação somente na tela administrativa após resposta bem-sucedida.
- Tentar copiar o link usando a API Clipboard quando disponível.
- Informar sucesso somente quando a cópia for confirmada.
- Quando a cópia falhar ou não existir, oferecer campo somente leitura e ação de seleção/cópia manual.
- Não persistir o token em localStorage, sessionStorage, logs ou URL permanente.
- Reutilizar Angular standalone, Angular Material e o feedback existente.
- Manter o contrato backend, o token de uso único, a validade e as regras RBAC/tenant.

## Fora do escopo

E-mail, recuperação de senha, login automático, alteração de token/backend, novo serviço de estado e redesign de Users.

## Aceite

Criação e regeneração exibem o link; clipboard bem-sucedido mostra confirmação correta; clipboard ausente/rejeitado mantém alternativa manual sem falsa confirmação; navegação remove o estado; testes Angular e build passam.
