import { get, put, post, del } from './apiClient';

function getInventory() {
  return get('/products', { auth: true });
}

function getProduct() {}

function createProduct(product) {
  return post('/products', product);
}

function updateProduct(productId, product) {
  return put(`/products/${productId}`, product)
}

function deleteProduct(productId) {
  return del(`/products/${productId}`);
}

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
};
