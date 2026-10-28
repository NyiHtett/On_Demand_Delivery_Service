import { get, put } from './apiClient';

function getCart() {
  return get('/cart');
}
function addCartItem() {}
function updateCartItemQuantity() {}
function removeCartItem() {}
function cancelCart() {}
function checkoutCart() {}
function getOrders() {
  return get('/orders');
}
function getCustomerOrders() {
  return get('/customer/orders');
}
function updateOrderStatus(orderId, orderStatus) {
  return put(`/orders/${orderId}/status`, { orderStatus })
}
function getOrder() {}
function cancelOrder() {}
function getAccount() {}
function updateAccount() {}
function getPaymentMethods() {}
function addPaymentMethod() {}
function deletePaymentMethod() {}

export {
  addCartItem,
  addPaymentMethod,
  cancelCart,
  cancelOrder,
  checkoutCart,
  deletePaymentMethod,
  getAccount,
  getCart,
  getCustomerOrders,
  getOrder,
  getOrders,
  updateOrderStatus,
  getPaymentMethods,
  removeCartItem,
  updateAccount,
  updateCartItemQuantity,
};
