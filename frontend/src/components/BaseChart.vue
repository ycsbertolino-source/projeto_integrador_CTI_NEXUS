<script setup>
// BaseChart.vue — desenha UM gráfico com Chart.js. A tela de Gráficos usa este componente 5 vezes.
import { onMounted, onBeforeUnmount, ref, watch } from 'vue' // ciclo de vida: onMounted = "a tela abriu"
import { Chart, registerables } from 'chart.js' // Chart.js: a biblioteca que desenha gráficos no <canvas>

Chart.register(...registerables) // liga todos os tipos de gráfico (barra, linha, rosca...)
Chart.defaults.color = '#94a3b8' // cor dos textos (combina com o tema escuro do site)
Chart.defaults.borderColor = 'rgba(148, 163, 184, 0.15)' // cor das linhas de grade

const props = defineProps({ // o que a tela entrega ao componente
  tipo: { type: String, default: 'bar' }, // 'bar' | 'line' | 'doughnut'
  rotulos: { type: Array, default: () => [] }, // nomes (eixo X): Comércio, Indústria...
  valores: { type: Array, default: () => [] }, // números (altura das barras)
  titulo: { type: String, default: '' }, // nome da série (aparece no tooltip)
  horizontal: Boolean, // true = barras deitadas
  moeda: Boolean, // true = mostra os números em R$
})

const canvas = ref(null) // aponta para o <canvas> do template
let grafico = null // guarda o gráfico atual para poder apagar depois
const CORES = ['#3b82f6', '#ef4444', '#f59e0b', '#22c55e', '#a78bfa', '#94a3b8'] // paleta (uma cor por fatia da rosca)

// 1500000 -> "R$ 1.500.000" (ou "1.500.000" sem moeda)
const formatar = (n) => props.moeda
  ? Number(n).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 })
  : Number(n).toLocaleString('pt-BR')

function desenhar() { // monta e desenha o gráfico
  grafico?.destroy() // apaga o desenho antigo, se houver
  const rosca = props.tipo === 'doughnut' // rosca usa uma cor por fatia
  const eixoValor = props.horizontal ? 'x' : 'y' // em qual eixo ficam os números
  grafico = new Chart(canvas.value, { // cria o gráfico
    type: props.tipo, // tipo
    data: {
      labels: props.rotulos, // nomes
      datasets: [{
        label: props.titulo, // nome da série
        data: props.valores, // números vindos do banco
        backgroundColor: rosca ? CORES : props.tipo === 'line' ? 'rgba(59, 130, 246, 0.18)' : '#3b82f6', // fatias coloridas ou azul
        borderColor: rosca ? '#111827' : '#3b82f6', // borda das fatias / cor da linha
        borderRadius: props.tipo === 'bar' ? 6 : 0, // cantos arredondados nas barras
        fill: props.tipo === 'line', // pinta embaixo da linha
        tension: 0.3, // curva suave na linha
      }],
    },
    options: {
      locale: 'pt-BR', // números no padrão brasileiro: 5.000.000 (e não 5,000,000)
      indexAxis: props.horizontal ? 'y' : 'x', // 'y' deita as barras
      responsive: true, maintainAspectRatio: false, // ocupa a largura da caixa
      plugins: {
        legend: { display: rosca, position: 'bottom' }, // legenda só na rosca
        tooltip: { callbacks: { label: (c) => ` ${c.label}: ${formatar(c.parsed?.[eixoValor] ?? c.parsed)}` } }, // valor ao passar o mouse
      },
      scales: rosca ? {} : { [eixoValor]: { beginAtZero: true, ticks: { callback: (v) => formatar(v) } } }, // eixo dos números começa no zero
    },
  })
}

onMounted(desenhar) // desenha quando a tela abre
watch(() => [props.rotulos, props.valores], desenhar) // redesenha se os dados mudarem
onBeforeUnmount(() => grafico?.destroy()) // limpa ao sair da tela
</script>

<template>
  <div class="chart-box"> <!-- altura fixa da caixa do gráfico -->
    <canvas ref="canvas"></canvas> <!-- o quadro onde o Chart.js desenha -->
  </div>
</template>

<style scoped>
.chart-box { position: relative; height: 260px; }
</style>
