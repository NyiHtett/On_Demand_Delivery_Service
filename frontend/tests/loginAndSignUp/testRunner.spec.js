import { test } from '@playwright/test';
import { readFileSync } from 'fs';
import { runLogIn_FollowingSignUpTest } from './loginFlow.js';

const scenarios = JSON.parse(
  readFileSync(
    new URL('../data/loginAndSignUpScenarios.json', import.meta.url)
    , 'utf-8'));

test.describe.configure({ mode: 'serial' });

const debugging = false;
if (debugging) {
  test.use({
    launchOptions: {
      slowMo: 1500, // 1.5-second delay for tests in this file
    },
  });
}

for (const scenario of scenarios) {
  test(`Successful Sign Up And Login — ${scenario.role}`, async ({ page }, testInfo) => {
    test.setTimeout(100_000);
    await runLogIn_FollowingSignUpTest(page, testInfo, { ...scenario, submit: false });
  });
}
