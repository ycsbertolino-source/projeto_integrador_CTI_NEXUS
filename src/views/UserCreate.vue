<script setup>
import { ref, computed } from 'vue'
import PageHeader from '../components/PageHeader.vue'
import PanelCard from '../components/PanelCard.vue'
import PrimaryButton from '../components/PrimaryButton.vue'

const search = ref('')
const status = ref('Todos')
const users = ref([
  { name: 'Yasmin Cristina', email: 'yasmin@ctinexus.com', role: 'Administrador', status: 'Ativo', lastAccess: 'Hoje, 09:42' },
  { name: 'Ana Paula Souza', email: 'ana@ctinexus.com', role: 'Analista', status: 'Ativo', lastAccess: 'Hoje, 08:16' },
  { name: 'Carlos Mendes', email: 'carlos@ctinexus.com', role: 'Consultor', status: 'Ativo', lastAccess: 'Ontem, 17:30' },
  { name: 'Maria Oliveira', email: 'maria@ctinexus.com', role: 'Consultor', status: 'Pendente', lastAccess: 'Nunca' },
])

const filteredUsers = computed(() =>
  users.value.filter(user =>
    (status.value === 'Todos' || user.status === status.value) &&
    `${user.name} ${user.email}`.toLowerCase().includes(search.value.toLowerCase())
  )
)
</script>

<template>
  <section class="page-container">
    <PageHeader eyebrow="Administracao" title="Usuarios" subtitle="Gerencie acessos, funcoes e permissoes da equipe.">
      <template #action>
        <PrimaryButton as="router-link" to="/app/usuarios/novo">Novo usuario</PrimaryButton>
      </template>
    </PageHeader>

    <div class="toolbar">
      <input v-model="search" type="search" placeholder="Buscar por nome ou e-mail" aria-label="Buscar usuarios" />
      <select v-model="status" aria-label="Filtrar por status">
        <option>Todos</option>
        <option>Ativo</option>
        <option>Pendente</option>
      </select>
    </div>

    <PanelCard as="div" class="table-panel">
      <table>
        <thead>
          <tr>
            <th>Usuario</th>
            <th>Funcao</th>
            <th>Status</th>
            <th>Ultimo acesso</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredUsers" :key="user.email">
            <td>
              <strong>{{ user.name }}</strong>
              <span>{{ user.email }}</span>
            </td>
            <td>{{ user.role }}</td>
            <td>
              <b :class="['status', user.status.toLowerCase()]">{{ user.status }}</b>
            </td>
            <td>{{ user.lastAccess }}</td>
            <td>
              <button class="more-button" type="button" aria-label="Mais opcoes">...</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!filteredUsers.length" class="empty">Nenhum usuario encontrado.</p>
    </PanelCard>
  </section>
</template>

<style scoped>
.page-container { max-width: 1280px; margin: 0 auto; }

.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }

.toolbar input, .toolbar select {
  min-height: 42px;
  border: 1px solid var(--color-border-input);
  border-radius: 8px;
  padding: 0 12px;
  background: var(--color-surface);
  color: var(--color-body);
}

.toolbar input { flex: 1; }

.table-panel { overflow-x: auto; padding: 0; }

table { width: 100%; min-width: 700px; border-collapse: collapse; text-align: left; }
th, td { padding: 16px 20px; border-bottom: 1px solid var(--color-border-muted); color: var(--color-muted); font-size: .84rem; }
th { color: var(--color-faint); font-size: .72rem; text-transform: uppercase; letter-spacing: .08em; }

td:first-child { display: grid; gap: 4px; }
td strong { color: var(--color-heading); }
td span { color: var(--color-faint); font-size: .75rem; }

.status { width: max-content; padding: 4px 8px; border-radius: 999px; font-size: .7rem; }
.ativo { color: #34d399; background: rgba(52, 211, 153, .14); }
.pendente { color: #fbbf24; background: rgba(251, 191, 36, .14); }

.more-button { border: 0; background: transparent; color: var(--color-faint); font-weight: 700; cursor: pointer; }
.empty { padding: 28px; text-align: center; color: var(--color-muted); }

@media (max-width: 600px) {
  .toolbar { flex-direction: column; }
}
</style>