import { test, expect } from '@playwright/test';

test.describe('home page', () => {
  test('shows the heading and items', async ({ page }) => {
    await page.goto('/');

    await expect(page.getByRole('heading', { name: 'fresh groceries' })).toBeVisible();
    await expect(page.getByRole('heading', { name: /available items at/ })).toBeVisible();
  });

  test('store button goes to the shop', async ({ page }) => {
    await page.goto('/');

    await page.getByRole('link', { name: 'click to go to store' }).click();

    await expect(page).toHaveURL(/\/shop$/);
  });
});
