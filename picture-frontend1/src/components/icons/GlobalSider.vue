<template>
  <div id="global-sider">
    <a-layout-sider v-if="loginUser.loginUser.id" breakpoint="lg" width="200" collapsed-width="0">
      <!--v-model:openKeys="openKeys" 默认打开的菜单栏-->
      <a-menu v-model:selectedKeys="current" mode="inline" :items="menuItems" @click="doMenuClick" />
    </a-layout-sider>
  </div>
</template>

<script setup lang="ts">
import { ref, h } from 'vue'
import { PictureOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
const menuItems = [
  {
    key: '/',
    icon: () => h(PictureOutlined),
    label: '公共图库',
  },
  {
    key: '/my_space',
    icon: () => h(UserOutlined),
    label: '我的空间',
  },
]
const router = useRouter()
// 当前要高亮的菜单项
const current = ref<string[]>([])
// 监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

// 路由跳转事件
const doMenuClick = ({ key }) => {
  router.push({
    path: key,
  })
}
const loginUser = useLoginUserStore()

</script>

<style scoped>
/* 整个导航栏样式 */
#global-sider {
  background: white;
  padding: 10px 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  border-bottom: 2px solid #f0f0f0;
  top: 0;
  margin-top: 1px;
  margin-bottom: 1px;
}
#global-sider .ant-layout-sider {
  background: none;
}
/* 菜单项 hover 效果 */
.menu :deep(.ant-menu-item:hover) {
  background-color: #f5f5f5;
}
</style>
