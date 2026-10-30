<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import PageHeader from '../components/PageHeader.vue'
import PanelCard from '../components/PanelCard.vue'

const router = useRouter()
const authStore = useAuthStore()

const formVazio = () => ({ name: '', email: '', password: '', status: 'Ativo' })
const form = ref(formVazio())

const error = ref('')
const success = ref('')
const salvando = ref(false)

// Cadastra pela API (POST /api/auth/register) e, se "Ativo", já aprova (PATCH .../status)
async function salvarUsuario() {
  error.value = ''
  success.value = ''

  if (!form.value.name.trim() || !form.value.email.trim() || !form.value.password) {
    error.value = 'Preencha nome, e-mail e senha.'
    return
  }

  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,72}$/.test(form.value.password)) {
    error.value = 'A senha deve ter de 8 a 72 caracteres, com letras e números.'
    return
  }

  salvando.value = true
  const result = await authStore.criarUsuario(form.value)
  salvando.value = false

  if (!result.ok) {
    error.value = result.message
    return
  }

  success.value = 'Usuário cadastrado com sucesso.'
  form.value = formVazio()

  setTimeout(() => {
    router.push({ name: 'users' })
  }, 500)
}
</script>

<template>
  <section class="page-container">
    <PageHeader
      eyebrow="Administracao"
      title="Novo usuario"
      subtitle="Crie uma conta para a equipe. Novos usuários entram com o perfil Usuário."
    />

    <PanelCard as="form" class="form-card" @submit.prevent="salvarUsuario">
      <div class="field-row">
        <label>
          <span>Nome completo</span>
          <input v-model="form.name" type="text" placeholder="Digite o nome completo" />
        </label>
      </div>

      <div class="field-row two-columns">
        <label>
          <span>E-mail</span>
          <input v-model="form.email" type="email" placeholder="nome@ctinexus.com" />
        </label>

        <label>
          <span>Senha</span>
          <input v-model="form.password" type="password" placeholder="Mínimo 8 caracteres, com letras e números" />
        </label>
      </div>

      <div class="field-row two-columns">
        <label>
          <span>Status</span>
          <select v-model="form.status">
            <option value="Ativo">Ativo (já aprovado)</option>
            <option value="Pendente">Pendente</option>
          </select>
        </label>
      </div>

      <div v-if="error" class="message error">{{ error }}</div>
      <div v-if="success" class="message success">{{ success }}</div>

      <div class="actions">
        <button type="button" class="secondary-button" @click="router.push({ name: 'users' })">Cancelar</button>
        <button type="submit" class="primary-button" :disabled="salvando">{{ salvando ? 'Salvando...' : 'Salvar usuario' }}</button>
      </div>
    </PanelCard>
  </section>
</template>

<style scoped>
.page-container { max-width: 980px; margin: 0 auto; }

.form-card {
  display: grid;
  gap: 20px;
}

.field-row {
  display: grid;
}

.two-columns {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

label {
  display: grid;
  gap: 8px;
  font-size: 0.82rem;
  color: var(--color-body);
  font-weight: 700;
}

input, select {
  min-height: 42px;
  padding: 0 12px;
  border: 1px solid var(--color-border-input);
  border-radius: 8px;
  color: var(--color-heading);
  background: var(--color-surface);
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.primary-button, .secondary-button {
  border-radius: 8px;
  padding: 10px 16px;
  font-weight: 700;
  cursor: pointer;
}

.primary-button {
  border: 0;
  background: var(--color-primary);
  color: var(--color-primary-foreground);
}

.secondary-button {
  border: 1px solid rgba(148, 163, 184, 0.3);
  background: transparent;
  color: var(--color-body);
}

.message {
  padding: 12px 14px;
  border-radius: 8px;
  font-size: 0.9rem;
}

.message.error {
  background: rgba(239, 68, 68, 0.08);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #fca5a5;
}

.message.success {
  background: rgba(52, 211, 153, 0.08);
  border: 1px solid rgba(52, 211, 153, 0.2);
  color: #a7f3d0;
}

@media (max-width: 640px) {
  .two-columns {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
  }
}
</style>