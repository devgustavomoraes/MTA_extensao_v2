//Estado Global
let userRole = 'visitor';
let calendar;
const state = {
    membros: [],
    eventos: [],
    avisos: [
        {
            id: Date.now(),
            titulo: 'Reunião da Liderança',
            mensagem: 'Sexta-feira às 20h no templo sede. Presença obrigatória para todos os líderes.',
            prioridade: 'urgente',
            data: new Date().toLocaleDateString('pt-BR')
        }
    ]
};
//  AUTENTICAÇÃO

const ADMIN_EMAIL = 'igreja@gmail.com';
const ADMIN_SENHA = 'mta';

function autenticar(role) {
    if (role === 'admin') {
        const email = document.getElementById('email').value.trim();
        const senha = document.getElementById('senha').value;

        if (email !== ADMIN_EMAIL || senha !== ADMIN_SENHA) {
            mostrarToast('E-mail ou senha incorretos.');
            return;
        }

        userRole = 'admin';
    } else {
        userRole = 'visitor';
    }
    configurarAcesso();
    document.getElementById('loginPage').classList.add('hidden');
    document.getElementById('app').classList.remove('hidden');
    inicializarCalendario();
    atualizarDashboard();
    renderizarEventos();
    renderizarAvisos();
}
//  CONFIGURAÇÃO DE ACESSO

function configurarAcesso() {
    const isAdmin = userRole === 'admin';
    document.getElementById('userName').innerText =
        isAdmin ? 'Olá, Administrador!' : 'Olá, Visitante!';
    document.getElementById('userStatus').innerText =
        isAdmin ? 'Gestor Geral' : 'Membro Visitante';
    document.querySelectorAll('.admin-only').forEach(el => {
        el.classList.toggle('hidden', !isAdmin);
    });
}
//  CALENDÁRIO

function inicializarCalendario() {
    const calendarEl = document.getElementById('calendar');
    if (!calendarEl) return;

    // evita duplicar calendário

    if (calendar) {
        calendar.destroy();
    }
    calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'pt-br',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,listWeek'
        },
        buttonText: {
            today: 'Hoje',
            month: 'Mês',
            week: 'Semana'
        },
        events: state.eventos.map(ev => ({
            title: ev.titulo,
            start: ev.data,
            backgroundColor: '#1a365d'
        }))
    });
    calendar.render();
}
function atualizarCalendario() {
    if (!calendar) return;

    calendar.removeAllEvents();

    state.eventos.forEach(ev => {
        calendar.addEvent({
            title: ev.titulo,
            start: ev.data,
            backgroundColor: '#1a365d'
        });
    });
}
function logout() {
    window.location.reload();
}
//  NAVEGAÇÃO

function nav(pageId) {
    document.querySelectorAll('.page').forEach(p => {
        p.classList.add('hidden');
    });
    document.getElementById(pageId).classList.remove('hidden');
    document.querySelectorAll('.tab-item').forEach(btn => {
        btn.classList.remove('active');
    });
    const tabBtn = document.getElementById(`tab-${pageId}`);
    if (tabBtn) {
        tabBtn.classList.add('active');
    }
    if (pageId === 'membros') renderizarMembros();
    if (pageId === 'agenda') {
        renderizarEventos();
        setTimeout(() => {
            if (calendar) {
                calendar.updateSize();
            }
        }, 100);
    }
    if (pageId === 'avisos') renderizarAvisos();
    if (pageId === 'dashboard') atualizarDashboard();
}
//  Painel de Controle

function atualizarDashboard() {
    document.getElementById('statMembros').innerText =
        state.membros.length;
    document.getElementById('statEventos').innerText =
        state.eventos.length;
    document.getElementById('statAvisos').innerText =
        state.avisos.length;
    const container = document.getElementById('listaEventosDash');
    const proximos = state.eventos.slice(0, 3);
    if (proximos.length === 0) {
        container.innerHTML =
            '<div class="empty-state">Nenhum evento cadastrado.</div>';
        return;
    }
    container.innerHTML = proximos.map(ev => `
        <div class="evento-card" style="margin-bottom:8px">
            <div class="evento-header">
                <strong>${ev.titulo}</strong>
                <span class="evento-data">
                    ${formatarData(ev.data)}
                    ${ev.hora ? '• ' + ev.hora : ''}
                </span>
            </div>
            ${ev.local
                ? `<div class="evento-local">📍 ${ev.local}</div>`
                : ''
            }
        </div>
    `).join('');
}
//  MEMBROS

