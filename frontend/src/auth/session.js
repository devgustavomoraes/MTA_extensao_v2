const TOKEN_KEY = 'mta_token';
const USER_KEY = 'mta_user';

export function salvarSessao(loginResponse) {
  sessionStorage.setItem(TOKEN_KEY, loginResponse.token);
  sessionStorage.setItem(USER_KEY, JSON.stringify({
    id: loginResponse.id,
    email: loginResponse.email,
    perfil: loginResponse.perfil
  }));
}

export function obterToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function obterUsuario() {
  const raw = sessionStorage.getItem(USER_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function limparSessao() {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(USER_KEY);
}

export function sessaoAtiva() {
  return Boolean(obterToken());
}

export function isAdmin() {
  const usuario = obterUsuario();
  return usuario?.perfil === 'ADMIN';
}
