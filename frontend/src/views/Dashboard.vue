<script setup>
import { computed, onMounted, ref } from 'vue'
import api, { mensagemDeErro } from '@/services/api'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const dashboard = ref(null)
const lotes = ref([])
const loading = ref(true)
const erro = ref('')
const isAdmin = computed(() => authStore.user?.role === 'admin')
const estatisticas = computed(() => dashboard.value?.estatisticas || null)

async function carregarDashboard() {
  loading.value = true
  erro.value = ''
  try {
    const [resposta, respostaLotes] = await Promise.all([
      api.get('/api/dashboard'),
      api.get('/api/lotes'),
    ])
    dashboard.value = resposta.data
    lotes.value = respostaLotes.data
  } catch (error) {
    if (error.response?.status === 404) {
      dashboard.value = null
      lotes.value = []
    } else {
      erro.value = mensagemDeErro(error, 'Não foi possível carregar o painel.')
    }
  } finally {
    loading.value = false
  }
}

onMounted(carregarDashboard)

const contratacoes = computed(() => {
  const porAno = new Map()
  for (const mes of estatisticas.value?.porMes || []) {
    const ano = Number(String(mes.mes).slice(0, 4))
    if (Number.isFinite(ano)) porAno.set(ano, (porAno.get(ano) || 0) + Number(mes.contratacoes || 0))
  }
  const anos = [...porAno.keys()].sort((a, b) => a - b)
  if (!anos.length) return { ano: null, total: 0, variacao: '' }
  const ano = anos.at(-1)
  const anterior = porAno.get(ano - 1)
  let variacao = 'sem ano anterior para comparar'
  if (anterior) {
    const pct = ((porAno.get(ano) - anterior) / anterior) * 100
    variacao = `${pct >= 0 ? '+' : ''}${pct.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}% vs ${ano - 1}`
  }
  return { ano, total: porAno.get(ano), variacao }
})

const kpis = computed(() => [
  {
    label: 'Faturamento total',
    value: Number(estatisticas.value?.resumo?.total || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }),
    change: '',
    tone: 'success',
  },
  { label: 'Clientes ativos', value: String(estatisticas.value?.totalClientes || 0), change: '', tone: 'info' },
  { label: 'Planilhas processadas', value: String(lotes.value.length), change: '', tone: 'warning' },
  {
    label: contratacoes.value.ano ? `Contratações em ${contratacoes.value.ano}` : 'Contratações',
    value: String(contratacoes.value.total),
    change: contratacoes.value.variacao,
    tone: contratacoes.value.variacao.startsWith('-') ? 'warning' : 'success',
  },
])

const levels = computed(() => {
  const contagens = { A: 0, B: 0, C: 0 }
  for (const item of estatisticas.value?.porNivel || []) {
    if (Object.hasOwn(contagens, item.nivel)) contagens[item.nivel] = Number(item.clientes || 0)
  }
  const total = Object.values(contagens).reduce((soma, valor) => soma + valor, 0) || 1
  return [
    { label: 'Nivel A', value: Math.round((contagens.A / total) * 100), color: '#2563eb' },
    { label: 'Nivel B', value: Math.round((contagens.B / total) * 100), color: '#0d9488' },
    { label: 'Nivel C', value: Math.round((contagens.C / total) * 100), color: '#94a3b8' },
  ]
})

const segments = computed(() => {
  const dados = estatisticas.value?.porSegmento || []
  const total = dados.reduce((soma, item) => soma + Number(item.faturamentoTotal || 0), 0) || 1
  return dados.map(item => {
    const faturamento = Number(item.faturamentoTotal || 0)
    return {
      label: item.segmento,
      value: faturamento.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }),
      width: `${Math.round((faturamento / total) * 100)}%`,
    }
  })
})

const NOMES_MES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']
const meses = computed(() => (estatisticas.value?.porMes || []).slice(-8).map(item => {
  const [ano, mes] = String(item.mes).split('-').map(Number)
  return {
    key: item.mes,
    valor: Number(item.faturamentoTotal || 0),
    rotulo: `${NOMES_MES[mes - 1] || ''}/${String(ano).slice(2)}`,
  }
}))

const periodoGrafico = computed(() => {
  if (!meses.value.length) return ''
  const primeiro = meses.value[0].rotulo
  const ultimo = meses.value.at(-1).rotulo
  return primeiro === ultimo ? primeiro : `${primeiro} a ${ultimo}`
})

