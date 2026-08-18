# Liderum — Agent Roles & Development Rules

> Versão: 1.0
>
> Este documento define as regras globais do sistema de agentes utilizado para desenvolver e manter o Liderum.
>
> É a fonte de verdade para responsabilidades, limites, fluxo de desenvolvimento e regras de colaboração entre os agentes.

---

## 1. Princípios fundamentais

### 1.1 Estado real antes de qualquer decisão

Nenhum agente deve assumir que o projeto está em determinado estado sem verificá-lo.

Antes de propor ou executar alterações, deve-se consultar, quando disponível:

* estado atual do projeto;
* código existente;
* configuração;
* testes;
* histórico Git;
* documentação relevante;
* tarefa atual.

O Liderum é um projeto existente. O sistema de agentes deve trabalhar sobre o estado real do projeto, não sobre uma implementação hipotética.

### 1.2 Simplicidade antes de complexidade

A solução escolhida deve ser a mais simples capaz de atender ao requisito real.

Evitar:

* abstrações especulativas;
* generalizações prematuras;
* padrões sem necessidade;
* novas tecnologias sem justificativa;
* infraestrutura desproporcional ao tamanho do projeto.

### 1.3 Escopo controlado

Uma tarefa deve modificar apenas o que é necessário para atingir seu objetivo.

Problemas encontrados durante a implementação que estejam fora do escopo devem ser registrados como pendências ou novas tarefas, salvo quando impedirem diretamente a implementação atual.

### 1.4 Evidência antes de opinião

Sempre que possível, decisões devem ser baseadas em evidências obtidas do projeto:

* código;
* testes;
* documentação;
* métricas;
* histórico;
* documentação oficial das bibliotecas.

Quando houver incerteza sobre uma biblioteca ou framework, consultar documentação atual antes de assumir seu comportamento.

---

# 2. Papéis dos agentes

O sistema possui os seguintes papéis:

| Agente             | Função                                 | Pode modificar código? |
| ------------------ | -------------------------------------- | ---------------------: |
| Planner            | Planejamento de tarefas                |                    Não |
| Arquiteto          | Decisões e avaliação arquitetural      |                    Não |
| Backend Developer  | Implementação backend                  |                    Sim |
| Frontend Developer | Implementação frontend                 |                    Sim |
| QA                 | Estratégia e avaliação de testes       |                    Não |
| Security           | Avaliação de segurança                 |                    Não |
| SRE/DevOps         | Avaliação de infraestrutura e operação |                    Não |
| Auditor            | Validação final da tarefa              |                    Não |

Os agentes consultivos analisam e recomendam.

Os agentes executores implementam.

A sessão principal é responsável por orquestrar o trabalho entre os agentes.

---

# 3. Fonte de verdade

O sistema utiliza diferentes fontes de informação, cada uma com uma responsabilidade específica.

## 3.1 Código

O código versionado pelo Git é a fonte de verdade da implementação.

Não considerar documentação como prova de que determinada funcionalidade existe quando o código indicar o contrário.

## 3.2 Git

O Git é a fonte de verdade do histórico de alterações.

Utilizar Git para verificar:

* alterações atuais;
* histórico;
* branches;
* commits;
* arquivos modificados.

## 3.3 state.md

`state.md` representa o estado técnico conhecido do projeto.

Ele deve responder, de forma resumida:

* o que já existe;
* o que está em desenvolvimento;
* o que está incompleto;
* quais são os principais riscos;
* quais são os próximos objetivos.

O `state.md` deve ser atualizado quando alterações relevantes modificarem o estado do projeto.

## 3.4 plan.md

`plan.md` representa o plano da tarefa atualmente em execução.

Ele deve registrar:

* objetivo;
* escopo;
* etapas;
* arquivos envolvidos;
* progresso;
* pendências.

## 3.5 PRD

O PRD define o comportamento esperado de uma funcionalidade ou tarefa relevante.

Quando uma tarefa possuir PRD, a implementação deve respeitar seu escopo.

## 3.6 ADR

ADRs registram decisões arquiteturais relevantes e permanentes.

Não criar ADR para decisões triviais.

## 3.7 Obsidian

O Obsidian é utilizado como memória e documentação de longo prazo do projeto.

Pode armazenar:

* decisões;
* sessões;
* tarefas;
* conhecimento técnico;
* histórico resumido;
* documentação complementar.

O Obsidian não substitui o código ou o Git como fonte de verdade da implementação.

---

# 4. Fluxo geral de desenvolvimento

Uma tarefa pode seguir o seguinte fluxo:

```text
Usuário
   ↓
Análise da tarefa
   ↓
Planner
   ↓
Avaliação arquitetural quando necessária
   ↓
PRD / Plano
   ↓
Implementação
   ↓
QA
   ↓
Security / SRE quando aplicável
   ↓
Auditoria
   ↓
Git
   ↓
Atualização da documentação e estado
```

O fluxo deve ser proporcional à complexidade da tarefa.

Não utilizar o fluxo completo para alterações triviais.

---

# 5. Classificação das tarefas

## 5.1 Tarefa trivial

Exemplos:

* correção simples;
* alteração local;
* typo;
* pequeno ajuste de configuração;
* correção de erro claramente identificado.

Pode seguir diretamente para o agente executor quando não houver impacto arquitetural, de segurança ou infraestrutura relevante.

## 5.2 Tarefa funcional

Exemplos:

* nova funcionalidade;
* alteração significativa de comportamento;
* novo fluxo de usuário;
* alteração de endpoint.

Normalmente deve possuir planejamento e estratégia de testes.

## 5.3 Tarefa estrutural

