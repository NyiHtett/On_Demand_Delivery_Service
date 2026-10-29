import { get, post, put, del } from './apiClient';

const CART_UPDATED_EVENT = 'cart-updated';

// Lets the header badge (and any open page) refresh whenever the cart changes.
function announceCart(cart) {
  window.dispatchEvent(new CustomEvent(CART_UPDATED_EVENT, { detail: cart }));
  return cart;
}

function getCart() {
  return get('/cart');
}
function addCartItem(productId, quantity) {
  return post('/cart/items', { productId, quantity }).then(announceCart);
}
function updateCartItemQuantity(productId, quantity) {
  return put(`/cart/items/${productId}`, { quantity }).then(announceCart);
}
function removeCartItem(productId) {
  return del(`/cart/items/${productId}`).then(announceCart);
}

// Turns the cart into an order and returns { orderId, url }; send the browser to url (Stripe).
function checkoutCart(deliveryAddress) {
  return post('/checkout/session', { deliveryAddress });
}
// After Stripe redirects back: { sessionId } or, when Stripe isn't configured, { orderId }.
function confirmCheckout(params) {
  return post('/checkout/confirm', params);
}
// Customer backed out of Stripe: release the unpaid order.
function cancelCheckout(sessionId) {
  return post('/checkout/cancel', { sessionId });
}

function getOrders() {
  return get('/orders');
}
function getCustomerOrders() {
  return get('/customer/orders');
}
function getOrder(orderId) {
  return get(`/customer/orders/${orderId}`);
}
function cancelOrder(orderId) {
  return post(`/customer/orders/${orderId}/cancel`);
}
function updateOrderStatus(orderId, orderStatus) {
  return put(`/orders/${orderId}/status`, { orderStatus })
}
function getAccount() {}
function updateAccount() {}
function getPaymentMethods() {}
function addPaymentMethod() {}
function deletePaymentMethod() {}

export {
  CART_UPDATED_EVENT,
  addCartItem,
  addPaymentMethod,
  cancelCheckout,
  cancelOrder,
  checkoutCart,
  confirmCheckout,
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
