import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
  ListResourcesRequestSchema,
  ReadResourceRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";
import * as fs from "fs";
import * as path from "path";
import { fileURLToPath } from "url";
import { exec } from "child_process";
import { promisify } from "util";

const execAsync = promisify(exec);

// The source and compiled files both live four directories below the project root.
// LIDERUM_PROJECT_ROOT remains an explicit override for client integrations.
const MODULE_DIR = path.dirname(fileURLToPath(import.meta.url));
const PROJECT_ROOT = path.resolve(process.env.LIDERUM_PROJECT_ROOT || path.join(MODULE_DIR, "..", "..", "..", ".."));

/**
 * Validates that a resolved path is inside PROJECT_ROOT and does not access forbidden files.
 */
function validateSafePath(targetRelativeOrAbsolute: string): string {
  const absolutePath = path.isAbsolute(targetRelativeOrAbsolute)
    ? path.normalize(targetRelativeOrAbsolute)
    : path.normalize(path.join(PROJECT_ROOT, targetRelativeOrAbsolute));

  const relative = path.relative(PROJECT_ROOT, absolutePath);

  if (relative.startsWith("..") || path.isAbsolute(relative)) {
    throw new Error(`Acesso negado: O caminho '${targetRelativeOrAbsolute}' está fora do repositório Liderum.`);
  }

  const baseName = path.basename(absolutePath).toLowerCase();
  if (baseName === ".env" || baseName.endsWith(".pem") || baseName.endsWith(".key") || baseName === "id_rsa") {
    throw new Error(`Acesso negado: O arquivo '${baseName}' contém segredos protegidos e não pode ser lido.`);
  }

  return absolutePath;
}

const server = new Server(
  {
    name: "liderum-mcp-server",
    version: "1.0.0",
  },
  {
    capabilities: {
      tools: {},
      resources: {},
    },
  }
);

/**
 * List Resources
 */
server.setRequestHandler(ListResourcesRequestSchema, async () => {
  return {
    resources: [
      {
        uri: "liderum://state",
        name: "Liderum Project State",
        description: "Snapshot do estado técnico conhecido do Liderum (.ai/state.md)",
        mimeType: "text/markdown",
      },
      {
        uri: "liderum://roles",
        name: "Liderum Agent Roles & Rules",
        description: "Constituição e regras globais dos agentes (.ai/roles.md)",
        mimeType: "text/markdown",
      },
      {
        uri: "liderum://lib",
        name: "Liderum Tech Library & Dependencies",
        description: "Catálogo de dependências e tecnologias (.ai/lib.md)",
        mimeType: "text/markdown",
      },
    ],
  };
});

/**
 * Read Resource
 */
server.setRequestHandler(ReadResourceRequestSchema, async (request) => {
  const { uri } = request.params;

  let filePath = "";
  if (uri === "liderum://state") filePath = path.join(PROJECT_ROOT, ".ai", "state.md");
  else if (uri === "liderum://roles") filePath = path.join(PROJECT_ROOT, ".ai", "roles.md");
  else if (uri === "liderum://lib") filePath = path.join(PROJECT_ROOT, ".ai", "lib.md");
  else throw new Error(`Recurso não encontrado: ${uri}`);

  const safePath = validateSafePath(filePath);
  if (!fs.existsSync(safePath)) {
    throw new Error(`Arquivo não encontrado para o recurso: ${safePath}`);
  }

  const content = fs.readFileSync(safePath, "utf-8");
  return {
    contents: [
      {
        uri,
        mimeType: "text/markdown",
        text: content,
      },
    ],
  };
});

/**
 * List Tools
 */
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: "get_project_state",
        description: "Obtém o estado técnico atual conhecido (.ai/state.md) e regras dos agentes (.ai/roles.md).",
        inputSchema: {
          type: "object",
          properties: {
            include_roles: {
              type: "boolean",
              description: "Se verdadeiro, inclui também o conteúdo de roles.md",
            },
          },
        },
      },
      {
        name: "read_project_file",
        description: "Lê o conteúdo de um arquivo do projeto Liderum de forma segura.",
        inputSchema: {
          type: "object",
          properties: {
            relative_path: {
              type: "string",
              description: "Caminho relativo a partir da raiz do projeto (ex.: backend/pom.xml, .ai/lib.md)",
            },
          },
          required: ["relative_path"],
        },
      },
      {
        name: "search_project",
        description: "Pesquisa por ocorrências de texto dentro dos arquivos do projeto Liderum.",
        inputSchema: {
          type: "object",
          properties: {
            query: {
              type: "string",
              description: "Texto ou termo a ser pesquisado",
            },
            directory: {
              type: "string",
              description: "Diretório opcional para limitar a busca (ex.: backend, frontend)",
            },
          },
          required: ["query"],
        },
      },
      {
        name: "list_project_structure",
        description: "Lista arquivos e diretórios a partir de uma pasta do repositório.",
        inputSchema: {
          type: "object",
          properties: {
            relative_path: {
              type: "string",
              description: "Caminho relativo da pasta (padrão: raiz do projeto)",
            },
          },
        },
      },
      {
        name: "get_current_task",
        description: "Obtém as tarefas ativas em .ai/tasks/active/.",
        inputSchema: {
          type: "object",
          properties: {},
        },
      },
      {
        name: "get_git_status",
        description: "Obtém o status do Git no repositório Liderum.",
        inputSchema: {
          type: "object",
          properties: {},
        },
      },
      {
        name: "get_git_diff",
        description: "Obtém o diff de alterações não salvas/commitadas do Git.",
        inputSchema: {
          type: "object",
          properties: {
            staged: {
              type: "boolean",
              description: "Se verdadeiro, traz o diff de alterações em staged (--staged)",
            },
          },
        },
      },
    ],
  };
});

