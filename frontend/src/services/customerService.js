import { API_TOKEN_KEY, get, post, put } from './apiClient';

/**
 * Extracts the API token from the response.
 * @param {*} response - The API response.
 * @returns {string|null} The API token, or null if not found.
 */
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

  if (!token) {
    throw new Error(
      'Authentication succeeded without a session token.'
    );
  }

  localStorage.setItem(API_TOKEN_KEY, token);
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

/**
 * for getting current user information in account page 
 */
function getCurrentUser() {
  return get('/users/me');
}

/**
 * for updating current user information in account page 
 */
function updateCurrentUser(profile) {
  return put('/users/me', profile);
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

export {
  getCurrentUser,
  getInventory,
  getProduct,
  loginUser,
  logoutUser,
  signUpUser,
  updateCurrentUser,
};
