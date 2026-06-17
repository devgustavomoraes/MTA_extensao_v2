import { config } from '../config/env.js';
import { obterToken } from '../auth/session.js';

export class ApiError extends Error {
  constructor(status, erro, mensagem) {
    super(mensagem);
    this.name = 'ApiError';
    this.status = status;
    this.erro = erro;
  }
}

/**
 * Requisição HTTP genérica.
 * @param {string} path — caminho relativo (ex: /api/membros). O host vem de VITE_API_BASE_URL.
 */
export async function apiRequest(path, options = {}) {
  const url = `${config.apiBaseUrl}${path}`;
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };

  const token = obterToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  try {
    const response = await fetch(url, { ...options, headers });

    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      throw new ApiError(
        response.status,
        body.erro || 'ERRO',
        body.mensagem || `Erro ${response.status} ao comunicar com o servidor`
      );
    }

    if (response.status === 204) {
      return null;
    }

    return response.json();
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    const destino = config.apiBaseUrl || 'http://localhost:8080 (via proxy do Vite)';
    throw new ApiError(
      0,
      'REDE',
      `Não foi possível conectar à API em ${destino}. ` +
      'Inicie o back-end antes do front: pasta backend → execute dev.bat (ou a tarefa "Backend: iniciar API" no VS Code). ' +
      'Aguarde a mensagem "Banco conectado" no terminal.'
    );
  }
}
