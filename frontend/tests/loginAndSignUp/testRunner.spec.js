import { test } from '@playwright/test';
import { readFileSync } from 'fs';
import { runLogIn_FollowingSignUpTest, runLogIn_OnlyTest } from './loginFlow.js';

const loginAndSignUpScenarios = JSON.parse(
  readFileSync(
    new URL('../data/loginAndSignUpScenarios.json', import.meta.url)
    , 'utf-8'));

const loginOnlyScenarios = JSON.parse(
  readFileSync(
    new URL('../data/loginOnlyScenarios.json', import.meta.url)
    , 'utf-8'));

const debugging = false;
test.use({
  launchOptions: {
    headless: false,
    slowMo: debugging ? 2000 : 0, //1.5 second delay for tests in this file
  },
});


for (const scenario of loginAndSignUpScenarios) {
  test(`Successful Sign Up And Login — ${scenario.role}`, async ({ page }, testInfo) => {
    test.setTimeout(100_000);
    await runLogIn_FollowingSignUpTest(page, testInfo, { ...scenario, submit: false });
  });
}

for (const scenario of loginOnlyScenarios) {
  test(`Successful Login Only — ${scenario.role}`, async ({ page }, testInfo) => {
    test.setTimeout(100_000);
    await runLogIn_OnlyTest(page, testInfo, { ...scenario, submit: false });
  });
}
