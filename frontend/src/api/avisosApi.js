import { apiRequest } from './client.js';
import { endpoints } from '../config/env.js';

export function listarAvisos({ page = 0, size = 50, incluirExpirados = false } = {}) {
  const params = new URLSearchParams({ page, size });
  if (incluirExpirados) params.set('incluirExpirados', 'true');
  return apiRequest(`${endpoints.avisos}?${params}`);
}

export function criarAviso(dados) {
  return apiRequest(endpoints.avisos, {
    method: 'POST',
    body: JSON.stringify(dados)
  });
}

export function excluirAviso(id) {
  return apiRequest(`${endpoints.avisos}/${id}`, { method: 'DELETE' });
}
