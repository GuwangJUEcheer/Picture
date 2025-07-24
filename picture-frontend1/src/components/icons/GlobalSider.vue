<template>
  <div id="global-sider">
    <a-layout-sider v-if="loginUser.loginUser.id" breakpoint="lg" width="200" collapsed-width="0">
      <!--v-model:openKeys="openKeys" 默认打开的菜单栏-->
      <a-menu v-model:selectedKeys="current" mode="inline" :items="menuItems" @click="doMenuClick" />
    </a-layout-sider>
  </div>
</template>

<script setup lang="ts">
import {ref, h, watchEffect} from 'vue'
import {PictureOutlined, TeamOutlined, UserOutlined} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import {SPACE_TYPE_ENUM} from "@/const/space";
import {message} from "ant-design-vue";
const menuItems = [
// 固定的菜单列表
  {
    key: '/',
    label: '公共图库',
    icon: () => h(PictureOutlined),
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: () => h(UserOutlined),
  },
  {
    key: '/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    label: '创建团队',
    icon: () => h(TeamOutlined),
  },
]

const teamSpaceList = ref<API.SpaceUserVO[]>([])
const menuItems = computed(() => {
  // 没有团队空间，只展示固定菜单
  if (teamSpaceList.value.length < 1) {
    return fixedMenuItems;
  }
  // 展示团队空间分组
  const teamSpaceSubMenus = teamSpaceList.value.map((spaceUser) => {
    const space = spaceUser.space
    return {
      key: '/space/' + spaceUser.spaceId,
      label: space?.spaceName,
    }
  })
  const teamSpaceMenuGroup = {
    type: 'group',
    label: '我的团队',
    key: 'teamSpace',
    children: teamSpaceSubMenus,
  }
  return [...menuItems, teamSpaceMenuGroup]
})

// 加载团队空间列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data) {
    teamSpaceList.value = res.data.data
  } else {
    message.error('加载我的团队空间失败，' + res.data.message)
  }
}

/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  // 登录才加载
  if (loginUserStore.loginUser.id) {
    fetchTeamSpaceList()
  }
})

const router = useRouter()
// 当前要高亮的菜单项
const current = ref<string[]>([])
// 监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

// 路由跳转事件
const doMenuClick = ({ key }) => {
  //如果router里面有key 直接push就行了 不要写对象，对象情况需要带着query 否则会不带有参数
  router.push(key)
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
