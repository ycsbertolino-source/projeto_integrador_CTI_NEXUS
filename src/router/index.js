import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

// Layouts e Páginas Públicas
import HomeView from '../views/Home.vue'
import LoginView from '../views/Login.vue'
import CadastroView from '../views/Cadastro.vue'
import AppLayout from '../Layouts/AppLayout.vue'

// Layout e Páginas do Painel Interno
import Upload from '../views/Upload.vue'
import Dashboard from '../views/Dashboard.vue'
import Statistics from '../views/Statistics.vue'
import Users from '../views/Users.vue'
import UserCreate from '../views/UserCreate.vue'
import Settings from '../views/Settings.vue'
import Relatorio from '../views/Relatorio.vue'
import Relatorios from '../views/Relatorios.vue'

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
      { path: 'dashboard', name: 'dashboard', component: Dashboard, meta: { requiresAuth: true } },
      { path: 'upload', name: 'upload', component: Upload, meta: { requiresAuth: true } },
      { path: 'relatorio', name: 'relatorio', component: Relatorio, meta: { requiresAuth: true } },
      { path: 'relatorios', name: 'relatorios', component: Relatorios, meta: { requiresAuth: true } },
      { path: 'graficos', name: 'statistics', component: Statistics, meta: { requiresAuth: true } },
      { path: 'configuracoes', name: 'settings', component: Settings, meta: { requiresAuth: true } },

      { path: 'usuarios', name: 'users', component: Users, meta: { requiresAuth: true, admin: true } },
      { path: 'usuarios/novo', name: 'user-create', component: UserCreate, meta: { requiresAuth: true, admin: true } },
      { path: 'usuarios/:id/editar', name: 'user-edit', component: UserCreate, meta: { requiresAuth: true, admin: true } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  authStore.checkAuth()

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