function salvarMembro() {
    const nome =
        document.getElementById('membroNome').value.trim();
    const telefone =
        document.getElementById('membroTelefone').value.trim();
    const email =
        document.getElementById('membroEmail').value.trim();
    const funcao =
        document.getElementById('membroFuncao').value;
    if (!nome) {
        mostrarToast('Informe o nome do membro.');
        return;
    }
    state.membros.push({
        id: Date.now(),
        nome,
        telefone,
        email,
        funcao
    });

    fecharModal('modalMembro');
    limparFormulario('modalMembro');
    mostrarToast('✅ Membro cadastrado com sucesso!');
    atualizarDashboard();
    if (
        !document.getElementById('membros')
            .classList.contains('hidden')
    ) {
        renderizarMembros();
    }
}
function excluirMembro(id) {
    if (!confirm('Deseja remover este membro?')) return;
    state.membros =
        state.membros.filter(m => m.id !== id);
    renderizarMembros();
    atualizarDashboard();
    mostrarToast('Membro removido.');
}
function renderizarMembros(lista = state.membros) {
    const container =
        document.getElementById('listaMembros');
    if (lista.length === 0) {
        container.innerHTML =
            '<div class="empty-state">Nenhum membro encontrado.</div>';

        return;
    }
    container.innerHTML = lista.map(m => `
        <div class="membro-card">
            <div class="membro-avatar">
                ${m.nome.charAt(0).toUpperCase()}
            </div>
            <div class="membro-info">
                <strong>${m.nome}</strong>
                <span>
                    ${m.telefone || m.email || 'Sem contato'}
                </span>
            </div>
            <span class="badge">${m.funcao}</span>
            ${userRole === 'admin'
                ? `
                <div class="membro-actions">
                    <button
                        class="btn-icon"
                        onclick="excluirMembro(${m.id})"
                    >
                        🗑
                    </button>
                </div>
                `
                : ''
            }
        </div>
    `).join('');
}
function filtrarMembros() {
    const q =
        document.getElementById('buscaMembro')
            .value
            .toLowerCase();
    const filtrados = state.membros.filter(m =>
        m.nome.toLowerCase().includes(q) ||
        m.telefone.includes(q)
    );
    renderizarMembros(filtrados);
}
//  EVENTOS

function salvarEvento() {
    const titulo =
        document.getElementById('eventoTitulo').value.trim();
    const data =
        document.getElementById('eventoData').value;
    const hora =
        document.getElementById('eventoHora').value;
    const local =
        document.getElementById('eventoLocal').value.trim();
    const desc =
        document.getElementById('eventoDesc').value.trim();
    if (!titulo) {
        mostrarToast('Informe o título do evento.');
        return;
    }
    if (!data) {
        mostrarToast('Informe a data do evento.');
        return;
    }
    state.eventos.push({
        id: Date.now(),
        titulo,
        data,
        hora,
        local,
        desc
    });
    state.eventos.sort(
        (a, b) => new Date(a.data) - new Date(b.data)
    );
    fecharModal('modalEvento');
    limparFormulario('modalEvento');
    mostrarToast('✅ Evento agendado com sucesso!');
    atualizarDashboard();
    renderizarEventos();
    atualizarCalendario();
}
function excluirEvento(id) {
    if (!confirm('Deseja remover este evento?')) return;
    state.eventos =
        state.eventos.filter(e => e.id !== id);
    renderizarEventos();
    atualizarDashboard();
    atualizarCalendario();
    mostrarToast('Evento removido.');
}
function renderizarEventos() {
    const container =
        document.getElementById('listaEventos');
    if (!container) return;
    if (state.eventos.length === 0) {
        container.innerHTML =
            '<div class="empty-state">Nenhum evento agendado.</div>';
        return;
    }
    container.innerHTML = state.eventos.map(ev => `
        <div class="evento-card">
            <div class="evento-header">
                <strong>${ev.titulo}</strong>
                <span class="evento-data">
                    ${formatarData(ev.data)}
                </span>
            </div>
            ${ev.hora
                ? `<div class="evento-local">🕐 ${ev.hora}</div>`
                : ''
            }
            ${ev.local
                ? `<div class="evento-local">📍 ${ev.local}</div>`
                : ''
            }
            ${ev.desc
                ? `<div class="evento-desc">${ev.desc}</div>`
                : ''
            }
            ${userRole === 'admin'
                ? `
                <div class="evento-footer">
                    <button
                        class="btn-icon"
                        onclick="excluirEvento(${ev.id})"
                    >
                        🗑 Remover
                    </button>
                </div>
                `
                : ''
            }

        </div>
    `).join('');
}
//  AVISOS

