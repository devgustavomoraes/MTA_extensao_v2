/**
 * Destinos de integração — valores definidos em runtime via .env (local) ou Vercel (produção).
 *
 * ⚠️ O endereço real da API NÃO está fixo no código.
 * Preencha VITE_API_BASE_URL quando souber a URL do back-end no Render.
 * Ver: integrações_faltantes.md → seção "Links em Aberto"
 */
export const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
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
