import { test, expect } from '@playwright/test';
import { apple, loginAsCustomer, makeCart, milk, mockApi } from '../utils/mockApi.js';

test.describe('cart page', () => {
  test('logged out shows a login prompt', async ({ page }) => {
    await page.goto('/cart');

    await expect(page.getByText('Log in to see your cart and check out.')).toBeVisible();
    await expect(page.getByRole('main').getByRole('link', { name: 'log in' })).toHaveAttribute('href', '/login');
  });

  test('empty cart shows the empty message', async ({ page }) => {
    await loginAsCustomer(page);
    await mockApi(page, '**/api/cart', makeCart([]));
    await page.goto('/cart');

    await expect(page.getByText('your cart is empty')).toBeVisible();
  });

  test('delivery is free under 20 lb and $10 at 20 lb or more', async ({ page }) => {
    await loginAsCustomer(page);
    await mockApi(page, '**/api/cart', makeCart([apple]));
    await page.goto('/cart');
    await expect(page.getByText('Robot delivery (under 20 lb)')).toBeVisible();
    await expect(page.getByRole('complementary').getByText('free')).toBeVisible();

    // 3 milk is about 25.8 lb
    await page.unroute('**/api/cart');
    await mockApi(page, '**/api/cart', makeCart([{ ...milk, quantity: 3 }]));
    await page.reload();
    await expect(page.getByText('Robot delivery (20 lb and over)')).toBeVisible();
    await expect(page.getByRole('complementary').getByText('$10.00')).toBeVisible();
  });

  test('plus and remove buttons call the cart api', async ({ page }) => {
    await loginAsCustomer(page);
    await mockApi(page, '**/api/cart', makeCart([milk]));
    const calls = [];
    await page.route('**/api/cart/items/7', (route) => {
      calls.push({ method: route.request().method(), body: route.request().postDataJSON() });
      route.fulfill({ json: makeCart([milk]) });
    });
    await page.goto('/cart');

    await page.getByRole('button', { name: 'Increase milk quantity' }).click();
    await page.getByRole('button', { name: 'Remove milk from cart' }).click();

    await expect.poll(() => calls.length).toBe(2);
    expect(calls[0]).toEqual({ method: 'PUT', body: { quantity: 2 } });
    expect(calls[1].method).toBe('DELETE');
  });

  test('plus is disabled when the cart has all the stock', async ({ page }) => {
    await loginAsCustomer(page);
    await mockApi(page, '**/api/cart', makeCart([{ ...milk, quantity: 40 }]));
    await page.goto('/cart');

    await expect(page.getByRole('button', { name: 'Increase milk quantity' })).toBeDisabled();
  });

  test('stock error from the api is shown', async ({ page }) => {
    await loginAsCustomer(page);
    await mockApi(page, '**/api/cart', makeCart([milk]));
    await mockApi(page, '**/api/cart/items/7', { message: 'Only 40 Milk in stock.' }, 409);
    await page.goto('/cart');

    await page.getByRole('button', { name: 'Increase milk quantity' }).click();

    await expect(page.getByRole('alert')).toHaveText('Only 40 Milk in stock.');
  });
});
