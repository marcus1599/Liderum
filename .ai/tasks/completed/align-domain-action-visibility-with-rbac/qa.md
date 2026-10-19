# QA — Alinhar ações das áreas de domínio ao RBAC

## Veredito: APROVADO

### Evidências

- ChromeHeadless final: 86 testes executados, 0 falhas, 0 erros, exit code 0. A execução anterior da versão funcional tinha 82/82 SUCCESS; não foi observada flakiness.
- Build de produção Angular: SUCCESS, exit code 0.
- Specs verificam consulta preservada para perfis de leitura, ocultação de controles mutáveis, bloqueio de handlers e ausência de chamadas aos serviços de escrita. Verificam também perfil ainda não resolvido e permissões MARECHAL/GENERAL/MAJOR.
- O fluxo anterior de drag-and-drop, troca de líder e Attendance dos perfis administrativos permaneceu coberto pela suíte existente.
- Loading, feedback de sucesso/erro e empty states permaneceram sem alteração funcional; 403 continua passando pelo helper de feedback seguro já existente.

### Limitações

- Não houve sessão manual autenticada para conferência visual em desktop/handset.
- O detector Impeccable sinalizou quatro elementos de imagem de classe existentes no template de Groups; sem relação com a alteração de visibilidade RBAC.
- Warnings Sass/budget e baseline-browser-mapping permanecem não bloqueantes.
