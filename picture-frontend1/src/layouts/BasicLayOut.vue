<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout>
        <GlobalSider class="sider" />
        <a-layout-content class="content">
          <RouterView />
        </a-layout-content>
      </a-layout>
      <a-layout-footer class="footer">
        <a href="https://www.google.co.jp">有问题 就Google</a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '../components/headers/GlobalHeader.vue'
import GlobalSider from '@/components/icons/GlobalSider.vue'
</script>

<style scoped>
#basicLayout .header {
  background-color: white;
  padding-inline: 20px;
  color: unset;
  margin-bottom: 1px;
}

#basicLayout .content {
  padding: 28px;
  background: linear-gradient(to right, #fefefe, #fff);
  margin-bottom: 28px;
}

#basicLayout .sider {
  background: #fff; /* ← 此处原本是逗号，应改为分号 */
  border-right: 0.5px solid #eee;
  padding-top: 20px;
  padding-left: 5px;
  padding-right: 5px;
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
}

:deep(.ant-layout-header) {
  height: auto !important;
  padding-left: 5px !important;
  padding-right: 5px !important;
}

#basicLayout .footer {
  background-color: #efefef;
  padding: 16px;
  /*
   让footer固定到底部
 */
  position: fixed;
  margin-top: 4px;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
</style>
