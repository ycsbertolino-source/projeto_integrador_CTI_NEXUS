<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUploadStore } from '@/store/uploadStore'
import { FileSpreadsheet, CheckCircle2, AlertTriangle, LayoutGrid, ArrowLeft } from '@/lib/lucide-vue-next'

const store = useUploadStore()
const router = useRouter()

// Campos conhecidos da planilha, usados pra extrair o "tipo" de cada mensagem de erro
// (o store guarda a mensagem já pronta em texto, ex: "codigo_cliente está em branco.")
const CAMPOS_CONHECIDOS = [
  'codigo_cliente', 'nome_cliente', 'consultor', 'segmento',
  'nivel_cliente', 'faturamento_anual', 'servicos_contratados',
  'data_contratacao', 'cidade', 'uf',
]

function extrairCampo(mensagemErro) {
  const campo = CAMPOS_CONHECIDOS.find((c) => mensagemErro.startsWith(c))
  return campo || 'outro'
}

// Lista "achatada": uma linha por erro (número da linha + campo + mensagem)
const errosDetalhados = computed(() => {
  const lista = []
  for (const linha of store.dadosTratados) {
    for (const mensagem of linha.erros) {
      lista.push({
        numeroLinha: linha.numero_linha,
        campo: extrairCampo(mensagem),
        mensagem,
      })
    }
  }
  return lista
})

// Agrupamento por tipo de validação, pra tabela "Validação realizada / Quantidade"
const resumoPorTipo = computed(() => {
  const contagem = {}
  for (const erro of errosDetalhados.value) {
    contagem[erro.campo] = (contagem[erro.campo] || 0) + 1
  }
  return Object.entries(contagem)
    .map(([campo, quantidade]) => ({ campo, quantidade }))
    .sort((a, b) => b.quantidade - a.quantidade)
})

const dataUploadFormatada = computed(() => {
  if (!store.dataUpload) return '—'
  return new Date(store.dataUpload).toLocaleString('pt-BR')
})

function voltarParaUpload() {
  router.push({ name: 'upload' })
}
</script>

