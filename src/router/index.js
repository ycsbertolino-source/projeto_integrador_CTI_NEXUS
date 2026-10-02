import { createRouter, createWebHistory } from 'vue-router'

// Layouts e Páginas Públicas
import HomeView from '../views/Home.vue'
import LoginView from '../views/Login.vue'
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
    component: HomeView 
  },
  { 
    path: '/login', 
    name: 'login', 
    component: LoginView 
  },
  // Rota Pai do Painel Interno (Contém a Sidebar e o Header)
  {
    path: '/app',
    component: AppLayout,
    redirect: '/app/upload',
    children: [
      { path: 'dashboard', name: 'dashboard', component: Dashboard },
      { path: 'upload', name: 'upload', component: Upload },
      { path: 'relatorio', name: 'relatorio', component: Relatorio },
      { path: 'relatorios', name: 'relatorios', component: Relatorios },
      { path: 'graficos', name: 'statistics', component: Statistics },
      { path: 'usuarios', name: 'users', component: Users },
      { path: 'usuarios/novo', name: 'user-create', component: UserCreate },
      { path: 'configuracoes', name: 'settings', component: Settings }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router