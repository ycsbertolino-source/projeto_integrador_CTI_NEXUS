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
  <section class="page-container"><header class="page-header"><div><p class="eyebrow">Administracao</p><h1>Usuarios</h1><p class="subtitle">Gerencie acessos, funcoes e permissoes da equipe.</p></div><router-link class="primary-button" to="/app/usuarios/novo">Novo usuario</router-link></header>
    <div class="toolbar"><input v-model="search" type="search" placeholder="Buscar por nome ou e-mail" aria-label="Buscar usuarios" /><select v-model="status" aria-label="Filtrar por status"><option>Todos</option><option>Ativo</option><option>Pendente</option></select></div>
    <div class="table-panel"><table><thead><tr><th>Usuario</th><th>Funcao</th><th>Status</th><th>Ultimo acesso</th><th></th></tr></thead><tbody><tr v-for="user in filteredUsers" :key="user.email"><td><strong>{{ user.name }}</strong><span>{{ user.email }}</span></td><td>{{ user.role }}</td><td><b :class="['status', user.status.toLowerCase()]">{{ user.status }}</b></td><td>{{ user.lastAccess }}</td><td><button class="more-button" type="button" aria-label="Mais opcoes">...</button></td></tr></tbody></table><p v-if="!filteredUsers.length" class="empty">Nenhum usuario encontrado.</p></div>
  </section>
</template>

<style scoped>
.page-container { max-width: 1280px; margin: 0 auto; }.page-header { display: flex; justify-content: space-between; align-items: end; gap: 16px; margin-bottom: 28px; }.eyebrow { margin: 0 0 8px; color: #2563eb; font-size: .72rem; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }h1 { margin: 0; color: #0f172a; font-size: clamp(1.8rem, 4vw, 2.35rem); }.subtitle { color: #64748b; margin: 8px 0 0; }.primary-button { display: inline-block; border-radius: 8px; padding: 11px 16px; background: #2563eb; color: white; font-weight: 700; text-decoration: none; }.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }.toolbar input, .toolbar select { min-height: 42px; border: 1px solid #cbd5e1; border-radius: 8px; padding: 0 12px; background: white; color: #334155; }.toolbar input { flex: 1; }.table-panel { overflow-x: auto; background: white; border: 1px solid #e2e8f0; border-radius: 12px; box-shadow: 0 2px 8px rgba(15, 23, 42, .04); }table { width: 100%; min-width: 700px; border-collapse: collapse; text-align: left; }th, td { padding: 16px 20px; border-bottom: 1px solid #f1f5f9; color: #475569; font-size: .84rem; }th { color: #64748b; font-size: .72rem; text-transform: uppercase; letter-spacing: .08em; }td:first-child { display: grid; gap: 4px; }td strong { color: #1e293b; }td span { color: #94a3b8; font-size: .75rem; }.status { width: max-content; padding: 4px 8px; border-radius: 999px; font-size: .7rem; }.ativo { color: #047857; background: #d1fae5; }.pendente { color: #b45309; background: #fef3c7; }.more-button { border: 0; background: transparent; color: #64748b; font-weight: 700; cursor: pointer; }.empty { padding: 28px; text-align: center; color: #64748b; }@media (max-width: 600px) { .page-header { align-items: start; flex-direction: column; }.primary-button { width: 100%; text-align: center; }.toolbar { flex-direction: column; } }
</style>