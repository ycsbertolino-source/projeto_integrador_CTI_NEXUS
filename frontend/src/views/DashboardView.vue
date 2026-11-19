<script setup>
// DashboardView.vue — gráficos com os dados que o Python calculou e GRAVOU NO BANCO (Azure ou Neon).
// Caminho do dado: banco (dashboard_lote) -> Spring GET /api/dashboard -> esta tela -> Chart.js.
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue' // reactive: objeto reativo só desta tela
import { useRoute, useRouter } from 'vue-router' // lê/atualiza o ?loteId= da URL
import { dashboardService, mensagemDeErro } from '@/services/api' // chamadas ao backend (sem store: só esta tela usa)
import BaseChart from '@/components/BaseChart.vue' // desenha gráficos
import KpiCard from '@/components/KpiCard.vue' // cartão de número
import StatusBadge from '@/components/StatusBadge.vue' // etiqueta de status

const route = useRoute()
const router = useRouter()

const moeda = (n) => (n == null ? '—' : Number(n).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })) // 1500 -> R$ 1.500,00
const numero = (n) => (n == null ? '—' : Number(n).toLocaleString('pt-BR', { maximumFractionDigits: 2 })) // 1234.5 -> 1.234,5
const dataHora = (t) => (t ? new Date(t).toLocaleString('pt-BR') : '—') // "2026-10-28T17:05:00" -> 28/10/2026 17:05:00
const NOMES_MES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']
const rotuloMes = (texto) => { // "2024-03" -> "Mar/24"
  const [ano, mes] = String(texto).split('-').map(Number)
  return NOMES_MES[mes - 1] ? `${NOMES_MES[mes - 1]}/${String(ano).slice(2)}` : texto
}

const dash = reactive({ // dados só desta tela (por isso não precisa de Pinia)
  lote: null, // o lote mostrado (status, totais)
  geradoEm: null, // quando o Python gravou as contas
  estatisticas: null, // JSON do Python (null enquanto o ETL não terminou)
  lotes: [], // histórico de lotes gravados no banco (para a caixa de escolha)
  erro: '', // mensagem de erro
  carregando: false, // true durante a busca
  async carregar(loteId) { // GET /api/dashboard
    this.carregando = true; this.erro = '' // começa
    try {
      const resp = await dashboardService.obter(loteId) // pede ao Spring, que lê o banco
      this.lote = resp.lote; this.geradoEm = resp.geradoEm; this.estatisticas = resp.estatisticas // guarda o lote e o JSON (ou null)
    } catch (e) {
      this.lote = null; this.estatisticas = null
      this.erro = mensagemDeErro(e, 'Não foi possível carregar os gráficos.') // 404 = ainda não há lote no banco
    } finally {
      this.carregando = false // desliga o 'Carregando...'
    }
  },
  async carregarLotes() { // GET /api/dashboard/lotes
    try { this.lotes = await dashboardService.lotes() } catch { this.lotes = [] } // se falhar, a caixa fica vazia
  },
})
const escolhido = ref(route.query.loteId ? String(route.query.loteId) : '') // id do lote escolhido ('' = o mais recente). Vem da tela Relatórios pelo ?loteId=
const d = computed(() => dash.estatisticas) // atalho: d.value.resumo, d.value.porSegmento...
let relogio = null // guarda o timer de atualização

// Cada gráfico lê uma lista que o Python gravou no JSON e separa em "nomes" e "números".
const seg = computed(() => d.value?.porSegmento ?? []) // clientes por segmento
const niv = computed(() => d.value?.porNivel ?? []) // clientes por nível
const con = computed(() => d.value?.porConsultor ?? []) // faturamento por consultor
const ser = computed(() => d.value?.porServico ?? []) // serviços mais contratados (clientes que contrataram)
const mes = computed(() => d.value?.porMes ?? []) // contratações por mês
const top = computed(() => d.value?.topClientes ?? []) // os 5 maiores clientes

async function buscar() { // pede os dados ao Spring
  await dash.carregar(escolhido.value || undefined) // sem escolha: o Spring devolve o último lote PROCESSADO
  const processando = dash.lote?.status === 'PROCESSANDO' // o Python ainda está trabalhando?
  if (processando && !relogio) relogio = setInterval(buscar, 5000) // pergunta de novo a cada 5 s
  if (!processando && relogio) { clearInterval(relogio); relogio = null } // terminou (ou deu erro): para o timer
}

async function trocarLote() { // a pessoa escolheu outro lote na caixa
  if (relogio) { clearInterval(relogio); relogio = null } // para o timer antigo
  router.replace({ query: escolhido.value ? { loteId: escolhido.value } : {} }) // guarda a escolha na URL
  await buscar() // busca o lote escolhido
}

onMounted(async () => { await dash.carregarLotes(); await buscar() }) // ao abrir: lista os lotes e mostra o mais recente
onBeforeUnmount(() => relogio && clearInterval(relogio)) // limpa o timer ao sair

