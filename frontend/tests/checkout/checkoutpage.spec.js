import { test, expect } from '@playwright/test';
import { apple, loginAsCustomer, makeCart, mockApi } from '../utils/mockApi.js';

const cart = makeCart([apple]);

test.describe('checkout page', () => {
  test('logged out shows a login prompt', async ({ page }) => {
    await page.goto('/checkout');

    await expect(page.getByText('Log in to check out.')).toBeVisible();
  });

  test('empty cart shows the empty message', async ({ page }) => {
    await loginAsCustomer(page);
    await mockApi(page, '**/api/cart', makeCart([]));
    await page.goto('/checkout');

    await expect(page.getByText('your cart is empty')).toBeVisible();
  });

  test('address is filled in and pay shows the total', async ({ page }) => {
    await loginAsCustomer(page);
    await mockApi(page, '**/api/cart', cart);
    await page.goto('/checkout');

    await expect(page.locator('#checkout-address')).toHaveValue('777 The Alameda, San Jose, CA 95126');
    await expect(page.getByRole('button', { name: 'pay $3.00' })).toBeEnabled();

    // no address means no paying
    await page.locator('#checkout-address').fill('');
    await expect(page.getByRole('button', { name: 'pay $3.00' })).toBeDisabled();
  });

  test('pay sends the address and goes to the returned url', async ({ page }) => {
    await loginAsCustomer(page);
    await mockApi(page, '**/api/cart', cart);
    let sentBody;
    await page.route('**/api/checkout/session', (route) => {
      sentBody = route.request().postDataJSON();
      route.fulfill({ json: { orderId: 9, url: '/order-confirmation?order_id=9' } });
    });
    await mockApi(page, '**/api/checkout/confirm', { message: 'not needed here' }, 404);
    await page.goto('/checkout');

    await page.getByRole('button', { name: 'pay $3.00' }).click();

    await expect(page).toHaveURL(/order-confirmation\?order_id=9/);
    expect(sentBody).toEqual({ deliveryAddress: '777 The Alameda, San Jose, CA 95126' });
  });

  test('out of stock error is shown and the page stays', async ({ page }) => {
    await loginAsCustomer(page);
    await mockApi(page, '**/api/cart', cart);
    await mockApi(page, '**/api/checkout/session', { message: 'Some items changed while you were shopping: Apple is sold out.' }, 409);
    await page.goto('/checkout');

    await page.getByRole('button', { name: 'pay $3.00' }).click();

    await expect(page.getByRole('alert')).toContainText('Apple is sold out');
    await expect(page).toHaveURL(/\/checkout$/);
  });

  test('coming back from a canceled payment releases the order', async ({ page }) => {
    await loginAsCustomer(page);
    await mockApi(page, '**/api/cart', cart);
    let cancelBody;
    await page.route('**/api/checkout/cancel', (route) => {
      cancelBody = route.request().postDataJSON();
      route.fulfill({ status: 204 });
    });
    await page.goto('/checkout?canceled=1&session_id=cs_test_123');

    await expect(page.getByText('Payment was canceled')).toBeVisible();
    expect(cancelBody).toEqual({ sessionId: 'cs_test_123' });
  });
});
