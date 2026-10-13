import { test, expect } from '@playwright/test';
import { logPass } from '../utils/reporterHelpers';
import { BASE_URL } from '../../playwright.config';

async function navigateAndFillSignUp(page, scenario, uniqueEmail) {
  await page.goto(BASE_URL + 'sign-up');
  await page.getByRole('textbox', { name: 'Enter your name...' }).fill(scenario.name);
  await page.getByRole('textbox', { name: 'Enter your email...' }).fill(uniqueEmail); 
  await page.getByRole('textbox', { name: 'Enter your password...' }).fill(scenario.password);
  await page.getByRole('textbox', { name: 'Confirm Your Password' }).fill(scenario.password);
}

async function submitSignUp(page, scenario, uniqueEmail, shouldSucceed) {
  if (shouldSucceed) {
    const signUpResponse = page.waitForResponse(response => 
      response.url().includes('/api/users/signup') && response.ok()
    );
    await page.getByRole('button', { name: 'Sign Up' }).click();
    const response = await signUpResponse;
    expect(response.ok()).toBe(shouldSucceed);

    await page.getByRole('link', { name: 'Open account' }).click();
    await expect(page.getByRole('textbox', { name: 'name', exact: true })).toHaveValue(scenario.name);
    await expect(page.getByRole('textbox', { name: 'email' })).toHaveValue(uniqueEmail);
    logPass("Sign Up Successful! Role: " + scenario.role);
    await page.getByRole('button', { name: 'Log out' }).click();
    return true;
  }
  else if (scenario.role === "Duplicate Email User") {
    await page.getByRole('button', { name: 'Sign Up' }).click();
    await expect(page.getByRole('alert')).toContainText('A user with that email already exists.');
    logPass("Sign Up Failed! Role: " + scenario.role);
    return true; //true because first sign up worked so login will too
  }
  else {
    await expect(page.getByRole('button', { name: 'Sign Up' })).toBeDisabled();
    logPass("Sign Up Failed! Role: " + scenario.role);
    return false;
  }
}

export async function runSignUpTest(page, testInfo, scenario) {
  //gen random email, avoid dup error
  const uniqueEmail = Date.now() + scenario.email; 
  const success = await test.step('Sign Up', async () => {
      await navigateAndFillSignUp(page, scenario, uniqueEmail);
      if (scenario.role === "Successful User") {
        return await submitSignUp(page, scenario, uniqueEmail, true);
      }
      else if (scenario.role === "Duplicate Email User") {
        await submitSignUp(page, scenario, uniqueEmail, true);
        await navigateAndFillSignUp(page, scenario, uniqueEmail);
        return await submitSignUp(page, scenario, uniqueEmail, false);
      }
      else {
        return await submitSignUp(page, scenario, uniqueEmail, false);
      }
  });

  return { success, uniqueEmail };
}
