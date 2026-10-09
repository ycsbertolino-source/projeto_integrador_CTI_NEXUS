<script setup>
import { computed } from 'vue'
import { usePlanilhaStore } from '@/stores/planilhaStore'
import { useAuthStore } from '@/stores/authStore'

const store = usePlanilhaStore()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.user?.role === 'admin')

const totalFaturamento = computed(() => {
  return (store.dadosTratados || []).reduce((s, r) => s + (Number(r.faturamento_anual_num) || 0), 0)
})

const clientesAtivos = computed(() => (store.dadosTratados || []).length)

const planilhasProcessadas = computed(() => {
  if (Array.isArray(store.historico) && store.historico.length > 0) return store.historico.length
  return store.dataUpload ? 1 : 0
})

const kpis = computed(() => [
  { label: 'Faturamento total', value: totalFaturamento.value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }), change: '', tone: 'success' },
  { label: 'Clientes ativos', value: String(clientesAtivos.value), change: '', tone: 'info' },
  { label: 'Planilhas processadas', value: String(planilhasProcessadas.value), change: '', tone: 'warning' },
  { label: 'Contratacoes', value: isAdmin.value ? '24' : '12', change: isAdmin.value ? '+18,2%' : '+9,4%', tone: 'success' },
])

const levels = computed(() => {
  const mapa = { A: 0, B: 0, C: 0 }
  for (const r of store.dadosTratados || []) {
    const n = (r.nivel_cliente || '').toUpperCase()
    if (mapa[n] !== undefined) mapa[n]++
  }
  const total = mapa.A + mapa.B + mapa.C || 1
  return [
    { label: 'Nivel A', value: Math.round((mapa.A / total) * 100), color: '#2563eb' },
    { label: 'Nivel B', value: Math.round((mapa.B / total) * 100), color: '#0d9488' },
    { label: 'Nivel C', value: Math.round((mapa.C / total) * 100), color: '#94a3b8' },
  ]
})

const segments = computed(() => {
  const mapa = {}
  for (const r of store.dadosTratados || []) {
    const seg = r.segmento || 'Outros'
    const val = Number(r.faturamento_anual_num) || 0
    mapa[seg] = (mapa[seg] || 0) + val
  }
  const total = Object.values(mapa).reduce((s, v) => s + v, 0) || 1
  return Object.entries(mapa).map(([label, value]) => ({ label, value: value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }), width: Math.round((value / total) * 100) + '%' }))
})

const pathForLineChart = computed(() => {
  const mapa = new Map()
  for (const row of store.dadosTratados || []) {
    const d = row.data_contratacao_date
    const v = Number(row.faturamento_anual_num) || 0
    if (!d) continue
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
    mapa.set(key, (mapa.get(key) || 0) + v)
  }
  const entries = Array.from(mapa.entries()).sort((a, b) => a[0].localeCompare(b[0])).slice(-8)
  if (!entries.length) return ''

  const values = entries.map(e => e[1])
  const max = Math.max(...values)
  const min = Math.min(...values)
  const w = 700
  const h = 220
  const gap = w / Math.max(values.length - 1, 1)
  const points = values.map((v, i) => {
    const x = Math.round(15 + i * gap)
    const norm = max === min ? 0.5 : (v - min) / (max - min)
    const y = Math.round(h - (norm * (h - 40)) - 20)
    return `${x} ${y}`
  })

  return 'M' + points.map((p) => p.replace(' ', ',')).join(' L ')
})

function exportRelatorio() {
  const linhas = store.dadosTratados || []
  if (!linhas.length) return

  const colunas = Object.keys(linhas[0]).filter((coluna) => !['erros', 'numero_linha'].includes(coluna))

  const html = `
    <html>
      <head>
        <title>Relatório CTI</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 24px; color: #0f172a; }
          h1 { margin-bottom: 10px; }
          table { width: 100%; border-collapse: collapse; margin-top: 18px; }
          th, td { border: 1px solid #cbd5e1; padding: 8px 10px; text-align: left; font-size: 12px; }
          th { background: #f1f5f9; }
          .meta { color: #475569; font-size: 12px; margin-bottom: 8px; }
        </style>
      </head>
      <body>
        <h1>Relatório CTI</h1>
        <div class="meta">Gerado em: ${new Date().toLocaleString('pt-BR')}</div>
        <table>
          <thead>
            <tr>${colunas.map((coluna) => `<th>${coluna}</th>`).join('')}</tr>
          </thead>
          <tbody>
            ${linhas.map((linha) => `
              <tr>
                ${colunas.map((coluna) => `<td>${String(linha[coluna] ?? '').replace(/</g, '&lt;')}</td>`).join('')}
              </tr>
            `).join('')}
          </tbody>
        </table>
      </body>
    </html>
  `

  const printWindow = window.open('', '_blank', 'width=900,height=700')
  if (!printWindow) return

  printWindow.document.write(html)
  printWindow.document.close()
  printWindow.focus()
  setTimeout(() => {
    printWindow.print()
  }, 300)
}
</script>

