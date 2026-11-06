<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

// Dados vêm do backend (GET /api/auth/me). Nome e e-mail só o administrador altera.
const profile = computed(() => ({
  name: authStore.user?.name || '',
  email: authStore.user?.email || '',
  role: authStore.user?.role || 'user',
}))

// ---- Trocar senha (PATCH /api/auth/senha) ----
const senha = ref({ atual: '', nova: '', confirmar: '' })
const senhaMsg = ref({ type: '', text: '' })
const salvandoSenha = ref(false)

async function trocarSenha() {
  senhaMsg.value = { type: '', text: '' }
  const { atual, nova, confirmar } = senha.value
  if (!atual || !nova || !confirmar) {
    senhaMsg.value = { type: 'error', text: 'Preencha os três campos.' }
    return
  }
  // Mesma regra do backend: 8 a 72 caracteres, com letras e números
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,72}$/.test(nova)) {
    senhaMsg.value = { type: 'error', text: 'A nova senha deve ter de 8 a 72 caracteres, com letras e números.' }
    return
  }
  if (nova !== confirmar) {
    senhaMsg.value = { type: 'error', text: 'A confirmação não é igual à nova senha.' }
    return
  }
  salvandoSenha.value = true
  const result = await authStore.trocarSenha(atual, nova)
  salvandoSenha.value = false
  senhaMsg.value = { type: result.ok ? 'success' : 'error', text: result.message }
  if (result.ok) senha.value = { atual: '', nova: '', confirmar: '' }
}

// ---- Preferências de notificação: ficam salvas neste navegador, por usuário ----
const PADRAO = { processamento: true, resumoSemanal: true, seguranca: true }
const chavePrefs = computed(() => `cti_prefs_${authStore.user?.id ?? 'guest'}`)
const prefs = ref({ ...PADRAO })
const prefsSalvas = ref(false)

function carregarPrefs() {
  try {
    const salvo = JSON.parse(localStorage.getItem(chavePrefs.value) || 'null')
    prefs.value = { ...PADRAO, ...(salvo || {}) }
  } catch (error) {
    prefs.value = { ...PADRAO }
  }
}
carregarPrefs()
watch(chavePrefs, carregarPrefs)

let aviso = null
watch(prefs, (valor) => {
  try {
    localStorage.setItem(chavePrefs.value, JSON.stringify(valor))
    prefsSalvas.value = true
    clearTimeout(aviso)
    aviso = setTimeout(() => { prefsSalvas.value = false }, 1500)
  } catch (error) { /* navegador sem armazenamento: só não salva */ }
}, { deep: true })

