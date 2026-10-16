import { createApp } from 'vue' 
import { createPinia } from 'pinia' 
import App from './App.vue' // Componente principal
import router from './router' // Importa as rotas
const app = createApp(App) // Cria a aplicação
app.use(createPinia()) // Ativa o Pinia
app.use(router) // Ativa o Vue Router
app.mount('#app') // Exibe a aplicação no index.htm

import axios from 'axios' // Biblioteca HTTP
const api = axios.create({ // Cria uma instância reutilizável
baseURL: 'http://localhost:8080', // Endereço do backend
headers: { 'Content-Type': 'application/json' } // Envia JSON
})
api.interceptors.request.use((config) => { // Executa antes de cada requisição
const token = localStorage.getItem('token') // Busca o token salvo
if (token) config.headers.Authorization = 'Bearer ' + token // Envia Bearer Token
return config // Continua a requisição
})
export default api // Permite usar api em outras telas
