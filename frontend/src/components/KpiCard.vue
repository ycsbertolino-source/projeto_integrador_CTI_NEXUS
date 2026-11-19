<script setup>
// KpiCard.vue — cartãozinho com um número grande (ex.: "Média: R$ 2.261.250").
defineProps({ titulo: String, valor: String, destaque: Boolean }) // defineProps: o que a tela pai nos entrega
</script>

<template>
  <div class="kpi" :class="{ destaque }"> <!-- borda de cima colorida -->
    <p class="kpi-titulo">{{ titulo }}</p> <!-- rótulo -->
    <p class="kpi-valor">{{ valor }}</p> <!-- número -->
  </div>
</template>

<style scoped>
.kpi { background: var(--color-panel); border: 1px solid var(--color-border-muted); border-top: 3px solid #3b82f6; border-radius: 12px; padding: 14px 16px; min-width: 0; }
.kpi.destaque { border-top-color: #ef4444; }
.kpi-titulo { margin: 0; color: var(--color-muted); font-size: .82rem; }
.kpi-valor { margin: 6px 0 0; color: var(--color-heading); font-size: 1.3rem; font-weight: 700; overflow-wrap: anywhere; }
</style>
