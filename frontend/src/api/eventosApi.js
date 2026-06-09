import { apiRequest } from './client.js';
import { endpoints } from '../config/env.js';

export function listarEventos({ page = 0, size = 50 } = {}) {
  const params = new URLSearchParams({ page, size });
  return apiRequest(`${endpoints.eventos}?${params}`);
}

export function listarProximosEventos() {
  return apiRequest(endpoints.eventosProximos);
}

export function criarEvento(dados) {
  return apiRequest(endpoints.eventos, {
    method: 'POST',
    body: JSON.stringify(dados)
  });
}

export function excluirEvento(id) {
  return apiRequest(`${endpoints.eventos}/${id}`, { method: 'DELETE' });
}

export function listarEscalas(eventoId) {
  return apiRequest(`${endpoints.eventos}/${eventoId}/escalas`);
}

export function associarEscala(eventoId, dados) {
  return apiRequest(`${endpoints.eventos}/${eventoId}/escalas`, {
    method: 'POST',
    body: JSON.stringify(dados)
  });
}

export function removerEscala(eventoId, escalaId) {
  return apiRequest(`${endpoints.eventos}/${eventoId}/escalas/${escalaId}`, {
    method: 'DELETE'
  });
}
