import { Calendar } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import listPlugin from '@fullcalendar/list';
import interactionPlugin from '@fullcalendar/interaction';
import ptBrLocale from '@fullcalendar/core/locales/pt-br.js';
import { config } from '../config/env.js';
import { login, recuperarSenha } from '../api/authApi.js';
import { listarMembros, criarMembro, excluirMembro as excluirMembroApi } from '../api/membrosApi.js';
import {
  listarEventos,
  listarProximosEventos,
  criarEvento,
  excluirEvento as excluirEventoApi,
  listarEscalas,
  associarEscala,
  removerEscala
} from '../api/eventosApi.js';
import { listarAvisos, criarAviso, excluirAviso as excluirAvisoApi } from '../api/avisosApi.js';
import { ApiError } from '../api/client.js';
import {
  salvarSessao,
  limparSessao,
  sessaoAtiva,
  isAdmin,
  obterUsuario
} from '../auth/session.js';
import { carregarTema, salvarTema, TEMAS_DISPONIVEIS } from '../config/theme.js';
import {
  formatarDataHora,
  formatarData,
  formatarHora,
  construirDateTime,
  labelPrioridade,
  prioridadeParaApi,
  classePrioridade
} from '../utils/format.js';
import { definirCarregamento, definirCarregamentoApp } from '../utils/loading.js';

let userRole = 'visitor';
let calendar;
let buscaMembrosTimer;
let eventoSelecionadoId = null;
let membrosParaEscala = [];

const state = {
  membros: [],
  eventos: [],
  proximosEventos: [],
  avisos: [],
  escalasCache: {}
};

const membrosPagina = {
  page: 0,
  size: 20,
  totalPages: 0,
  totalElements: 0,
  busca: ''
};

async function autenticarAdmin() {
  const email = document.getElementById('email').value.trim();
  const senha = document.getElementById('senha').value;
  const btn = document.getElementById('btnLoginAdmin');

  if (!email || !senha) {
    mostrarErroLogin('Preencha e-mail e senha para entrar.');
    return;
  }

  limparErroLogin();
  btn.disabled = true;
  btn.textContent = 'Entrando...';

  try {
    const response = await login(email, senha);
    salvarSessao(response);
    userRole = response.perfil === 'ADMIN' ? 'admin' : 'usuario';
    fecharModal('modalLogin');
    limparFormulario('modalLogin');
    await recarregarDadosAutenticados();
  } catch (error) {
    tratarErroLogin(error);
  } finally {
    btn.disabled = false;
    btn.textContent = 'Entrar';
  }
}

function tratarErroLogin(error) {
  if (error instanceof ApiError) {
    if (error.erro === 'CONTA_BLOQUEADA') {
      mostrarErroLogin(
        'Conta temporariamente bloqueada por excesso de tentativas. ' +
        'Aguarde alguns minutos e tente novamente, ou fale com a liderança.'
      );
      return;
    }
    if (error.erro === 'CREDENCIAIS_INVALIDAS') {
      mostrarErroLogin('E-mail ou senha incorretos. Verifique os dados e tente novamente.');
      return;
    }
    if (error.erro === 'REDE') {
      mostrarErroLogin(error.message);
      return;
    }
    mostrarErroLogin(error.message);
    return;
  }
  mostrarErroLogin('Não foi possível entrar. Tente novamente em instantes.');
}

function mostrarErroLogin(mensagem) {
  const el = document.getElementById('loginErro');
  el.textContent = mensagem;
  el.classList.remove('hidden');
}

function limparErroLogin() {
  const el = document.getElementById('loginErro');
  el.textContent = '';
  el.classList.add('hidden');
}

