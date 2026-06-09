const THEME_KEY = 'mta_theme';

export const TEMAS_DISPONIVEIS = [
  { id: 'light', nome: 'Claro', emoji: '☀️', descricao: 'Fundo claro, ideal para o dia' },
  { id: 'dark', nome: 'Escuro', emoji: '🌙', descricao: 'Confortável à noite' },
  { id: 'blue', nome: 'Azul', emoji: '💙', descricao: 'Paleta azul clássica' },
  { id: 'pink', nome: 'Rosa', emoji: '💗', descricao: 'Paleta rosa suave' }
];

const META_CORES = {
  light: '#1a365d',
  dark: '#0f172a',
  blue: '#1e40af',
  pink: '#9d174d'
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
