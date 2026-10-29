// Runs every test suite against a freshly seeded database:
//   npm test                  backend (JUnit) then frontend (Playwright)
//   npm test -- --backend     backend only
//   npm test -- --e2e         Playwright only (starts backend/frontend if they aren't running)
import { spawnSync } from 'node:child_process';
import {
  BACKEND,
  FRONTEND,
  IS_WINDOWS,
  ensureDatabase,
  ensureFrontendDeps,
  loadDotEnv,
  log,
  mavenCommand,
  reseedDatabase,
} from './lib.mjs';

const args = new Set(process.argv.slice(2));
const runBackend = !args.has('--e2e');
const runE2e = !args.has('--backend');

loadDotEnv();
await ensureDatabase();
reseedDatabase();

const results = [];
function suite(name, command, commandArgs, cwd) {
  log('test', `running ${name}`);
  const { status } = spawnSync(command, commandArgs, { cwd, stdio: 'inherit', env: process.env, shell: IS_WINDOWS });
  results.push([name, status === 0]);
}

if (runBackend) suite('backend (mvnw test)', mavenCommand(), ['test'], BACKEND);
if (runE2e) {
  ensureFrontendDeps();
  spawnSync('npx', ['playwright', 'install', 'chromium'], { cwd: FRONTEND, stdio: 'inherit', shell: IS_WINDOWS });
  // Playwright tests sign up users, so start them from a clean seed too.
  if (runBackend) reseedDatabase();
  suite('frontend (playwright)', 'npx', ['playwright', 'test'], FRONTEND);
}

for (const [name, passed] of results) log('test', `${passed ? '\x1b[32mPASS' : '\x1b[31mFAIL'}\x1b[0m ${name}`);
process.exit(results.every(([, passed]) => passed) ? 0 : 1);
