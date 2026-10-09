<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUploadStore } from '@/store/uploadStore'

const router = useRouter()
const store = useUploadStore()
const errosLista = computed(() => store.erros || [])

const selectedFile = ref(null)
const fileInput = ref(null)
const enviando = ref(false)

function selectFile(event) {
	selectedFile.value = event.target.files?.[0] || null
}

function dropFile(event) {
	selectedFile.value = event.dataTransfer.files?.[0] || null
}

function openFilePicker() {
	fileInput.value?.click()
}

function formatSize(bytes) {
	return bytes ? `${(bytes / 1024 / 1024).toFixed(2)} MB` : ''
}

function removerArquivo() {
	selectedFile.value = null
	store.limpar()
}

// Envia o arquivo pro store (lê, trata e valida) e, se não houver erro
// global de leitura, navega pra tela de relatório.
async function enviarParaValidacao() {
	if (!selectedFile.value) return
	enviando.value = true
	await store.lerArquivo(selectedFile.value)
	enviando.value = false

	// Se houver erros de validação por linha, permanecemos aqui e mostramos os detalhes
	if (store.totalComErro > 0) {
		// não navegar, mostrar painel de erros
		return
	}

	if (!store.erro) {
		router.push({ name: 'relatorio' })
	}
}
</script>

<template>
	<section class="upload-page">
		<header class="page-header">
			<div>
				<p class="eyebrow">Entrada de dados</p>
				<h1>Enviar planilha</h1>
				<p class="subtitle">Importe os dados operacionais para iniciar o tratamento automático.</p>
			</div>
			<span class="step-label">Etapa 01 <small>de 02</small></span>
		</header>

		<div class="upload-layout">
			<article class="upload-card" @dragover.prevent @drop.prevent="dropFile">
				<div class="upload-icon">↑</div>
				<h2>Arraste sua planilha aqui</h2>
				<p>ou selecione um arquivo do seu computador</p>
				<input ref="fileInput" class="hidden-input" type="file" accept=".xlsx,.xls,.csv" aria-label="Selecionar planilha" @change="selectFile" />
				<div class="upload-actions">
					<button class="primary-button" type="button" @click="openFilePicker">Selecionar arquivo</button>
					<a class="secondary-button" href="/planilhas/modelo_clientes.csv" download="modelo_clientes.csv">Baixar modelo</a>
				</div>
				<small class="file-hint">Formatos aceitos: .XLSX, .XLS e .CSV · Limite de 10 MB</small>
			</article>

			<aside class="guide-card">
				<h2>Antes de enviar</h2>
				<ul>
					<li><b>01</b><span>Confira se a primeira linha contém os nomes das colunas.</span></li>
					<li><b>02</b><span>Inclua clientes, segmentos, níveis e faturamento.</span></li>
					<li><b>03</b><span>O sistema valida e padroniza os dados automaticamente.</span></li>
				</ul>
			</aside>
		</div>

		<div v-if="selectedFile" class="selected-file">
			<div><strong>{{ selectedFile.name }}</strong><span>{{ formatSize(selectedFile.size) }}</span></div>
			<div class="selected-file-actions">
				<button type="button" class="remove-button" @click="removerArquivo">Remover</button>
				<button type="button" class="primary-button" :disabled="enviando" @click="enviarParaValidacao">
					{{ enviando ? 'Validando...' : 'Validar e continuar' }}
				</button>
			</div>
		</div>

		<!-- Painel de erros de validação (aparece quando planilha foi processada e contém erros) -->
		<section v-if="store.processado && store.totalComErro > 0" class="error-panel" style="margin-top:18px;">
			<div class="panel-heading" style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px">
				<div>
					<strong>Erros encontrados</strong>
					<div style="color:var(--color-muted);font-size:0.9rem">Total de linhas com erro: {{ store.totalComErro }} · Total de erros: {{ errosLista.length }}</div>
				</div>
				<div>
					<button class="primary-button" type="button" @click="router.push({ name: 'relatorio' })">Abrir relatório completo</button>
				</div>
			</div>
			<div style="background:var(--color-panel);border:1px solid var(--color-border);border-radius:8px;padding:12px;max-height:320px;overflow:auto">
				<table style="width:100%;border-collapse:collapse;font-size:0.92rem">
					<thead style="color:var(--color-muted);text-align:left">
						<tr><th style="padding:8px">Linha</th><th style="padding:8px">Campo</th><th style="padding:8px">Descrição</th></tr>
					</thead>
					<tbody>
						<tr v-for="(e, i) in errosLista" :key="i" style="border-top:1px solid rgba(148,163,184,0.06)">
							<td style="padding:8px">{{ e.linha || '—' }}</td>
							<td style="padding:8px">{{ e.campo }}</td>
							<td style="padding:8px;color:var(--color-heading)">{{ e.descricao }}</td>
						</tr>
						<tr v-if="!errosLista.length"><td colspan="3" style="padding:12px;color:var(--color-muted)">Nenhum erro listado.</td></tr>
					</tbody>
				</table>
			</div>
		</section>

		<p v-if="store.erro && !store.processado" class="error-message">{{ store.erro }}</p>
	</section>
