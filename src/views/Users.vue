<script setup>
import { ref, computed } from 'vue'

const search = ref('')
const status = ref('Todos')
const users = ref([
  { name: 'Yasmin Cristina', email: 'yasmin@ctinexus.com', role: 'Administrador', status: 'Ativo', lastAccess: 'Hoje, 09:42' },
  { name: 'Ana Paula Souza', email: 'ana@ctinexus.com', role: 'Analista', status: 'Ativo', lastAccess: 'Hoje, 08:16' },
  { name: 'Carlos Mendes', email: 'carlos@ctinexus.com', role: 'Consultor', status: 'Ativo', lastAccess: 'Ontem, 17:30' },
  { name: 'Maria Oliveira', email: 'maria@ctinexus.com', role: 'Consultor', status: 'Pendente', lastAccess: 'Nunca' },
])
const filteredUsers = computed(() => users.value.filter(user => (status.value === 'Todos' || user.status === status.value) && `${user.name} ${user.email}`.toLowerCase().includes(search.value.toLowerCase())))
</script>

<template>
  <section class="page-container"><header class="page-header"><div>
    <p class="eyebrow">Administracao</p><h1>Usuarios</h1><p class="subtitle">Gerencie acessos, funcoes e permissoes da equipe.</p></div><router-link class="primary-button" to="/app/usuarios/novo">Novo usuario</router-link></header>
    <div class="toolbar"><input v-model="search" type="search" placeholder="Buscar por nome ou e-mail" aria-label="Buscar usuarios" /><select v-model="status" aria-label="Filtrar por status"><option>Todos</option><option>Ativo</option><option>Pendente</option></select></div>
    <div class="table-panel"><table><thead><tr><th>Usuario</th><th>Funcao</th><th>Status</th><th>Ultimo acesso</th><th></th></tr></thead><tbody><tr v-for="user in filteredUsers" :key="user.email"><td><strong>{{ user.name }}</strong><span>{{ user.email }}</span></td><td>{{ user.role }}</td><td><b :class="['status', user.status.toLowerCase()]">{{ user.status }}</b></td><td>{{ user.lastAccess }}</td><td><button class="more-button" type="button" aria-label="Mais opcoes">...</button>
    </td></tr></tbody></table><p v-if="!filteredUsers.length" class="empty">Nenhum usuario encontrado.</p></div>
  </section>
</template>

<style scoped>
.page-container {
  max-width: 1280px;
  margin: 0 auto;
  min-height: 100vh;
  background: #050b14;
  color: #e2e8f0;
  padding: 32px 20px;
}.page-header 
{ display: flex; justify-content: space-between; align-items: end; gap: 16px; margin-bottom: 28px; }.eyebrow { margin: 0 0 8px; color: #60a5fa; font-size: .72rem; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }h1 { margin: 0; color: #f8fafc; font-size: clamp(1.8rem, 4vw, 2.35rem); }.subtitle { color: #94a3b8; margin: 8px 0 0; }.primary-button { display: inline-block; border-radius: 8px; padding: 11px 16px; background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; font-weight: 700; text-decoration: none; }.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }.toolbar input, .toolbar select { min-height: 42px; border: 1px solid rgba(148, 163, 184, 0.25); border-radius: 8px; padding: 0 12px; background: rgba(15, 23, 42, 0.8); color: #e2e8f0; }.toolbar input { flex: 1; }.table-panel { overflow-x: auto; background: #0f172a; border: 1px solid rgba(148, 163, 184, 0.18); border-radius: 12px; box-shadow: 0 10px 30px rgba(2, 6, 23, 0.45); }table { width: 100%; min-width: 700px; border-collapse: collapse; text-align: left; }th, td { padding: 16px 20px; border-bottom: 1px solid rgba(148, 163, 184, 0.12); color: #cbd5e1; font-size: .84rem; }th { color: #94a3b8; font-size: .72rem; text-transform: uppercase; letter-spacing: .08em; }td:first-child { display: grid; gap: 4px; }td strong { color: #f8fafc; }td span { color: #94a3b8; font-size: .75rem; }.status { width: max-content; padding: 4px 8px; border-radius: 999px; font-size: .7rem; }.ativo { color: #a7f3d0; background: rgba(16, 185, 129, 0.18); }.pendente { color: #fde68a; background: rgba(245, 158, 11, 0.18); }.more-button { border: 0; background: transparent; color: #cbd5e1; font-weight: 700; cursor: pointer; }.empty { padding: 28px; text-align: center; color: #94a3b8; }@media (max-width: 600px) { .page-header { align-items: start; flex-direction: column; }.primary-button { width: 100%; text-align: center; }.toolbar { flex-direction: column; } }
</style>