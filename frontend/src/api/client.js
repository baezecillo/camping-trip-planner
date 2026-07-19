const BASE_URL = 'http://localhost:8080';

export class ApiError extends Error {
  constructor(status, body) {
    super(`API request failed with status ${status}`);
    this.status = status;
    this.body = body;
  }
}

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    credentials: 'include',
    headers: options.body ? { 'Content-Type': 'application/json' } : undefined,
    ...options,
  });

  if (!response.ok) {
    let body = null;
    try {
      body = await response.json();
    } catch {
      // no JSON body
    }
    throw new ApiError(response.status, body);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export function login(username, password) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
}

export function register(username, password) {
  return request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
}

export function logout() {
  return request('/api/auth/logout', { method: 'POST' });
}

export function getCurrentTrip() {
  return request('/api/trips/current');
}

export function createTrip({ origin, destination, startDate, endDate }) {
  return request('/api/trips', {
    method: 'POST',
    body: JSON.stringify({ origin, destination, startDate, endDate }),
  });
}

export function deleteCurrentTrip() {
  return request('/api/trips/current', { method: 'DELETE' });
}

export function updateChecklistItem(id, isPacked) {
  return request(`/api/checklist/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ isPacked }),
  });
}
