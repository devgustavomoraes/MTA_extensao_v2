const THEME_KEY = 'mta_theme';

export const TEMAS_DISPONIVEIS = [
  { id: 'light', nome: 'Claro', emoji: '☀️', descricao: 'Fundo claro, ideal para o dia' },
  { id: 'dark', nome: 'Escuro', emoji: '🌙', descricao: 'Confortável à noite' }
];

const META_CORES = {
  light: '#1b3a5c',
  dark: '#141b24'
};

export function carregarTema() {
  const salvo = localStorage.getItem(THEME_KEY);
  return TEMAS_DISPONIVEIS.some((t) => t.id === salvo) ? salvo : 'light';
}

export function salvarTema(temaId) {
  localStorage.setItem(THEME_KEY, temaId);
  aplicarTema(temaId);
}

export function aplicarTema(temaId) {
  document.documentElement.setAttribute('data-theme', temaId);
  const meta = document.querySelector('meta[name="theme-color"]');
  if (meta) {
    meta.setAttribute('content', META_CORES[temaId] || META_CORES.light);
  }
}
