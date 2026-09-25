<template>
  <section class="form-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Administracao</p>
        <h1>Novo usuario</h1>
        <p class="subtitle">Cadastre um colaborador e defina os modulos permitidos.</p>
      </div>
      <router-link class="back-link" to="/app/usuarios">Voltar para usuarios</router-link>
    </header>

    <form class="form-panel" @submit.prevent>
      <div class="form-grid">
        <label>
          Nome completo
          <input type="text" placeholder="Ex.: Ana Paula Souza" />
        </label>
        <label>
          E-mail corporativo
          <input type="email" placeholder="nome@empresa.com" />
        </label>
        <label>
          Funcao
          <select>
            <option>Analista</option>
            <option>Consultor</option>
            <option>Administrador</option>
          </select>
        </label>
        <label>
          Status
          <select>
            <option>Ativo</option>
            <option>Pendente</option>
          </select>
        </label>
      </div>

      <fieldset>
        <legend>Permissoes por modulo</legend>
        <label class="check"><input type="checkbox" checked /> Dashboard e relatorios</label>
        <label class="check"><input type="checkbox" checked /> Upload de planilhas</label>
        <label class="check"><input type="checkbox" /> Gestao de usuarios</label>
      </fieldset>

      <div class="form-actions">
        <router-link class="cancel-button" to="/app/usuarios">Cancelar</router-link>
        <button class="primary-button" type="submit">Cadastrar usuario</button>
      </div>
    </form>
  </section>
</template>

<style scoped>
.form-page {
  max-width: 900px;
  margin: 0 auto;
  min-height: 100vh;
  background: var(--color-app-bg);
  color: var(--color-body); /* era #3b3f44 — corrigido */
  padding: 32px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
  margin-bottom: 28px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #60a5fa;
  font-size: .72rem;
  font-weight: 700;
  letter-spacing: .12em;
  text-transform: uppercase;
}

h1 { margin: 0; color: var(--color-heading); font-size: clamp(1.8rem, 4vw, 2.35rem); }
.subtitle { color: var(--color-muted); margin: 8px 0 0; }

.back-link, .cancel-button {
  color: #93c5fd;
  text-decoration: none;
  font-weight: 700;
  font-size: .85rem;
  transition: opacity .15s ease;
}
.back-link:hover, .cancel-button:hover { opacity: .8; }

.form-panel {
  padding: 26px;
  background: var(--color-panel);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(2, 6, 23, 0.45);
}

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

label {
  display: grid;
  gap: 8px;
  color: var(--color-body);
  font-size: .82rem;
  font-weight: 700;
}

input, select {
  min-height: 42px;
  border: 1px solid var(--color-border-input);
  border-radius: 8px;
  padding: 0 12px;
  font: inherit;
  color: var(--color-heading);
  background: var(--color-surface);
}

input:focus, select:focus {
  outline: 2px solid rgba(96, 165, 250, 0.55);
  border-color: #60a5fa;
}

fieldset {
  display: grid;
  gap: 14px;
  margin: 28px 0 0;
  padding: 20px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

legend { padding: 0 8px; color: var(--color-heading); font-weight: 700; }

.check {
  display: flex; /* removida a linha de grid-template-columns que não fazia nada */
  align-items: center;
  gap: 9px;
  font-weight: 500;
  color: var(--color-muted);
}
.check input { min-height: auto; accent-color: #60a5fa; }

.form-actions {
  display: flex;
  justify-content: end;
  align-items: center;
  gap: 20px;
  margin-top: 28px;
}

.primary-button {
  border: 0;
  border-radius: 8px;
  padding: 11px 16px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white;
  font-weight: 700;
  cursor: pointer;
  transition: filter .15s ease;
}
.primary-button:hover { filter: brightness(1.1); }

@media (max-width: 600px) {
  .page-header { align-items: start; flex-direction: column; }
  .form-grid { grid-template-columns: 1fr; }
  .form-panel { padding: 18px; }
  .form-actions { justify-content: stretch; flex-direction: column-reverse; }
  .primary-button, .cancel-button { width: 100%; text-align: center; }
}
</style>