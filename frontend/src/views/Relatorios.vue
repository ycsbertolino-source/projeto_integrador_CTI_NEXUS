<script setup>
import { computed, onMounted } from 'vue'
import { useUploadStore } from '@/stores/uploadStore'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'

const store = useUploadStore()
const authStore = useAuthStore()
const router = useRouter()

const isAdmin = computed(() => authStore.user?.role === 'admin')

// A lista vem do banco (GET /api/lotes), então aparece igual em qualquer computador.
onMounted(() => store.carregarLotes())

const STATUS = {
  PROCESSADO: { texto: 'Processado', tom: 'ok' },
  PROCESSANDO: { texto: 'Processando', tom: 'info' },
  ERRO: { texto: 'Erro no processamento', tom: 'erro' },
}

const reports = computed(() => {
  // 1) Uploads salvos no banco. Se este navegador tem as linhas guardadas, dá para abrir o detalhe.
  const doBanco = store.lotesServidor.map((lote) => {
    const local = store.historico.find((h) => h.loteId === lote.loteId)
    const recebidas = Number(lote.linhasRecebidas) || 0
    const validas = Number(lote.linhasValidas) || 0
    return {
      key: `lote-${lote.loteId}`,
      loteId: lote.loteId,
      localId: local?.id ?? null,
      name: lote.arquivoNome || 'Planilha',
      uploadedAt: lote.criadoEm,
      total: recebidas,
      valid: validas,
      invalid: Number(lote.linhasInvalidas) || Math.max(recebidas - validas, 0),
      status: STATUS[lote.status] || { texto: lote.status || '—', tom: 'info' },
      erro: lote.mensagemErro || '',
    }
  })

  // 2) Planilhas lidas neste navegador que ainda não foram enviadas para análise.
  const soLocais = store.historico
    .filter((h) => h.loteId == null)
    .map((h) => ({
      key: `local-${h.id}`,
      loteId: null,
      localId: h.id,
      name: h.name || 'Planilha',
      uploadedAt: h.uploadedAt || h.createdAt,
      total: Number(h.total) || 0,
      valid: Number(h.valid) || 0,
      invalid: Number(h.invalid) || 0,
      status: { texto: 'Não enviado · só neste navegador', tom: 'aviso' },
      erro: '',
    }))

  return [...soLocais, ...doBanco]
})

function abrirDetalhe(item) {
  store.carregarHistorico(item.localId)
  router.push({ name: 'relatorio' })
}

function abrirDashboard(item) {
  router.push({ name: 'dashboard', query: { loteId: item.loteId } })
}

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('pt-BR')
}
</script>

<template>
  <section class="page-container">
    <header class="page-header">
      <div>
        <p class="eyebrow">Relatórios</p>
        <h1>Relatórios gerados</h1>
        <p class="subtitle">Planilhas processadas e salvas no banco, mais as lidas neste navegador que ainda não foram enviadas.</p>
      </div>
    </header>

    <div v-if="store.erroLotes" class="panel alert-panel">
      {{ store.erroLotes }} Mostrando só o histórico deste navegador.
    </div>

    <div v-if="store.carregandoLotes && reports.length === 0" class="panel empty-panel">
      Carregando relatórios...
    </div>

    <div v-else-if="reports.length === 0" class="panel empty-panel">
      {{ isAdmin
        ? 'Nenhuma planilha processada ainda. Faça um upload para gerar relatórios.'
        : 'Aguardando a geração do relatório pela área administrativa.' }}
    </div>

    <div v-else class="reports-list">
      <article v-for="r in reports" :key="r.key" class="panel">
        <div class="report-row">
          <div>
            <strong>{{ r.name }}</strong>
            <span v-if="r.loteId" class="muted"> · lote #{{ r.loteId }}</span>
            <div class="muted small">Enviado: {{ formatDate(r.uploadedAt) }}</div>
            <span class="badge" :class="`badge-${r.status.tom}`">{{ r.status.texto }}</span>
            <div v-if="r.erro" class="erro small">{{ r.erro }}</div>
          </div>
          <div class="report-actions">
            <div class="count"><div class="muted small">Registros</div><strong>{{ r.total }}</strong></div>
            <div class="count"><div class="muted small">Válidos</div><strong>{{ r.valid }}</strong></div>
            <div class="count"><div class="muted small">Inválidos</div><strong>{{ r.invalid }}</strong></div>
            <button v-if="r.localId" class="primary-button" @click="abrirDetalhe(r)">Abrir relatório</button>
            <button v-if="r.loteId && r.status.tom === 'ok'" class="secondary-button" @click="abrirDashboard(r)">Ver no Dashboard</button>
          </div>
        </div>
        <p v-if="r.loteId && !r.localId" class="muted small nota">
          As linhas desta planilha ficaram no navegador onde ela foi enviada; aqui aparece só o resumo.
        </p>
      </article>
    </div>
  </section>
</template>

<style scoped>
.page-container { max-width:1280px;margin:0 auto;padding:32px 20px }
.eyebrow{color:#60a5fa;font-weight:700;letter-spacing:.12em;text-transform:uppercase}
.panel{background:#0f172a;border-radius:12px;padding:18px;border:1px solid rgba(148,163,184,.12)}
.empty-panel{color:#cbd5e1}
.primary-button{background:linear-gradient(135deg,#2563eb,#1d4ed8);color:white;padding:8px 12px;border-radius:8px;border:0}
.reports-list{display:grid;gap:12px}
.report-row{display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap}
.report-actions{display:flex;gap:12px;align-items:center;flex-wrap:wrap}
.count{text-align:right}
.muted{color:#94a3b8}
.small{font-size:.85rem}
.nota{margin:10px 0 0}
.erro{color:#fca5a5;margin-top:4px}
.alert-panel{color:#fde68a;border-color:rgba(245,158,11,.35);margin-bottom:12px}
.badge{display:inline-block;margin-top:6px;padding:2px 8px;border-radius:999px;font-size:.75rem;font-weight:600}
.badge-ok{color:#a7f3d0;background:rgba(16,185,129,.14)}
.badge-info{color:#bae6fd;background:rgba(56,189,248,.14)}
.badge-aviso{color:#fde68a;background:rgba(245,158,11,.14)}
.badge-erro{color:#fecaca;background:rgba(239,68,68,.14)}
.secondary-button{background:transparent;color:#bfdbfe;padding:8px 12px;border-radius:8px;border:1px solid rgba(96,165,250,.45);cursor:pointer}
.primary-button{cursor:pointer}
</style>
