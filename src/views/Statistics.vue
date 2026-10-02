<script setup>
import { computed } from 'vue'
import { usePlanilhaStore } from '@/stores/planilhaStore'

const store = usePlanilhaStore()

// Helper to format currency
const fmtBRL = (v) => {
  if (v == null || isNaN(v)) return 'R$ 0'
  return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

// 1) Evolução de faturamento por mês (group by month/year)
const faturamentoPorMes = computed(() => {
  const mapa = new Map()
  for (const row of store.dadosTratados || []) {
    const date = row.data_contratacao_date
    const valor = row.faturamento_anual_num
    if (!date || !valor) continue
    const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
    mapa.set(key, (mapa.get(key) || 0) + valor)
  }
  // sort keys
  const entries = Array.from(mapa.entries()).sort((a, b) => a[0].localeCompare(b[0]))
  return entries.map(([k, v]) => ({ label: k, value: v }))
})

// 2) Clientes por nível
const clientesPorNivel = computed(() => {
  const mapa = {}
  for (const row of store.dadosTratados || []) {
    const n = (row.nivel_cliente || 'N/A')
    mapa[n] = (mapa[n] || 0) + 1
  }
  return Object.entries(mapa).map(([k, v]) => ({ label: k, value: v }))
})

// 3) Faturamento por segmento
const faturamentoPorSegmento = computed(() => {
  const mapa = {}
  for (const row of store.dadosTratados || []) {
    const seg = row.segmento || 'Outros'
    const val = Number(row.faturamento_anual_num) || 0
    mapa[seg] = (mapa[seg] || 0) + val
  }
  return Object.entries(mapa).map(([k, v]) => ({ label: k, value: v }))
})

// 4) Faturamento por consultor (top 6)
const faturamentoPorConsultor = computed(() => {
  const mapa = {}
  for (const row of store.dadosTratados || []) {
    const c = row.consultor || 'Sem consultor'
    const val = Number(row.faturamento_anual_num) || 0
    mapa[c] = (mapa[c] || 0) + val
  }
  return Object.entries(mapa).map(([k, v]) => ({ label: k, value: v })).sort((a, b) => b.value - a.value).slice(0, 6)
})

const charts = computed(() => {
  return [
    { title: 'Evolução de faturamento (por mês)', values: faturamentoPorMes.value.map(x => `${x.label}: ${fmtBRL(x.value)}`), color: '#2563eb' },
    { title: 'Clientes por nível', values: clientesPorNivel.value.map(x => `${x.label} ${x.value}`), color: '#0d9488' },
    { title: 'Faturamento por segmento', values: faturamentoPorSegmento.value.map(x => `${x.label} ${fmtBRL(x.value)}`), color: '#7c3aed' },
    { title: 'Faturamento por consultor (top)', values: faturamentoPorConsultor.value.map(x => `${x.label} ${fmtBRL(x.value)}`), color: '#f59e0b' },
  ]
})
</script>

<template>
  <section class="page-container">
    <header class="page-header">
      <div>
        <p class="eyebrow">Analise detalhada</p>
        <h1>Estatisticas</h1>
        <p class="subtitle">Explore os indicadores por periodo, nivel e segmento.</p>
      </div>
      <select aria-label="Periodo">
        <option>Ultimos 12 meses</option>
        <option>Ultimos 6 meses</option>
      </select>
    </header>

    <div class="stats-grid">
      <article v-for="chart in charts" :key="chart.title" class="chart-card">
        <h2>{{ chart.title }}</h2>
        <div class="fake-chart">
          <div v-for="(value, index) in chart.values" :key="value" class="chart-item">
            <i :style="{ height: (42 + index * 9) + '%', background: chart.color }"></i>
            <span>{{ value }}</span>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.page-container {
  max-width: 1280px;
  margin: 0 auto;
  min-height: 100vh;
  background: var(--color-app-bg);
  color: var(--color-body);
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

select {
  border: 1px solid var(--color-border-input);
  border-radius: 8px;
  padding: 10px 12px;
  background: var(--color-surface);
  color: var(--color-body);
}

.stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18px; }

.chart-card {
  min-height: 260px;
  padding: 22px;
  background: var(--color-panel);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(2, 6, 23, 0.45);
}

.chart-card h2 { margin: 0; color: var(--color-heading); font-size: 1rem; }

.fake-chart {
  height: 190px;
  display: flex;
  align-items: end;
  gap: 12px;
  padding-top: 25px;
}

.chart-item {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: end;
  gap: 8px;
}

.chart-item i { display: block; min-height: 18px; border-radius: 5px 5px 0 0; opacity: .9; }
.chart-item span {
  color: var(--color-muted);
  font-size: .65rem;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 700px) {
  .page-header { align-items: start; flex-direction: column; }
  .stats-grid { grid-template-columns: 1fr; }
  select { width: 100%; }
}
</style>