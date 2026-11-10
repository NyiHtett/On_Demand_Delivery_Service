import { test } from '@playwright/test';
import { readFileSync } from 'fs';
import { runCreateReadUpdateProductTest } from './createReadUpdateDeleteProducts.js';

const crudScenarios = JSON.parse(
  readFileSync(
    new URL('../data/createReadUpdateDeleteProductsScenarios.json', import.meta.url)
    , 'utf-8'));

const debugging = false;
test.use({
  launchOptions: {
    headless: false,
    slowMo: debugging ? 2000 : 0, //1.5 second delay for tests in this file
  },
});

for (const scenario of crudScenarios) {
  test(`Employee CRUD Product Test — ${scenario.role}`, async ({ page }, testInfo) => {
    test.setTimeout(100_000);
    await runCreateReadUpdateProductTest(page, testInfo, { ...scenario, submit: false });
  });
}

