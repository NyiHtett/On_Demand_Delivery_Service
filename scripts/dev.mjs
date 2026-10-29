// One command to run the whole app locally:
//   npm run dev              MySQL (Docker) + fresh schema/seed + Spring Boot + Vite
//   npm run dev -- --keep-db skip the reseed and keep whatever data is in MySQL
//   npm run db:reset         only start MySQL and reseed it, then exit
// Ctrl+C stops the backend and frontend. MySQL keeps running (docker compose down to stop it).
import {
  BACKEND,
  FRONTEND,
  ensureDatabase,
  ensurePortsFree,
  ensureFrontendDeps,
  killTree,
  loadDotEnv,
  log,
  mavenCommand,
  reseedDatabase,
  startService,
  waitForUrl,
} from './lib.mjs';

const args = new Set(process.argv.slice(2));
const keepDb = args.has('--keep-db');
const dbOnly = args.has('--db-only');

loadDotEnv();
if (!dbOnly) await ensurePortsFree([8080, 5173]);
await ensureDatabase();
if (!keepDb) reseedDatabase();
if (dbOnly) process.exit(0);

ensureFrontendDeps();

const children = [
  startService('backend', mavenCommand(), ['-q', 'spring-boot:run'], BACKEND),
  // strictPort: Stripe redirects back to :5173, so never silently move to another port.
  startService('frontend', 'npm', ['run', 'dev', '--', '--strictPort'], FRONTEND),
];

let stopping = false;
function stop(code = 0) {
  if (stopping) return;
  stopping = true;
  log('dev', 'stopping backend and frontend...');
  children.forEach(killTree);
  process.exit(code);
}

process.on('SIGINT', () => stop(0));
process.on('SIGTERM', () => stop(0));
children.forEach((child, index) =>
  child.on('exit', (code) => {
    if (!stopping) {
      log('error', `${index === 0 ? 'backend' : 'frontend'} exited (code ${code}); shutting down`);
      stop(code || 1);
    }
  }),
);

if (await waitForUrl('http://localhost:8080/api/products', 180_000)) {
  log('dev', 'backend ready on http://localhost:8080');
  log('dev', '\x1b[1mopen http://localhost:5173\x1b[0m  (Ctrl+C to stop)');
} else {
  log('error', 'backend did not respond on :8080 within 3 minutes; check the [backend] output above');
}
