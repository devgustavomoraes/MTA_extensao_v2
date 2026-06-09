export function formatarDataHora(isoString) {
  if (!isoString) return '';
  const data = new Date(isoString);
  return data.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

export function formatarData(isoString) {
  if (!isoString) return '';
  const data = new Date(isoString);
  return data.toLocaleDateString('pt-BR');
}

export function formatarHora(isoString) {
  if (!isoString) return '';
  const data = new Date(isoString);
  return data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

export function construirDateTime(data, hora, horaPadrao = '09:00') {
  const h = hora || horaPadrao;
  return `${data}T${h.length === 5 ? h : horaPadrao}:00`;
}

export function labelPrioridade(prioridade) {
  const mapa = {
    NORMAL: 'Normal',
    URGENTE: 'Urgente',
    IMPORTANTE: 'Importante',
    normal: 'Normal',
    urgente: 'Urgente',
    info: 'Importante'
  };
  return mapa[prioridade] || prioridade;
}

export function prioridadeParaApi(valor) {
  const mapa = {
    normal: 'NORMAL',
    urgente: 'URGENTE',
    info: 'IMPORTANTE'
  };
  return mapa[valor] || 'NORMAL';
}

export function classePrioridade(prioridade) {
  const valor = String(prioridade).toUpperCase();
  if (valor === 'URGENTE') return 'urgente';
  if (valor === 'IMPORTANTE') return 'info';
  return 'normal';
}
