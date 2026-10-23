# Liderum — Direção visual

## Direção

SaaS moderno e profissional com identidade RPG discreta. A interface serve primeiro às tarefas administrativas da Guild; a temática aparece na linguagem de domínio já estabelecida (Guild, grupos, membros, cargos e classes), não em ornamentos ou metáforas que reduzam a clareza.

Este documento é a referência visual canônica do produto. Impeccable deve consultá-lo como contexto durável. `PRODUCT.md` e `DESIGN.md` não são necessários para pequenas melhorias em superfícies existentes; quando uma ferramenta exigir esses nomes, eles devem apontar para esta direção ou resumi-la sem criar regras concorrentes. Não duplicar aqui regras específicas de cada task.

Para Impeccable, tratar este produto existente como modo **Operate** e esta direção como brief canônico do produto. Não gerar um novo `PRODUCT.md` ou `DESIGN.md` como efeito colateral de refinamentos delimitados; se no futuro o projeto exigir um desses formatos para um redesign, manter este documento como fonte e referenciá-lo explicitamente.

## Evidência visual existente

- Frontend Angular standalone com Angular Material; preservar seus controles, estados e comportamento de teclado.
- Tema global em `frontend/src/styles.scss`: paleta Material violeta, Roboto e densidade `-2`.
- Superfície global observada: `#363a41`; texto principal em áreas internas: `#e8e6f0`; superfícies de grupos em `#23242a8c` e acentos violeta/lilás existentes.
- SCSS local por componente e breakpoints específicos já são usados. Esses valores descrevem o estado atual, não autorizam uma troca de paleta global.

## Princípios

### Tipografia e hierarquia

- Manter Roboto e a escala provida pelo Material salvo necessidade demonstrada.
- Uma página tem um título principal; seções usam títulos menores com nomes de tarefa, não etiquetas decorativas.
- Escrever em português claro e usar termos de domínio consistentes. Cargos/classes podem preservar os nomes oficiais do produto.
- Labels identificam o valor esperado; instruções curtas explicam sequência, consequência ou recuperação.

### Cor e identidade

- Usar o violeta/lilás do tema Material para ações primárias, seleção e foco, e superfícies escuras existentes como base.
- Não escolher novas cores sem verificar tokens atuais, contraste e uso semântico.
- Sucesso, alerta, erro e desabilitado devem seguir a semântica do Material; cor nunca é o único indicador.
- RPG fica discreto na terminologia e nos dados do produto. Evitar brasões, ornamentos, texturas e ícones temáticos sem função.

### Espaçamento e superfícies

- Preservar a densidade Material atual onde ela facilita tarefas repetidas; criar espaço entre grupos de decisões diferentes.
- Usar alinhamento e agrupamento para mostrar a ordem da tarefa. Evitar cartões aninhados e bordas decorativas pesadas.
- Superfícies existentes podem ganhar contraste e separação com tokens já presentes, sem CSS global ou sombras redundantes.

### Formulários e dados

- Preferir campos, selects, botões, tabelas e componentes Material já instalados.
- Campo sempre tem label persistente; instruções e validação aparecem junto do campo.
- Agrupar controles pela decisão que representam e explicitar valores de origem/destino antes de mutações.
- Listas e tabelas mantêm cabeçalhos/nomes compreensíveis, estados vazios honestos e ações relacionadas ao registro correto.

### Navegação e ações

- Preservar rotas, shell compartilhado e RBAC existente.
- Ação primária é evidente; ações destrutivas ou administrativas têm nome e contexto do alvo.
- A interface não substitui autorização server-side nem decide tenant a partir de dados do cliente.
- Alternativas por teclado e toque permanecem disponíveis quando houver gestos como arrastar.

### Estados

- Distinguir carregamento, vazio real, erro, dados desatualizados, salvamento e sucesso.
- Confirmar sucesso somente após resposta positiva do servidor quando a operação depender dele.
- Em erro, preservar seleção/contexto quando seguro e oferecer recuperação clara.
- Desabilitar controles durante operações concorrentes e anunciar feedback assíncrono relevante de forma acessível.

### Acessibilidade e responsividade

- Usar semântica HTML, nomes acessíveis em controles e ícones, foco visível e ordem de teclado lógica.
- Feedback importante usa regiões `status`/`alert` conforme o caso; mudanças não dependem apenas de cor.
- Verificar contraste, zoom, conteúdo longo e alvos de toque.
- Projetar primeiro para a largura disponível: layouts empilham controles quando necessário e evitam rolagem horizontal involuntária.
- Respeitar movimento reduzido quando houver animações. Não declarar conformidade WCAG completa sem auditoria específica.

## Aplicação

Para refinamentos, preservar aparência, função e comportamento fora do escopo. Alterações em tokens ou identidade global requerem evidência comparativa e planejamento próprio. Impeccable, `ux-review`, os testes de frontend e os gates do Router se complementam; nenhum substitui os demais.