async function enviarRecuperacaoSenha() {
  const email = document.getElementById('recuperacaoEmail').value.trim();
  const erroEl = document.getElementById('recuperacaoErro');
  const sucessoEl = document.getElementById('recuperacaoSucesso');
  const btn = document.getElementById('btnEnviarRecuperacao');

  erroEl.classList.add('hidden');
  sucessoEl.classList.add('hidden');

  if (!email) {
    erroEl.textContent = 'Informe seu e-mail para continuar.';
    erroEl.classList.remove('hidden');
    return;
  }

  btn.disabled = true;
  btn.textContent = 'Enviando...';

  try {
    const resposta = await recuperarSenha(email);
    sucessoEl.textContent = resposta.mensagem;
    sucessoEl.classList.remove('hidden');
  } catch (error) {
    erroEl.textContent = error instanceof ApiError ? error.message : 'Não foi possível enviar a solicitação.';
    erroEl.classList.remove('hidden');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Enviar instruções';
  }
}

async function recarregarDadosAutenticados() {
  configurarAcesso();
  definirCarregamento('listaEventosDash', true, 'Atualizando painel...');
  try {
    await Promise.all([
      carregarAgenda(),
      carregarAvisos(),
      isAdmin() ? carregarMembros() : Promise.resolve()
    ]);
  } catch (error) {
    mostrarToast(tratarErroApi(error, 'Alguns dados não puderam ser carregados.'));
  } finally {
    definirCarregamento('listaEventosDash', false);
  }
  atualizarDashboard();
  renderizarEventos();
  renderizarAvisos();
}

async function iniciarApp() {
  definirCarregamentoApp(true);
  userRole = sessaoAtiva() ? (isAdmin() ? 'admin' : 'usuario') : 'visitor';
  configurarAcesso();
  inicializarCalendario();
  renderizarSeletorTemas();

  try {
    await Promise.all([carregarAgenda(), carregarAvisos()]);
    if (sessaoAtiva() && isAdmin()) await carregarMembros();
  } catch (error) {
    mostrarToast(tratarErroApi(error, 'Não foi possível carregar os dados. Verifique a conexão.'));
  } finally {
    definirCarregamentoApp(false);
  }

  atualizarDashboard();
  renderizarEventos();
  renderizarAvisos();
}

function configurarAcesso() {
  const admin = userRole === 'admin';
  const usuario = obterUsuario();

  if (admin) {
    document.getElementById('userName').innerText = 'Olá, Administrador!';
    document.getElementById('userStatus').innerText = usuario?.email || 'Gestor Geral';
  } else if (userRole === 'usuario') {
    document.getElementById('userName').innerText = 'Olá!';
    document.getElementById('userStatus').innerText = usuario?.email || 'Usuário';
  } else {
    document.getElementById('userName').innerText = 'Olá, Visitante!';
    document.getElementById('userStatus').innerText = 'Você pode ver agenda e avisos';
  }

  document.querySelectorAll('.admin-only').forEach((el) => {
    el.classList.toggle('hidden', !admin);
  });

  const autenticado = sessaoAtiva();
  document.getElementById('btnEntrar').classList.toggle('hidden', autenticado);
  document.getElementById('btnLogout').classList.toggle('hidden', !autenticado);
}

async function carregarAgenda() {
  const [pagina, proximos] = await Promise.all([
    listarEventos({ page: 0, size: 100 }),
    listarProximosEventos()
  ]);

  state.eventos = pagina.content || [];
  state.proximosEventos = proximos || [];
  atualizarCalendario();
}

async function carregarAvisos() {
  const pagina = await listarAvisos({ page: 0, size: 50 });
  state.avisos = pagina.content || [];
}

function inicializarCalendario() {
  const calendarEl = document.getElementById('calendar');
  if (!calendarEl) return;

  if (calendar) calendar.destroy();

  calendar = new Calendar(calendarEl, {
    plugins: [dayGridPlugin, listPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
    locale: ptBrLocale,
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,listWeek'
    },
    buttonText: { today: 'Hoje', month: 'Mês', week: 'Semana' },
    height: 'auto',
    events: state.eventos.map((ev) => ({
      title: ev.titulo,
      start: ev.dataInicio,
      end: ev.dataFim,
      backgroundColor: '#1a365d'
    }))
  });

  calendar.render();
}

