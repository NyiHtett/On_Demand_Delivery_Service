import { readFileSync } from 'fs';
import { runLogIn_OnlyTest } from '../loginAndSignUp/loginFlow.js';
import { logPass } from '../utils/reporterHelpers.js';
import EmployeePage from './employeePage.js';

const scenarios = JSON.parse(
  readFileSync(new URL('../data/loginOnlyScenarios.json', import.meta.url), 'utf-8'),
);

export async function runCreateReadUpdateProductTest(page, testInfo, product) {
  const employee = scenarios.find((user) => user.role === 'Existing Employee');
  if (!employee) {
    throw new Error('The Existing Employee login scenario is required.');
  }

  await runLogIn_OnlyTest(page, testInfo, employee);
  const employeePage = new EmployeePage(page);
  await employeePage.openDashboard();

  const initialProduct = {
    name: product.name,
    unitPrice: Number(product.unitPrice),
    unitWeight: Number(product.unitWeight),
    quantity: Number(product.quantity),
    description: product.description,
    imageUrl: product.imageUrl,
  };

  const invalidFieldByRole = {
    'Invalid Price Product': 'unitPrice',
    'Invalid Weight Product': 'unitWeight',
    'Invalid Quantity Product': 'quantity',
  };
  const invalidField = invalidFieldByRole[product.role];
  if (invalidField) {
    await employeePage.expectCreateProductRangeError(initialProduct, invalidField);
    logPass(`Product creation rejected for invalid ${invalidField}.`);
    return;
  }

  await employeePage.createProduct(initialProduct);
  logPass(`Created Product: ${initialProduct.name} Successfully`);

  if (product.role === "Duplicate Product") {
    await employeePage.expectProductCreationError(
      initialProduct,
      'A record with that value already exists.',
    );
    logPass(`Created Product: ${initialProduct.name} Unsuccessfully, duplicate.`);
  }

  const updates = {
    unitPrice: Number((initialProduct.unitPrice + 0.1).toFixed(2)),
    unitWeight: Number((initialProduct.unitWeight + 0.1).toFixed(1)),
    quantity: initialProduct.quantity + 1,
    description: `${initialProduct.description} Updated`,
  };
  await employeePage.updateProduct(initialProduct.name, updates);
  logPass(`Updated Product: ${initialProduct.name} Successfully`);

  await employeePage.deleteProduct(initialProduct.name);
  logPass(`Deleted Product: ${initialProduct.name} Successfully`);
}
