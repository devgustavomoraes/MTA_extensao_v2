import { apiRequest } from './client.js';
import { endpoints } from '../config/env.js';

export function listarMembros({ page = 0, size = 20, busca = '' } = {}) {
  const params = new URLSearchParams({ page, size });
  if (busca) {
    params.set('busca', busca);
  }
  return apiRequest(`${endpoints.membros}?${params}`);
}

export function criarMembro(dados) {
  return apiRequest(endpoints.membros, {
    method: 'POST',
    body: JSON.stringify(dados)
  });
}

export function excluirMembro(id) {
  return apiRequest(`${endpoints.membros}/${id}`, { method: 'DELETE' });
}
