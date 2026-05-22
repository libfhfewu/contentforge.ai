<!-- 三步骤进度指示器：策略规划 → 内容创作 → 平台适配 -->
<template>
  <n-steps :current="step">
    <n-step title="策略规划" description="AI 分析选题策略" />
    <n-step title="内容创作" description="AI 撰写内容" />
    <n-step title="平台适配" description="适配三个平台" />
  </n-steps>
</template>

<script setup lang="ts">
defineProps<{ step: number }>()
</script>
