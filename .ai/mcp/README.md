# Liderum MCP Server — Manual & Configuração

> Servidor MCP (Model Context Protocol) oficial do ecossistema de agentes do **Liderum**.
>
> Este servidor fornece ferramentas (*tools*) e recursos (*resources*) seguros para que agentes de IA e clientes MCP (como Codex, AGY, Claude Desktop, Cursor e Antigravity) interajam com o projeto Liderum de forma controlada.

---

## 1. Visão Geral e Arquitetura

O MCP atua como a ponte de contexto entre os modelos de linguagem (IA) e o repositório do Liderum:

```text
  Agente / Codex / IA
         │ (JSON-RPC via stdio)
         ▼
 ┌───────────────────────┐
 │  Liderum MCP Server   │
 └──────────┬────────────┘
            │ (Acesso Seguro com Sandbox)
            ▼
 ┌───────────────────────┐
 │ Repositório Liderum   │
 │ (.ai/, backend, etc.) │
 └───────────────────────┘
```

---

## 2. Ferramentas Disponíveis (*Tools*)

| Ferramenta | Descrição | Parâmetros |
| --- | --- | --- |
| `get_project_state` | Retorna o snapshot técnico conhecido do projeto (`.ai/state.md`) e opcionalmente as regras (`.ai/roles.md`). | `include_roles` (boolean, opcional) |
| `read_project_file` | Lê o conteúdo de um arquivo do repositório de forma segura. | `relative_path` (string, obrigatório) |
| `search_project` | Pesquisa por termos de texto dentro do código do projeto. | `query` (string), `directory` (string, opcional) |
| `list_project_structure` | Lista os arquivos e subdiretórios de uma pasta do projeto. | `relative_path` (string, opcional) |
| `get_current_task` | Obtém o plano e tarefas ativas registradas em `.ai/tasks/active/`. | nenhum |
| `get_git_status` | Retorna o status do Git no repositório Liderum (`git status`). | nenhum |
| `get_git_diff` | Retorna o diff de alterações pendentes (`git diff`). | `staged` (boolean, opcional) |

---

## 3. Recursos Disponíveis (*Resources*)

| URI | Nome | Descrição |
| --- | --- | --- |
| `liderum://state` | Liderum Project State | Leitura direta do snapshot `.ai/state.md` |
| `liderum://roles` | Liderum Agent Roles | Leitura direta das regras e constituição `.ai/roles.md` |
| `liderum://lib` | Liderum Tech Library | Leitura direta do catálogo de dependências `.ai/lib.md` |

---

## 4. Modelo de Segurança

O servidor MCP foi construído com as seguintes camadas de proteção:
1. **Sandbox de Caminho (Path Traversal Protection):** Impede qualquer leitura fora do diretório raiz do repositório Liderum. Tentativas de acessar `../../` são bloqueadas.
2. **Proteção de Segredos:** Bloqueia a leitura de arquivos contendo chaves ou dados sensíveis (ex.: `.env`, `*.pem`, `*.key`, `id_rsa`).
3. **Operações Somente Leitura:** As ferramentas expostas não realizam edições destrutivas ou comandos remotos sem controle no repositório.
4. **Comandos Git Restritos:** Apenas `git status` e `git diff` são suportados via ferramentas. Operações de `git push`, `git reset` ou `git commit` automatizadas não são executadas pelo servidor MCP.

---

## 5. Como Executar e Testar Localmente

### 5.1 Requisitos
- Node.js (v20 ou superior)
- NPM

### 5.2 Compilação
No diretório `.ai/mcp/server`:
```bash
npm install
npm run build
npm test
```

### 5.3 Execução em Modo de Desenvolvimento
```bash
npm run dev
```

---

## 6. Configuração de Integração com o Codex / Clientes MCP

Para registrar este servidor MCP no seu ambiente do **Codex**, **Claude Desktop** ou **Antigravity**, copie `.ai/mcp/config/mcp-config.example.json` para `mcp-config.json`, ajuste os caminhos locais e adicione a configuração ao arquivo global do cliente MCP. O arquivo `mcp-config.json` é local e não deve ser versionado.

### Localização típica dos arquivos de configuração MCP:
- **Codex / AGY:** `~/.codex/mcp.json` ou `%USERPROFILE%\.codex\mcp.json`
- **Claude Desktop:** `%APPDATA%\Claude\claude_desktop_config.json`
- **Antigravity / VS Code:** `.vscode/mcp.json`

### Exemplo de Configuração JSON
```json
{
  "mcpServers": {
    "liderum-mcp": {
      "command": "node",
      "args": [
        "<CAMINHO-DO-PROJETO>/.ai/mcp/server/dist/index.js"
      ]
    }
  }
}
```

`LIDERUM_PROJECT_ROOT` é opcional: o servidor resolve automaticamente a raiz a partir do próprio arquivo. Defina-a apenas quando precisar sobrescrever essa localização.

---

## 7. Verificação da Conexão

Após adicionar a configuração ao seu cliente MCP:
1. Inicie ou reinicie a sessão do Codex / IDE.
2. Digite ou solicite: *"Quais ferramentas MCP do Liderum estão registradas?"*
3. O cliente exibirá `get_project_state`, `read_project_file`, `search_project`, etc.
