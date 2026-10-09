<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

function handleLogout() {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="brand-mark">C</div>
        <div>
          <p class="brand-label">CTI</p>
          <h2>Nexus</h2>
        </div>
      </div>

      <nav>
        <router-link to="/app/dashboard"><span class="nav-icon">⌂</span> Visão geral</router-link>
        <router-link to="/app/upload"><span class="nav-icon">↑</span> Upload</router-link>
        <router-link to="/app/graficos"><span class="nav-icon">▥</span> Estatísticas</router-link>
        <router-link to="/app/relatorios"><span class="nav-icon">📄</span> Relatórios</router-link>
        <router-link v-if="authStore.user?.role === 'admin'" to="/app/usuarios"><span class="nav-icon">◎</span> Usuários</router-link>
        <router-link to="/app/configuracoes"><span class="nav-icon">⚙</span> Configurações</router-link>
      </nav>

      <div class="sidebar-user">
        <div class="user-avatar">{{ (authStore.user?.name || 'U').charAt(0).toUpperCase() }}</div>
        <div class="user-meta">
          <strong>{{ authStore.user?.name || 'Usuário' }}</strong>
          <span>{{ authStore.user?.role === 'admin' ? 'Administrador' : 'Operador' }}</span>
        </div>
        <button class="logout-button" type="button" @click="handleLogout">Sair</button>
      </div>
    </aside>

    <main class="content">
      <div class="content-bar">
        <span>CTI Insights</span>
        <span class="online-dot">Sistema online</span>
      </div>
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(180deg, #050b14 0%, #08131e 100%);
  color: #e2e8f0;
}

.sidebar {
  width: 250px;
  background: rgba(15, 23, 42, 0.92);
  border-right: 1px solid rgba(148, 163, 184, 0.18);
  padding: 22px 18px 18px;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(12px);
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
  margin-bottom: 18px;
}

.brand-mark {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #2563eb, #10b981);
  color: white;
  font-weight: 800;
  box-shadow: 0 10px 25px rgba(37, 99, 235, 0.35);
}

.brand-label {
  margin: 0;
  font-size: 0.62rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #8ea8d1;
}

.sidebar-header h2 {
  margin: 2px 0 0;
  font-size: 1.2rem;
  color: #f8fafc;
}

.sidebar nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar a {
  color: #cbd5e1;
  text-decoration: none;
  padding: 11px 12px;
  border-radius: 10px;
  transition: 0.2s ease;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.86rem;
  border: 1px solid transparent;
}

.sidebar a:hover,
.sidebar a.router-link-active {
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.22), rgba(16, 185, 129, 0.12));
  color: white;
  border-color: rgba(96, 165, 250, 0.3);
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.12);
}

.nav-icon {
  width: 18px;
  color: #94a3b8;
  text-align: center;
  font-size: 1rem;
}

.router-link-active .nav-icon { color: white; }

.sidebar-user {
  margin-top: auto;
  padding: 18px 12px 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.14);
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d4ed8, #0ea5e9);
  font-weight: 700;
  color: white;
}

.user-meta {
  display: grid;
  gap: 3px;
}

.user-meta strong {
  color: #f8fafc;
  font-size: 0.82rem;
}

.user-meta span {
  color: #94a3b8;
  font-size: 0.7rem;
}

.logout-button {
  grid-column: 1 / -1;
  margin-top: 8px;
  border: 1px solid rgba(248, 113, 113, 0.42);
  background: rgba(127, 29, 29, 0.2);
  color: #fecaca;
  border-radius: 10px;
  padding: 9px 10px;
  font-weight: 700;
  cursor: pointer;
  transition: 0.2s ease;
}

.logout-button:hover {
  background: rgba(127, 29, 29, 0.32);
}

.content {
  flex: 1;
  padding: 24px 28px 32px;
  min-width: 0;
  background: #050b14;
}

.content-bar {
  max-width: 1280px;
  margin: 0 auto 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #94a3b8;
  font-size: .72rem;
  letter-spacing: .08em;
  text-transform: uppercase;
  padding: 10px 12px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.7);
}

.online-dot::before {
  content: '';
  display: inline-block;
  width: 7px;
  height: 7px;
  margin-right: 6px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 0 6px rgba(16, 185, 129, 0.15);
}

@media (max-width: 700px) {
  .app-shell {
    display: block;
    padding-bottom: 76px;
  }

  .sidebar {
    position: fixed;
    z-index: 10;
    bottom: 0;
    left: 0;
    right: 0;
    width: auto;
    height: 72px;
    padding: 8px 10px;
    border-right: 0;
    border-top: 1px solid rgba(148, 163, 184, 0.2);
    border-bottom: 0;
    background: rgba(15, 23, 42, 0.98);
  }

  .sidebar-header,
  .sidebar-user {
    display: none;
  }

  .sidebar nav {
    height: 100%;
    flex-direction: row;
    justify-content: space-around;
    align-items: center;
    gap: 4px;
  }

  .sidebar a {
    padding: 8px 6px;
    font-size: 0.68rem;
    text-align: center;
    flex: 1;
    justify-content: center;
  }

  .content {
    padding: 18px 16px 90px;
  }

  .content-bar {
    margin: 0 auto 16px;
    font-size: 0.6rem;
  }
}

:global(html), :global(body), :global(#app) {
  margin: 0;
  min-height: 100%;
  font-family: Arial, sans-serif;
  background: #050b14;
}

:global(.page-shell) {
  background: #111827;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.25);
}
</style>