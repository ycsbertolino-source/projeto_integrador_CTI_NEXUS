<script setup>
import { computed } from 'vue'
import { usePlanilhaStore } from '@/stores/planilhaStore'
import { useRouter } from 'vue-router'

const store = usePlanilhaStore()
const router = useRouter()

const reports = computed(() => {
  // For now each uploaded spreadsheet counts as one report (store keeps last upload)
  // If you keep history, adapt this to read an array of uploads
  if (!store.dadosTratados || store.dadosTratados.length === 0) return []
  return [{
    id: 1,
    name: store.nomeArquivo || 'Planilha',
    uploadedAt: store.dataUpload,
    total: store.dadosTratados.length,
    valid: store.dadosTratados.filter(d => (d.erros || []).length === 0).length
  }]
})

function openReport(report) {
  // navigate to existing detailed report view (route name 'relatorio')
  router.push({ name: 'relatorio' })
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

    <div v-if="reports.length === 0" class="panel">
      <div style="padding:24px">Nenhuma planilha processada ainda. Faça um upload para gerar relatórios.</div>
    </div>

    <div v-else class="reports-list" style="display:grid;gap:12px">
      <article v-for="r in reports" :key="r.id" class="panel">
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <strong>{{ r.name }}</strong>
            <div style="color:#94a3b8;font-size:.9rem">Enviado: {{ r.uploadedAt ? new Date(r.uploadedAt).toLocaleString() : '—' }}</div>
          </div>
          <div style="display:flex;gap:12px;align-items:center">
            <div style="text-align:right">
              <div style="color:#94a3b8;font-size:.85rem">Registros</div>
              <strong>{{ r.total }}</strong>
            </div>
            <div style="text-align:right">
              <div style="color:#94a3b8;font-size:.85rem">Válidos</div>
              <strong>{{ r.valid }}</strong>
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
.primary-button{background:linear-gradient(135deg,#2563eb,#1d4ed8);color:white;padding:8px 12px;border-radius:8px;border:0}
</style>
