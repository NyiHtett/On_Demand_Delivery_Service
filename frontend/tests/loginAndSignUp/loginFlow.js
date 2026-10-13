import { test, expect } from '@playwright/test';
import { runSignUpTest } from './signupFlow.js';
import { logPass } from '../utils/reporterHelpers.js';
import { BASE_URL } from '../../playwright.config.js';

export async function runLogIn_FollowingSignUpTest(page, testInfo, scenario) {
  await test.step('Login Following Sign Up', async () => {
    const {success, uniqueEmail} = await runSignUpTest(page, testInfo, scenario);
    await page.goto(BASE_URL + 'login');

    await page.getByRole('textbox', { name: 'Enter your email...' }).fill(uniqueEmail);
    await page.getByRole('textbox', { name: 'Enter your password...' }).fill(scenario.password);

    if (success) {
      const loginResponse = page.waitForResponse(response =>
          response.url().includes('/api/users/login') && response.ok()
      );
      await page.getByRole('button', { name: 'Login' }).click();
      await loginResponse;

      await page.getByRole('link', { name: 'Open account' }).click();
      await expect(page.getByRole('textbox', { name: 'name', exact: true })).toHaveValue(scenario.name);
      await expect(page.getByRole('textbox', { name: 'email' })).toHaveValue(uniqueEmail);
      logPass("Login Successful! Role: " + scenario.role);
    }
    else if (scenario.role === "Invalid Email User") {
      await expect(page.getByRole('button', { name: 'Login' })).toBeDisabled();
      logPass("Login Failed! Role: " + scenario.role);
    }
    else {
      await page.getByRole('button', { name: 'Login' }).click();
      await expect(page.getByRole('alert')).toContainText('Invalid email or password.');
      logPass("Login Failed! Role: " + scenario.role);
    }
  });
}