function atualizarCalendario() {
  if (!calendar) return;
  calendar.removeAllEvents();
  state.eventos.forEach((ev) => {
    calendar.addEvent({
      title: ev.titulo,
      start: ev.dataInicio,
      end: ev.dataFim,
      backgroundColor: '#1a365d'
    });
  });
}

async function logout() {
  limparSessao();
  userRole = 'visitor';
  state.membros = [];
  membrosPagina.totalElements = 0;
  configurarAcesso();
  nav('dashboard');
  try {
    await Promise.all([carregarAgenda(), carregarAvisos()]);
    atualizarDashboard();
    renderizarEventos();
    renderizarAvisos();
  } catch (error) {
    mostrarToast(tratarErroApi(error, 'Erro ao atualizar dados.'));
  }
  mostrarToast('Você saiu da conta. Continua como visitante.');
}

function renderizarSeletorTemas() {
  const grid = document.getElementById('themeGrid');
  if (!grid) return;

  const temaAtual = carregarTema();
  grid.innerHTML = TEMAS_DISPONIVEIS.map((tema) => `
    <button type="button" class="theme-card ${tema.id === temaAtual ? 'active' : ''}"
            data-theme-select="${tema.id}" aria-pressed="${tema.id === temaAtual}">
      <span class="theme-card-emoji" aria-hidden="true">${tema.emoji}</span>
      <strong>${tema.nome}</strong>
      <span>${tema.descricao}</span>
    </button>
  `).join('');
}

function selecionarTema(temaId) {
  salvarTema(temaId);
  renderizarSeletorTemas();
  mostrarToast('Tema aplicado!');
}

function nav(pageId) {
  document.querySelectorAll('.page').forEach((p) => p.classList.add('hidden'));
  document.getElementById(pageId).classList.remove('hidden');
  document.querySelectorAll('.tab-item').forEach((btn) => btn.classList.remove('active'));
  document.getElementById(`tab-${pageId}`)?.classList.add('active');

  if (pageId === 'membros' && isAdmin()) {
    carregarMembros({ silencioso: false })
      .catch((e) => mostrarToast(tratarErroApi(e, 'Erro ao carregar membros.')));
  }
  if (pageId === 'agenda') {
    definirCarregamento('listaEventos', true, 'Carregando agenda...');
    carregarAgenda()
      .then(() => { renderizarEventos(); setTimeout(() => calendar?.updateSize(), 100); })
      .catch((e) => mostrarToast(tratarErroApi(e, 'Erro ao carregar agenda.')))
      .finally(() => definirCarregamento('listaEventos', false));
  }
  if (pageId === 'avisos') {
    definirCarregamento('listaAvisos', true, 'Carregando comunicados...');
    carregarAvisos()
      .then(renderizarAvisos)
      .catch((e) => mostrarToast(tratarErroApi(e, 'Erro ao carregar avisos.')))
      .finally(() => definirCarregamento('listaAvisos', false));
  }
  if (pageId === 'dashboard') atualizarDashboard();
  if (pageId === 'configuracoes') renderizarSeletorTemas();
}

function atualizarDashboard() {
  const totalMembros = isAdmin() ? membrosPagina.totalElements : '—';
  document.getElementById('statMembros').innerText = totalMembros;
  document.getElementById('statEventos').innerText = state.eventos.length;
  document.getElementById('statAvisos').innerText = state.avisos.length;

  const container = document.getElementById('listaEventosDash');
  const proximos = state.proximosEventos.slice(0, 3);

  if (proximos.length === 0) {
    container.innerHTML = '<div class="empty-state">Nenhum evento nos próximos dias.</div>';
    return;
  }

  container.innerHTML = proximos.map((ev) => `
    <div class="evento-card" style="margin-bottom:8px">
      <div class="evento-header">
        <strong>${escapeHtml(ev.titulo)}</strong>
        <span class="evento-data">${formatarDataHora(ev.dataInicio)}</span>
      </div>
      ${ev.local ? `<div class="evento-local">📍 ${escapeHtml(ev.local)}</div>` : ''}
    </div>
  `).join('');
}

