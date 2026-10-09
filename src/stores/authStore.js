import { defineStore } from 'pinia'
import { useUploadStore } from '@/store/uploadStore'

const STORAGE_KEY = 'cti_auth_user'
const USERS_STORAGE_KEY = 'cti_users'

const defaultUsers = [
  {
    id: 1,
    name: 'Administrador',
    email: 'admin@ctinexus.com',
    password: '123456',
    role: 'admin',
    status: 'Ativo',
    lastAccess: 'Hoje',
  },
  {
    id: 2,
    name: 'Usuário Teste',
    email: 'usuario@ctinexus.com',
    password: '123456',
    role: 'user',
    status: 'Ativo',
    lastAccess: 'Hoje',
  },
    {
    id: 3,
    name: 'Yasmin',
    email: 'ycsbertolino@gmail.com',
    password: 'ya020408',
    role: 'user',
    status: 'Ativo',
    lastAccess: 'Hoje',
  },

]

const normalizeUser = (user) => ({
  id: user.id || Date.now() + Math.random(),
  name: String(user.name || '').trim(),
  email: String(user.email || '').trim().toLowerCase(),
  password: String(user.password || ''),
  role: user.role === 'admin' ? 'admin' : 'user',
  status: user.status || 'Ativo',
  lastAccess: user.lastAccess || 'Nunca',
})

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    loading: true,
    authError: '',
    users: [],
  }),

  actions: {
    loadUsers() {
      if (typeof window === 'undefined') {
        this.users = defaultUsers.map(normalizeUser)
        return this.users
      }

      const savedUsers = localStorage.getItem(USERS_STORAGE_KEY)

      if (!savedUsers) {
        this.users = defaultUsers.map(normalizeUser)
        localStorage.setItem(USERS_STORAGE_KEY, JSON.stringify(this.users))
        return this.users
      }

      try {
        const parsed = JSON.parse(savedUsers)
        this.users = Array.isArray(parsed) ? parsed.map(normalizeUser) : defaultUsers.map(normalizeUser)
      } catch (error) {
        console.error('Erro ao carregar usuários:', error)
        this.users = defaultUsers.map(normalizeUser)
      }

      return this.users
    },

    persistUsers() {
      if (typeof window !== 'undefined') {
        localStorage.setItem(USERS_STORAGE_KEY, JSON.stringify(this.users))
      }
    },

    async login({ email, password }) {
      this.loading = true
      this.authError = ''

      await new Promise((resolve) => setTimeout(resolve, 300))

      const foundUser = this.users.find(
        (user) => user.email.trim().toLowerCase() === String(email).trim().toLowerCase()
      )

      if (!foundUser || foundUser.password !== String(password)) {
        this.user = null
        this.authError = 'E-mail ou senha inválidos.'
        this.loading = false

        if (typeof window !== 'undefined') {
          localStorage.removeItem(STORAGE_KEY)
        }

        return false
      }

      foundUser.lastAccess = new Date().toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })

      this.persistUsers()

      this.user = {
        id: foundUser.id,
        name: foundUser.name,
        email: foundUser.email,
        role: foundUser.role,
      }

      if (typeof window !== 'undefined') {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(this.user))
      }

      const uploadStore = useUploadStore()
      uploadStore.syncUserHistory()

      this.loading = false
      return true
    },

    logout() {
      this.user = null
      this.authError = ''
      this.loading = false

      if (typeof window !== 'undefined') {
        localStorage.removeItem(STORAGE_KEY)
      }

      const uploadStore = useUploadStore()
      uploadStore.historico = []
      uploadStore.syncUserHistory()
    },

    checkAuth() {
      this.loading = true
      this.loadUsers()

      try {
        if (typeof window === 'undefined') {
          this.user = null
          this.loading = false
          return false
        }

        const savedUser = localStorage.getItem(STORAGE_KEY)

        if (!savedUser) {
          this.user = null
          this.loading = false
          return false
        }

        const parsedUser = JSON.parse(savedUser)

        if (parsedUser && parsedUser.email) {
          const storedUser = this.users.find((user) => user.email === parsedUser.email)

          if (storedUser) {
            this.user = {
              id: storedUser.id,
              name: storedUser.name,
              email: storedUser.email,
              role: storedUser.role,
            }

            const uploadStore = useUploadStore()
            uploadStore.syncUserHistory()

            this.loading = false
            return true
          }
        }

        localStorage.removeItem(STORAGE_KEY)
        this.user = null
        this.loading = false
        return false
      } catch (error) {
        console.error('Erro ao verificar autenticação:', error)
        this.user = null
        this.loading = false
        return false
      }
    },

    getUserById(userId) {
      return this.users.find((user) => Number(user.id) === Number(userId)) || null
    },

    registerUser(payload) {
      const data = {
        name: String(payload?.name || '').trim(),
        email: String(payload?.email || '').trim().toLowerCase(),
        password: String(payload?.password || '').trim(),
        role: payload?.role === 'admin' ? 'admin' : 'user',
        status: payload?.status || 'Ativo',
      }

      if (!data.name || !data.email || !data.password) {
        return { ok: false, message: 'Preencha nome, e-mail e senha.' }
      }

      const exists = this.users.some((user) => user.email === data.email)

      if (exists) {
        return { ok: false, message: 'Já existe um usuário com este e-mail.' }
      }

      const newUser = normalizeUser({
        ...data,
        id: Date.now(),
        lastAccess: 'Nunca',
      })

      this.users.push(newUser)
      this.persistUsers()

      return { ok: true, user: newUser }
    },

    updateUser(userId, payload) {
      const targetUser = this.getUserById(userId)

      if (!targetUser) {
        return { ok: false, message: 'Usuário não encontrado.' }
      }

      const nextData = {
        name: String(payload?.name || targetUser.name).trim(),
        email: String(payload?.email || targetUser.email).trim().toLowerCase(),
        password: payload && payload.password !== undefined ? String(payload.password).trim() : targetUser.password,
        role: payload?.role === 'admin' ? 'admin' : 'user',
        status: payload?.status || targetUser.status,
      }

      if (!nextData.name || !nextData.email || !nextData.password) {
        return { ok: false, message: 'Nome, e-mail e senha são obrigatórios.' }
      }

      const duplicate = this.users.some(
        (user) => Number(user.id) !== Number(userId) && user.email === nextData.email
      )

      if (duplicate) {
        return { ok: false, message: 'Já existe um usuário com este e-mail.' }
      }

      const mergedUser = normalizeUser({
        ...targetUser,
        ...nextData,
        id: targetUser.id,
      })

      this.users = this.users.map((user) =>
        Number(user.id) === Number(userId) ? mergedUser : user
      )

      this.persistUsers()

      if (this.user && Number(this.user.id) === Number(userId)) {
        this.user = {
          id: mergedUser.id,
          name: mergedUser.name,
          email: mergedUser.email,
          role: mergedUser.role,
        }

        if (typeof window !== 'undefined') {
          localStorage.setItem(STORAGE_KEY, JSON.stringify(this.user))
        }
      }

      return { ok: true, user: mergedUser }
    },

    deleteUser(userId) {
      const adminUsers = this.users.filter((user) => user.role === 'admin')

      if (adminUsers.length <= 1 && this.getUserById(userId)?.role === 'admin') {
        return { ok: false, message: 'Não é possível remover o último administrador.' }
      }

      const nextUsers = this.users.filter((user) => user.id !== userId)
      this.users = nextUsers
      this.persistUsers()

      if (this.user && this.user.id === userId) {
        this.logout()
      }

      return { ok: true }
    },
  },
})
