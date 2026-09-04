<template>
  <div class="app-shell">
    <aside class="sidebar">
      <h2>CTI Nexus</h2>
      <nav>
        <router-link to="/app/upload">Upload</router-link>
        <router-link to="/app/relatorios">Relatórios</router-link>
        <router-link to="/app/graficos">Gráficos</router-link>
      </nav>
    </aside>

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100vh;
  background: #0b1020;
  color: #e2e8f0;
}

.sidebar {
  width: 240px;
  background: #111827;
  border-right: 1px solid rgba(148, 163, 184, 0.2);
  padding: 24px 18px;
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
}

.sidebar a:hover,
.sidebar a.router-link-active {
  background: rgba(59, 130, 246, 0.18);
  color: white;
}

.content {
  flex: 1;
  padding: 32px;
}

:global(body) {
  margin: 0;
  font-family: Arial, sans-serif;
  background: #0b1020;
}

:global(.page-shell) {
  background: #111827;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.25);
}
</style>