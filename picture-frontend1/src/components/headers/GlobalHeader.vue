<template>
  <div id="global-header">
    <a-row :wrap="false">
      <!-- 左侧 Logo -->
      <a-col flex="300px">
        <router-link to="/">
          <div class="title-bar">
            <img src="../../assets/logo.jpg" alt="logo" class="logo" />
            <div class="title">智能云图库</div>
          </div>
        </router-link>
      </a-col>

      <!-- 中间导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          class="menu"
          @click="handleClick"
        />
      </a-col>

      <!-- 右侧 登录 按钮 -->
      <a-col flex="120px" class="login-button">
        <div class="user-login-status">
          <div v-if="LoginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="LoginUserStore.loginUser.userAvatar" />
                {{ LoginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LoginOutlined />
                    退出登录
                  </a-menu-item>
                  <a-menu-item>
                  <router-link to="/my_space">
                    <UserOutlined />
                    我的空间
                  </router-link>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" @click="clickToRelocate('/user/login')">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { HomeOutlined, LoginOutlined,UserOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { logOutUsingPost } from '@/api/userController'
import CheckAccess from '@/checkAccess'

const LoginUserStore = useLoginUserStore()
// 菜单列表
const menus = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/pic/add',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/manage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://www.codefather.cn', target: '_blank' }, '编程导航'),
    title: '编程导航',
  },
]

//获取所有的路由
const routes = useRouter().options.routes

const menuToRouteItem = (menu) => {
  return routes.find((route) => {
    return route.path === menu.key // ✅ 直接使用 `===` 比较字符串
  })
}

// 过滤菜单项
const items = computed(() => {
  return menus.filter((menu) => {
    const item = menuToRouteItem(menu)
    if (!item?.meta) {
      return true
    }
    if (item.meta?.hideInMenu) {
      return false
    }
    // 根据权限过滤菜单，有权限则返回 true，则保留该菜单
    return CheckAccess(LoginUserStore.loginUser, item.meta?.access as string)
  })
})

const router = useRouter()
const handleClick = ({ key }) => {
  router.push(key)
}

const clickToRelocate = (url: string) => {
  router.push(url)
}

const doLogout = () => {
  if (LoginUserStore.loginUser.id && LoginUserStore.loginUser.id > 0) {
    logOutUsingPost(LoginUserStore.loginUser.id).then((res) => {
      if (res.data.data) {
        message.success('退出登陆成功')
      }
      LoginUserStore.setLoginUser({
        userName: '未登录',
      })
      router.push('/user/login')
    })
  }
}

const current = ref<string[]>(['/'])
</script>

<style>
/* 整个导航栏样式 */
#global-header {
  background: white;
  padding: 10px 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  border-bottom: 2px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 1000;
}

/* 头部 Logo 区域 */
#global-header .title-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

#global-header .logo {
  height: 50px;
  width: auto;
  border-radius: 10px; /* 让 Logo 变得更柔和 */
}

#global-header .title {
  font-size: 22px;
  font-weight: bold;
  color: #333;
}

/* 菜单栏 */
#global-header .menu {
  font-size: 18px;
  border-bottom: none !important; /* 移除底部边框 */
}

/* 菜单项 hover 效果 */
.menu :deep(.ant-menu-item:hover) {
  background-color: #f5f5f5;
}
</style>