async function carregarMembros(opcoes = {}) {
  const { silencioso = true } = opcoes;
  if (!silencioso) definirCarregamento('listaMembros', true, 'Carregando membros...');

  try {
    const pagina = await listarMembros({
      page: membrosPagina.page,
      size: membrosPagina.size,
      busca: membrosPagina.busca
    });

    state.membros = pagina.content || [];
    membrosPagina.totalPages = pagina.totalPages ?? 0;
    membrosPagina.totalElements = pagina.totalElements ?? 0;
    membrosPagina.page = pagina.number ?? 0;

    renderizarMembros();
    atualizarPaginacaoMembros();
    atualizarDashboard();
  } finally {
    if (!silencioso) definirCarregamento('listaMembros', false);
  }
}

function atualizarPaginacaoMembros() {
  const paginacao = document.getElementById('paginacaoMembros');
  const info = document.getElementById('infoPaginacaoMembros');
  const btnAnterior = document.getElementById('btnMembrosAnterior');
  const btnProximo = document.getElementById('btnMembrosProximo');

  if (!isAdmin() || membrosPagina.totalPages <= 1) {
    paginacao.classList.add('hidden');
    return;
  }

  paginacao.classList.remove('hidden');
  info.textContent = `Página ${membrosPagina.page + 1} de ${membrosPagina.totalPages} (${membrosPagina.totalElements} membros)`;
  btnAnterior.disabled = membrosPagina.page <= 0;
  btnProximo.disabled = membrosPagina.page >= membrosPagina.totalPages - 1;
}

async function salvarMembro() {
  const nomeCompleto = document.getElementById('membroNome').value.trim();
  const telefone = document.getElementById('membroTelefone').value.trim();
  const email = document.getElementById('membroEmail').value.trim();
  const funcao = document.getElementById('membroFuncao').value;

  if (!nomeCompleto) {
    mostrarToast('Informe o nome do membro.');
    return;
  }

  const btn = document.getElementById('btnSalvarMembro');
  btn.disabled = true;
  btn.classList.add('is-loading-btn');

  try {
    await criarMembro({ nomeCompleto, telefone: telefone || null, email: email || null, funcao: funcao || null });
    fecharModal('modalMembro');
    limparFormulario('modalMembro');
    mostrarToast('Membro cadastrado com sucesso!');
    membrosPagina.page = 0;
    await carregarMembros();
  } catch (error) {
    mostrarToast(tratarErroApi(error, 'Não foi possível salvar o membro.'));
  } finally {
    btn.disabled = false;
    btn.classList.remove('is-loading-btn');
  }
}

async function excluirMembro(id) {
  if (!confirm('Deseja remover este membro da lista?')) return;
  try {
    await excluirMembroApi(id);
    mostrarToast('Membro removido.');
    await carregarMembros();
  } catch (error) {
    mostrarToast(tratarErroApi(error, 'Não foi possível remover o membro.'));
  }
}

function renderizarMembros() {
  const container = document.getElementById('listaMembros');
  if (state.membros.length === 0) {
    container.innerHTML = '<div class="empty-state">Nenhum membro encontrado.</div>';
    return;
  }

  container.innerHTML = state.membros.map((m) => `
    <div class="membro-card">
      <div class="membro-avatar" aria-hidden="true">${escapeHtml(m.nomeCompleto).charAt(0).toUpperCase()}</div>
      <div class="membro-info">
        <strong>${escapeHtml(m.nomeCompleto)}</strong>
        <span>${escapeHtml(m.telefone || m.email || 'Sem contato')}</span>
      </div>
      <span class="badge">${escapeHtml(m.funcao || 'Membro')}${m.ativo === false ? ' • Inativo' : ''}</span>
      ${isAdmin() ? `<div class="membro-actions"><button type="button" class="btn-icon" data-excluir-membro="${m.id}" aria-label="Remover ${escapeHtml(m.nomeCompleto)}">🗑</button></div>` : ''}
    </div>
  `).join('');
}

