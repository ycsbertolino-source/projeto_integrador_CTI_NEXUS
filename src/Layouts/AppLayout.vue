<template>
  <div class="app-shell">
    <aside class="sidebar">
      <h2>CTI Nexus</h2>
      <nav>
        <router-link to="/app/dashboard"><span class="nav-icon">⌂</span> Visao geral</router-link>
        <router-link to="/app/upload"><span class="nav-icon">↑</span> Upload</router-link>
        <router-link to="/app/graficos"><span class="nav-icon">▥</span> Estatisticas</router-link>
        <router-link to="/app/usuarios"><span class="nav-icon">◎</span> Usuarios</router-link>
        <router-link to="/app/configuracoes"><span class="nav-icon">⚙</span> Configuracoes</router-link>
      </nav>
      <div class="sidebar-user">
        <strong>Yasmin Silva</strong>
        <span>Administrador</span>
      </div>
    </aside>

    <main class="content">
      <div class="content-bar"><span>CTI Insights</span><span class="online-dot">Sistema online</span></div>
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
  color: #1e293b;
}

.sidebar {
  width: 240px;
  background: #111827;
  border-right: 1px solid rgba(148, 163, 184, 0.2);
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
}

.sidebar-user {
  margin-top: auto;
  padding-top: 24px;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  display: grid;
  gap: 4px;
  font-size: 0.8rem;
}

.sidebar-user span {
  color: #94a3b8;
}

.sidebar h2 {
  margin: 0 0 24px;
  font-size: 1.3rem;
}

.sidebar nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sidebar a {
  color: #cbd5e1;
  text-decoration: none;
  padding: 10px 12px;
  border-radius: 8px;
  transition: 0.2s ease;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.86rem;
}

.sidebar a:hover,
.sidebar a.router-link-active {
  background: #2563eb;
  color: white;
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.2);
}

.nav-icon { width: 18px; color: #94a3b8; text-align: center; font-size: 1rem; }
.router-link-active .nav-icon { color: white; }

.content {
  flex: 1;
  padding: 32px;
  min-width: 0;
  background: #f8fafc;
}

.content-bar {
  max-width: 1280px;
  margin: -8px auto 28px;
  display: flex;
  justify-content: space-between;
  color: #94a3b8;
  font-size: .72rem;
  letter-spacing: .08em;
  text-transform: uppercase;
}

.online-dot::before {
  content: '';
  display: inline-block;
  width: 7px;
  height: 7px;
  margin-right: 6px;
  border-radius: 50%;
  background: #10b981;
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
    height: 68px;
    padding: 8px 10px;
    border-right: 0;
    border-top: 1px solid rgba(148, 163, 184, 0.2);
  }

  .sidebar h2,
  .sidebar-user {
    display: none;
  }

  .sidebar nav {
    height: 100%;
    flex-direction: row;
    justify-content: space-around;
    align-items: center;
    gap: 2px;
  }

  .sidebar a {
    padding: 8px 6px;
    font-size: 0.7rem;
    text-align: center;
  }

  .content {
    padding: 20px 16px;
  }

  .content-bar { margin: -4px 0 22px; }
}

:global(body) {
  margin: 0;
  font-family: Arial, sans-serif;
  background: #f8fafc;
}

:global(.page-shell) {
  background: #111827;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.25);
}
</style>