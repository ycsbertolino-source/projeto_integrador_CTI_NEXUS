<script setup>
const kpis = [
  { label: 'Faturamento total', value: 'R$ 842.000', change: '+12,4%', tone: 'success' },
  { label: 'Clientes ativos', value: '128', change: '+8 este mes', tone: 'info' },
  { label: 'Planilhas processadas', value: '36', change: '+5 este mes', tone: 'warning' },
  { label: 'Contratacoes', value: '24', change: '+18,2%', tone: 'success' },
]

const levels = [
  { label: 'Nivel A', value: 62, color: '#2563eb' },
  { label: 'Nivel B', value: 28, color: '#0d9488' },
  { label: 'Nivel C', value: 10, color: '#94a3b8' },
]

const segments = [
  { label: 'Tecnologia', value: 'R$ 328K', width: '78%' },
  { label: 'Financeiro', value: 'R$ 246K', width: '58%' },
  { label: 'Varejo', value: 'R$ 172K', width: '42%' },
  { label: 'Industria', value: 'R$ 96K', width: '24%' },
]
</script>

<template>
  <section class="page-container">
    <header class="page-header">
      <div>
        <p class="eyebrow">Central de inteligencia</p>
        <h1>Visao geral</h1>
        <p class="subtitle">Acompanhe os principais indicadores da sua operacao.</p>
      </div>
      <button class="primary-button" type="button">Exportar relatorio</button>
    </header>

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
          <path d="M15 184 C75 170 100 150 155 158 S240 126 290 138 S380 95 430 111 S520 62 575 80 S645 32 690 48" fill="none" stroke="#2563eb" stroke-width="4" stroke-linecap="round" />
          <path d="M15 184 C75 170 100 150 155 158 S240 126 290 138 S380 95 430 111 S520 62 575 80 S645 32 690 48 L690 210 L15 210Z" fill="url(#area)" opacity=".45" />
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
  </section>
</template>

<style scoped>
.page-container { max-width: 1280px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; gap: 20px; align-items: end; margin-bottom: 28px; }
.eyebrow { margin: 0 0 8px; color: #2563eb; font-size: .72rem; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }
h1 { margin: 0; color: #0f172a; font-size: clamp(1.8rem, 4vw, 2.35rem); }
.subtitle, .panel-heading p { color: #64748b; margin: 8px 0 0; }
.primary-button { border: 0; border-radius: 8px; padding: 11px 16px; background: #2563eb; color: white; font-weight: 700; cursor: pointer; }
.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.metric-card, .panel { background: white; border: 1px solid #e2e8f0; border-radius: 12px; box-shadow: 0 2px 8px rgba(15, 23, 42, .04); }
.metric-card { padding: 20px; display: grid; gap: 11px; }
.metric-label { color: #64748b; font-size: .8rem; }
.metric-card strong { color: #1e293b; font-size: 1.55rem; }
.metric-card small { font-weight: 700; font-size: .75rem; }.success { color: #059669; }.info { color: #0891b2; }.warning { color: #d97706; }
.dashboard-grid { display: grid; grid-template-columns: 1.5fr 1fr; gap: 18px; margin-top: 18px; }
.panel { padding: 22px; }.chart-panel { min-width: 0; }.segment-panel { grid-column: 1 / -1; }
.panel-heading { display: flex; justify-content: space-between; gap: 16px; align-items: start; }.panel-heading h2 { margin: 0; color: #1e293b; font-size: 1rem; }.chart-tag { color: #2563eb; background: #eff6ff; border-radius: 6px; padding: 5px 9px; font-size: .75rem; font-weight: 700; }
.line-chart { width: 100%; height: 220px; margin-top: 22px; }.chart-axis { display: flex; justify-content: space-between; color: #94a3b8; font-size: .7rem; }
.level-list, .segment-list { display: grid; gap: 22px; margin-top: 28px; }.level-row > div:first-child, .segment-row > div:first-child { display: flex; justify-content: space-between; margin-bottom: 9px; color: #475569; font-size: .82rem; }.level-row strong, .segment-row strong { color: #1e293b; }.progress { height: 8px; overflow: hidden; border-radius: 20px; background: #f1f5f9; }.progress i { display: block; height: 100%; border-radius: inherit; background: #0d9488; }
@media (max-width: 900px) { .kpi-grid { grid-template-columns: repeat(2, 1fr); }.dashboard-grid { grid-template-columns: 1fr; }.segment-panel { grid-column: auto; } }
@media (max-width: 540px) { .page-header { align-items: start; flex-direction: column; }.primary-button { width: 100%; }.kpi-grid { gap: 10px; }.metric-card { padding: 14px; }.metric-card strong { font-size: 1.2rem; } }
</style>