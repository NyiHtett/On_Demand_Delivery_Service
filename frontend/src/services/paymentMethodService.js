import { del, get, post } from './apiClient';

function getPaymentMethods() {
  return get('/payment-methods');
}

function createPaymentMethodSetupSession() {
  return post('/payment-methods/setup-session');
}

function confirmPaymentMethodSetup(sessionId) {
  return post('/payment-methods/setup-session/confirm', { sessionId });
}

function deletePaymentMethod(id) {
  return del(`/payment-methods/${id}`);
}

export {
  confirmPaymentMethodSetup,
  createPaymentMethodSetupSession,
  deletePaymentMethod,
  getPaymentMethods,
};
