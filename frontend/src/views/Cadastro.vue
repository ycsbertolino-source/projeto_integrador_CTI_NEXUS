<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const errorMessage = ref('')
const successMessage = ref('')
const loading = computed(() => authStore.loading)

const particles = ref([])
const floatingCards = [
  { id: 1, label: 'FATURAMENTO', value: 'R$ 842K', top: '12%', left: '8%', rotate: -12 },
  { id: 2, label: 'CLIENTES', value: '37', top: '18%', left: '70%', rotate: 10 },
  { id: 3, label: 'QUALIDADE', value: '98.4%', top: '48%', left: '9%', rotate: -8 },
  { id: 4, label: 'PIPELINE', value: '28', top: '52%', left: '75%', rotate: 12 },
  { id: 5, label: 'Bônus', value: '06', top: '70%', left: '18%', rotate: -10 },
  { id: 6, label: 'Ações', value: '12', top: '72%', left: '72%', rotate: 8 },
]

const peopleCards = [
  { id: 1, name: 'Ana', role: 'Financeiro', top: '20%', left: '60%', rotate: -7 },
  { id: 2, name: 'Bruno', role: 'Operações', top: '68%', left: '57%', rotate: 8 },
  { id: 3, name: 'Cris', role: 'CRM', top: '37%', left: '83%', rotate: 10 },
]

const createParticles = () => {
  const count = 28
  particles.value = Array.from({ length: count }, (_, index) => ({
    id: index,
    x: Math.random() * 100,
    y: Math.random() * 100,
    size: Math.random() * 4 + 2,
    driftX: (Math.random() - 0.5) * 0.6,
    driftY: (Math.random() - 0.5) * 0.6,
    opacity: Math.random() * 0.7 + 0.2,
    delay: Math.random() * 4,
  }))
}

const pointer = ref({ x: 50, y: 50, active: false })

const getCardTransform = (card) => {
  const offsetX = (pointer.value.x - 50) * 0.7
  const offsetY = (pointer.value.y - 50) * 0.7
  const shiftX = offsetX * (card.top.includes('70%') ? 0.8 : 0.5)
  const shiftY = offsetY * (card.left.includes('70%') ? 0.8 : 0.5)

  return {
    left: card.left,
    top: card.top,
    transform: `translate(${shiftX}px, ${shiftY}px) rotate(${card.rotate}deg)`,
  }
}

const getPeopleTransform = (card) => {
  const offsetX = (pointer.value.x - 50) * 0.8
  const offsetY = (pointer.value.y - 50) * 0.8
  return {
    left: card.left,
    top: card.top,
    transform: `translate(${offsetX * 0.9}px, ${offsetY * 0.9}px) rotate(${card.rotate}deg)`,
  }
}

const updatePointer = (event) => {
  const rect = event.currentTarget.getBoundingClientRect()
  pointer.value = {
    x: ((event.clientX - rect.left) / rect.width) * 100,
    y: ((event.clientY - rect.top) / rect.height) * 100,
    active: true,
  }
}

const resetPointer = () => {
  pointer.value.active = false
}

const handleRegister = async () => {
  errorMessage.value = ''
  successMessage.value = ''
  authStore.authError = ''

  if (!form.value.name || !form.value.email || !form.value.password || !form.value.confirmPassword) {
    errorMessage.value = 'Preencha todos os campos para continuar.'
    return
  }

  // Mesma regra do backend (AuthDtos.CadastroRequest): 8 a 72, com letras e números
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,72}$/.test(form.value.password)) {
    errorMessage.value = 'A senha deve ter de 8 a 72 caracteres, com letras e números.'
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = 'As senhas não coincidem.'
    return
  }

  const result = await authStore.registerUser({
    name: form.value.name,
    email: form.value.email,
    password: form.value.password,
  })

  if (!result.ok) {
    errorMessage.value = result.message
    return
  }

  successMessage.value = result.message

  form.value = {
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  }

  setTimeout(() => {
    router.push({ name: 'login' })
  }, 1500)
}

onMounted(async () => {
  createParticles()
  await authStore.checkAuth()

  if (authStore.user) {
    router.replace({ name: 'upload' })
  }
})