function filtrarMembros() {
  clearTimeout(buscaMembrosTimer);
  buscaMembrosTimer = setTimeout(() => {
    membrosPagina.busca = document.getElementById('buscaMembro').value.trim();
    membrosPagina.page = 0;
    carregarMembros({ silencioso: false })
      .catch((e) => mostrarToast(tratarErroApi(e, 'Erro na busca.')));
  }, 350);
}

async function salvarEvento() {
  const titulo = document.getElementById('eventoTitulo').value.trim();
  const data = document.getElementById('eventoData').value;
  const horaInicio = document.getElementById('eventoHora').value;
  const horaFim = document.getElementById('eventoHoraFim').value;
  const local = document.getElementById('eventoLocal').value.trim();
  const descricao = document.getElementById('eventoDesc').value.trim();

  if (!titulo || !data || !horaInicio || !horaFim) {
    mostrarToast('Preencha título, data e horários de início e término.');
    return;
  }

  const dataInicio = construirDateTime(data, horaInicio);
  const dataFim = construirDateTime(data, horaFim);

  if (new Date(dataFim) <= new Date(dataInicio)) {
    mostrarToast('O horário de término deve ser depois do início.');
    return;
  }

  const btn = document.getElementById('btnSalvarEvento');
  btn.disabled = true;
  btn.classList.add('is-loading-btn');

  try {
    await criarEvento({ titulo, descricao: descricao || null, dataInicio, dataFim, local: local || null });
    fecharModal('modalEvento');
    limparFormulario('modalEvento');
    mostrarToast('Evento agendado com sucesso!');
    await carregarAgenda();
    atualizarDashboard();
    renderizarEventos();
  } catch (error) {
    if (error instanceof ApiError && error.erro === 'CONFLITO_HORARIO') {
      mostrarToast('Já existe outro evento neste horário. Escolha outro período.');
    } else {
      mostrarToast(tratarErroApi(error, 'Não foi possível agendar o evento.'));
    }
  } finally {
    btn.disabled = false;
    btn.classList.remove('is-loading-btn');
  }
}

async function excluirEvento(id) {
  if (!confirm('Deseja remover este evento da agenda?')) return;
  try {
    await excluirEventoApi(id);
    delete state.escalasCache[id];
    mostrarToast('Evento removido.');
    await carregarAgenda();
    atualizarDashboard();
    renderizarEventos();
  } catch (error) {
    mostrarToast(tratarErroApi(error, 'Não foi possível remover o evento.'));
  }
}

function renderizarEventos() {
  const container = document.getElementById('listaEventos');
  if (!container) return;

  if (state.eventos.length === 0) {
    container.innerHTML = '<div class="empty-state">Nenhum evento agendado. Volte em breve!</div>';
    return;
  }

  container.innerHTML = state.eventos.map((ev) => `
    <div class="evento-card">
      <div class="evento-header">
        <strong>${escapeHtml(ev.titulo)}</strong>
        <span class="evento-data">${formatarData(ev.dataInicio)}</span>
      </div>
      <div class="evento-local">🕐 ${formatarHora(ev.dataInicio)} — ${formatarHora(ev.dataFim)}</div>
      ${ev.local ? `<div class="evento-local">📍 ${escapeHtml(ev.local)}</div>` : ''}
      ${ev.descricao ? `<div class="evento-desc">${escapeHtml(ev.descricao)}</div>` : ''}
      <div id="escala-resumo-${ev.id}" class="escala-inline hidden"></div>
      <div class="evento-acoes">
        <button type="button" class="btn-texto" data-ver-escala="${ev.id}" title="Veja quem foi escalado para este evento">👥 Ver equipe</button>
        ${isAdmin() ? `
          <button type="button" class="btn-texto" data-gerenciar-escala="${ev.id}" title="Adicionar ou remover pessoas da equipe">Gerenciar equipe</button>
          <button type="button" class="btn-icon" data-excluir-evento="${ev.id}" title="Remover este evento">🗑</button>
        ` : ''}
      </div>
    </div>
  `).join('');
}

async function carregarEscalasResumo(eventoId) {
  if (!state.escalasCache[eventoId]) {
    state.escalasCache[eventoId] = await listarEscalas(eventoId);
  }
  return state.escalasCache[eventoId];
}