</template>

<style scoped>
.upload-page {
	max-width: 1100px;
	margin: 0 auto;
	min-height: 100vh;
	background: var(--color-app-bg);
	color: var(--color-body);
	padding: 32px 20px;
}

.page-header {
	display: flex;
	justify-content: space-between;
	align-items: end;
	gap: 16px;
	margin-bottom: 28px;
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
.subtitle { color: var(--color-muted); margin: 8px 0 0; }

.step-label { color: #93c5fd; font-size: .82rem; font-weight: 700; }
.step-label small { color: var(--color-muted); font-weight: 500; }

.upload-layout {
	display: grid;
	grid-template-columns: minmax(0, 1.5fr) minmax(280px, .8fr);
	gap: 18px;
}

.upload-card, .guide-card, .selected-file {
	background: var(--color-panel);
	border: 1px solid var(--color-border);
	border-radius: 12px;
	box-shadow: 0 10px 30px rgba(2, 6, 23, 0.45);
}

.upload-card {
	min-height: 380px;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	padding: 36px;
	border-style: dashed;
	border-color: rgba(96, 165, 250, 0.65);
	background: var(--color-panel-alt);
	text-align: center;
}

.upload-icon {
	width: 54px;
	height: 54px;
	display: grid;
	place-items: center;
	margin-bottom: 20px;
	border-radius: 50%;
	background: rgba(96, 165, 250, 0.18);
	color: #bfdbfe;
	font-size: 1.8rem;
}

.upload-card h2, .guide-card h2 { margin: 0; color: var(--color-heading); font-size: 1.1rem; }
.upload-card p { margin: 8px 0 22px; color: var(--color-muted); font-size: .88rem; }

.upload-actions {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12px;
	flex-wrap: wrap;
}

.primary-button, .secondary-button {
	border: 0;
	border-radius: 8px;
	padding: 11px 18px;
	font-weight: 700;
	cursor: pointer;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	text-decoration: none;
	transition: opacity 0.2s ease, transform 0.2s ease;
}

.primary-button {
	background: var(--color-primary);
	color: var(--color-primary-foreground);
}

.secondary-button {
	background: rgba(148, 163, 184, 0.12);
	border: 1px solid rgba(148, 163, 184, 0.25);
	color: var(--color-heading);
}

.primary-button:hover, .secondary-button:hover {
	opacity: 0.96;
	transform: translateY(-1px);
}

.primary-button:disabled {
	opacity: .5;
	cursor: not-allowed;
}

.file-hint { margin-top: 18px; color: var(--color-muted); font-size: .72rem; }
.hidden-input { display: none; }

.guide-card { padding: 26px; }
.guide-card ul { display: grid; gap: 22px; margin: 26px 0 0; padding: 0; list-style: none; }
.guide-card li {
	display: grid;
	grid-template-columns: 30px 1fr;
	gap: 12px;
	align-items: start;
	color: #cbd5e1;
	font-size: .82rem;
	line-height: 1.5;
}
.guide-card b { color: #93c5fd; font-size: .76rem; }

.selected-file {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 16px;
	margin-top: 18px;
	padding: 16px 20px;
}
.selected-file div { display: grid; gap: 4px; }
.selected-file strong { color: var(--color-heading); font-size: .84rem; }
.selected-file span { color: var(--color-muted); font-size: .74rem; }

.selected-file-actions {
	display: flex;
	align-items: center;
	gap: 12px;
}
.remove-button { border: 0; background: transparent; color: #fca5a5; font-weight: 700; cursor: pointer; }

.error-message {
	margin-top: 14px;
	color: #fca5a5;
	font-size: .82rem;
	font-weight: 600;
}

@media (max-width: 700px) {
	.page-header { align-items: start; flex-direction: column; }
	.upload-layout { grid-template-columns: 1fr; }
	.upload-card { min-height: 320px; padding: 24px; }
	.selected-file { align-items: start; flex-direction: column; }
	.selected-file-actions { padding: 0; }
}
</style>
