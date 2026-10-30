import axios from 'axios' // Biblioteca HTTP

export const TOKEN_KEY = 'cti_token' // Onde o token JWT fica salvo no navegador

const api = axios.create({ // Cria uma instância reutilizável
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080', // Endereço do backend (Spring Boot)
  headers: { 'Content-Type': 'application/json' }, // Envia JSON
})

api.interceptors.request.use((config) => { // Executa antes de cada requisição
  const token = localStorage.getItem(TOKEN_KEY) // Busca o token salvo
  if (token) config.headers.Authorization = 'Bearer ' + token // Envia Bearer Token
  return config // Continua a requisição
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Token vencido ou inválido: apaga e volta para o login
    const rotaDeLogin = String(error.config?.url || '').includes('/api/auth/login')
    if (error.response?.status === 401 && !rotaDeLogin) {
      localStorage.removeItem(TOKEN_KEY)
      if (window.location.pathname.startsWith('/app')) window.location.assign('/login')
    }
    return Promise.reject(error)
  }
)

// Transforma o erro do backend ({"erro": "...", "campos": {...}}) em uma frase para a tela
export function mensagemDeErro(error, padrao = 'Não foi possível concluir a operação.') {
  if (!error?.response) {
    return 'Não foi possível conectar ao servidor. Verifique se o backend está ligado.'
  }
  const dados = error.response.data || {}
  const campos = dados.campos ? Object.values(dados.campos) : []
  if (campos.length) return campos.join(' ')
  return dados.erro || padrao
}

export default api // Permite usar api em outras telas
