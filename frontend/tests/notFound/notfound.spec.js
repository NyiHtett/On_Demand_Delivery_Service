import { test, expect } from '@playwright/test';

test.describe('not found page', () => {
  test('unknown url shows the 404 page', async ({ page }) => {
    await page.goto('/nope');

    await expect(page.getByRole('link', { name: 'return home' })).toBeVisible();
  });

  test('return home goes back to the home page', async ({ page }) => {
    await page.goto('/nope');

    await page.getByRole('link', { name: 'return home' }).click();

    await expect(page.getByRole('heading', { name: 'fresh groceries' })).toBeVisible();
  });
});
