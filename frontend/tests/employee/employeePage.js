import { expect } from '@playwright/test';

class EmployeePage {
  constructor(page) {
    this.page = page;
  }

  async openDashboard() {
    await this.page
      .getByRole('link', { name: 'Switch to manager view' })
      .click();
    await expect(
      this.page.getByRole('heading', { name: 'Employee Product Dashboard' }),
    ).toBeVisible();
  }

  productRow(name) {
    return this.page.getByRole('listitem').filter({
      has: this.page.getByRole('heading', {
        name: name.toLowerCase(),
        exact: true,
      }),
    });
  }

  async fillCreateProductForm(product) {
    await this.page.getByRole('button', { name: 'Create Product' }).click();
    await this.page.getByRole('textbox', { name: 'Name' }).fill(product.name);
    await this.page
      .getByRole('spinbutton', { name: 'Price ($)' })
      .fill(String(product.unitPrice));
    await this.page
      .getByRole('spinbutton', { name: 'Weight (lb)' })
      .fill(String(product.unitWeight));
    await this.page
      .getByRole('spinbutton', { name: 'Stock' })
      .fill(String(product.quantity));
    await this.page
      .getByRole('textbox', { name: 'Description', exact: true })
      .fill(product.description);
    await this.page
      .getByRole('textbox', { name: 'Image URL', exact: true })
      .fill(product.imageUrl);
  }

  async createProduct(product) {
    await this.fillCreateProductForm(product);
    await this.page.getByRole('button', { name: 'Create', exact: true }).click();
    await expect(this.productRow(product.name)).toBeVisible();
  }

  async expectProductCreationError(product, expectedMessage) {
    await this.fillCreateProductForm(product);

    const dialogMessagePromise = this.page
      .waitForEvent('dialog')
      .then(async (dialog) => {
        const message = dialog.message();
        await dialog.accept();
        return message;
      });

    await this.page.getByRole('button', { name: 'Create', exact: true }).click();
      const dialogMessage = await dialogMessagePromise;
      expect(dialogMessage).toContain(expectedMessage);
      await expect(this.productRow(product.name)).toHaveCount(1);
  }

  async expectCreateProductRangeError(product, fieldName) {
    await this.fillCreateProductForm(product);

    const fields = {
      unitPrice: this.page.getByRole('spinbutton', { name: 'Price ($)' }),
      unitWeight: this.page.getByRole('spinbutton', { name: 'Weight (lb)' }),
      quantity: this.page.getByRole('spinbutton', { name: 'Stock' }),
    };
    const field = fields[fieldName];
    if (!field) {
      throw new Error(`Unsupported product field: ${fieldName}`);
    }

    await this.page.getByRole('button', { name: 'Create', exact: true }).click();
    expect(await field.evaluate((input) => input.validity.rangeUnderflow)).toBe(
      true,
    );
  }

  async updateProduct(name, updates) {
    const row = this.productRow(name);
    const numericFields = row.getByRole('spinbutton');
    const fields = {
      unitWeight: numericFields.nth(0),
      unitPrice: numericFields.nth(1),
      quantity: numericFields.nth(2),
      description: row.getByPlaceholder('product description here...'),
      imageUrl: row.getByPlaceholder('image url here...'),
    };

    for (const [field, value] of Object.entries(updates)) {
      const locator = fields[field];
      if (!locator) {
        throw new Error(`Unsupported product field: ${field}`);
      }
      await locator.fill(String(value));
      await locator.blur();
    }

    const saveResponsePromise = this.page.waitForResponse((response) =>
      response.request().method() === 'PUT' &&
      /\/api\/products\/[^/]+$/.test(new URL(response.url()).pathname)
    );

    const dialogPromise = this.page.waitForEvent('dialog').then((dialog) =>
      dialog.accept(),
    );

    await this.page.getByRole('button', { name: 'Save', exact: true }).click();
    const [saveResponse] = await Promise.all([
      saveResponsePromise,
      dialogPromise,
    ]);

    expect(saveResponse.ok()).toBeTruthy();

    await this.page.reload();
    const savedRow = this.productRow(name);
    await expect(savedRow.getByRole('spinbutton').nth(0))
      .toHaveValue(Number(updates.unitWeight).toFixed(3));
    await expect(savedRow.getByRole('spinbutton').nth(1))
      .toHaveValue(Number(updates.unitPrice).toFixed(2));

    if (updates.unitWeight !== undefined) {
      await expect(fields.unitWeight).toHaveValue(
        Number(updates.unitWeight).toFixed(3),
      );
    }
    if (updates.unitPrice !== undefined) {
      await expect(fields.unitPrice).toHaveValue(
        Number(updates.unitPrice).toFixed(2),
      );
    }
    if (updates.quantity !== undefined) {
      await expect(fields.quantity).toHaveValue(String(updates.quantity));
    }
    if (updates.description !== undefined) {
      await expect(fields.description).toHaveValue(updates.description);
    }
    if (updates.imageUrl !== undefined) {
      await expect(fields.imageUrl).toHaveValue(updates.imageUrl);
    }
  }

  async expectProduct(name, exists) {
    await expect(this.productRow(name)).toHaveCount(exists ? 1 : 0);
  }

  async deleteProduct(name) {
    const row = this.productRow(name);
    const dialogPromise = this.page.waitForEvent('dialog').then((dialog) =>
      dialog.accept(),
    );
    await row.getByRole('button', { name: 'X', exact: true }).click();
    await dialogPromise;
    await expect(row).toHaveCount(0);
  }
}

export default EmployeePage;