async function mostrarEscalasInline(eventoId) {
  const el = document.getElementById(`escala-resumo-${eventoId}`);
  if (!el) return;

  el.innerHTML = '<span class="loading-inline">Carregando equipe...</span>';
  el.classList.remove('hidden');
  try {
    const escalas = await carregarEscalasResumo(eventoId);
    if (escalas.length === 0) {
      el.innerHTML = '<strong>Equipe:</strong> ninguém escalado ainda.';
    } else {
      el.innerHTML = `<strong>Equipe:</strong> ${escalas.map((e) => escapeHtml(e.membroNome) + (e.funcaoEscala ? ` (${escapeHtml(e.funcaoEscala)})` : '')).join(', ')}`;
    }
    el.classList.remove('hidden');
  } catch (error) {
    el.classList.add('hidden');
    mostrarToast(tratarErroApi(error, 'Não foi possível carregar a equipe.'));
  }
}

async function abrirModalEscala(eventoId) {
  eventoSelecionadoId = eventoId;
  const evento = state.eventos.find((e) => e.id === eventoId);
  document.getElementById('escalaEventoInfo').textContent = evento
    ? `${evento.titulo} — ${formatarDataHora(evento.dataInicio)}`
    : '';

  if (isAdmin()) {
    await carregarMembrosParaEscala();
  }

  await renderizarEscalasModal();
  abrirModal('modalEscala');
}

async function carregarMembrosParaEscala() {
  const pagina = await listarMembros({ page: 0, size: 100 });
  membrosParaEscala = (pagina.content || []).filter((m) => m.ativo !== false);
  const select = document.getElementById('escalaMembroId');
  select.innerHTML = '<option value="">Selecione um membro...</option>' +
    membrosParaEscala.map((m) => `<option value="${m.id}">${escapeHtml(m.nomeCompleto)}</option>`).join('');
}

async function renderizarEscalasModal() {
  const container = document.getElementById('listaEscalas');
  definirCarregamento('listaEscalas', true, 'Carregando equipe...');
  try {
    const escalas = await listarEscalas(eventoSelecionadoId);
    state.escalasCache[eventoSelecionadoId] = escalas;

    if (escalas.length === 0) {
      container.innerHTML = '<div class="empty-state">Ninguém escalado ainda. Adicione membros abaixo.</div>';
      return;
    }

    container.innerHTML = escalas.map((e) => `
      <div class="escala-item">
        <div>
          <strong>${escapeHtml(e.membroNome)}</strong>
          <span>${escapeHtml(e.funcaoEscala || 'Participante')}${e.confirmado ? ' • Confirmado' : ''}</span>
        </div>
        ${isAdmin() ? `<button type="button" class="btn-icon" data-remover-escala="${e.id}" title="Remover da equipe">🗑</button>` : ''}
      </div>
    `).join('');
  } catch (error) {
    container.innerHTML = '<div class="empty-state">Não foi possível carregar a equipe.</div>';
  } finally {
    definirCarregamento('listaEscalas', false);
  }
}

async function adicionarMembroEscala() {
  const membroId = document.getElementById('escalaMembroId').value;
  const funcaoEscala = document.getElementById('escalaFuncao').value.trim();

  if (!membroId) {
    mostrarToast('Selecione um membro para adicionar à equipe.');
    return;
  }

  try {
    await associarEscala(eventoSelecionadoId, {
      membroId: Number(membroId),
      funcaoEscala: funcaoEscala || null
    });
    document.getElementById('escalaFuncao').value = '';
    document.getElementById('escalaMembroId').value = '';
    delete state.escalasCache[eventoSelecionadoId];
    mostrarToast('Membro adicionado à equipe!');
    await renderizarEscalasModal();
    const resumo = document.getElementById(`escala-resumo-${eventoSelecionadoId}`);
    if (resumo && !resumo.classList.contains('hidden')) {
      await mostrarEscalasInline(eventoSelecionadoId);
    }
  } catch (error) {
    if (error instanceof ApiError && error.erro === 'CONFLITO_HORARIO') {
      mostrarToast('Este membro já tem outro evento no mesmo horário.');
    } else {
      mostrarToast(tratarErroApi(error, 'Não foi possível adicionar à equipe.'));
    }
  }
}

