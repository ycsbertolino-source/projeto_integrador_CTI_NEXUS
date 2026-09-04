<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const email = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')

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

const handleLogin = async () => {
  errorMessage.value = ''

  if (!email.value || !password.value) {
    errorMessage.value = 'Preencha todos os campos para continuar.'
    return
  }

  try {
    loading.value = true

    await new Promise((resolve) => setTimeout(resolve, 1000))

    router.push('/app/upload')
  } catch (error) {
    errorMessage.value = error.message || 'Erro ao autenticar. Verifique suas credenciais.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  createParticles()
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
      <router-link to="/" class="flex items-center gap-3">
        <svg viewBox="0 0 100 100" class="w-[30px] h-[30px]" fill="none" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="g1" x1="0" y1="100" x2="100" y2="0">
              <stop offset="0%" stop-color="#2563EB" />
              <stop offset="100%" stop-color="#10B981" />
            </linearGradient>
          </defs>
          <path d="M10 30 L10 85 L30 85 L30 55 L48 73 L48 55 L30 37 L30 30 Z" fill="white" opacity="0.95" />
          <path d="M48 55 L48 73 L66 85 L66 55 L48 37 Z" fill="white" opacity="0.95" />
          <path d="M8 48 L48 8 L70 30 L88 12 L88 38 L62 38 L74 26 L48 26 L28 46 Z" fill="url(#g1)" />
        </svg>
        <span class="font-display font-bold text-lg text-white">CTI Nexus</span>
      </router-link>

      <router-link to="/" class="text-sm text-slate-400 hover:text-white transition-colors">
        ← Voltar para o início
      </router-link>
    </header>

    <main class="relative z-10 max-w-md w-full mx-auto px-6 py-12 flex flex-col justify-center flex-1">
      <div class="bg-[#071829]/70 border border-white/10 rounded-[28px] p-8 md:p-10 backdrop-blur-md shadow-[0_30px_80px_rgba(15,118,110,0.18)] ring-1 ring-white/5">
        <div class="mb-8 text-center">
          <span class="inline-flex items-center gap-2 font-mono text-[11px] tracking-widest uppercase text-slate-300 bg-white/5 border border-white/10 px-3 py-1.5 rounded-full mb-4">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Área Restrita
          </span>
          <h1 class="font-display font-bold text-2xl md:text-3xl text-white tracking-tight">
            Acesse sua conta
          </h1>
          <p class="text-slate-400 text-sm mt-2">
            Insira suas credenciais corporativas para gerenciar os dados.
          </p>
        </div>

        <div v-if="errorMessage" class="mb-6 bg-red-500/10 border border-red-500/20 text-red-300 text-sm px-4 py-3 rounded-xl">
          {{ errorMessage }}
        </div>

        <form @submit.prevent="handleLogin" class="space-y-5">
          <div>
            <label class="block font-mono text-xs text-slate-300 uppercase tracking-wider mb-2">E-mail corporativo</label>
            <input
              v-model="email"
              type="email"
              required
              placeholder="nome@empresa.com"
              class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
            />
          </div>

          <div>
            <div class="flex items-center justify-between mb-2">
              <label class="font-mono text-xs text-slate-300 uppercase tracking-wider">Senha</label>
            </div>
            <input
              v-model="password"
              type="password"
              required
              placeholder="••••••••"
              class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
            />
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full mt-2 bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-500 hover:to-blue-400 text-white font-semibold text-sm py-3.5 px-6 rounded-xl shadow-lg transition-all transform hover:scale-[1.01] active:scale-[0.99] disabled:opacity-70 disabled:cursor-not-allowed"
          >
            {{ loading ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>

        <div class="mt-6 text-center">
          <p class="text-slate-400 text-xs">
            Precisa de ajuda? <a href="#" class="text-blue-400 hover:text-blue-300">Contato do suporte</a>
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
  color: rgba(255, 255, 255, 0.95);
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.8), rgba(16, 185, 129, 0.8));
  font-weight: 700;
  font-size: 0.85rem;
}

.person-card strong,
.person-card small {
  display: block;
}

.person-card small {
  color: rgba(191, 219, 254, 0.75);
}

.particles {
  filter: blur(0.15px);
}

.particle {
  position: absolute;
  display: block;
  border-radius: 9999px;
  background: radial-gradient(circle, rgba(147, 197, 253, 0.9), rgba(59, 130, 246, 0.25));
  box-shadow: 0 0 12px rgba(96, 165, 250, 0.5);
  transform: translate3d(0, 0, 0);
  animation: floatParticle 9s ease-in-out infinite alternate;
  transition: transform 0.35s ease-out, opacity 0.35s ease-out;
}

.particle::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 9999px;
  background: rgba(255, 255, 255, 0.5);
  opacity: 0.7;
}

.particle {
  transform: translate(var(--dx, 0px), var(--dy, 0px));
}

@keyframes floatParticle {
  0% {
    transform: translate(0, 0) scale(0.96);
  }
  25% {
    transform: translate(var(--float-x), calc(var(--float-y) * -0.3)) scale(1);
  }
  50% {
    transform: translate(calc(var(--float-x) * -0.6), var(--float-y)) scale(1.08);
  }
  75% {
    transform: translate(var(--float-x), calc(var(--float-y) * 0.7)) scale(1);
  }
  100% {
    transform: translate(calc(var(--float-x) * -0.9), calc(var(--float-y) * -0.8)) scale(0.96);
  }
}

@media (max-width: 900px) {
  .sheet-card {
    transform: scale(0.8) !important;
  }

  .person-card {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .particle,
  .sheet-card,
  .person-card {
    animation: none;
    transition: none;
  }
}
</style>