function salvarAviso() {
    const titulo =
        document.getElementById('avisoTitulo').value.trim();
    const mensagem =
        document.getElementById('avisoMensagem').value.trim();
    const prioridade =
        document.getElementById('avisoPrioridade').value;
    if (!titulo) {
        mostrarToast('Informe o título do aviso.');
        return;
    }
    if (!mensagem) {
        mostrarToast('Escreva a mensagem do aviso.');
        return;
    }
    state.avisos.unshift({
        id: Date.now(),
        titulo,
        mensagem,
        prioridade,
        data: new Date().toLocaleDateString('pt-BR')
    });
    fecharModal('modalAviso');
    limparFormulario('modalAviso');
    mostrarToast('✅ Aviso publicado!');
    atualizarDashboard();
    renderizarAvisos();
}
function excluirAviso(id) {
    if (!confirm('Deseja remover este aviso?')) return;
    state.avisos =
        state.avisos.filter(a => a.id !== id);
    renderizarAvisos();
    atualizarDashboard();
    mostrarToast('Aviso removido.');
}
function renderizarAvisos() {
    const container =
        document.getElementById('listaAvisos');
    if (state.avisos.length === 0) {
        container.innerHTML =
            '<div class="empty-state">Nenhum comunicado publicado.</div>';
        return;
    }
    container.innerHTML = state.avisos.map(av => `
        <div class="aviso-card">
            <div class="aviso-header">
                <strong>${av.titulo}</strong>
                <span class="tag tag-${av.prioridade}">
                    ${av.prioridade}
                </span>
            </div>
            <p>${av.mensagem}</p>
            <div class="aviso-footer">
                <span class="aviso-data">${av.data}</span>
                ${userRole === 'admin'
                    ? `
                    <button
                        class="btn-icon"
                        onclick="excluirAviso(${av.id})"
                    >
                        🗑
                    </button>
                    `
                    : ''
                }
            </div>

        </div>
    `).join('');
}
//  MODAIS
function abrirModal(id) {
    document.getElementById(id)
        .classList.remove('hidden');
}
function fecharModal(id) {
    document.getElementById(id)
        .classList.add('hidden');
}
function fecharModalFora(event, id) {
    if (event.target === document.getElementById(id)) {
        fecharModal(id);
    }
}
function limparFormulario(modalId) {
    document.querySelectorAll(
        `#${modalId} input,
         #${modalId} textarea,
         #${modalId} select`
    ).forEach(el => {
        if (el.tagName === 'SELECT') {
            el.selectedIndex = 0;
        } else {
            el.value = '';
        }

    });
}
//  UTILITÁRIOS

function formatarData(dataStr) {
    if (!dataStr) return '';
    const [ano, mes, dia] = dataStr.split('-');
    return `${dia}/${mes}/${ano}`;
}
function mostrarToast(msg) {
    const toast = document.getElementById('toast');
    toast.innerText = msg;
    toast.classList.remove('hidden');
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3200);
}
//  TECLADO

document.addEventListener('keydown', e => {
    if (e.key === 'Escape') {
        document
            .querySelectorAll('.modal-overlay:not(.hidden)')
            .forEach(m => {
                m.classList.add('hidden');
            });
    }
});