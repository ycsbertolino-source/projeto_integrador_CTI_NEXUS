import { defineStore } from 'pinia' // Cria uma store Pinia
import api from '../services/api' // Importa o Axios configurado
export const useAuthStore = defineStore('auth', {
state: () => ({ // Dados globais do login
token: localStorage.getItem('token'), // Recupera token após F5
usuario: JSON.parse(localStorage.getItem('usuario') || 'null')
}),
getters: {
logado: (state) => !!state.token, // true quando existe token
isAdmin: (state) => state.usuario?.perfil === 'ADMIN' // controla menu ADMIN
},
actions: {
async login(email, senha) { // Chamada feita pela tela Login
const { data } = await api.post('/auth/login', { email, senha })
this.token = data.token // Guarda token na store
this.usuario = data.usuario // Guarda usuário e perfil
localStorage.setItem('token', data.token)
localStorage.setItem('usuario', JSON.stringify(data.usuario))
},
logout() { // Limpa a sessão no front
this.token = null; this.usuario = null
localStorage.removeItem('token'); localStorage.removeItem('usuario')
}
}
})