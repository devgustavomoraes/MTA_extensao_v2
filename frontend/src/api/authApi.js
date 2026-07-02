import { apiRequest } from './client.js';
import { endpoints } from '../config/env.js';

export function login(email, senha) {
  return apiRequest(endpoints.login, {
    method: 'POST',
    body: JSON.stringify({ email, senha })
  });
}

