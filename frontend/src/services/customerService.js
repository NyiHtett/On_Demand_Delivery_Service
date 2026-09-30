import { API_TOKEN_KEY, get, post } from './apiClient';

// api token stuff
function getTokenFromResponse(response) {
  return (
    response?.apiToken ||
    response?.api_token ||
    response?.token ||
    response?.sessionId ||
    response?.session_id ||
    null
  );
}

function storeTokenFromResponse(response) {
  const token = getTokenFromResponse(response);
  if (token) localStorage.setItem(API_TOKEN_KEY, token);
  return response;
}

// actual helper function stuff
async function signUpUser(signUpData) {
  const response = await post('/users/signup', signUpData, { auth: false });
  return storeTokenFromResponse(response);
}

async function loginUser(loginData) {
  const response = await post('/users/login', loginData, { auth: false });
  return storeTokenFromResponse(response);
}

async function logoutUser() {
  try {
    await post('/users/logout');
  } finally {
    localStorage.removeItem(API_TOKEN_KEY);
  }
}

function getInventory() {
  return get('/products', { auth: false });
}

function getProduct(productId) {
  if (productId === undefined || productId === null || productId === '') {
    throw new Error('A product ID is required.');
  }

  return get(`/products/${encodeURIComponent(productId)}`, { auth: false });
}

export { getInventory, getProduct, loginUser, logoutUser, signUpUser };