const pathForLineChart = computed(() => {
  const values = meses.value.map(item => item.valor)
  if (!values.length) return ''
  const max = Math.max(...values)
  const min = Math.min(...values)
  const gap = 670 / Math.max(values.length - 1, 1)
  const points = values.map((value, index) => {
    const x = Math.round(15 + index * gap)
    const norm = max === min ? 0.5 : (value - min) / (max - min)
    const y = Math.round(200 - (norm * 180))
    return `${x},${y}`
  })
  return `M${points.join(' L ')}`
})

function escaparHtml(valor) {
  return String(valor ?? '').replace(/[&<>"']/g, caractere => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  })[caractere])
}

function exportRelatorio() {
  if (!estatisticas.value) return
  const linhas = [
    ['Arquivo', dashboard.value.arquivo_nome],
    ['Status', dashboard.value.status],
    ['Clientes', estatisticas.value.totalClientes],
    ['Contratos', estatisticas.value.totalContratos],
    ['Faturamento total', estatisticas.value.resumo?.total],
    ['Faturamento médio', estatisticas.value.resumo?.media],
  ]
  const html = `
    <html><head><title>Relatório CTI</title>
      <style>
        body { font-family: Arial, sans-serif; padding: 24px; color: #0f172a; }
        h1 { margin-bottom: 10px; }
        table { width: 100%; border-collapse: collapse; margin-top: 18px; }
        th, td { border: 1px solid #cbd5e1; padding: 8px 10px; text-align: left; font-size: 12px; }
        th { background: #f1f5f9; }
      </style>
    </head><body>
      <h1>Relatório CTI</h1>
      <p>Gerado em: ${escaparHtml(new Date().toLocaleString('pt-BR'))}</p>
      <table><thead><tr><th>Indicador</th><th>Valor</th></tr></thead>
      <tbody>${linhas.map(([chave, valor]) => `<tr><td>${escaparHtml(chave)}</td><td>${escaparHtml(valor)}</td></tr>`).join('')}</tbody></table>
    </body></html>`

  const janela = window.open('', '_blank', 'width=900,height=700')
  if (!janela) {
    erro.value = 'Permita a abertura de janelas para exportar o relatório.'
    return
  }
  janela.document.write(html)
  janela.document.close()
  janela.focus()
  setTimeout(() => janela.print(), 300)
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
      <button class="primary-button" type="button" :disabled="!estatisticas" @click="exportRelatorio">
        Exportar PDF
      </button>
    </header>

    <div v-if="loading" class="panel empty-panel" role="status">Carregando indicadores...</div>
    <div v-else-if="erro" class="panel empty-panel error-panel" role="alert">{{ erro }}</div>
    <div v-else-if="!estatisticas" class="panel empty-panel">
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
            <div><h2>Evolucao de faturamento</h2><p>Por mês de contratação (até 8 meses)</p></div>
            <span v-if="periodoGrafico" class="chart-tag">{{ periodoGrafico }}</span>
          </div>
          <svg class="line-chart" viewBox="0 0 700 220" role="img" aria-label="Grafico de evolucao de faturamento">
            <path v-if="pathForLineChart" :d="pathForLineChart + ' L685 210 L15 210Z'" fill="url(#area)" opacity=".45" />
            <path :d="pathForLineChart" fill="none" stroke="#2563eb" stroke-width="4" stroke-linecap="round" />
            <defs><linearGradient id="area" x1="0" y1="0" x2="0" y2="1"><stop stop-color="#2563eb" stop-opacity=".28"/><stop offset="1" stop-color="#2563eb" stop-opacity="0"/></linearGradient></defs>
          </svg>
          <div class="chart-axis"><span v-for="mes in meses" :key="mes.key">{{ mes.rotulo }}</span></div>
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
          <div v-if="segments.length" class="segment-list">
            <div v-for="segment in segments" :key="segment.label" class="segment-row">
              <div><span>{{ segment.label }}</span><strong>{{ segment.value }}</strong></div>
              <div class="progress"><i :style="{ width: segment.width }"></i></div>
            </div>
          </div>
          <p v-else class="subtitle">Sem dados de segmento neste lote.</p>
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
.primary-button:disabled { opacity: .5; cursor: not-allowed; }
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
.empty-panel { padding: 24px; }.empty-panel h2 { margin-top: 0; }.error-panel { color: #fca5a5; }
@media (max-width: 900px) { .kpi-grid { grid-template-columns: repeat(2, 1fr); }.dashboard-grid { grid-template-columns: 1fr; }.segment-panel { grid-column: auto; } }
@media (max-width: 540px) { .page-header { align-items: start; flex-direction: column; }.primary-button { width: 100%; }.kpi-grid { gap: 10px; }.metric-card { padding: 14px; }.metric-card strong { font-size: 1.2rem; } }
</style>
