// helpers for page tests that fake the backend instead of using the real one

// pretend a customer is logged in
export async function loginAsCustomer(page) {
  await page.addInitScript(() => localStorage.setItem('api_token', 'test-token'));
  await mockApi(page, '**/api/users/me', {
    user_id: 4,
    user_name: 'Maria Lopez',
    email: 'maria.lopez@example.com',
    address: '777 The Alameda, San Jose, CA 95126',
    phone: '14085550101',
    user_type: 'Customer',
  });
}

// answer every request to url with the given json and status
export async function mockApi(page, url, body, status = 200) {
  await page.route(url, (route) => route.fulfill({ status, json: body }));
}

// build a cart the way the backend sends it
export function makeCart(items) {
  const subtotal = items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
  const totalWeight = items.reduce((sum, item) => sum + item.unitWeight * item.quantity, 0);
  const deliveryFee = totalWeight >= 20 ? 10 : 0;
  return {
    cartId: 1,
    items: items.map((item, index) => ({ cartItemID: index + 1, imageUrl: '', ...item })),
    subtotal,
    totalWeight,
    deliveryFee,
    total: subtotal + deliveryFee,
  };
}

export const milk = { productID: 7, productName: 'Milk', unitPrice: 6.13, unitWeight: 8.601, quantity: 1, stock: 40 };
export const apple = { productID: 1, productName: 'Apple', unitPrice: 1, unitWeight: 0.4, quantity: 3, stock: 100 };
