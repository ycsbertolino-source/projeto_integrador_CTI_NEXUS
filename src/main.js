import { createApp } from 'vue'
import App from './App.vue' // Importa o componente raiz da aplicação
import { createPinia } from 'pinia'
import router from './router/index.js' // Importa as rotas de src/router/index.js
import './style.css' // Importa o Tailwind CSS (se houver)

function showErrorOverlay(title, message) {
	try {
		let overlay = document.getElementById('dev-error-overlay')
		if (!overlay) {
			overlay = document.createElement('div')
			overlay.id = 'dev-error-overlay'
			overlay.style.position = 'fixed'
			overlay.style.left = '0'
			overlay.style.top = '0'
			overlay.style.right = '0'
			overlay.style.background = '#2b2b2b'
			overlay.style.color = 'white'
			overlay.style.padding = '12px 16px'
			overlay.style.zIndex = '99999'
			overlay.style.fontFamily = 'monospace'
			overlay.style.whiteSpace = 'pre-wrap'
			overlay.style.maxHeight = '40vh'
			overlay.style.overflow = 'auto'
			document.body.appendChild(overlay)
		}
		overlay.innerText = title + "\n\n" + message
	} catch (e) {
		// ignore
	}
}

try {
	const app = createApp(App)

	app.config.errorHandler = (err, vm, info) => {
		console.error('Vue errorHandler:', err, info)
		showErrorOverlay('Vue runtime error', (err && err.stack) || String(err))
	}

	window.addEventListener('error', (ev) => {
		console.error('Window error:', ev.error || ev.message)
		showErrorOverlay('Window error', (ev.error && ev.error.stack) || ev.message || 'Unknown')
	})

	window.addEventListener('unhandledrejection', (ev) => {
		console.error('Unhandled rejection:', ev.reason)
		showErrorOverlay('Unhandled rejection', (ev.reason && ev.reason.stack) || String(ev.reason))
	})

	app.use(createPinia()) // Registra o Pinia na aplicação
	app.use(router) // Registra o Vue Router na aplicação
	app.mount('#app') // Monta a aplicação no elemento com id "app"
	console.log('App mounted')
} catch (err) {
	console.error('Failed to mount app:', err)
	showErrorOverlay('Failed to mount app', (err && err.stack) || String(err))
}
