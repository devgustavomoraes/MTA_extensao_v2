import './styles/tokens.css';
import './styles/main.css';
import { carregarTema, aplicarTema } from './config/theme.js';
import { registerSW } from 'virtual:pwa-register';
import './js/app.js';

aplicarTema(carregarTema());

if ('serviceWorker' in navigator) {
  registerSW({
    immediate: true,
    onRegistered() {
      console.info('[PWA] Service Worker registrado.');
    },
    onRegisterError(error) {
      console.error('[PWA] Falha ao registrar Service Worker:', error);
    }
  });
}
