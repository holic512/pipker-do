<template>
  <section class="page-container admin-card">
    <header class="page-container__header">
      <div>
        <h1 class="page-container__title">{{ title }}</h1>
        <p v-if="description" class="page-container__description">{{ description }}</p>
      </div>
      <slot name="actions" />
    </header>
    <div class="page-container__body">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style scoped lang="scss">
.page-container {
  padding: 28px 30px;
}

.page-container__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-container__title {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  font-weight: 700;
}

.page-container__description {
  margin: 8px 0 0;
  color: var(--admin-text-soft);
  font-size: 14px;
}
</style>
