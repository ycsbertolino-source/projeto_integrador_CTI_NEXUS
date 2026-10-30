import { defineStore } from 'pinia'
import api, { TOKEN_KEY, mensagemDeErro } from '@/services/api'
import { useUploadStore } from '@/store/uploadStore'

// O backend usa APROVADO/PENDENTE/BLOQUEADO; as telas mostram Ativo/Pendente/Bloqueado
const STATUS_PARA_TELA = { APROVADO: 'Ativo', PENDENTE: 'Pendente', BLOQUEADO: 'Bloqueado' }
const STATUS_PARA_API = { Ativo: 'APROVADO', Pendente: 'PENDENTE', Bloqueado: 'BLOQUEADO' }

// UsuarioDto do backend -> formato usado nas telas
const paraTela = (dto) => ({
  id: dto.id,
  name: dto.nome,
  email: dto.email,
  role: dto.perfil === 'ADMIN' ? 'admin' : 'user',
  status: STATUS_PARA_TELA[dto.status] || dto.status,
  matricula: dto.matricula,
  turma: dto.turma,
  criadoEm: dto.criadoEm,
})

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    loading: false,
    authError: '',
    users: [],
    verificado: false, // já perguntou ao backend quem é o usuário?
  }),

  actions: {
    // POST /api/auth/login -> guarda o token JWT
    async login({ email, password }) {
      this.loading = true
      this.authError = ''

      try {
        const { data } = await api.post('/api/auth/login', {
          email: String(email || '').trim(),
          senha: password,
        })
        localStorage.setItem(TOKEN_KEY, data.token)
        this.user = paraTela(data.usuario)
        this.verificado = true
        useUploadStore().syncUserHistory()
        return true
      } catch (error) {
        this.user = null
        this.authError = mensagemDeErro(error, 'Erro ao autenticar. Verifique suas credenciais.')
        return false
      } finally {
        this.loading = false
      }
    },

    logout() {
      localStorage.removeItem(TOKEN_KEY)
      this.user = null
      this.users = []
      this.authError = ''
      this.verificado = true

      const uploadStore = useUploadStore()
      uploadStore.historico = []
      uploadStore.syncUserHistory()
    },

    // GET /api/auth/me -> descobre quem é o dono do token (só uma vez por carregamento)
    async checkAuth() {
      if (this.verificado) return Boolean(this.user)

      const token = localStorage.getItem(TOKEN_KEY)
      if (!token) {
        this.user = null
        this.verificado = true
        return false
      }

      this.loading = true
      try {
        const { data } = await api.get('/api/auth/me')
        this.user = paraTela(data)
        useUploadStore().syncUserHistory()
      } catch (error) {
        this.user = null
        if ([401, 403].includes(error.response?.status)) localStorage.removeItem(TOKEN_KEY)
      } finally {
        this.loading = false
        this.verificado = true
      }
      return Boolean(this.user)
    },

    // POST /api/auth/register -> o usuário nasce PENDENTE
    async registerUser({ name, email, password, matricula, turma }) {
      this.loading = true
      try {
        const { data } = await api.post('/api/auth/register', {
          nome: String(name || '').trim(),
          email: String(email || '').trim().toLowerCase(),
          senha: password,
          matricula: matricula || null,
          turma: turma || null,
        })
        return { ok: true, message: data.mensagem }
      } catch (error) {
        return { ok: false, message: mensagemDeErro(error, 'Não foi possível concluir o cadastro.') }
      } finally {
        this.loading = false
      }
    },

    // GET /api/admin/usuarios (só ADMIN)
    async loadUsers() {
      try {
        const { data } = await api.get('/api/admin/usuarios')
        this.users = data.map(paraTela)
        return { ok: true }
      } catch (error) {
        return { ok: false, message: mensagemDeErro(error, 'Não foi possível carregar os usuários.') }
      }
    },

    getUserById(userId) {
      return this.users.find((user) => Number(user.id) === Number(userId)) || null
    },

    // PATCH /api/admin/usuarios/{id}/status -> aprova ou bloqueia
    async alterarStatus(userId, status) {
      try {
        const { data } = await api.patch(`/api/admin/usuarios/${userId}/status`, {
          status: STATUS_PARA_API[status] || status,
        })
        const atualizado = paraTela(data)
        this.users = this.users.map((user) => (user.id === atualizado.id ? atualizado : user))
        return { ok: true, user: atualizado }
      } catch (error) {
        return { ok: false, message: mensagemDeErro(error, 'Não foi possível alterar o status.') }
      }
    },

    // Admin cria um usuário: cadastra (nasce PENDENTE) e, se pedido, já aprova
    async criarUsuario({ name, email, password, status }) {
      const cadastro = await this.registerUser({ name, email, password })
      if (!cadastro.ok || status !== 'Ativo') return cadastro

      await this.loadUsers()
      const novo = this.users.find((user) => user.email === String(email).trim().toLowerCase())
      if (!novo) return { ok: false, message: 'Usuário criado, mas não foi possível aprová-lo agora.' }
      return this.alterarStatus(novo.id, 'Ativo')
    },
  },
})
