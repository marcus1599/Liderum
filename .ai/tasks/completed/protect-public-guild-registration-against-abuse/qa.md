# QA — protect-public-guild-registration-against-abuse

## Veredito

**APROVADO**

## Evidências

- Testes unitários verificam limite, isolamento por chave e expiração sem espera real.
- Integração MockMvc verifica cinco admissões, sexta tentativa 429, ausência de persistência, headers `X-Forwarded-For`/`Forwarded` sem efeito e quotas independentes por endereço remoto.
- A mesma integração comprova que login e `GET /users/me` continuam funcionando após o bucket de registro ser esgotado.
- A resposta 429 não contém senha nem IP completo.
- `clean verify`: 57 testes, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS.
- O override de limite em `RbacUserTenantBoundariesIntegrationTest` é explícito e limitado ao teste que exercita muitos onboardings; não altera configuração runtime.

## Avaliação

O filtro ocorre antes do controller e da persistência. Os testes são determinísticos, não dependem de relógio real, rede externa ou segredo da máquina. Não foram removidas assertions úteis nem criadas features fora do plano.