// ---- Exportar: abre uma janela com o resumo pronta para imprimir ou salvar em PDF ----
function escaparHtml(valor) {
  return String(valor ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c])
}
function exportar() {
  if (!d.value) return
  const linhas = [
    ['Arquivo', dash.lote?.arquivoNome], ['Lote', dash.lote?.id], ['Status', dash.lote?.status],
    ['Clientes', d.value.totalClientes], ['Faturamento total', moeda(d.value.resumo?.total)],
    ['Média', moeda(d.value.resumo?.media)], ['Mediana', moeda(d.value.resumo?.mediana)],
    ['Desvio padrão', moeda(d.value.resumo?.desvioPadrao)],
  ]
  const html = `<html><head><title>Relatório CTI</title><style>
      body { font-family: Arial, sans-serif; padding: 24px; color: #0f172a; }
      table { width: 100%; border-collapse: collapse; margin-top: 18px; }
      th, td { border: 1px solid #cbd5e1; padding: 8px 10px; text-align: left; font-size: 12px; }
      th { background: #f1f5f9; } li { margin: 4px 0; }
    </style></head><body>
    <h1>Relatório CTI</h1><p>Gerado em: ${escaparHtml(new Date().toLocaleString('pt-BR'))}</p>
    <table><thead><tr><th>Indicador</th><th>Valor</th></tr></thead>
    <tbody>${linhas.map(([k, v]) => `<tr><td>${escaparHtml(k)}</td><td>${escaparHtml(v)}</td></tr>`).join('')}</tbody></table>
    ${(d.value.insights || []).length ? `<h2>O que os dados mostram</h2><ul>${d.value.insights.map((i) => `<li>${escaparHtml(i)}</li>`).join('')}</ul>` : ''}
    </body></html>`
  const janela = window.open('', '_blank', 'width=900,height=700')
  if (!janela) { dash.erro = 'Permita a abertura de janelas para exportar o relatório.'; return }
  janela.document.write(html); janela.document.close(); janela.focus()
  setTimeout(() => janela.print(), 300)
}
</script>

