<script setup>
import { ref } from 'vue'

const selectedFile = ref(null)
const fileInput = ref(null)

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
				<input ref="fileInput" class="hidden-input" type="file" accept=".xlsx,.xls" aria-label="Selecionar planilha" @change="selectFile" />
				<button class="primary-button" type="button" @click="openFilePicker">Selecionar arquivo</button>
				<small class="file-hint">Formatos aceitos: .XLSX e .XLS · Limite de 10 MB</small>
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
			<button type="button" @click="selectedFile = null">Remover</button>
		</div>
	</section>
</template>

<style scoped>
.upload-page {
  max-width: 1100px;
  margin: 0 auto;
  min-height: 100vh;
  background: #050b14;
  color: #e2e8f0;
  padding: 32px 20px;
}.page-header { display: flex; justify-content: space-between; align-items: end; gap: 16px; margin-bottom: 28px; }.eyebrow { margin: 0 0 8px; color: #60a5fa; font-size: .72rem; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }h1 { margin: 0; color: #f8fafc; font-size: clamp(1.8rem, 4vw, 2.35rem); }.subtitle { color: #94a3b8; margin: 8px 0 0; }.step-label { color: #93c5fd; font-size: .82rem; font-weight: 700; }.step-label small { color: #94a3b8; font-weight: 500; }.upload-layout { display: grid; grid-template-columns: minmax(0, 1.5fr) minmax(280px, .8fr); gap: 18px; }.upload-card, .guide-card, .selected-file { background: #0f172a; border: 1px solid rgba(148, 163, 184, 0.18); border-radius: 12px; box-shadow: 0 10px 30px rgba(2, 6, 23, 0.45); }.upload-card { min-height: 380px; display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 36px; border-style: dashed; border-color: rgba(96, 165, 250, 0.65); background: linear-gradient(180deg, rgba(15, 23, 42, 0.95), rgba(9, 14, 26, 0.95)); text-align: center; }.upload-icon { width: 54px; height: 54px; display: grid; place-items: center; margin-bottom: 20px; border-radius: 50%; background: rgba(96, 165, 250, 0.18); color: #bfdbfe; font-size: 1.8rem; }.upload-card h2, .guide-card h2 { margin: 0; color: #f8fafc; font-size: 1.1rem; }.upload-card p { margin: 8px 0 22px; color: #94a3b8; font-size: .88rem; }.primary-button { border: 0; border-radius: 8px; padding: 11px 18px; background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; font-weight: 700; cursor: pointer; }.file-hint { margin-top: 18px; color: #94a3b8; font-size: .72rem; }.hidden-input { display: none; }.guide-card { padding: 26px; }.guide-card ul { display: grid; gap: 22px; margin: 26px 0 0; padding: 0; list-style: none; }.guide-card li { display: grid; grid-template-columns: 30px 1fr; gap: 12px; align-items: start; color: #cbd5e1; font-size: .82rem; line-height: 1.5; }.guide-card b { color: #93c5fd; font-size: .76rem; }.selected-file { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-top: 18px; padding: 16px 20px; }.selected-file div { display: grid; gap: 4px; }.selected-file strong { color: #f8fafc; font-size: .84rem; }.selected-file span { color: #94a3b8; font-size: .74rem; }.selected-file button { border: 0; background: transparent; color: #fca5a5; font-weight: 700; cursor: pointer; }@media (max-width: 700px) { .page-header { align-items: start; flex-direction: column; }.upload-layout { grid-template-columns: 1fr; }.upload-card { min-height: 320px; padding: 24px; }.selected-file { align-items: start; flex-direction: column; }.selected-file button { padding: 0; } }
</style>
