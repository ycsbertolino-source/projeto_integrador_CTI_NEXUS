<script setup>
// StatusBadge.vue — etiqueta colorida com o status do lote (PROCESSADO, PROCESSANDO, ERRO...).
import { computed } from 'vue' // computed: recalcula a cor quando o status muda
const props = defineProps({ status: String }) // recebe o texto do status
const tom = computed(() => ({ // escolhe a cor pelo status
  APROVADO: 'ok', PROCESSADO: 'ok', // verde: aprovado ou processado
  PENDENTE: 'aviso', AGUARDANDO_ETL: 'aviso', PROCESSANDO: 'aviso', // laranja: pendente, aguardando ou processando
  BLOQUEADO: 'erro', REJEITADO: 'erro', ERRO: 'erro', // vermelho: bloqueado, rejeitado ou erro no Python
}[props.status] || 'neutro')) // cor padrão: cinza
</script>

<template>
  <span class="badge" :class="tom">{{ status }}</span> <!-- etiqueta colorida com o texto do status -->
</template>

<style scoped>
.badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: .72rem; font-weight: 700; letter-spacing: .02em; }
.ok { color: #a7f3d0; background: rgba(16, 185, 129, .15); }
.aviso { color: #fde68a; background: rgba(245, 158, 11, .15); }
.erro { color: #fecaca; background: rgba(239, 68, 68, .15); }
.neutro { color: #cbd5e1; background: rgba(148, 163, 184, .15); }
</style>