Exemplos:

* alteração arquitetural;
* mudança significativa no banco;
* nova integração externa;
* alteração de autenticação/autorização;
* mudança relevante de infraestrutura;
* adoção de nova tecnologia.

Deve ser avaliada pelo Arquiteto e pelos agentes especializados afetados antes da implementação.

---

# 6. Regras de arquitetura

## 6.1 ADR

O Arquiteto pode recomendar um ADR.

Um ADR somente deve ser criado quando houver uma decisão arquitetural relevante, como:

* escolha entre tecnologias;
* mudança de arquitetura;
* decisão com impacto significativo e duradouro;
* trade-off que será importante preservar historicamente.

Nenhum agente deve criar um ADR sem autorização da sessão principal/usuário.

## 6.2 Anti-overengineering

Não adicionar complexidade apenas porque uma solução poderia futuramente escalar.

Considerar:

* necessidade atual;
* volume esperado;
* custo operacional;
* complexidade;
* manutenção;
* possibilidade de evolução futura.

---

# 7. Regras de implementação

## 7.1 Executor

Somente agentes executores podem modificar código.

Os executores atuais são:

* Backend Developer;
* Frontend Developer.

## 7.2 Escopo

O executor deve permanecer dentro do escopo definido para a tarefa.

Alterações adicionais devem ser registradas como novas tarefas ou aprovadas antes de serem incorporadas.

## 7.3 Banco de dados

Alterações estruturais no banco devem utilizar migrations versionadas.

Nunca alterar o schema de produção manualmente como parte normal do desenvolvimento.

## 7.4 Dependências

Uma nova dependência deve possuir justificativa técnica.

Antes de sua adoção devem ser avaliados:

* necessidade;
* manutenção;
* compatibilidade;
* impacto;
* riscos de segurança.

---

# 8. Regras de testes

Toda alteração funcional deve possuir validação adequada ao seu risco.

A estratégia deve priorizar:

1. testes unitários quando apropriados;
2. testes de integração para interação entre componentes;
3. testes end-to-end para fluxos críticos.

Não buscar cobertura de código como objetivo isolado.

Um teste deve validar comportamento relevante do sistema.

Testes flaky são considerados defeitos.

---

# 9. Regras de segurança

Segurança deve ser considerada desde o planejamento.

É proibido:

* commitar secrets;
* expor credenciais;
* registrar tokens em logs;
* registrar dados sensíveis desnecessariamente;
* ignorar vulnerabilidades conhecidas de dependências.

Alterações relacionadas a:

* autenticação;
* autorização;
* multi-tenancy;
* secrets;
* exposição de dados;
* integrações externas;

devem ser avaliadas pelo agente Security.

---

# 10. Regras de infraestrutura

Infraestrutura deve permanecer proporcional ao projeto.

O sistema deve priorizar:

* ambiente reproduzível;
* configuração simples;
* diagnóstico fácil;
* baixo custo operacional.

Não introduzir infraestrutura complexa sem necessidade real.

Alterações relevantes em:

* Docker;
* CI/CD;
* deploy;
* observabilidade;
* banco;
* mensageria;
* serviços externos;

devem envolver o agente SRE/DevOps quando aplicável.

---

# 11. Git

Git deve ser utilizado para preservar o histórico do desenvolvimento.

Antes de alterações relevantes, o estado atual deve ser verificável.

Antes de qualquer commit importante, deve-se verificar:

* arquivos modificados;
* diff;
* testes;
* secrets;
* alterações fora do escopo.

Agentes consultivos não devem realizar commits.

Push não deve ser realizado automaticamente pelo sistema de agentes.

O push deve ocorrer somente após validação da alteração.

---

# 12. Documentação

Alterações relevantes devem atualizar a documentação correspondente quando necessário.

A documentação deve refletir o estado real do projeto.

Não documentar funcionalidades que não existem apenas para manter a documentação "bonita".

Quando documentação e código divergirem, a divergência deve ser identificada e corrigida.

---

# 13. Obsidian

O sistema deve registrar no Obsidian informações úteis para continuidade do projeto.

Após tarefas relevantes, considerar registrar:

* objetivo;
* alterações;
* decisões;
* problemas encontrados;
* testes executados;
* próximos passos.

Evitar registrar informações redundantes ou grandes cópias do código.

O Obsidian deve funcionar como memória útil, não como depósito de logs indiscriminados.

---

# 14. Limites dos agentes

Nenhum agente deve:

* assumir que uma funcionalidade existe sem verificá-la;
* modificar arquivos fora do escopo sem autorização;
* criar decisões arquiteturais permanentes sem registro adequado;
* ignorar testes;
* ignorar problemas de segurança relevantes;
* introduzir tecnologia sem justificativa;
* substituir o julgamento da sessão principal em decisões que exigem autorização do usuário.

---

# 15. Escalonamento

Um agente deve interromper a execução e solicitar decisão da sessão principal quando encontrar:

* conflito entre requisitos;
* conflito entre PRD e arquitetura;
* decisão arquitetural sem definição;
* risco de segurança relevante;
* alteração fora do escopo;
* necessidade de nova tecnologia com impacto significativo;
* necessidade de quebrar uma tarefa grande;
* informação insuficiente para implementar corretamente.

---

# 16. Princípio final

O sistema de agentes existe para ajudar a desenvolver o Liderum com mais consistência, segurança e rastreabilidade.

Ele não deve transformar o desenvolvimento em um processo burocrático.

A regra geral é:

> **Inspecionar → entender → planejar → implementar → validar → registrar.**

E sempre:

> **A menor complexidade que resolve corretamente o problema real.**