const logoutAll = () => {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <section class="settings-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Preferencias</p>
        <h1>Configuracoes</h1>
        <p class="subtitle">Personalize seu perfil e a forma como recebe atualizacoes.</p>
      </div>
    </header>

    <div class="settings-grid">
      <div class="settings-panel">
        <h2>Dados do perfil</h2>
        <p class="panel-text">Para alterar nome ou e-mail, fale com o administrador.</p>

        <label>
          Nome completo
          <input :value="profile.name" type="text" disabled />
        </label>

        <label>
          E-mail
          <input :value="profile.email" type="email" disabled />
        </label>

        <label>
          Funcao
          <input :value="profile.role === 'admin' ? 'Administrador' : 'Usuário'" type="text" disabled />
        </label>
      </div>

      <div class="settings-panel">
        <h2>Notificacoes</h2>
        <p class="panel-text">
          Escolha quais atualizacoes deseja receber. Fica salvo neste navegador.
          <span v-if="prefsSalvas" class="saved-tag">Salvo</span>
        </p>
        <label class="toggle-row"><span><strong>Processamento concluido</strong><small>Receber aviso quando uma planilha terminar.</small></span><input v-model="prefs.processamento" type="checkbox" /></label>
        <label class="toggle-row"><span><strong>Resumo semanal</strong><small>Receber um resumo dos principais indicadores.</small></span><input v-model="prefs.resumoSemanal" type="checkbox" /></label>
        <label class="toggle-row"><span><strong>Alertas de seguranca</strong><small>Notificacoes sobre acessos e alteracoes.</small></span><input v-model="prefs.seguranca" type="checkbox" /></label>
      </div>

      <form class="settings-panel" @submit.prevent="trocarSenha">
        <h2>Trocar senha</h2>
        <p class="panel-text">A nova senha deve ter de 8 a 72 caracteres, com letras e números.</p>

        <label>
          Senha atual
          <input v-model="senha.atual" type="password" autocomplete="current-password" />
        </label>
        <label>
          Nova senha
          <input v-model="senha.nova" type="password" autocomplete="new-password" />
        </label>
        <label>
          Confirmar nova senha
          <input v-model="senha.confirmar" type="password" autocomplete="new-password" />
        </label>

        <p v-if="senhaMsg.text" :class="['form-message', senhaMsg.type]">{{ senhaMsg.text }}</p>
        <button class="primary-button" type="submit" :disabled="salvandoSenha">
          {{ salvandoSenha ? 'Salvando...' : 'Salvar nova senha' }}
        </button>
      </form>

      <div class="settings-panel">
        <h2>Seguranca da conta</h2>
        <p class="panel-text">Mantenha seus dados de acesso protegidos.</p>
        <button class="danger-button" type="button" @click="logoutAll">Sair desta sessao</button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.settings-page {
  max-width: 1000px;
  margin: 0 auto;
  min-height: 100vh;
  background: var(--color-app-bg);
  color: var(--color-body);
  padding: 32px 20px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
h1 {
  margin: 0;
  color: var(--color-heading);
  font-size: clamp(1.8rem, 4vw, 2.35rem);
}
.subtitle, .panel-text {
  color: var(--color-muted);
  margin: 8px 0 0;
}
.settings-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}
.settings-panel {
  display: grid;
  gap: 18px;
  align-content: start;
  padding: 24px;
  background: var(--color-panel);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(2, 6, 23, 0.45);
}
.settings-panel h2 {
  margin: 0;
  color: var(--color-heading);
  font-size: 1rem;
}
.settings-panel .panel-text {
  margin-top: -12px;
  font-size: .82rem;
}
label:not(.toggle-row) {
  display: grid;
  gap: 8px;
  color: var(--color-body);
  font-size: .82rem;
  font-weight: 700;
}
input {
  min-height: 42px;
  padding: 0 12px;
  border: 1px solid var(--color-border-input);
  border-radius: 8px;
  color: var(--color-heading);
  background: var(--color-surface);
}
input:disabled {
  opacity: .6;
  cursor: not-allowed;
}
.primary-button, .outline-button, .danger-button {
  width: max-content;
  border-radius: 8px;
  padding: 10px 14px;
  font-weight: 700;
  cursor: pointer;
}
.primary-button {
  border: 0;
  background: var(--color-primary);
  color: var(--color-primary-foreground);
}
.outline-button {
  border: 1px solid rgba(96, 165, 250, 0.6);
  background: var(--color-surface);
  color: #bfdbfe;
}
.danger-button {
  border: 1px solid rgba(248, 113, 113, 0.6);
  background: rgba(127, 29, 29, 0.2);
  color: #fecaca;
}
.toggle-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  font-size: .82rem;
  color: var(--color-body);
}
.toggle-row span {
  display: grid;
  gap: 4px;
}
.toggle-row small {
  color: var(--color-muted);
  font-weight: 400;
}
.toggle-row input {
  min-height: auto;
  width: 18px;
  height: 18px;
  accent-color: #60a5fa;
}
.saved-tag {
  margin-left: 6px;
  color: #34d399;
  font-weight: 700;
}
.primary-button:disabled {
  opacity: .6;
  cursor: not-allowed;
}
.form-message {
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 0.85rem;
}
.form-message.error {
  background: rgba(239, 68, 68, 0.08);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #fca5a5;
}
.form-message.success {
  background: rgba(52, 211, 153, 0.08);
  border: 1px solid rgba(52, 211, 153, 0.2);
  color: #a7f3d0;
}
@media (max-width: 700px) {
  .page-header { flex-direction: column; align-items: flex-start; }
  .settings-grid { grid-template-columns: 1fr; }
  .primary-button, .outline-button, .danger-button { width: 100%; }
}
</style>