async function removerMembroEscala(escalaId) {
  if (!confirm('Remover esta pessoa da equipe do evento?')) return;
  try {
    await removerEscala(eventoSelecionadoId, escalaId);
    delete state.escalasCache[eventoSelecionadoId];
    mostrarToast('Removido da equipe.');
    await renderizarEscalasModal();
  } catch (error) {
    mostrarToast(tratarErroApi(error, 'Não foi possível remover.'));
  }
}

async function salvarAviso() {
  const titulo = document.getElementById('avisoTitulo').value.trim();
  const mensagem = document.getElementById('avisoMensagem').value.trim();
  const prioridade = prioridadeParaApi(document.getElementById('avisoPrioridade').value);

  if (!titulo || !mensagem) {
    mostrarToast('Preencha título e mensagem do comunicado.');
    return;
  }

  const btn = document.getElementById('btnSalvarAviso');
  btn.disabled = true;
  btn.classList.add('is-loading-btn');

  try {
    await criarAviso({ titulo, mensagem, prioridade });
    fecharModal('modalAviso');
    limparFormulario('modalAviso');
    mostrarToast('Comunicado publicado!');
    await carregarAvisos();
    atualizarDashboard();
    renderizarAvisos();
  } catch (error) {
    mostrarToast(tratarErroApi(error, 'Não foi possível publicar o aviso.'));
  } finally {
    btn.disabled = false;
    btn.classList.remove('is-loading-btn');
  }
}

async function excluirAviso(id) {
  if (!confirm('Deseja remover este comunicado do mural?')) return;
  try {
    await excluirAvisoApi(id);
    mostrarToast('Comunicado removido.');
    await carregarAvisos();
    atualizarDashboard();
    renderizarAvisos();
  } catch (error) {
    mostrarToast(tratarErroApi(error, 'Não foi possível remover o aviso.'));
  }
}

function renderizarAvisos() {
  const container = document.getElementById('listaAvisos');
  if (state.avisos.length === 0) {
    container.innerHTML = '<div class="empty-state">Nenhum comunicado no momento.</div>';
    return;
  }

  container.innerHTML = state.avisos.map((av) => `
    <div class="aviso-card">
      <div class="aviso-header">
        <strong>${escapeHtml(av.titulo)}</strong>
        <span class="tag tag-${classePrioridade(av.prioridade)}">${labelPrioridade(av.prioridade)}</span>
      </div>
      <p>${escapeHtml(av.mensagem)}</p>
      <div class="aviso-footer">
        <span class="aviso-data">${formatarData(av.criadoEm)}</span>
        ${isAdmin() ? `<button type="button" class="btn-icon" data-excluir-aviso="${av.id}" title="Remover comunicado">🗑</button>` : ''}
      </div>
    </div>
  `).join('');
}

function abrirModal(id) {
  document.getElementById(id).classList.remove('hidden');
}

function fecharModal(id) {
  document.getElementById(id).classList.add('hidden');
}

function fecharModalFora(event, id) {
  if (event.target === document.getElementById(id)) fecharModal(id);
}

function limparFormulario(modalId) {
  document.querySelectorAll(`#${modalId} input, #${modalId} textarea, #${modalId} select`).forEach((el) => {
    if (el.tagName === 'SELECT') el.selectedIndex = 0;
    else el.value = '';
  });
}

