// Shared helpers for scripts/dev.mjs and scripts/test.mjs.
// Plain Node (no dependencies) so it works the same on Windows, macOS, and Linux.
import { spawn, spawnSync } from 'node:child_process';
import { existsSync, readFileSync, statSync } from 'node:fs';
import net from 'node:net';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

export const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
export const BACKEND = path.join(ROOT, 'backend');
export const FRONTEND = path.join(ROOT, 'frontend');
export const IS_WINDOWS = process.platform === 'win32';

const DB_NAME = 'on_demand_delivery_db';
const CONTAINER = 'on-demand-mysql';

const colors = { backend: 36, frontend: 35, db: 33, dev: 32, test: 34, error: 31 };
export function log(tag, message) {
  const code = colors[tag] ?? 37;
  console.log(`\x1b[${code}m[${tag}]\x1b[0m ${message}`);
}

export function fail(message) {
  log('error', message);
  process.exit(1);
}

/** Loads KEY=value lines from the repo-root .env (gitignored) without overriding real env vars. */
export function loadDotEnv() {
  const file = path.join(ROOT, '.env');
  if (!existsSync(file)) return;
  for (const line of readFileSync(file, 'utf8').split(/\r?\n/)) {
    const match = line.match(/^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*?)\s*$/);
    if (!match || line.trimStart().startsWith('#')) continue;
    const [, key, raw] = match;
    if (process.env[key] === undefined) process.env[key] = raw.replace(/^(['"])(.*)\1$/, '$2');
  }
}

function run(command, args, options = {}) {
  return spawnSync(command, args, { cwd: ROOT, encoding: 'utf8', shell: IS_WINDOWS, ...options });
}

function isPortFree(port) {
  return new Promise((resolve) => {
    const server = net.createServer();
    server.once('error', () => resolve(false));
    server.once('listening', () => server.close(() => resolve(true)));
    server.listen(port, '0.0.0.0');
  });
}

/** Host port our MySQL container is already published on, or null. */
function runningContainerPort() {
  const result = run('docker', ['port', CONTAINER, '3306/tcp']);
  if (result.status !== 0) return null;
  const match = result.stdout.match(/:(\d+)\s*$/m);
  return match ? Number(match[1]) : null;
}

/**
 * Picks the host port for MySQL. A locally installed MySQL often already owns 3306,
 * which makes Spring Boot silently talk to the wrong database, so fall back to 3307+.
 */
async function chooseDbPort() {
  if (process.env.MYSQL_HOST_PORT) return Number(process.env.MYSQL_HOST_PORT);
  const existing = runningContainerPort();
  if (existing) return existing;
  for (const port of [3306, 3307, 3308, 3309]) {
    if (await isPortFree(port)) {
      if (port !== 3306) log('db', `port 3306 is taken by another MySQL on this machine; using ${port} for Docker`);
      return port;
    }
  }
  fail('ports 3306-3309 are all in use; set MYSQL_HOST_PORT in .env to a free port');
}

function mysqlRoot(sql, input) {
  return run('docker', ['exec', '-i', CONTAINER, 'mysql', '-h127.0.0.1', '-uroot', '-proot', ...(sql ? ['-e', sql] : [])], {
    input,
    shell: false,
  });
}

/** Starts the MySQL container and waits until it accepts TCP connections. Sets DB_PORT for Spring. */
export async function ensureDatabase() {
  if (run('docker', ['info'], { stdio: 'ignore' }).status !== 0) {
    fail('Docker is not running. Start Docker Desktop and try again.');
  }

  const port = await chooseDbPort();
  process.env.MYSQL_HOST_PORT = String(port);
  process.env.DB_PORT = String(port);

  log('db', `starting MySQL container on localhost:${port}`);
  const up = run('docker', ['compose', 'up', '-d', 'mysql'], { stdio: 'inherit' });
  if (up.status !== 0) fail('docker compose up failed');

  // The init-time temporary server has networking disabled, so a TCP query only
  // succeeds once MySQL is fully up (including the first-run schema/seed).
  const deadline = Date.now() + 120_000;
  while (mysqlRoot('SELECT 1').status !== 0) {
    if (Date.now() > deadline) fail('MySQL did not become ready within 2 minutes (docker compose logs mysql)');
    await new Promise((resolve) => setTimeout(resolve, 1500));
  }
  log('db', 'MySQL is ready');
}

/** Drops and recreates the app database from backend/db/schema.sql + seed.sql. */
export function reseedDatabase() {
  const schema = readFileSync(path.join(BACKEND, 'db', 'schema.sql'), 'utf8');
  const seed = readFileSync(path.join(BACKEND, 'db', 'seed.sql'), 'utf8');
  const sql = `DROP DATABASE IF EXISTS ${DB_NAME};\nCREATE DATABASE ${DB_NAME};\nUSE ${DB_NAME};\n${schema}\n${seed}\n`;

  log('db', 'resetting database from schema.sql + seed.sql');
  const result = mysqlRoot(null, sql);
  const errors = (result.stderr || '').split('\n').filter((l) => l && !l.includes('Using a password'));
  if (result.status !== 0) fail(`reseed failed:\n${errors.join('\n')}`);
  log('db', 'database reseeded');
}

/** Runs `npm install` in frontend/ when node_modules is missing or older than package-lock.json. */
export function ensureFrontendDeps() {
  const marker = path.join(FRONTEND, 'node_modules', '.package-lock.json');
  const lock = path.join(FRONTEND, 'package-lock.json');
  if (existsSync(marker) && statSync(marker).mtimeMs >= statSync(lock).mtimeMs) return;
  log('frontend', 'installing npm dependencies');
  if (run('npm', ['install'], { cwd: FRONTEND, stdio: 'inherit' }).status !== 0) fail('npm install failed');
}

export function mavenCommand() {
  // Absolute path: cmd.exe won't run a bare name from cwd when NoDefaultCurrentDirectoryInExePath is set.
  return IS_WINDOWS ? `"${path.join(BACKEND, 'mvnw.cmd')}"` : path.join(BACKEND, 'mvnw');
}

/** Spawns a long-running child whose output is line-prefixed with [tag]. */
export function startService(tag, command, args, cwd) {
  const child = spawn(command, args, { cwd, env: process.env, shell: IS_WINDOWS });
  const pipe = (stream) => {
    let buffer = '';
    stream.on('data', (chunk) => {
      buffer += chunk;
      const lines = buffer.split(/\r?\n/);
      buffer = lines.pop();
      for (const line of lines) log(tag, line);
    });
  };
  pipe(child.stdout);
  pipe(child.stderr);
  return child;
}

/** Kills a child and everything it spawned (mvnw -> java, npm -> vite). */
export function killTree(child) {
  if (!child || child.exitCode !== null) return;
  if (IS_WINDOWS) spawnSync('taskkill', ['/pid', String(child.pid), '/T', '/F'], { stdio: 'ignore' });
  else child.kill('SIGTERM');
}

function canConnect(port, host) {
  return new Promise((resolve) => {
    const socket = net.connect({ port, host });
    socket.setTimeout(500);
    socket.once('connect', () => { socket.destroy(); resolve(true); });
    socket.once('timeout', () => { socket.destroy(); resolve(false); });
    socket.once('error', () => resolve(false));
  });
}

/** Stops early with a clear message if something (often an old dev run) still holds the app ports. */
export async function ensurePortsFree(ports) {
  const busy = [];
  for (const port of ports) {
    if ((await canConnect(port, '127.0.0.1')) || (await canConnect(port, '::1'))) busy.push(port);
  }
  if (busy.length === 0) return;
  const hint = IS_WINDOWS
    ? `Find it with:  netstat -ano | findstr ":${busy[0]}"   then stop it with:  taskkill /PID <pid> /F`
    : `Find it with:  lsof -i :${busy[0]}   then stop it with:  kill <pid>`;
  fail(`port ${busy.join(' and ')} is already in use (is another dev server still running?)\n        ${hint}`);
}

export async function waitForUrl(url, timeoutMs) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    try {
      const response = await fetch(url);
      if (response.status < 500) return true;
    } catch {
      // not up yet
    }
    await new Promise((resolve) => setTimeout(resolve, 1500));
  }
  return false;
}
