import { BASE_URL } from "../../playwright.config.js";
import { test, expect } from "@playwright/test";
import { loginAsCustomer, makeCart, mockApi } from "../utils/mockApi.js";

const products = [
    { productId: 1, name: 'Apple', description:'fresh apples', unitWeight:0.400, unitPrice:1.00, imageUrl:'', quantity:10 }
]

test.describe('Shopping Page', () => {
    test('page loads ok', async ({ page }) => {
        await page.goto(BASE_URL + 'shop');
        await expect(page).toHaveURL(/shop/);
        await expect(page.getByText("delivery")).toBeVisible();
    });

    test('product is able to be added to the cart', async ({ page }) => {
        await loginAsCustomer(page);
        await mockApi(page, '**/api/products', products);
        await mockApi(page, '**/api/cart', {});
        await mockApi(page, '**/api/cart/items', {});

        await page.goto(BASE_URL + 'shop');

        await page.getByRole('button', { name: 'Increase apple quantity' }).click();
        await page.getByRole('button', { name: 'add to cart' }).click();
        
        await expect(page.getByText("Added 1 apple to your cart.")).toBeVisible();
    });

    test('search filters products properly', async ({ page }) => {
        await loginAsCustomer(page);
        await mockApi(page, '**/api/products', products);
        await mockApi(page, '**/api/cart', {});

        await page.goto(BASE_URL + 'shop');

        await page.getByLabel('Search products').fill('app');
        await expect(page.getByText('Apple')).toBeVisible();
    });

    test('searching something that doesn\'t exist shows nothing', async ({ page }) => {
        await loginAsCustomer(page);
        await mockApi(page, '**/api/products', products);
        await mockApi(page, '**/api/cart', {});

        await page.goto(BASE_URL + 'shop');

        await page.getByLabel('Search products').fill('random word');
        await expect(page.getByText('Apple')).not.toBeVisible();
        await expect(page.getByText('No products found. Try another search.')).toBeVisible();
    });

    test('adding to cart as a guest goes to login page', async ({ page }) => {
        await mockApi(page, '**/api/products', products);
        await mockApi(page, '**/api/cart', {});
        await mockApi(page, '**/api/cart/items', { message: 'you are not logged in'}, 401);

        await page.goto(BASE_URL + 'shop');

        await page.getByRole('button', { name: 'Increase apple quantity' }).click();
        await page.getByRole('button', { name: 'add to cart' }).click();
        
        await expect(page).toHaveURL(/login/);
    });

    test('add button is disabled when the quantity is equal to the stock', async ({ page }) => {
        await loginAsCustomer(page);
        await mockApi(page, '**/api/products', products);
        await mockApi(page, '**/api/cart', {});
        await mockApi(page, '**/api/cart/items', {});

        await page.goto(BASE_URL + 'shop');

        for (let i = 0; i < 10; i++) await page.getByRole('button', { name: 'Increase apple quantity' }).click();
        
        await expect(page.getByRole('button', { name: 'Increase apple quantity' })).toBeDisabled();
    });

    test('view button works in showing product description', async ({ page }) => {
        await loginAsCustomer(page);
        await mockApi(page, '**/api/products', products);
        await mockApi(page, '**/api/cart', {});
        await mockApi(page, '**/api/cart/items', {});

        await page.goto(BASE_URL + 'shop');

        await page.getByRole('button', { name: 'View apple details' }).click();
        
        await expect(page.getByText("fresh apples")).toBeVisible();
    });
});