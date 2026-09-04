import { createRouter, createWebHistory } from 'vue-router'

// Layouts e Páginas Públicas
import HomeView from '../views/Home.vue'
import LoginView from '../views/Login.vue'
import AppLayout from '../layouts/AppLayout.vue'

// Layout e Páginas do Painel Interno
import Upload from '../views/UploadView.vue'

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
  {
  path: 'upload',
  name: 'upload',
  component: Upload
},


  // Rota Pai do Painel Interno (Contém a Sidebar e o Header)
  {
    path: '/app',
    component: AppLayout,
    redirect: '/app/upload',
    children: [
      // Adicione as rotas filhas do painel aqui (ex: upload)
      // { path: 'upload', name: 'upload', component: UploadView }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router