onBeforeUnmount(() => {
  pointer.value.active = false
})
</script>

<template>
  <div
    class="login-shell min-h-screen bg-[#050505] text-slate-100 font-sans flex flex-col justify-between relative overflow-hidden"
    @pointermove="updatePointer"
    @pointerleave="resetPointer"
  >
    <div class="ambient-grid"></div>
    <div class="floating-layer">
      <div
        v-for="card in floatingCards"
        :key="card.id"
        class="sheet-card"
        :style="getCardTransform(card)"
      >
        <span class="sheet-label">{{ card.label }}</span>
        <div class="sheet-lines">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <strong>{{ card.value }}</strong>
      </div>

      <div
        v-for="person in peopleCards"
        :key="person.id"
        class="person-card"
        :style="getPeopleTransform(person)"
      >
        <div class="avatar">{{ person.name.charAt(0) }}</div>
        <div>
          <strong>{{ person.name }}</strong>
          <small>{{ person.role }}</small>
        </div>
      </div>
    </div>

    <div class="pointer-events-none absolute -top-[20%] -right-[10%] w-[600px] h-[600px] rounded-full bg-gradient-to-tr from-blue-700/20 via-blue-600/10 to-transparent blur-3xl"></div>
    <div class="pointer-events-none absolute -bottom-[20%] -left-[10%] w-[500px] h-[500px] rounded-full bg-emerald-500/5 blur-3xl"></div>

    <div class="particles pointer-events-none absolute inset-0 overflow-hidden">
      <span
        v-for="particle in particles"
        :key="particle.id"
        class="particle"
        :style="{
          left: `${particle.x}%`,
          top: `${particle.y}%`,
          width: `${particle.size}px`,
          height: `${particle.size}px`,
          opacity: particle.opacity,
          animationDelay: `${particle.delay}s`,
          '--dx': `${(pointer.x - particle.x) * 0.08}px`,
          '--dy': `${(pointer.y - particle.y) * 0.08}px`,
          '--float-x': `${particle.driftX * 18}px`,
          '--float-y': `${particle.driftY * 18}px`,
          '--pointer-scale': pointer.active ? 1 : 0,
        }"
      ></span>
    </div>

    <header class="w-full max-w-[1180px] mx-auto px-6 h-[76px] flex items-center justify-between relative z-10">
      <router-link to="/" class="brand-lockup flex items-center gap-3">
        <div class="brand-mark">C</div>
        <div class="brand-copy">
          <span class="brand-top">CTI</span>
          <span class="brand-bottom">Nexus</span>
        </div>
      </router-link>

      <router-link to="/login" class="text-sm text-slate-400 hover:text-white transition-colors">
        Já tem conta? Entrar
      </router-link>
    </header>

    <main class="relative z-10 max-w-md w-full mx-auto px-6 py-12 flex flex-col justify-center flex-1">
      <div class="bg-[#071829]/70 border border-white/10 rounded-[28px] p-8 md:p-10 backdrop-blur-md shadow-[0_30px_80px_rgba(15,118,110,0.18)] ring-1 ring-white/5">
        <div class="mb-8 text-center">
          <span class="inline-flex items-center gap-2 font-mono text-[11px] tracking-widest uppercase text-slate-300 bg-white/5 border border-white/10 px-3 py-1.5 rounded-full mb-4">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Novo cadastro
          </span>
          <h1 class="font-display font-bold text-2xl md:text-3xl text-white tracking-tight">
            Crie sua conta
          </h1>
          <p class="text-slate-400 text-sm mt-2">
            Cadastre-se para acessar o painel da operação e acompanhar os relatórios.
          </p>
        </div>

        <div v-if="errorMessage" class="mb-6 bg-red-500/10 border border-red-500/20 text-red-300 text-sm px-4 py-3 rounded-xl">
          {{ errorMessage }}
        </div>

        <div v-if="successMessage" class="mb-6 bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 text-sm px-4 py-3 rounded-xl">
          {{ successMessage }}
        </div>

        <form @submit.prevent="handleRegister" class="space-y-5">
          <div>
            <label class="block font-mono text-xs text-slate-300 uppercase tracking-wider mb-2">Nome completo</label>
            <input
              v-model="form.name"
              type="text"
              required
              placeholder="Seu nome"
              class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
            />
          </div>

          <div>
            <label class="block font-mono text-xs text-slate-300 uppercase tracking-wider mb-2">E-mail corporativo</label>
            <input
              v-model="form.email"
              type="email"
              required
              placeholder="nome@empresa.com"
              class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
            />
          </div>

          <div>
            <label class="block font-mono text-xs text-slate-300 uppercase tracking-wider mb-2">Senha</label>
            <input
              v-model="form.password"
              type="password"
              required
              placeholder="Mínimo 8 caracteres, com letras e números"
              class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
            />
          </div>

          <div>
            <label class="block font-mono text-xs text-slate-300 uppercase tracking-wider mb-2">Confirmar senha</label>
            <input
              v-model="form.confirmPassword"
              type="password"
              required
              placeholder="Digite novamente"
              class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
            />
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full mt-2 bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-500 hover:to-blue-400 text-white font-semibold text-sm py-3.5 px-6 rounded-xl shadow-lg transition-all transform hover:scale-[1.01] active:scale-[0.99] disabled:opacity-70 disabled:cursor-not-allowed"
          >
            {{ loading ? 'Cadastrando...' : 'Cadastrar conta' }}
          </button>
        </form>

        <div class="mt-6 text-center">
          <p class="text-slate-400 text-xs">
            Já possui acesso? <router-link to="/login" class="text-blue-400 hover:text-blue-300">Faça login</router-link>
          </p>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.login-shell {
  position: relative;
  background:
    radial-gradient(circle at top, rgba(37, 99, 235, 0.1), transparent 30%),
    radial-gradient(circle at bottom left, rgba(16, 185, 129, 0.08), transparent 25%),
    linear-gradient(135deg, #020914 0%, #071327 45%, #050a12 100%);
}

.ambient-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.08) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: radial-gradient(circle at center, rgba(0, 0, 0, 1) 38%, transparent 90%);
  pointer-events: none;
}

