<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  name: '',
  email: '',
  password: '',
  role: 'user',
})

const formMessage = ref({ type: '', text: '' })

const currentUser = computed(() => authStore.getUserById(authStore.user?.id))

const fillForm = () => {
  const user = currentUser.value || authStore.user

  form.value = {
    name: user?.name || '',
    email: user?.email || '',
    password: '',
    role: user?.role || 'user',
  }
}

watch(
  () => authStore.user,
  () => {
    fillForm()
  },
  { immediate: true }
)

const saveProfile = () => {
  formMessage.value = { type: '', text: '' }

  if (!authStore.user?.id) {
    formMessage.value = { type: 'error', text: 'Você precisa estar autenticado para editar o perfil.' }
    return
  }

  if (!form.value.name.trim() || !form.value.email.trim()) {
    formMessage.value = { type: 'error', text: 'Nome e e-mail são obrigatórios.' }
    return
  }

  const payload = {
    name: form.value.name.trim(),
    email: form.value.email.trim().toLowerCase(),
    password: form.value.password.trim() || currentUser.value?.password || '',
    role: form.value.role,
  }

  const result = authStore.updateUser(authStore.user.id, payload)

  if (!result.ok) {
    formMessage.value = { type: 'error', text: result.message }
    return
  }

  formMessage.value = { type: 'success', text: 'Perfil atualizado com sucesso.' }
  form.value.password = ''
}

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
      <form class="settings-panel" @submit.prevent="saveProfile">
        <h2>Dados do perfil</h2>
        <p class="panel-text">Estas informacoes aparecem para a sua equipe.</p>

        <label>
          Nome completo
          <input v-model="form.name" type="text" placeholder="Digite o nome completo" />
        </label>

        <label>
          E-mail
          <input v-model="form.email" type="email" placeholder="seu@email.com" />
        </label>

        <label>
          Senha
          <input v-model="form.password" type="password" placeholder="Deixe vazio para manter a atual" />
        </label>

        <label>
          Funcao
          <input :value="form.role === 'admin' ? 'Administrador' : 'Usuário'" type="text" disabled />
        </label>

        <div v-if="formMessage.text" :class="['form-message', formMessage.type]">
          {{ formMessage.text }}
        </div>

        <button class="primary-button" type="submit">Salvar alteracoes</button>
      </form>

      <div class="settings-panel">
        <h2>Notificacoes</h2>
        <p class="panel-text">Escolha quais atualizacoes deseja receber.</p>
        <label class="toggle-row"><span><strong>Processamento concluido</strong><small>Receber aviso quando uma planilha terminar.</small></span><input type="checkbox" checked /></label>
        <label class="toggle-row"><span><strong>Resumo semanal</strong><small>Receber um resumo dos principais indicadores.</small></span><input type="checkbox" checked /></label>
        <label class="toggle-row"><span><strong>Alertas de seguranca</strong><small>Notificacoes sobre acessos e alteracoes.</small></span><input type="checkbox" checked /></label>
      </div>

      <div class="settings-panel security">
        <h2>Seguranca da conta</h2>
        <p class="panel-text">Mantenha seus dados de acesso protegidos.</p>
        <button class="outline-button" type="button" @click="form.password = ''">Limpar campo de senha</button>
        <button class="danger-button" type="button" @click="logoutAll">Encerrar todas as sessoes</button>
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
.security {
  grid-column: 1 / -1;
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
  .security { grid-column: auto; }
  .primary-button, .outline-button, .danger-button { width: 100%; }
}
</style>