/**
 * Call Tool Handler
 */
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    if (name === "get_project_state") {
      const statePath = validateSafePath(path.join(".ai", "state.md"));
      const stateContent = fs.existsSync(statePath) ? fs.readFileSync(statePath, "utf-8") : "state.md não encontrado.";

      let rolesContent = "";
      if (args && args.include_roles) {
        const rolesPath = validateSafePath(path.join(".ai", "roles.md"));
        rolesContent = fs.existsSync(rolesPath) ? "\n\n--- ROLES.MD ---\n\n" + fs.readFileSync(rolesPath, "utf-8") : "";
      }

      return {
        content: [
          {
            type: "text",
            text: stateContent + rolesContent,
          },
        ],
      };
    }

    if (name === "read_project_file") {
      const relPath = String(args?.relative_path || "");
      const safePath = validateSafePath(relPath);

      if (!fs.existsSync(safePath)) {
        return {
          isError: true,
          content: [{ type: "text", text: `Arquivo não encontrado: ${relPath}` }],
        };
      }

      const stat = fs.statSync(safePath);
      if (stat.isDirectory()) {
        return {
          isError: true,
          content: [{ type: "text", text: `'${relPath}' é um diretório, não um arquivo. Use list_project_structure.` }],
        };
      }

      const content = fs.readFileSync(safePath, "utf-8");
      return {
        content: [{ type: "text", text: content }],
      };
    }

    if (name === "search_project") {
      const query = String(args?.query || "");
      const subDir = args?.directory ? String(args.directory) : "";
      const searchTarget = validateSafePath(subDir);

      const results: string[] = [];
      function searchRecursive(dir: string) {
        const entries = fs.readdirSync(dir, { withFileTypes: true });
        for (const entry of entries) {
          if (entry.name === "node_modules" || entry.name === ".git" || entry.name === "target" || entry.name === "dist") {
            continue;
          }
          const fullPath = path.join(dir, entry.name);
          if (entry.isDirectory()) {
            searchRecursive(fullPath);
          } else if (entry.isFile()) {
            try {
              const content = fs.readFileSync(fullPath, "utf-8");
              if (content.includes(query)) {
                const rel = path.relative(PROJECT_ROOT, fullPath);
                results.push(rel);
              }
            } catch {
              // Ignore binary files or unreadable files
            }
          }
        }
      }

      searchRecursive(searchTarget);

      return {
        content: [
          {
            type: "text",
            text: results.length > 0
              ? `Ocorrências encontradas para '${query}':\n` + results.map((r) => `- ${r}`).join("\n")
              : `Nenhuma ocorrência encontrada para '${query}'.`,
          },
        ],
      };
    }

    if (name === "list_project_structure") {
      const relPath = String(args?.relative_path || "");
      const targetDir = validateSafePath(relPath);

      if (!fs.existsSync(targetDir) || !fs.statSync(targetDir).isDirectory()) {
        return {
          isError: true,
          content: [{ type: "text", text: `Diretório não encontrado: ${relPath}` }],
        };
      }

      const entries = fs.readdirSync(targetDir, { withFileTypes: true });
      const items = entries.map((e) => {
        const type = e.isDirectory() ? "[DIR]" : "[FILE]";
        return `${type} ${e.name}`;
      });

      return {
        content: [
          {
            type: "text",
            text: `Conteúdo de '${relPath || "."}':\n` + items.join("\n"),
          },
        ],
      };
    }

    if (name === "get_current_task") {
      const activeDir = validateSafePath(path.join(".ai", "tasks", "active"));
      if (!fs.existsSync(activeDir)) {
        return {
          content: [{ type: "text", text: "Nenhuma tarefa ativa encontrada (diretório active/ não existe)." }],
        };
      }

      const files = fs.readdirSync(activeDir);
      if (files.length === 0) {
        return {
          content: [{ type: "text", text: "Nenhuma tarefa ativa em .ai/tasks/active/." }],
        };
      }

      const taskContents = files.map((f) => {
        const content = fs.readFileSync(path.join(activeDir, f), "utf-8");
        return `=== Arquivo: ${f} ===\n${content}`;
      });

      return {
        content: [{ type: "text", text: taskContents.join("\n\n") }],
      };
    }

    if (name === "get_git_status") {
      const { stdout } = await execAsync("git status --porcelain", { cwd: PROJECT_ROOT });
      return {
        content: [
          {
            type: "text",
            text: stdout ? `Git Status:\n${stdout}` : "Repositório limpo. Nenhuma alteração pendente.",
          },
        ],
      };
    }

    if (name === "get_git_diff") {
      const stagedFlag = args?.staged ? "--staged" : "";
      const { stdout } = await execAsync(`git diff ${stagedFlag}`, { cwd: PROJECT_ROOT });
      return {
        content: [
          {
            type: "text",
            text: stdout ? `Git Diff (${stagedFlag || "unstaged"}):\n${stdout}` : "Nenhum diff encontrado.",
          },
        ],
      };
    }

    throw new Error(`Ferramenta não reconhecida: ${name}`);
  } catch (error: any) {
    return {
      isError: true,
      content: [
        {
          type: "text",
          text: `Erro ao executar a ferramenta '${name}': ${error.message}`,
        },
      ],
    };
  }
});

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("Liderum MCP Server executando via stdio.");
}

main().catch((error) => {
  console.error("Erro fatal no servidor MCP:", error);
  process.exit(1);
});
