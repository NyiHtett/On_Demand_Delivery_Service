import { get, put } from './apiClient';

function getInventory() {
  return get('/products', { auth: true });
}
function getProduct() {}
function createProduct() {}
function updateProduct() {}

function updateProductQuantity(productId, quantity) {
  return put(`/products/${productId}/quantity`, { quantity });
}

function deleteProduct() {}
function getInventoryUpdates() {}
function getCustomerOrders() {}
function updateOrderStatus() {}

export {
  createProduct,
  deleteProduct,
  getCustomerOrders,
  getInventory,
  getInventoryUpdates,
  getProduct,
  updateOrderStatus,
  updateProduct,
  updateProductQuantity,
};
