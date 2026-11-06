<script setup>
import { computed, ref } from 'vue'
import { usePlanilhaStore } from '@/stores/planilhaStore'
import { useAuthStore } from '@/stores/authStore'

const store = usePlanilhaStore()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.user?.role === 'admin')

const fmtBRL = (v) => {
  if (v == null || isNaN(v)) return 'R$ 0'
  return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}
// "R$ 1.850.000" -> "R$ 1,9 mi" para caber embaixo da barra
const fmtCurto = (v) => {
  if (v >= 1e6) return `R$ ${(v / 1e6).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} mi`
  if (v >= 1e3) return `R$ ${(v / 1e3).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} mil`
  return fmtBRL(v)
}

const dataDe = (row) => {
  const d = row.data_contratacao_date ? new Date(row.data_contratacao_date) : null
  return d && !Number.isNaN(d.getTime()) ? d : null
}

// ---- Filtro de período ----
// A planilha pode ter datas antigas, então o período conta a partir da contratação MAIS RECENTE da planilha.
const periodo = ref('todos') // 'todos' | '12' | '6'

const ultimaData = computed(() => {
  let maior = null
  for (const row of store.dadosTratados || []) {
    const d = dataDe(row)
    if (d && (!maior || d > maior)) maior = d
  }
  return maior
})

const linhas = computed(() => {
  const todas = store.dadosTratados || []
  if (periodo.value === 'todos' || !ultimaData.value) return todas
  const inicio = new Date(ultimaData.value)
  inicio.setMonth(inicio.getMonth() - Number(periodo.value))
  return todas.filter((row) => {
    const d = dataDe(row)
    return d && d > inicio
  })
})

const descricaoPeriodo = computed(() => {
  if (periodo.value === 'todos' || !ultimaData.value) return `${linhas.value.length} clientes no total`
  return `${linhas.value.length} clientes contratados nos ${periodo.value} meses até ${ultimaData.value.toLocaleDateString('pt-BR')}`
})

// ---- Agrupamentos ----
const NOMES_MES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']

const faturamentoPorMes = computed(() => {
  const mapa = new Map()
  for (const row of linhas.value) {
    const date = dataDe(row)
    const valor = Number(row.faturamento_anual_num) || 0
    if (!date || !valor) continue
    const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
    mapa.set(key, (mapa.get(key) || 0) + valor)
  }
  return Array.from(mapa.entries()).sort((a, b) => a[0].localeCompare(b[0])).map(([k, v]) => {
    const [ano, mes] = k.split('-').map(Number)
    return { label: `${NOMES_MES[mes - 1]}/${String(ano).slice(2)}`, value: v, texto: fmtCurto(v) }
  })
})

const clientesPorNivel = computed(() => {
  const mapa = {}
  for (const row of linhas.value) {
    const n = (row.nivel_cliente || 'N/A')
    mapa[n] = (mapa[n] || 0) + 1
  }
  return Object.entries(mapa).sort((a, b) => a[0].localeCompare(b[0]))
    .map(([k, v]) => ({ label: `Nível ${k}`, value: v, texto: `${v} ${v === 1 ? 'cliente' : 'clientes'}` }))
})

const somaPor = (campo, vazio) => {
  const mapa = {}
  for (const row of linhas.value) {
    const chave = row[campo] || vazio
    mapa[chave] = (mapa[chave] || 0) + (Number(row.faturamento_anual_num) || 0)
  }
  return Object.entries(mapa).map(([k, v]) => ({ label: k, value: v, texto: fmtCurto(v) }))
    .sort((a, b) => b.value - a.value)
}

const faturamentoPorSegmento = computed(() => somaPor('segmento', 'Outros'))
const faturamentoPorConsultor = computed(() => somaPor('consultor', 'Sem consultor').slice(0, 6))

// Altura de cada barra proporcional ao maior valor do gráfico
const comAltura = (itens) => {
  const max = Math.max(...itens.map((x) => x.value), 0)
  return itens.map((x) => ({ ...x, altura: max ? Math.max(4, Math.round((x.value / max) * 100)) : 0 }))
}

const charts = computed(() => [
  { title: 'Evolução de faturamento (por mês de contratação)', itens: comAltura(faturamentoPorMes.value), color: '#2563eb' },
  { title: 'Clientes por nível', itens: comAltura(clientesPorNivel.value), color: '#0d9488' },
  { title: 'Faturamento por segmento', itens: comAltura(faturamentoPorSegmento.value), color: '#7c3aed' },
  { title: 'Faturamento por consultor (top 6)', itens: comAltura(faturamentoPorConsultor.value), color: '#f59e0b' },
])
</script>

<template>
  <section class="page-container">
    <header class="page-header">
      <div>
        <p class="eyebrow">Analise detalhada</p>
        <h1>Estatisticas</h1>
        <p class="subtitle">Explore os indicadores por periodo, nivel e segmento.</p>
        <p v-if="store.dadosTratados.length" class="period-info">{{ descricaoPeriodo }}</p>
      </div>
      <select v-model="periodo" aria-label="Periodo">
        <option value="todos">Todo o período</option>
        <option value="12">Últimos 12 meses</option>
        <option value="6">Últimos 6 meses</option>
      </select>
    </header>

    <div v-if="!store.dadosTratados.length" class="panel empty-panel">
      <h2>Estatísticas indisponíveis</h2>
      <p>
        {{ isAdmin ? 'Faça o upload de uma planilha para gerar os gráficos analíticos.' : 'As métricas ficam disponíveis assim que a equipe administrativa carregar os dados.' }}
      </p>
    </div>

    <div v-else class="stats-grid">
      <article v-for="chart in charts" :key="chart.title" class="chart-card">
        <h2>{{ chart.title }}</h2>
        <p v-if="!chart.itens.length" class="chart-empty">Nenhum dado neste período.</p>
        <div v-else class="bar-chart">
          <div v-for="(item, index) in chart.itens" :key="index" class="chart-item" :title="`${item.label}: ${item.texto}`">
            <small class="bar-value">{{ item.texto }}</small>
            <div class="bar-track"><i :style="{ height: item.altura + '%', background: chart.color }"></i></div>
            <span>{{ item.label }}</span>
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

.panel {
  background: var(--color-panel);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(2, 6, 23, 0.45);
}

.empty-panel {
  padding: 28px;
  color: var(--color-body);
}

.empty-panel h2 {
  margin: 0 0 8px;
  color: var(--color-heading);
}

.empty-panel p {
  margin: 0;
  color: var(--color-muted);
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

.bar-chart {
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

.chart-item i { display: block; border-radius: 5px 5px 0 0; opacity: .9; }
.chart-item span {
  color: var(--color-muted);
  font-size: .65rem;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bar-chart .chart-item { min-width: 0; }
.bar-track { flex: 1; display: flex; flex-direction: column; justify-content: flex-end; }
.bar-track i { width: 100%; }
.bar-value {
  color: var(--color-body);
  font-size: .62rem;
  font-weight: 700;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.chart-empty { color: var(--color-muted); font-size: .85rem; margin: 24px 0 0; }
.period-info { color: #93c5fd; font-size: .8rem; margin: 6px 0 0; }

@media (max-width: 700px) {
  .page-header { align-items: start; flex-direction: column; }
  .stats-grid { grid-template-columns: 1fr; }
  select { width: 100%; }
}
</style>