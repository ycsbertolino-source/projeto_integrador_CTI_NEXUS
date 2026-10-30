<script setup>
defineProps({
  as: { type: String, default: 'article' }, // permite usar como <form>, <div>, <aside> etc.
})
</script>

<template>
  <component :is="as" class="panel-card">
    <slot />
  </component>
</template>

<style scoped>
.panel-card {
  background: var(--color-panel);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(2, 6, 23, 0.45);
  padding: 22px;
}
</style>