<template>
  <section class="page-container">
    <header class="page-header">
      <div>
        <p class="eyebrow">Central de inteligencia</p>
        <h1>Visao geral</h1>
        <p class="subtitle">Acompanhe os principais indicadores da sua operacao.</p>
      </div>
      <button class="primary-button" type="button" :disabled="!store.dadosTratados.length" @click="exportRelatorio">
        Exportar PDF
      </button>
    </header>

    <div v-if="!store.dadosTratados.length" class="panel empty-panel">
      <h2>Dados ainda não carregados</h2>
      <p>
        {{ isAdmin ? 'Faça o upload de uma planilha para visualizar os indicadores do painel.' : 'Aguardando envio da planilha pela equipe administrativa.' }}
      </p>
    </div>

    <template v-else>
      <div class="kpi-grid">
        <article v-for="kpi in kpis" :key="kpi.label" class="metric-card">
          <span class="metric-label">{{ kpi.label }}</span>
          <strong>{{ kpi.value }}</strong>
          <small :class="kpi.tone">{{ kpi.change }}</small>
        </article>
      </div>

      <div class="dashboard-grid">
        <article class="panel chart-panel">
          <div class="panel-heading">
            <div><h2>Evolucao de faturamento</h2><p>Ultimos 8 meses</p></div>
            <span class="chart-tag">2026</span>
          </div>
          <svg class="line-chart" viewBox="0 0 700 220" role="img" aria-label="Grafico de evolucao de faturamento">
            <path :d="pathForLineChart" fill="none" stroke="#2563eb" stroke-width="4" stroke-linecap="round" />
            <path v-if="pathForLineChart" :d="pathForLineChart + ' L690 210 L15 210Z'" fill="url(#area)" opacity=".45" />
            <defs><linearGradient id="area" x1="0" y1="0" x2="0" y2="1"><stop stop-color="#2563eb" stop-opacity=".28"/><stop offset="1" stop-color="#2563eb" stop-opacity="0"/></linearGradient></defs>
          </svg>
          <div class="chart-axis"><span>Jan</span><span>Mar</span><span>Mai</span><span>Jul</span><span>Set</span><span>Nov</span></div>
        </article>

        <article class="panel">
          <div class="panel-heading"><div><h2>Clientes por nivel</h2><p>Classificacao atual</p></div></div>
          <div class="level-list">
            <div v-for="level in levels" :key="level.label" class="level-row">
              <div><span>{{ level.label }}</span><strong>{{ level.value }}%</strong></div>
              <div class="progress"><i :style="{ width: level.value + '%', background: level.color }"></i></div>
            </div>
          </div>
        </article>

        <article class="panel segment-panel">
          <div class="panel-heading"><div><h2>Faturamento por segmento</h2><p>Distribuicao por mercado</p></div></div>
          <div class="segment-list">
            <div v-for="segment in segments" :key="segment.label" class="segment-row">
              <div><span>{{ segment.label }}</span><strong>{{ segment.value }}</strong></div>
              <div class="progress"><i :style="{ width: segment.width }"></i></div>
            </div>
          </div>
        </article>
      </div>
    </template>
  </section>
</template>

<style scoped>
.page-container {
  max-width: 1280px;
  margin: 0 auto;
  min-height: 100vh;
  background: #050b14;
  color: #e2e8f0;
  padding: 32px 20px;
}
.page-header { display: flex; justify-content: space-between; gap: 20px; align-items: end; margin-bottom: 28px; }
.eyebrow { margin: 0 0 8px; color: #60a5fa; font-size: .72rem; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }
h1 { margin: 0; color: #f8fafc; font-size: clamp(1.8rem, 4vw, 2.35rem); }
.subtitle, .panel-heading p { color: #94a3b8; margin: 8px 0 0; }
.primary-button { border: 0; border-radius: 8px; padding: 11px 16px; background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; font-weight: 700; cursor: pointer; }
.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.metric-card, .panel { background: #0f172a; border: 1px solid rgba(148, 163, 184, 0.18); border-radius: 12px; box-shadow: 0 10px 30px rgba(2, 6, 23, 0.45); }
.metric-card { padding: 20px; display: grid; gap: 11px; }
.metric-label { color: #94a3b8; font-size: .8rem; }
.metric-card strong { color: #f8fafc; font-size: 1.55rem; }
.metric-card small { font-weight: 700; font-size: .75rem; }.success { color: #34d399; }.info { color: #7dd3fc; }.warning { color: #fbbf24; }
.dashboard-grid { display: grid; grid-template-columns: 1.5fr 1fr; gap: 18px; margin-top: 18px; }
.panel { padding: 22px; }.chart-panel { min-width: 0; }.segment-panel { grid-column: 1 / -1; }
.panel-heading { display: flex; justify-content: space-between; gap: 16px; align-items: start; }.panel-heading h2 { margin: 0; color: #f8fafc; font-size: 1rem; }.chart-tag { color: #bfdbfe; background: rgba(37, 99, 235, 0.18); border: 1px solid rgba(96, 165, 250, 0.25); border-radius: 6px; padding: 5px 9px; font-size: .75rem; font-weight: 700; }
.line-chart { width: 100%; height: 220px; margin-top: 22px; }.chart-axis { display: flex; justify-content: space-between; color: #94a3b8; font-size: .7rem; }
.level-list, .segment-list { display: grid; gap: 22px; margin-top: 28px; }.level-row > div:first-child, .segment-row > div:first-child { display: flex; justify-content: space-between; margin-bottom: 9px; color: #e2e8f0; font-size: .82rem; }.level-row strong, .segment-row strong { color: #f8fafc; }.progress { height: 8px; overflow: hidden; border-radius: 20px; background: rgba(148, 163, 184, 0.14); }.progress i { display: block; height: 100%; border-radius: inherit; background: #0d9488; }
@media (max-width: 900px) { .kpi-grid { grid-template-columns: repeat(2, 1fr); }.dashboard-grid { grid-template-columns: 1fr; }.segment-panel { grid-column: auto; } }
@media (max-width: 540px) { .page-header { align-items: start; flex-direction: column; }.primary-button { width: 100%; }.kpi-grid { gap: 10px; }.metric-card { padding: 14px; }.metric-card strong { font-size: 1.2rem; } }
</style>