<template>
  <section class="report-page">
    <button type="button" class="back-link" @click="voltarParaUpload">
      <ArrowLeft class="icon-sm" /> Voltar para Upload
    </button>

    <header class="page-header">
      <div>
        <p class="eyebrow">Validação</p>
        <h1>Relatório de Validação</h1>
        <p class="subtitle">
          Arquivo <strong>{{ store.arquivo?.name || '—' }}</strong> · enviado em {{ dataUploadFormatada }}
        </p>
      </div>
      <span class="step-label">Etapa 02 <small>de 02</small></span>
    </header>

    <div v-if="!store.dadosTratados.length" class="panel empty-panel">
      Nenhuma planilha processada ainda.
      <button type="button" class="link-button" @click="voltarParaUpload">Ir para o Upload</button>
    </div>

    <template v-else>
      <!-- Cards de resumo -->
      <div class="stats-grid">
        <div class="panel stat-card">
          <FileSpreadsheet class="icon tone-neutral" />
          <p class="stat-value">{{ store.totalLinhas }}</p>
          <p class="stat-label">Total de registros</p>
        </div>
        <div class="panel stat-card">
          <CheckCircle2 class="icon tone-success" />
          <p class="stat-value">{{ store.totalValidas }}</p>
          <p class="stat-label">Registros válidos</p>
        </div>
        <div class="panel stat-card">
          <AlertTriangle class="icon tone-warning" />
          <p class="stat-value">{{ store.totalComErro }}</p>
          <p class="stat-label">Linhas com erro</p>
        </div>
        <div class="panel stat-card">
          <LayoutGrid class="icon tone-info" />
          <p class="stat-value">{{ store.totalColunas }}</p>
          <p class="stat-label">Colunas identificadas</p>
        </div>
      </div>

      <!-- Etapa 02: envia só as linhas válidas ao backend (que repassa ao Python) -->
      <div class="panel send-panel">
        <div class="send-row">
          <div>
            <h2>Enviar para o banco de dados</h2>
            <p class="panel-text">
              Somente as {{ store.totalValidas }} linhas válidas serão enviadas. As linhas com erro ficam de fora.
            </p>
          </div>
          <button
            type="button"
            class="primary-button"
            :disabled="store.enviando || !store.totalValidas || Boolean(store.resultadoEnvio)"
            @click="store.enviarValidadas()"
          >
            {{ store.enviando ? 'Enviando...' : store.resultadoEnvio ? 'Enviado' : 'Enviar linhas válidas' }}
          </button>
        </div>

        <p v-if="store.erroEnvio" class="alert alert-error">{{ store.erroEnvio }}</p>

        <div v-if="store.resultadoEnvio" class="alert alert-success">
          <strong>Planilha processada!</strong>
          <p>
            {{ store.resultadoEnvio.linhasValidas }} linhas gravadas ·
            {{ store.resultadoEnvio.linhasInvalidas }} rejeitadas ·
            {{ store.resultadoEnvio.linhasComAviso }} com aviso
          </p>
          <ul v-if="store.resultadoEnvio.rejeitadas?.length" class="rejected-list">
            <li v-for="(motivo, i) in store.resultadoEnvio.rejeitadas" :key="i">{{ motivo }}</li>
          </ul>
        </div>
      </div>

      <!-- Tabela resumo por tipo de validação -->
      <div class="panel">
        <div class="panel-heading"><h2>Validação realizada</h2></div>
        <table>
          <thead>
            <tr><th>Campo</th><th>Quantidade</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in resumoPorTipo" :key="item.campo">
              <td>{{ item.campo }}</td>
              <td class="strong tone-warning">{{ item.quantidade }}</td>
            </tr>
            <tr>
              <td class="strong">Registros válidos</td>
              <td class="strong tone-success">{{ store.totalValidas }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Detalhe linha a linha dos erros -->
      <div class="panel">
        <div class="panel-heading"><h2>Detalhamento dos erros</h2></div>
        <div class="table-scroll">
          <table>
            <thead>
              <tr><th>Linha</th><th>Campo</th><th>Descrição</th></tr>
            </thead>
            <tbody>
              <tr v-for="(erro, i) in errosDetalhados" :key="i">
                <td class="muted">{{ erro.numeroLinha }}</td>
                <td><span class="field-tag">{{ erro.campo }}</span></td>
                <td>{{ erro.mensagem }}</td>
              </tr>
              <tr v-if="errosDetalhados.length === 0">
                <td colspan="3" class="empty-row">Nenhum erro encontrado 🎉</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Linhas totalmente válidas, pra conferência -->
      <div v-if="store.linhasValidas.length" class="panel">
        <div class="panel-heading"><h2>Registros válidos</h2></div>
        <div class="table-scroll">
          <table>
            <thead>
              <tr>
                <th v-for="coluna in store.colunas" :key="coluna" class="nowrap">{{ coluna }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(linha, i) in store.linhasValidas" :key="i">
                <td v-for="coluna in store.colunas" :key="coluna" class="nowrap">{{ linha[coluna] }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.report-page {
  max-width: 1100px;
  margin: 0 auto;
  min-height: 100vh;
  background: var(--color-app-bg);
  color: var(--color-body);
  padding: 32px 20px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
  border: 0;
  background: transparent;
  color: var(--color-muted);
  font-size: .85rem;
  cursor: pointer;
}
.back-link:hover { color: var(--color-heading); }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
  margin-bottom: 24px;
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
.subtitle { color: var(--color-muted); margin: 8px 0 0; font-size: .88rem; }
.subtitle strong { color: var(--color-body); font-weight: 600; }
.step-label { color: #93c5fd; font-size: .82rem; font-weight: 700; white-space: nowrap; }
.step-label small { color: var(--color-muted); font-weight: 500; }

.panel {
  background: var(--color-panel);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(2, 6, 23, 0.45);
  margin-bottom: 18px;
  overflow: hidden;
}
.panel h2 { margin: 0; color: var(--color-heading); font-size: .95rem; }
.panel-text { margin: 6px 0 0; color: var(--color-muted); font-size: .8rem; }
.panel-heading { padding: 16px 20px; border-bottom: 1px solid var(--color-border-muted); }

.empty-panel { padding: 40px; text-align: center; color: var(--color-muted); }
.link-button { border: 0; background: transparent; color: #7dd3fc; font-weight: 700; cursor: pointer; margin-left: 4px; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}
.stat-card { display: grid; gap: 6px; padding: 18px; }
.stat-value { margin: 4px 0 0; color: var(--color-heading); font-size: 1.6rem; font-weight: 700; line-height: 1.1; }
.stat-label { margin: 0; color: var(--color-muted); font-size: .76rem; }

.icon { width: 22px; height: 22px; }
.icon-sm { width: 16px; height: 16px; }
.tone-neutral { color: var(--color-muted); }
.tone-success { color: #34d399; }
.tone-warning { color: #fbbf24; }
.tone-info { color: #60a5fa; }

.send-panel { padding: 20px; }
.send-row { display: flex; justify-content: space-between; align-items: center; gap: 16px; }

.primary-button {
  border: 0;
  border-radius: 8px;
  padding: 11px 18px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  font-weight: 700;
  white-space: nowrap;
  cursor: pointer;
  transition: opacity .2s ease, transform .2s ease;
}
.primary-button:hover:not(:disabled) { transform: translateY(-1px); }
.primary-button:disabled { opacity: .45; cursor: not-allowed; }

.alert { margin: 14px 0 0; padding: 12px 14px; border-radius: 8px; font-size: .84rem; }
.alert p { margin: 4px 0 0; }
.alert-error { color: #fecaca; background: rgba(239, 68, 68, 0.12); border: 1px solid rgba(239, 68, 68, 0.3); }
.alert-success { color: #a7f3d0; background: rgba(16, 185, 129, 0.12); border: 1px solid rgba(16, 185, 129, 0.3); }
.rejected-list { margin: 8px 0 0; padding-left: 18px; color: #fde68a; }

.table-scroll { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; text-align: left; font-size: .84rem; }
th {
  padding: 12px 20px;
  color: var(--color-muted);
  font-size: .72rem;
  font-weight: 600;
  letter-spacing: .06em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--color-border-muted);
}
td { padding: 12px 20px; color: var(--color-body); border-bottom: 1px solid var(--color-border-muted); }
tbody tr:last-child td { border-bottom: 0; }
tbody tr:hover td { background: rgba(148, 163, 184, 0.04); }
.strong { font-weight: 700; }
.muted { color: var(--color-muted); }
.nowrap { white-space: nowrap; }
.empty-row { padding: 32px; text-align: center; color: var(--color-muted); }

.field-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(245, 158, 11, 0.15);
  color: #fcd34d;
  font-size: .72rem;
  font-weight: 600;
}

@media (max-width: 900px) {
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 600px) {
  .page-header, .send-row { flex-direction: column; align-items: stretch; }
  .primary-button { width: 100%; }
}
</style>
