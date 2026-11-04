import { test, expect } from '@playwright/test';

const frontendUrl = 'http://localhost:5173';
const backendUrl = 'http://localhost:8080';

const account = {
  user_id: 1,
  user_name: 'Test Customer',
  email: 'testcustomer@example.com',
  address: '123 Test Street',
  phone: '555-123-4567',
  user_type: 'Customer',
};

function getToken(responseBody) {
  return (
    responseBody.apiToken ||
    responseBody.api_token ||
    responseBody.token ||
    responseBody.sessionId ||
    responseBody.session_id
  );
}

test.describe('Account page frontend', () => {
  test.beforeEach(async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('api_token', 'playwright-test-token');
    });

    await page.route('**/api/users/me', async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(account),
        });
        return;
      }

      if (route.request().method() === 'PUT') {
        const updatedAccount = route.request().postDataJSON();

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            ...account,
            user_name: updatedAccount.name,
            email: updatedAccount.email,
            address: updatedAccount.address,
            phone: updatedAccount.phone,
          }),
        });
      }
    });

    await page.goto(`${frontendUrl}/account`);
  });

  test('loads account information', async ({ page }) => {
    await expect(
      page.getByRole('heading', { name: 'my account' })
    ).toBeVisible();

    await expect(page.locator('#account-name')).toHaveValue(
      'Test Customer'
    );

    await expect(page.locator('#account-email')).toHaveValue(
      'testcustomer@example.com'
    );

    await expect(page.locator('#account-address')).toHaveValue(
      '123 Test Street'
    );

    await expect(page.locator('#account-phone')).toHaveValue(
      '555-123-4567'
    );
  });

  test('updates account information', async ({ page }) => {
    await page.locator('#account-name').fill('Updated Customer');
    await page.locator('#account-address').fill('456 Updated Street');

    await page
      .getByRole('button', { name: 'update information' })
      .click();

    await expect(
      page.getByText('Account information updated.')
    ).toBeVisible();
  });

  test('shows customer payment methods', async ({ page }) => {
    await expect(
      page.getByRole('heading', { name: 'payment methods' })
    ).toBeVisible();

    await expect(
      page.getByText('Visa ending in 1121', { exact: true })
    ).toBeVisible();
  });
});

test.describe('Account backend API', () => {
  test('gets the current account with a valid token', async ({
    request,
  }) => {
    const email = `playwright-${Date.now()}@example.com`;

    const signupResponse = await request.post(
      `${backendUrl}/api/users/signup`,
      {
        data: {
          name: 'Playwright Test User',
          email,
          password: 'TestP@ssw0rd',
        },
      }
    );

    expect(signupResponse.ok()).toBeTruthy();

    const signupBody = await signupResponse.json();
    const token = getToken(signupBody);

    expect(token).toBeTruthy();

    const accountResponse = await request.get(
      `${backendUrl}/api/users/me`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    expect(accountResponse.status()).toBe(200);

    const accountBody = await accountResponse.json();

    expect(accountBody.email).toBe(email);
  });

  test('rejects unauthenticated account requests', async ({
    request,
  }) => {
    const response = await request.get(`${backendUrl}/api/users/me`);

    expect(response.status()).toBe(401);
  });
});