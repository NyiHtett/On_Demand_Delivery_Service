const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '');
const API_TOKEN_KEY = 'api_token';

// error/response handling
class ApiError extends Error {
  constructor(message, status, details = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}

function buildUrl(path) {
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`;
}

async function parseResponse(response) {
  if (response.status === 204) return null;

  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return response.json();
  }

  const text = await response.text();
  return text || null;
}

function getErrorMessage(responseBody, status) {
  if (typeof responseBody === 'string' && responseBody.trim()) {
    return responseBody;
  }

  if (responseBody && typeof responseBody === 'object') {
    return (
      responseBody.message ||
      responseBody.error ||
      responseBody.details ||
      `Request failed (HTTP ${status}).`
    );
  }

  return `Request failed (HTTP ${status}).`;
}

// main request function
async function request(path, { method, body, headers = {}, auth = true, ...options }) {
  const requestHeaders = new Headers(headers);
  const token = localStorage.getItem(API_TOKEN_KEY);
  const hasBody = body !== undefined && body !== null;
  const isFormData = typeof FormData !== 'undefined' && body instanceof FormData;

  if (hasBody && !isFormData && !requestHeaders.has('Content-Type')) {
    requestHeaders.set('Content-Type', 'application/json');
  }

  if (auth && token && !requestHeaders.has('Authorization')) {
    requestHeaders.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(buildUrl(path), {
    ...options,
    method,
    headers: requestHeaders,
    body: hasBody && !isFormData ? JSON.stringify(body) : body,
  });

  const responseBody = await parseResponse(response);

  if (!response.ok) {
    throw new ApiError(
      getErrorMessage(responseBody, response.status),
      response.status,
      responseBody,
    );
  }

  return responseBody;
}

// helper functions for rest apis we are using
function get(path, options = {}) {
  return request(path, { ...options, method: 'GET' });
}

function post(path, body, options = {}) {
  return request(path, { ...options, method: 'POST', body });
}

function put(path, body, options = {}) {
  return request(path, { ...options, method: 'PUT', body });
}

function del(path, options = {}) {
  return request(path, { ...options, method: 'DELETE' });
}

export { API_TOKEN_KEY, ApiError, del, get, post, put };