.floating-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.sheet-card,
.person-card {
  position: absolute;
  border: 1px solid rgba(96, 165, 250, 0.24);
  background: rgba(10, 19, 32, 0.36);
  backdrop-filter: blur(18px);
  box-shadow: 0 18px 48px rgba(15, 118, 110, 0.12), inset 0 0 20px rgba(96, 165, 250, 0.06);
  transition: transform 0.4s ease-out;
}

.sheet-card {
  width: 170px;
  padding: 12px 12px 10px;
  border-radius: 18px;
  color: white;
}

.sheet-label {
  display: block;
  font-size: 9px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(191, 219, 254, 0.8);
}

.sheet-lines {
  display: grid;
  gap: 5px;
  margin: 10px 0 8px;
}

.sheet-lines span {
  display: block;
  height: 6px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.17);
}

.brand-lockup {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
}

.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2563eb 0%, #0ea5e9 100%);
  color: #f8fafc;
  font-size: 1.2rem;
  font-weight: 800;
  box-shadow: 0 10px 28px rgba(37, 99, 235, 0.35);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  line-height: 0.9;
}

.brand-top,
.brand-bottom {
  color: #f8fafc;
  font-weight: 700;
}

.brand-top {
  font-size: 0.62rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  opacity: 0.9;
}

.brand-bottom {
  font-size: 1.15rem;
  letter-spacing: -0.04em;
}

.sheet-card strong {
  font-size: 1.25rem;
  font-weight: 700;
}

.person-card {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 140px;
  max-width: 170px;
  padding: 10px 12px;
  border-radius: 16px;
  color: white;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #10b981);
  font-weight: 700;
  font-size: 0.9rem;
}

.person-card strong {
  display: block;
  font-size: 0.82rem;
}

.person-card small {
  color: rgba(191, 219, 254, 0.72);
}

.particle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.9), rgba(96,165,250,0.15));
  box-shadow: 0 0 12px rgba(96,165,250,0.18);
  animation: float 8s ease-in-out infinite alternate;
}

@keyframes float {
  0% {
    transform: translate3d(0, 0, 0) scale(0.9);
  }
  100% {
    transform: translate3d(var(--dx), var(--dy), 0) scale(var(--pointer-scale));
  }
}
</style>
