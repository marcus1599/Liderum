# Execução — `clarify-user-management-actions-and-language`

## Alterações realizadas

- Reorganizada cada linha de usuário em identidade, cargo e ações da conta, sem alterar serviços, rotas, RBAC ou payloads.
- Localizados `Role` e estados internos para `Cargo`, `Aguardando ativação`, `Ativo` e `Desativado` somente na camada de apresentação.
- Mantida a matriz existente: link apenas para `PENDING`; status apenas para `ACTIVE`/`DISABLED`; controles condicionados por `canActOn`.
- Adicionados rótulos contextuais, região de status e testes de apresentação/estado pendente.

## Limitação de inspeção

Não havia sessão autenticada com MARECHAL/GENERAL e exemplos reais de PENDING, ACTIVE e DISABLED nesta execução. A validação usou fixtures locais e ChromeHeadless; não é apresentada como inspeção autenticada ou de produção.

## Validações executadas

- `npm test -- --watch=false --browsers=ChromeHeadless`: 106/106 SUCCESS.
- `npm run build`: SUCCESS fora do sandbox; avisos Sass/budgets já existentes fora do escopo.
- `impeccable.cmd detect --json frontend/src/app/users/users.component.html frontend/src/app/users/users.component.scss`: `[]`.
- `git diff --check`: aprovado, sem erros; avisos LF/CRLF do Git não alteram o conteúdo.
