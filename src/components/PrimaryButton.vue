<script setup>
defineProps({
  as: { type: String, default: 'button' },
  type: { type: String, default: 'button' },
  to: { type: [String, Object], default: undefined },
})
</script>

<template>
  <component :is="as" class="primary-button" :type="as === 'button' ? type : undefined" :to="to">
    <slot />
  </component>
</template>

<style scoped>
.primary-button {
  display: inline-block;
  border: 0;
  border-radius: 8px;
  padding: 11px 16px;
  background: var(--color-primary);
  color: var(--color-primary-foreground);
  font-weight: 700;
  text-decoration: none;
  cursor: pointer;
  text-align: center;
}
</style>