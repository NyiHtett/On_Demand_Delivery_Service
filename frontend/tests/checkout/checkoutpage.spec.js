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

    await expect(page.getByRole('textbox', { name: 'street address' })).toHaveValue('777 The Alameda');
    await expect(page.getByRole('textbox', { name: 'city', exact: true })).toHaveValue('San Jose');
    await expect(page.getByRole('textbox', { name: 'state', exact: true })).toHaveValue('CA');
    await expect(page.getByRole('textbox', { name: 'ZIP code', exact: true })).toHaveValue('95126');
    await expect(page.getByRole('button', { name: 'pay $3.00' })).toBeEnabled();

    // no address means no paying
    await clearDisableFillEnable(page, page.getByRole('textbox', { name: 'street address' }), '777 The Alameda');
    await clearDisableFillEnable(page, page.getByRole('textbox', { name: 'city' }), 'San Jose');
    await clearDisableFillEnable(page, page.getByRole('textbox', { name: 'state' }), 'CA');
    await clearDisableFillEnable(page, page.getByRole('textbox', { name: 'ZIP code' }), '95126');
  });
  async function clearDisableFillEnable(page, locator, fillText) {
    await locator.fill('');
    await expect(page.getByRole('button', { name: 'pay $3.00' })).toBeDisabled();
    await locator.fill(fillText);
    await expect(page.getByRole('button', { name: 'pay $3.00' })).toBeEnabled();
  }

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
    expect(sentBody).toEqual({ 
      "deliveryAddress" : {
        "city" : "San Jose",
        "state": "CA",
        "street": "777 The Alameda",
        "zip": "95126",
      }
    });
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
