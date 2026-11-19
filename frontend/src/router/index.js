import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

// Home, Login e Cadastro entram no pacote principal (são as primeiras telas abertas).
import HomeView from '../views/Home.vue'
import LoginView from '../views/Login.vue'
import CadastroView from '../views/Cadastro.vue'

// O painel interno só é baixado quando o usuário entra nele (deixa o primeiro carregamento mais leve).
const AppLayout = () => import('../Layouts/AppLayout.vue')
const Upload = () => import('../views/Upload.vue')
const DashboardView = () => import('../views/DashboardView.vue') // Gráficos: lê o banco (Azure/Neon)
const Users = () => import('../views/Users.vue')
const UserCreate = () => import('../views/UserCreate.vue')
const Settings = () => import('../views/Settings.vue')
const Relatorio = () => import('../views/Relatorio.vue')
const Relatorios = () => import('../views/Relatorios.vue')

const routes = [
  // Páginas Públicas
  {
    path: '/',
    name: 'home',
    component: HomeView,
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },
  {
    path: '/cadastro',
    name: 'cadastro',
    component: CadastroView,
  },
  // Rota Pai do Painel Interno (Contém a Sidebar e o Header)
  {
    path: '/app',
    component: AppLayout,
    redirect: '/app/upload',
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', name: 'dashboard', component: DashboardView, meta: { requiresAuth: true } }, // Gráficos
      { path: 'upload', name: 'upload', component: Upload, meta: { requiresAuth: true } },
      { path: 'relatorio', name: 'relatorio', component: Relatorio, meta: { requiresAuth: true } },
      { path: 'relatorios', name: 'relatorios', component: Relatorios, meta: { requiresAuth: true } },
      { path: 'graficos', redirect: { name: 'dashboard' } }, // endereço antigo da tela Estatísticas
      { path: 'configuracoes', name: 'settings', component: Settings, meta: { requiresAuth: true } },

      { path: 'usuarios', name: 'users', component: Users, meta: { requiresAuth: true, admin: true } },
      { path: 'usuarios/novo', name: 'user-create', component: UserCreate, meta: { requiresAuth: true, admin: true } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  await authStore.checkAuth()

  const user = authStore.user
  const needsAuth = Boolean(to.meta?.requiresAuth)
  const adminOnly = Boolean(to.meta?.admin)

  if (needsAuth && !user) {
    next({ name: 'login' })
    return
  }

  if (adminOnly && user?.role !== 'admin') {
    next({ name: 'upload' })
    return
  }

  if (to.name === 'login' && user) {
    next({ name: 'upload' })
    return
  }

  next()
})

export default router