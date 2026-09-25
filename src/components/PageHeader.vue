<script setup>
defineProps({
  eyebrow: { type: String, required: true },
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
})
</script>

<template>
  <header class="page-header">
    <div>
      <p class="eyebrow">{{ eyebrow }}</p>
      <h1>{{ title }}</h1>
      <p v-if="subtitle" class="subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.action" class="page-header-action">
      <slot name="action" />
    </div>
  </header>
</template>

<style scoped>
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

@media (max-width: 600px) {
  .page-header { align-items: start; flex-direction: column; }
}
</style>