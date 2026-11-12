import { test, expect } from '@playwright/test';
import { loginAsCustomer, mockApi } from '../utils/mockApi.js';

function makeOrder(status) {
  return {
    orderId: 7,
    orderStatus: status,
    deliveryAddress: '777 The Alameda, San Jose, CA 95126',
    subtotal: 2,
    deliveryFee: 0,
    totalWeight: 0.8,
    items: [{ orderItemId: 1, productName: 'Apple', unitPrice: 1, quantity: 2 }],
    createdAt: Date.now(),
    estimatedDispatch: Date.now() + 60_000,
    estimatedArrival: Date.now() + 120_000,
  };
}

// fake the confirm call and remember what the page sent
async function mockConfirm(page, order) {
  const sent = [];
  await page.route('**/api/checkout/confirm', (route) => {
    sent.push(route.request().postDataJSON());
    route.fulfill({ json: order });
  });
  await mockApi(page, '**/api/customer/orders/7', order);
  return sent;
}

test.describe('order confirmation page', () => {
  test('order_id shows the order', async ({ page }) => {
    await loginAsCustomer(page);
    const sent = await mockConfirm(page, makeOrder('Ordered'));
    await page.goto('/order-confirmation?order_id=7');

    await expect(page.getByText('order #7')).toBeVisible();
    await expect(page.getByText('2 × Apple')).toBeVisible();
    await expect(page.getByText('Total paid')).toBeVisible();
    expect(sent[0]).toEqual({ orderId: 7 });
  });

  test('session_id is sent to confirm', async ({ page }) => {
    await loginAsCustomer(page);
    const sent = await mockConfirm(page, makeOrder('Ordered'));
    await page.goto('/order-confirmation?session_id=cs_test_x');

    await expect(page.getByText('order #7')).toBeVisible();
    expect(sent[0]).toEqual({ sessionId: 'cs_test_x' });
  });

  test('shows robot status for waiting and shipping orders', async ({ page }) => {
    await loginAsCustomer(page);
    await mockConfirm(page, makeOrder('Ordered'));
    await page.goto('/order-confirmation?order_id=7');
    await expect(page.getByText('waiting for robot')).toBeVisible();
    await expect(page.getByText(/Robot leaves the store in/)).toBeVisible();

    await page.unroute('**/api/checkout/confirm');
    await page.unroute('**/api/customer/orders/7');
    await mockConfirm(page, makeOrder('Shipping'));
    await page.reload();
    await expect(page.getByText('on the way')).toBeVisible();
    await expect(page.getByText(/Robot arrives around/)).toBeVisible();
  });

  test('payment not done shows an error', async ({ page }) => {
    await loginAsCustomer(page);
    await mockApi(page, '**/api/checkout/confirm', { message: "Payment hasn't gone through yet." }, 402);
    await page.goto('/order-confirmation?session_id=cs_test_x');

    await expect(page.getByText('something went wrong')).toBeVisible();
    await expect(page.getByRole('link', { name: 'view my orders' })).toBeVisible();
  });

  test('no order in the url says so', async ({ page }) => {
    await loginAsCustomer(page);
    await page.goto('/order-confirmation');

    await expect(page.getByText('No order to show.')).toBeVisible();
  });
});
