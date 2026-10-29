import { defineConfig } from '@playwright/test';

// Generate timestamped report folder: test_reports/YEAR-MONTH-DAY_HOURMINUTE
const now = new Date();
const timestamp = now.toISOString().replace(/T/, '_').replace(/[:.]/g, '').slice(0, 15);
const reportDir = `./test_reports/${timestamp}`;
const CI = false;
export const BASE_URL = "http://localhost:5173/";

export default defineConfig({
  testDir: './tests',

  webServer: [
    {
      command: '..\\backend\\mvnw.cmd -f ..\\backend\\pom.xml spring-boot:run',
      url: 'http://127.0.0.1:8080/api/products',
      reuseExistingServer: !CI,
      timeout: 120_000,
    },
    {
      command: 'npm run dev -- --host 127.0.0.1',
      url: 'http://127.0.0.1:5173',
      reuseExistingServer: !CI,
    },
  ],

  // Run one-by-one
  fullyParallel: false,
  workers: 1,

  // Give E2E room
  timeout: 300_000,

  // CI hygiene & flake control
  forbidOnly: !!CI,
  retries: 1,
  reportSlowTests: { max: 5, threshold: 60_000 },

  // Do not launch a new browser window for the report after every test run.
  reporter: [['list'], ['html', { outputFolder: reportDir, open: 'never' }]],

  expect: { timeout: 15_000 },

  use: {
    baseURL: BASE_URL,
    // Set PW_CHANNEL=chrome (or msedge) to use the installed browser if Playwright's bundled Chromium can't launch.
    channel: process.env.PW_CHANNEL || undefined,
    viewport: null,
    launchOptions: {
      args: ['--start-maximized'],
      headless: !!CI,
    },

    // UI stability
    actionTimeout: 30_000,
    navigationTimeout: 60_000,

    // Artifacts — only keep on failure to bound disk + memory for long runs.
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [{ name: 'chromium' }],
});
