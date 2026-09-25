<script setup>
const charts = [
  { title: 'Evolucao de faturamento', values: ['R$ 82K', 'R$ 96K', 'R$ 108K', 'R$ 124K', 'R$ 136K', 'R$ 148K'], color: '#2563eb' },
  { title: 'Clientes por nivel', values: ['Nivel A 62%', 'Nivel B 28%', 'Nivel C 10%'], color: '#0d9488' },
  { title: 'Faturamento por segmento', values: ['Tecnologia 39%', 'Financeiro 29%', 'Varejo 20%', 'Industria 12%'], color: '#7c3aed' },
  { title: 'Faturamento por consultor', values: ['Ana 32%', 'Carlos 26%', 'Joao 23%', 'Maria 19%'], color: '#f59e0b' },
]
</script>

<template>
  <section class="page-container">
    <header class="page-header"><div><p class="eyebrow">Analise detalhada</p><h1>Estatisticas</h1><p class="subtitle">Explore os indicadores por periodo, nivel e segmento.</p></div><select aria-label="Periodo"><option>Ultimos 12 meses</option><option>Ultimos 6 meses</option></select></header>
    <div class="stats-grid">
      <article v-for="chart in charts" :key="chart.title" class="chart-card"><h2>{{ chart.title }}</h2><div class="fake-chart"><div v-for="(value, index) in chart.values" :key="value" class="chart-item"><i :style="{ height: (42 + index * 9) + '%', background: chart.color }"></i><span>{{ value }}</span></div></div></article>
    </div>
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
}.page-header { display: flex; justify-content: space-between; align-items: end; gap: 16px; margin-bottom: 28px; }.eyebrow { margin: 0 0 8px; color: #60a5fa; font-size: .72rem; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }h1 { margin: 0; color: #f8fafc; font-size: clamp(1.8rem, 4vw, 2.35rem); }.subtitle { color: #94a3b8; margin: 8px 0 0; }select { border: 1px solid rgba(148, 163, 184, 0.25); border-radius: 8px; padding: 10px 12px; background: rgba(15, 23, 42, 0.8); color: #e2e8f0; }.stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18px; }.chart-card { min-height: 260px; padding: 22px; background: #0f172a; border: 1px solid rgba(148, 163, 184, 0.18); border-radius: 12px; box-shadow: 0 10px 30px rgba(2, 6, 23, 0.45); }.chart-card h2 { margin: 0; color: #f8fafc; font-size: 1rem; }.fake-chart { height: 190px; display: flex; align-items: end; gap: 12px; padding-top: 25px; }.chart-item { flex: 1; height: 100%; display: flex; flex-direction: column; justify-content: end; gap: 8px; }.chart-item i { display: block; min-height: 18px; border-radius: 5px 5px 0 0; opacity: .9; }.chart-item span { color: #94a3b8; font-size: .65rem; text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }@media (max-width: 700px) { .page-header { align-items: start; flex-direction: column; }.stats-grid { grid-template-columns: 1fr; }select { width: 100%; } }
</style>