<template>
  <section class="page-container">
    <header class="page-header"> <!-- título + caixa de escolha -->
      <div>
        <p class="eyebrow">Central de inteligência</p>
        <h1>Gráficos da carteira</h1>
        <p v-if="dash.lote" class="subtitle lote-info"> <!-- mostra o lote só se já existir -->
          Lote #{{ dash.lote.id }} · {{ dash.lote.arquivoNome }} · enviado em {{ dataHora(dash.lote.enviadoEm) }}
          <StatusBadge :status="dash.lote.status" />
        </p>
      </div>
      <div class="header-actions">
        <label v-if="dash.lotes.length" class="lote-select"> <!-- caixa de escolha do lote -->
          Planilha (lote)
          <select v-model="escolhido" @change="trocarLote"> <!-- ao trocar, busca de novo -->
            <option value="">Mais recente processado</option> <!-- padrão -->
            <option v-for="l in dash.lotes" :key="l.id" :value="String(l.id)">#{{ l.id }} · {{ l.arquivoNome }} · {{ l.status }}</option>
          </select>
        </label>
        <button v-if="d" type="button" class="ghost-button" @click="exportar">Exportar</button>
      </div>
    </header>

    <p v-if="dash.erro" class="aviso aviso-laranja">{{ dash.erro }} <RouterLink to="/app/upload">Enviar planilha</RouterLink></p> <!-- ex.: nenhum lote ainda -->
    <p v-else-if="dash.carregando && !d" class="aviso">Carregando...</p>
    <p v-else-if="dash.lote?.status === 'ERRO'" class="aviso aviso-vermelho">O Python não conseguiu processar este lote: {{ dash.lote.mensagem }}</p> <!-- o Python gravou ERRO: mostra o motivo -->
    <p v-else-if="dash.lote && !d" class="aviso aviso-azul">O Python está processando a planilha. Esta tela atualiza sozinha.</p> <!-- lote existe, mas o Python ainda não terminou -->

    <template v-if="d"> <!-- só mostra os gráficos se há dados -->
      <p class="origem">Dados lidos do banco (tabela <b>dashboard_lote</b>) · calculados pelo Python em {{ dataHora(dash.geradoEm) }}</p>

      <div class="kpi-grid"> <!-- cartões de resumo -->
        <KpiCard titulo="Clientes" :valor="String(d.totalClientes ?? 0)" />
        <KpiCard titulo="Faturamento total" :valor="moeda(d.resumo?.total)" />
        <KpiCard titulo="Média" :valor="moeda(d.resumo?.media)" destaque /> <!-- média calculada pelo Python -->
        <KpiCard titulo="Mediana" :valor="moeda(d.resumo?.mediana)" />
        <KpiCard titulo="Desvio padrão" :valor="moeda(d.resumo?.desvioPadrao)" />
      </div>

      <div class="chart-grid"> <!-- 1 coluna no celular, 2 no computador -->
        <div class="panel">
          <h2>Clientes por segmento</h2>
          <BaseChart tipo="bar" titulo="Clientes" :rotulos="seg.map((x) => x.segmento)" :valores="seg.map((x) => x.clientes)" />
        </div>
        <div class="panel">
          <h2>Clientes por nível</h2>
          <BaseChart tipo="doughnut" titulo="Clientes" :rotulos="niv.map((x) => 'Nível ' + x.nivel)" :valores="niv.map((x) => x.clientes)" />
        </div>
        <div class="panel">
          <h2>Faturamento por consultor</h2>
          <BaseChart tipo="bar" horizontal moeda titulo="Faturamento" :rotulos="con.map((x) => x.consultor)" :valores="con.map((x) => x.faturamentoTotal)" />
        </div>
        <div class="panel">
          <h2>Serviços mais contratados</h2>
          <BaseChart tipo="bar" horizontal titulo="Clientes" :rotulos="ser.map((x) => x.servico)" :valores="ser.map((x) => x.clientes)" />
        </div>
        <div class="panel span-2">
          <h2>Contratações por mês</h2>
          <BaseChart tipo="line" titulo="Contratações" :rotulos="mes.map((x) => rotuloMes(x.mes))" :valores="mes.map((x) => x.contratacoes)" />
        </div>
      </div>

      <div v-if="top.length" class="panel table-panel"> <!-- tabela: os 5 maiores clientes -->
        <h2>Maiores clientes</h2>
        <div class="table-scroll">
          <table>
            <thead>
              <tr><th>Código</th><th>Cliente</th><th>Segmento</th><th>Nível</th><th class="num">Faturamento anual</th></tr>
            </thead>
            <tbody>
              <tr v-for="c in top" :key="c.codigoCliente">
                <td>{{ c.codigoCliente }}</td><td>{{ c.nomeCliente }}</td><td>{{ c.segmento ?? '—' }}</td><td>{{ c.nivel ?? '—' }}</td><td class="num">{{ moeda(c.faturamentoAnual) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="d.insights?.length" class="panel insights"> <!-- frases geradas pelo Python -->
        <h2>O que os dados mostram</h2>
        <ul><li v-for="i in d.insights" :key="i">{{ i }}</li></ul>
      </div>
      <p class="origem">Linhas recebidas: {{ numero(dash.lote?.totalLinhas) }} · válidas: {{ numero(dash.lote?.linhasValidas) }} · com aviso: {{ numero(dash.lote?.linhasComAviso) }}</p>
    </template>
  </section>
</template>

<style scoped>
.page-container { max-width: 1280px; margin: 0 auto; padding: 32px 20px; }
.page-header { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: flex-end; gap: 16px; margin-bottom: 20px; }
.eyebrow { margin: 0; color: #60a5fa; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; font-size: .78rem; }
h1 { margin: 6px 0 0; color: var(--color-heading); font-size: 1.8rem; }
.subtitle { color: var(--color-muted); margin: 8px 0 0; }
.lote-info { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; }
.header-actions { display: flex; align-items: flex-end; gap: 10px; flex-wrap: wrap; }
.lote-select { display: flex; flex-direction: column; gap: 4px; color: var(--color-muted); font-size: .8rem; }
.lote-select select { background: var(--color-surface); color: var(--color-body); border: 1px solid var(--color-border-input); border-radius: 8px; padding: 8px 10px; max-width: 100%; }
.ghost-button { background: transparent; color: #bfdbfe; padding: 9px 14px; border-radius: 8px; border: 1px solid rgba(96, 165, 250, .45); cursor: pointer; }
.aviso { padding: 12px 14px; border-radius: 10px; color: var(--color-muted); }
.aviso a { color: inherit; font-weight: 700; margin-left: 4px; }
.aviso-laranja { color: #fde68a; background: rgba(245, 158, 11, .12); border: 1px solid rgba(245, 158, 11, .3); }
.aviso-vermelho { color: #fecaca; background: rgba(239, 68, 68, .12); border: 1px solid rgba(239, 68, 68, .3); }
.aviso-azul { color: #bfdbfe; background: rgba(59, 130, 246, .12); border: 1px solid rgba(59, 130, 246, .3); }
.origem { color: var(--color-faint); font-size: .78rem; margin: 0 0 12px; }
.origem b { color: var(--color-muted); }
.kpi-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 12px; margin-bottom: 20px; }
.chart-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.panel { background: var(--color-panel); border: 1px solid var(--color-border-muted); border-radius: 12px; padding: 16px 18px; min-width: 0; }
.panel h2 { margin: 0 0 12px; color: var(--color-heading); font-size: 1rem; }
.span-2 { grid-column: span 2; }
.table-panel, .insights { margin-top: 20px; }
.table-scroll { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: .86rem; }
th { text-align: left; color: var(--color-muted); font-weight: 600; padding: 8px; border-bottom: 1px solid var(--color-border); }
td { padding: 8px; border-bottom: 1px solid var(--color-border-muted); }
.num { text-align: right; white-space: nowrap; }
.insights { background: rgba(59, 130, 246, .08); border-color: rgba(59, 130, 246, .25); }
.insights ul { margin: 0; padding-left: 20px; }
.insights li { margin: 4px 0; }
@media (max-width: 1100px) { .kpi-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
@media (max-width: 760px) {
  .page-container { padding: 24px 16px; }
  .kpi-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .chart-grid { grid-template-columns: 1fr; }
  .span-2 { grid-column: auto; }
}
</style>
