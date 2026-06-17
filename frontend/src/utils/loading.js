/**
 * Indicadores visuais de carregamento para listas e seções.
 */
export function definirCarregamento(elementId, ativo, mensagem = 'Carregando...') {
  const el = document.getElementById(elementId);
  if (!el) return;

  if (ativo) {
    el.setAttribute('aria-busy', 'true');
    el.classList.add('is-loading');

    let overlay = el.querySelector(':scope > .loading-overlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.className = 'loading-overlay';
      overlay.setAttribute('role', 'status');
      overlay.innerHTML = `
        <div class="spinner" aria-hidden="true"></div>
        <span class="loading-text">${mensagem}</span>
      `;
      el.appendChild(overlay);
    } else {
      const texto = overlay.querySelector('.loading-text');
      if (texto) texto.textContent = mensagem;
    }
    return;
  }

  el.removeAttribute('aria-busy');
  el.classList.remove('is-loading');
  el.querySelector(':scope > .loading-overlay')?.remove();
}

export function definirCarregamentoApp(ativo) {
  const loader = document.getElementById('appLoader');
  if (!loader) return;

  loader.classList.toggle('hidden', !ativo);
  loader.setAttribute('aria-busy', ativo ? 'true' : 'false');
  document.getElementById('app')?.setAttribute('aria-hidden', ativo ? 'true' : 'false');
}
