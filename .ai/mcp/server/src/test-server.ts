import * as path from "path";
import * as fs from "fs";
import { fileURLToPath } from "url";

const MODULE_DIR = path.dirname(fileURLToPath(import.meta.url));
const PROJECT_ROOT = path.resolve(process.env.LIDERUM_PROJECT_ROOT || path.join(MODULE_DIR, "..", "..", "..", ".."));

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

function runTests() {
  console.log("=== INICIANDO TESTES DO SERVIDOR MCP LIDERUM ===");
  let passed = 0;
  let total = 0;

  function assert(condition: boolean, testName: string) {
    total++;
    if (condition) {
      console.log(`[PASS] ${testName}`);
      passed++;
    } else {
      console.error(`[FAIL] ${testName}`);
    }
  }

  // Teste 1: Validação de arquivo válido
  try {
    const safePath = validateSafePath(".ai/roles.md");
    assert(fs.existsSync(safePath), "Leitura de .ai/roles.md dentro do repositório");
  } catch (e: any) {
    assert(false, `Falha inesperada ao ler .ai/roles.md: ${e.message}`);
  }

  // Teste 2: Rejeição de Path Traversal fora do repositório
  try {
    validateSafePath("../../Windows/System32");
    assert(false, "Bloqueio de Path Traversal fora da raiz do projeto");
  } catch (e: any) {
    assert(e.message.includes("Acesso negado"), "Bloqueio de Path Traversal fora da raiz do projeto");
  }

  // Teste 3: Proteção de arquivo de segredo .env
  try {
    validateSafePath(".env");
    assert(false, "Bloqueio de leitura de arquivo .env");
  } catch (e: any) {
    assert(e.message.includes("contém segredos protegidos"), "Bloqueio de leitura de arquivo .env");
  }

  // Teste 4: Existência dos arquivos de controle do ecossistema de agentes
  assert(fs.existsSync(path.join(PROJECT_ROOT, ".ai", "roles.md")), "Existência de .ai/roles.md");
  assert(fs.existsSync(path.join(PROJECT_ROOT, ".ai", "state.md")), "Existência de .ai/state.md");
  assert(fs.existsSync(path.join(PROJECT_ROOT, ".ai", "lib.md")), "Existência de .ai/lib.md");
  assert(fs.existsSync(path.join(PROJECT_ROOT, ".ai", "agents", "planner.md")), "Existência de agent planner.md");
  assert(fs.existsSync(path.join(PROJECT_ROOT, ".ai", "agents", "arquiteto.md")), "Existência de agent arquiteto.md");
  assert(fs.existsSync(path.join(PROJECT_ROOT, ".ai", "agents", "backend-dev.md")), "Existência de agent backend-dev.md");
  assert(fs.existsSync(path.join(PROJECT_ROOT, ".ai", "agents", "frontend-dev.md")), "Existência de agent frontend-dev.md");
  assert(fs.existsSync(path.join(PROJECT_ROOT, ".ai", "agents", "qa.md")), "Existência de agent qa.md");
  assert(fs.existsSync(path.join(PROJECT_ROOT, ".ai", "agents", "security.md")), "Existência de agent security.md");
  assert(fs.existsSync(path.join(PROJECT_ROOT, ".ai", "agents", "sre-devops.md")), "Existência de agent sre-devops.md");
  assert(fs.existsSync(path.join(PROJECT_ROOT, ".ai", "agents", "auditor.md")), "Existência de agent auditor.md");

  console.log(`\nResultado dos testes: ${passed}/${total} testes aprovados.`);
  if (passed !== total) {
    process.exit(1);
  }
}

runTests();
