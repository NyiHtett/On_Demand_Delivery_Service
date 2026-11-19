import { test, expect } from '@playwright/test';
import { runLogIn_OnlyTest } from '../loginAndSignUp/loginFlow';

const frontendUrl = 'http://localhost:5173';
const backendUrl = 'http://localhost:8080';

const account = {
  user_id: 4,
  user_name: 'Maria Lopez',
  email: 'maria.lopez@example.com',
  address: '777 The Alameda, San Jose, CA 95126',
  phone: '14085550101',
  user_type: 'Customer',
};
const updatedAccount = {
  user_id: 4,
  user_name: 'Updated Maria Lopez',
  email: 'updated.maria.lopez@example.com',
  address: '5095 Almaden Expy, San Jose, CA 95118',
  phone: '14086003072',
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
  test("account login, read, update", async ({page}, testInfo) => {
    await runLogIn_OnlyTest(page, testInfo, 
      {
      "role": "Existing Customer",
      "name": account.user_name,
      "email": account.email,
      "password": "P@ssw0rd!",
      "userType": "Customer",
      "submit": false
    });
    await readAccountInfo(account, page);
    await updateAccountInfo(updatedAccount, page);
    await page.reload();
    await readAccountInfo(updatedAccount, page);
    await updateAccountInfo(account, page);
    await readAccountInfo(account, page);
  });
});

test('shows customer payment methods in account', async ({ page }, testInfo) => {
  await runLogIn_OnlyTest(page, testInfo, 
  {
    "role": "Existing Customer",
    "name": account.user_name,
    "email": account.email,
    "password": "P@ssw0rd!",
    "userType": "Customer",
    "submit": false
  });
  await expect(
    page.getByRole('heading', { name: 'payment methods' })
  ).toBeVisible();

  await expect(page.getByText('visa ending in 4242Maria Lopez · Expires 2027-11delete')).toBeVisible();
});

async function readAccountInfo(account, page) {
  await expect(
    page.getByRole('heading', { name: 'my account' })
  ).toBeVisible();

  await expect(page.locator('#account-name')).toHaveValue(
    account.user_name
  );

  await expect(page.locator('#account-email')).toHaveValue(
    account.email
  );

  await expect(page.locator('#account-address')).toHaveValue(
    account.address
  );

  await expect(page.locator('#account-phone')).toHaveValue(
    account.phone
  );
}

async function updateAccountInfo(account, page) {
  await page.locator('#account-name').fill(account.user_name);
  await page.locator('#account-email').fill(account.email);
  await page.locator('#account-address').fill(account.address);
  await page.locator('#account-phone').fill(account.phone);

  await page
    .getByRole('button', { name: 'update information' })
    .click();

  await expect(
    page.getByText('Account information updated.')
  ).toBeVisible();
}

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
