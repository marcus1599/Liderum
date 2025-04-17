## 📄 Como Criar e Adicionar o `README.md` no Windows

1. **Abra o Explorador de Arquivos**  
   Navegue até a pasta raiz do seu projeto `liderum`.

2. **Crie o Arquivo `README.md`**  
   - Clique com o botão direito → Novo → Documento de Texto.  
   - Renomeie `Novo documento de texto.txt` para `README.md`.

3. **Abra o `README.md` no seu Editor**  
   - No Visual Studio Code (ou similar), abra a pasta `liderum` e dê um duplo‑clique em `README.md`.

4. **Cole o Conteúdo Markdown**  
   Copie todo o conteúdo abaixo e cole dentro do `README.md`:

   ```md
   # 🎮 Liderum

   ## 🚀 Descrição
   Liderum é um sistema de gestão para guildas de RPG, projetado para facilitar a organização de membros, equipes, eventos e o controle de presença. A aplicação permite que líderes de guildas monitorem a participação de membros, mantenham informações organizadas e integrem‑se com ferramentas como autenticação JWT.

   ## ⚙️ Tecnologias Utilizadas
   - **Back-End**:
     - Spring Boot
     - Spring Security (JWT)
     - JPA/Hibernate
     - Banco de Dados H2 (em memória)
   - **Front-End**:
     - Angular (com standalone components)
     - Angular Material


   
    ## 🔧 Como Rodar o Projeto Localmente

    ### Back-End
    ```bash
    cd backend
    mvn spring-boot:run

    A API estará em http://localhost:8080

    📝 Funcionalidades Implementadas
        Autenticação com JWT

        Gestão de membros (add, editar, listar)

        Controle de presença em eventos

        Dashboard para visualizar dados da guilda

    🚧 Status do Projeto
        ✅ Back-End completo

        🚧 Front-End em desenvolvimento

    📜 Documentação da API
        Acesse no navegador: http://localhost:8080/swagger-ui.html

    🤝 Contribuição
        Faça um fork deste repositório

        Crie uma branch: git checkout -b feature/minha-feature

        Commit suas mudanças: git commit -m "feat: minha feature"

        Envie para o repositório original: git push origin feature/minha-feature

        Abra um Pull Request