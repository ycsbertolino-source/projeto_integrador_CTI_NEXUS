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
  <main class="p-4 sm:p-6 max-w-5xl mx-auto w-full">
    <button
      type="button"
      class="inline-flex items-center gap-1 text-sm text-slate-500 hover:text-slate-700 mb-3"
      @click="voltarParaUpload"
    >
      <ArrowLeft class="h-4 w-4" /> Voltar para Upload
    </button>

    <h1 class="text-xl sm:text-2xl font-bold text-slate-800 mb-1">Relatório de Validação</h1>
    <p class="text-sm text-slate-500 mb-6">
      Arquivo <span class="font-medium text-slate-700">{{ store.arquivo?.name || '—' }}</span>
      · enviado em {{ dataUploadFormatada }}
    </p>

    <div v-if="!store.dadosTratados.length" class="bg-white border border-slate-200 rounded-xl shadow-sm p-10 text-center text-slate-400">
      Nenhuma planilha processada ainda.
      <button type="button" class="text-blue-600 hover:text-blue-700 font-medium ml-1" @click="voltarParaUpload">
        Ir para o Upload
      </button>
    </div>

    <template v-else>
      <!-- Cards de resumo -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 mb-6">
        <div class="bg-white border border-slate-200 rounded-xl shadow-sm p-4 flex flex-col gap-2">
          <FileSpreadsheet class="h-5 w-5 text-slate-400" />
          <p class="text-xl font-bold text-slate-800">{{ store.totalLinhas }}</p>
          <p class="text-xs text-slate-500">Total de registros</p>
        </div>
        <div class="bg-white border border-slate-200 rounded-xl shadow-sm p-4 flex flex-col gap-2">
          <CheckCircle2 class="h-5 w-5 text-teal-600" />
          <p class="text-xl font-bold text-slate-800">{{ store.totalValidas }}</p>
          <p class="text-xs text-slate-500">Registros válidos</p>
        </div>
        <div class="bg-white border border-slate-200 rounded-xl shadow-sm p-4 flex flex-col gap-2">
          <AlertTriangle class="h-5 w-5 text-amber-500" />
          <p class="text-xl font-bold text-slate-800">{{ store.totalComErro }}</p>
          <p class="text-xs text-slate-500">Linhas com erro</p>
        </div>
        <div class="bg-white border border-slate-200 rounded-xl shadow-sm p-4 flex flex-col gap-2">
          <LayoutGrid class="h-5 w-5 text-blue-600" />
          <p class="text-xl font-bold text-slate-800">{{ store.totalColunas }}</p>
          <p class="text-xs text-slate-500">Colunas identificadas</p>
        </div>
      </div>

      <!-- Tabela resumo por tipo de validação -->
      <div class="bg-white border border-slate-200 rounded-xl shadow-sm mb-6">
        <div class="px-5 py-4 border-b border-slate-200">
          <h2 class="text-sm font-semibold text-slate-800">Validação realizada</h2>
        </div>
        <table class="w-full text-sm">
          <thead>
            <tr class="text-left text-slate-500 border-b border-slate-200">
              <th class="px-5 py-2 font-medium">Campo</th>
              <th class="px-5 py-2 font-medium">Quantidade</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in resumoPorTipo" :key="item.campo" class="border-b border-slate-100 last:border-0">
              <td class="px-5 py-2 text-slate-700">{{ item.campo }}</td>
              <td class="px-5 py-2 text-slate-700 font-medium">{{ item.quantidade }}</td>
            </tr>
            <tr>
              <td class="px-5 py-2 text-slate-700 font-medium">Registros válidos</td>
              <td class="px-5 py-2 text-teal-600 font-semibold">{{ store.totalValidas }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Detalhe linha a linha dos erros -->
      <div class="bg-white border border-slate-200 rounded-xl shadow-sm">
        <div class="px-5 py-4 border-b border-slate-200">
          <h2 class="text-sm font-semibold text-slate-800">Detalhamento dos erros</h2>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="text-left text-slate-500 border-b border-slate-200">
                <th class="px-5 py-2 font-medium">Linha</th>
                <th class="px-5 py-2 font-medium">Campo</th>
                <th class="px-5 py-2 font-medium">Descrição</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(erro, i) in errosDetalhados" :key="i" class="border-b border-slate-100 last:border-0">
                <td class="px-5 py-2 text-slate-500">{{ erro.numeroLinha }}</td>
                <td class="px-5 py-2">
                  <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-amber-50 text-amber-700">
                    {{ erro.campo }}
                  </span>
                </td>
                <td class="px-5 py-2 text-slate-500">{{ erro.mensagem }}</td>
              </tr>
              <tr v-if="errosDetalhados.length === 0">
                <td colspan="3" class="px-5 py-8 text-center text-slate-400">
                  Nenhum erro encontrado 🎉
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Linhas totalmente válidas, pra conferência -->
      <div v-if="store.linhasValidas.length" class="bg-white border border-slate-200 rounded-xl shadow-sm mt-6">
        <div class="px-5 py-4 border-b border-slate-200">
          <h2 class="text-sm font-semibold text-slate-800">Registros válidos</h2>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="text-left text-slate-500 border-b border-slate-200">
                <th v-for="coluna in store.colunas" :key="coluna" class="px-5 py-2 font-medium whitespace-nowrap">
                  {{ coluna }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(linha, i) in store.linhasValidas" :key="i" class="border-b border-slate-100 last:border-0">
                <td v-for="coluna in store.colunas" :key="coluna" class="px-5 py-2 text-slate-600 whitespace-nowrap">
                  {{ linha[coluna] }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </main>
</template>