function escapeHtml(texto) {
  return String(texto ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function tratarErroApi(error, fallback) {
  if (error instanceof ApiError) {
    if (error.status === 403) return 'Você não tem permissão para esta ação.';
    if (error.erro === 'SERVICO_INDISPONIVEL' || error.status === 503) {
      return 'O servidor está temporariamente indisponível. Aguarde alguns segundos e tente novamente.';
    }
    return error.message;
  }
  return fallback;
}

function mostrarToast(msg) {
  const toast = document.getElementById('toast');
  toast.innerText = msg;
  toast.classList.remove('hidden');
  setTimeout(() => toast.classList.add('hidden'), 3200);
}

function registrarEventos() {
  document.getElementById('btnLoginAdmin').addEventListener('click', autenticarAdmin);
  document.getElementById('btnEntrar').addEventListener('click', () => {
    limparErroLogin();
    abrirModal('modalLogin');
  });
  document.getElementById('btnConfig').addEventListener('click', () => nav('configuracoes'));
  document.getElementById('btnLogout').addEventListener('click', logout);
  document.getElementById('btnSalvarMembro').addEventListener('click', salvarMembro);
  document.getElementById('btnSalvarEvento').addEventListener('click', salvarEvento);
  document.getElementById('btnSalvarAviso').addEventListener('click', salvarAviso);
  document.getElementById('buscaMembro').addEventListener('input', filtrarMembros);
  document.getElementById('btnAbrirRecuperacao').addEventListener('click', () => abrirModal('modalRecuperacao'));
  document.getElementById('btnEnviarRecuperacao').addEventListener('click', enviarRecuperacaoSenha);
  document.getElementById('btnAdicionarEscala').addEventListener('click', adicionarMembroEscala);

  document.getElementById('btnMembrosAnterior').addEventListener('click', () => {
    if (membrosPagina.page > 0) {
      membrosPagina.page -= 1;
      carregarMembros({ silencioso: false });
    }
  });
  document.getElementById('btnMembrosProximo').addEventListener('click', () => {
    if (membrosPagina.page < membrosPagina.totalPages - 1) {
      membrosPagina.page += 1;
      carregarMembros({ silencioso: false });
    }
  });

  document.querySelectorAll('[data-page]').forEach((btn) => {
    btn.addEventListener('click', () => nav(btn.dataset.page));
  });
  document.querySelectorAll('[data-modal]').forEach((btn) => {
    btn.addEventListener('click', () => abrirModal(btn.dataset.modal));
  });
  document.querySelectorAll('[data-close]').forEach((btn) => {
    btn.addEventListener('click', () => fecharModal(btn.dataset.close));
  });
  document.querySelectorAll('.modal-overlay').forEach((modal) => {
    modal.addEventListener('click', (event) => fecharModalFora(event, modal.id));
  });

  document.getElementById('listaMembros').addEventListener('click', (e) => {
    const btn = e.target.closest('[data-excluir-membro]');
    if (btn) excluirMembro(Number(btn.dataset.excluirMembro));
  });

  document.getElementById('listaEventos').addEventListener('click', (e) => {
    if (e.target.closest('[data-excluir-evento]')) {
      excluirEvento(Number(e.target.closest('[data-excluir-evento]').dataset.excluirEvento));
    }
    if (e.target.closest('[data-ver-escala]')) {
      mostrarEscalasInline(Number(e.target.closest('[data-ver-escala]').dataset.verEscala));
    }
    if (e.target.closest('[data-gerenciar-escala]')) {
      abrirModalEscala(Number(e.target.closest('[data-gerenciar-escala]').dataset.gerenciarEscala));
    }
  });

  document.getElementById('listaEscalas').addEventListener('click', (e) => {
    const btn = e.target.closest('[data-remover-escala]');
    if (btn) removerMembroEscala(Number(btn.dataset.removerEscala));
  });

  document.getElementById('listaAvisos').addEventListener('click', (e) => {
    const btn = e.target.closest('[data-excluir-aviso]');
    if (btn) excluirAviso(Number(btn.dataset.excluirAviso));
  });

  document.getElementById('themeGrid')?.addEventListener('click', (e) => {
    const btn = e.target.closest('[data-theme-select]');
    if (btn) selecionarTema(btn.dataset.themeSelect);
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-overlay:not(.hidden)').forEach((m) => m.classList.add('hidden'));
    }
  });
}

document.title = config.appName;
registrarEventos();
iniciarApp();

console.info(`[ProjetoMTA] API: ${config.apiBaseUrl} (configure em frontend/.env)`);
