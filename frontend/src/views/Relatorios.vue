<script setup>
import { computed } from 'vue'
import { usePlanilhaStore } from '@/stores/planilhaStore'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'

const store = usePlanilhaStore()
const authStore = useAuthStore()
const router = useRouter()

const isAdmin = computed(() => authStore.user?.role === 'admin')

const reports = computed(() => {
  if (Array.isArray(store.historico) && store.historico.length > 0) {
    return store.historico.map((item) => ({
      id: item.id || `${item.name}-${item.uploadedAt}`,
      name: item.name || 'Planilha',
      uploadedAt: item.uploadedAt || item.createdAt,
      total: Number(item.total) || 0,
      valid: Number(item.valid) || 0,
      invalid: Number(item.invalid) || 0,
    }))
  }

  if (!store.dadosTratados || store.dadosTratados.length === 0) return []

  return [{
    id: 1,
    name: store.nomeArquivo || 'Planilha',
    uploadedAt: store.dataUpload,
    total: store.dadosTratados.length,
    valid: store.dadosTratados.filter(d => (d.erros || []).length === 0).length,
    invalid: store.dadosTratados.filter(d => (d.erros || []).length > 0).length,
  }]
})

function openReport(item = null) {
  if (item) {
    store.carregarHistorico(item)
  }
  router.push({ name: 'relatorio' })
}

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('pt-BR')
}
</script>

<template>
  <section class="page-container">
    <header class="page-header">
      <div>
        <p class="eyebrow">Relatórios</p>
        <h1>Relatórios gerados</h1>
        <p class="subtitle">Histórico de planilhas processadas e links para relatório detalhado.</p>
      </div>
    </header>

    <div v-if="reports.length === 0" class="panel empty-panel">
      <div style="padding:24px">
        {{ isAdmin
          ? 'Nenhuma planilha processada ainda. Faça um upload para gerar relatórios.'
          : 'Aguardando a geração do relatório pela área administrativa.' }}
      </div>
    </div>

    <div v-else class="reports-list" style="display:grid;gap:12px">
      <article v-for="r in reports" :key="r.id" class="panel">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap">
          <div>
            <strong>{{ r.name }}</strong>
            <div style="color:#94a3b8;font-size:.9rem">Enviado: {{ formatDate(r.uploadedAt) }}</div>
          </div>
          <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
            <div style="text-align:right">
              <div style="color:#94a3b8;font-size:.85rem">Registros</div>
              <strong>{{ r.total }}</strong>
            </div>
            <div style="text-align:right">
              <div style="color:#94a3b8;font-size:.85rem">Válidos</div>
              <strong>{{ r.valid }}</strong>
            </div>
            <div v-if="r.invalid !== undefined" style="text-align:right">
              <div style="color:#94a3b8;font-size:.85rem">Inválidos</div>
              <strong>{{ r.invalid }}</strong>
            </div>
            <button class="primary-button" @click="openReport(r)">Abrir relatório</button>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.page-container { max-width:1280px;margin:0 auto;padding:32px 20px }
.eyebrow{color:#60a5fa;font-weight:700;letter-spacing:.12em;text-transform:uppercase}
.panel{background:#0f172a;border-radius:12px;padding:18px;border:1px solid rgba(148,163,184,.12)}
.empty-panel{color:#cbd5e1}
.primary-button{background:linear-gradient(135deg,#2563eb,#1d4ed8);color:white;padding:8px 12px;border-radius:8px;border:0}
</style>
