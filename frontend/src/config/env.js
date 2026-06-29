/**
 * Destinos de integração — valores definidos em runtime via .env (local) ou Vercel (produção).
 *
 * ⚠️ O endereço real da API NÃO está fixo no código.
 * Preencha VITE_API_BASE_URL quando souber a URL do back-end no Render.
 * Ver: integrações_faltantes.md → seção "Links em Aberto"
 */
function resolverApiBaseUrl() {
  const fromEnv = import.meta.env.VITE_API_BASE_URL;
  if (fromEnv !== undefined && String(fromEnv).trim() !== '') {
    return String(fromEnv).trim().replace(/\/$/, '');
  }
  // Em dev o Vite faz proxy de /api → http://localhost:8080 (mesma origem, sem CORS)
  if (import.meta.env.DEV) {
    return '';
  }
  return 'http://localhost:8080';
}

export const config = {
  apiBaseUrl: resolverApiBaseUrl(),
  appName: import.meta.env.VITE_APP_NAME || 'MTA - Ministério Templo Da Adoração'
};

export const endpoints = {
  login: '/api/auth/login',
  recuperarSenha: '/api/auth/recuperar-senha',
  membros: '/api/membros',
  eventos: '/api/eventos',
  eventosProximos: '/api/eventos/proximos',
  avisos: '/api/avisos'
};
