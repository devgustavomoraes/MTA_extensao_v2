let membros = JSON.parse(localStorage.getItem("membros")) || [];
let eventos = JSON.parse(localStorage.getItem("eventos")) || [];
let avisos = JSON.parse(localStorage.getItem("avisos")) || [];

let editIndex = null;

/* TOAST */
function toast(msg) {
  let t = document.getElementById("toast");
  t.innerText = msg;
  t.style.display = "block";
  setTimeout(() => t.style.display = "none", 2000);
}

/* LOGIN */
function login() {
  let email = document.getElementById("email").value;
  let senha = document.getElementById("senha").value;

  if (email === "matheuslindo" && senha === "123") {
    document.getElementById("loginPage").classList.add("hidden");
    document.getElementById("app").classList.remove("hidden");
    updateDashboard();
  } else toast("Login inválido");
}

function logout() { location.reload(); }

function toggleRecovery() {
  document.getElementById("loginPage").classList.toggle("hidden");
  document.getElementById("recoveryPage").classList.toggle("hidden");
}

function recover() { toast("Email enviado!"); }

/* NAV */
function nav(id) {
  document.querySelectorAll("main section").forEach(s => s.classList.add("hidden"));
  document.getElementById(id).classList.remove("hidden");
}

/* MEMBROS */
function salvarMembro() {
  let nome = document.getElementById("nome").value;
  let funcao = document.getElementById("funcao").value;

  if (!nome || !funcao) return toast("Preencha tudo");

  if (editIndex !== null) {
    membros[editIndex] = { nome, funcao };
    editIndex = null;
  } else {
    membros.push({ nome, funcao });
  }

  localStorage.setItem("membros", JSON.stringify(membros));
  renderMembros();
  updateDashboard();
}

function renderMembros() {
  let tbody = document.getElementById("tabelaMembros");
  let search = document.getElementById("search").value.toLowerCase();

  tbody.innerHTML = "";

  membros
    .filter(m => m.nome.toLowerCase().includes(search))
    .forEach((m, i) => {
      tbody.innerHTML += `
        <tr>
          <td>${m.nome}</td>
          <td>${m.funcao}</td>
          <td>
            <button onclick="editarMembro(${i})">Editar</button>
            <button onclick="excluirMembro(${i})">X</button>
          </td>
        </tr>
      `;
    });
}

function editarMembro(i) {
  document.getElementById("nome").value = membros[i].nome;
  document.getElementById("funcao").value = membros[i].funcao;
  editIndex = i;
}

function excluirMembro(i) {
  membros.splice(i, 1);
  localStorage.setItem("membros", JSON.stringify(membros));
  renderMembros();
  updateDashboard();
}

/* EVENTOS */
function salvarEvento() {
  let nome = document.getElementById("eventoNome").value;
  let data = document.getElementById("eventoData").value;

  if (!nome || !data) return toast("Preencha tudo");

  eventos.push({ nome, data });
  localStorage.setItem("eventos", JSON.stringify(eventos));
  renderEventos();
  updateDashboard();
}

function renderEventos() {
  let lista = document.getElementById("listaEventos");
  lista.innerHTML = "";

  eventos.forEach((e, i) => {
    lista.innerHTML += `
      <li>${e.nome} - ${e.data}
      <button onclick="deleteEvento(${i})">X</button></li>`;
  });
}

function deleteEvento(i) {
  eventos.splice(i, 1);
  localStorage.setItem("eventos", JSON.stringify(eventos));
  renderEventos();
  updateDashboard();
}

/* AVISOS */
function salvarAviso() {
  let texto = document.getElementById("avisoTexto").value;
  if (!texto) return toast("Digite algo");

  avisos.push(texto);
  localStorage.setItem("avisos", JSON.stringify(avisos));
  renderAvisos();
  updateDashboard();
}

function renderAvisos() {
  let lista = document.getElementById("listaAvisos");
  lista.innerHTML = "";

  avisos.forEach((a, i) => {
    lista.innerHTML += `
      <li>${a}
      <button onclick="deleteAviso(${i})">X</button></li>`;
  });
}

function deleteAviso(i) {
  avisos.splice(i, 1);
  localStorage.setItem("avisos", JSON.stringify(avisos));
  renderAvisos();
  updateDashboard();
}

/* DASHBOARD */
function updateDashboard() {
  document.getElementById("totalMembros").innerText = membros.length;
  document.getElementById("totalEventos").innerText = eventos.length;
  document.getElementById("totalAvisos").innerText = avisos.length;
}

/* INIT */
renderMembros();
renderEventos();
renderAvisos();
updateDashboard();