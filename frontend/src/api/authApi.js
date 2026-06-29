import { apiRequest } from './client.js';
import { endpoints } from '../config/env.js';

export function login(email, senha) {
  return apiRequest(endpoints.login, {
    method: 'POST',
    body: JSON.stringify({ email, senha })
  });
}

export function recuperarSenha(email) {
  return apiRequest(endpoints.recuperarSenha, {
    method: 'POST',
    body: JSON.stringify({ email })
  });
}

