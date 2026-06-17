/** Evento encerrado quando a data/hora de término já passou. */
export function eventoEncerrado(evento) {
  if (!evento?.dataFim) return false;
  return new Date(evento.dataFim) <= new Date();
}

/** Aviso expirado quando a data/hora de término já passou. */
export function avisoExpirado(aviso) {
  if (!aviso?.dataExpiracao) return false;
  return new Date(aviso.dataExpiracao) <= new Date();
}

export function contarEventosAtivos(eventos) {
  return (eventos || []).filter((e) => !eventoEncerrado(e)).length;
}

export function contarAvisosAtivos(avisos) {
  return (avisos || []).filter((a) => !avisoExpirado(a)).length;
}

/** Ativos primeiro; encerrados no final (admin). */
export function ordenarEventosAdmin(eventos) {
  return [...(eventos || [])].sort((a, b) => {
    const aEnc = eventoEncerrado(a);
    const bEnc = eventoEncerrado(b);
    if (aEnc !== bEnc) return aEnc ? 1 : -1;
    return new Date(a.dataInicio) - new Date(b.dataInicio);
  });
}

export function ordenarAvisosAdmin(avisos) {
  return [...(avisos || [])].sort((a, b) => {
    const aExp = avisoExpirado(a);
    const bExp = avisoExpirado(b);
    if (aExp !== bExp) return aExp ? 1 : -1;
    return new Date(b.criadoEm) - new Date(a.criadoEm);
  });
}
