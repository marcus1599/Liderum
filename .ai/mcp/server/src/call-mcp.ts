import * as path from "path";
import * as fs from "fs";

const PROJECT_ROOT = path.resolve(process.env.LIDERUM_PROJECT_ROOT || path.join(process.cwd(), "..", "..", ".."));

function getProjectState() {
  const statePath = path.join(PROJECT_ROOT, ".ai", "state.md");
  const libPath = path.join(PROJECT_ROOT, ".ai", "lib.md");
  return {
    state: fs.readFileSync(statePath, "utf-8"),
    lib: fs.readFileSync(libPath, "utf-8")
  };
}

console.log(JSON.stringify(getProjectState()));
