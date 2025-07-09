import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import AboutView from '../views/AboutView.vue';
import UserLoginPage from '../pages/user/UserLoginPage.vue'
import UserManagePage from '../pages/admin/UserManagePage.vue'
import UserRegisterPage from '../pages/user/UserRegisterPage.vue'
import noAuth from '../pages/noAuth.vue'
import ACCESS_ENUM  from '../enum/accessNum'
import AddPicturePage from '../pages/picture/AddPicturePage.vue'
import PictureManagePage from '../pages/admin/PictureManagePage.vue'
import SpaceManagePage from '../pages/space/SpaceManagePage.vue'
import PictureDetailPage from '../pages/picture/PictureDetailPage.vue'
import AddPictureBatchPage from '../pages/picture/AddPictureBatchPage.vue'
import AddSpacePage from '../pages/space/AddSpacePage.vue'
import MySpacePage from '../pages/space/MySpacePage.vue'
import SpaceDetailPage from '../pages/space/SpaceDetailPage.vue'
import SearchPicturePage from '../pages/picture/SearchPicturePage.vue'
import SpaceAnalyzePage from '../pages/space/SpaceAnalyzePage.vue'

const router = createRouter({
  history: createWebHistory(), // ✅ 确保 BASE_URL 配置正确
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/noAuth',
      name: 'noAuth',
      component: noAuth,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/admin/manage',
      name: '用户管理',
      component: UserManagePage,
      meta:{
        access:ACCESS_ENUM.ADMIN
      }
    },
    {
      path: '/admin/pictureManage',
      name: '图片管理',
      component: PictureManagePage,
      meta:{
        access:ACCESS_ENUM.ADMIN
      }
    },
    {
      path: '/admin/spaceManage',
      name: '空间管理',
      component: SpaceManagePage,
    },
    {
      path: '/about',
      name: 'about',
      component:AboutView,
    },
    {
      path: '/add_picture',
      name: '添加图片',
      component: AddPicturePage,
      meta:{
        access:ACCESS_ENUM.ADMIN
      }
    },
    {
      path: '/add_space',
      name: '创建空间',
      component: AddSpacePage,
    },
    {
      path: '/pic_add/batch',
      name: '批量导入图片',
      component: AddPictureBatchPage,
      meta:{
        access:ACCESS_ENUM.ADMIN
      }
    },
    //动态路由
    {
      path: '/pic/edit/:id',
      name: '图片详情',
      component: PictureDetailPage,
      props:true
    },
    {
      path: '/my_space',
      name: '我的空间',
      component: MySpacePage,
    },
    {
      path: '/space/:id',
      name: '空间详情',
      component: SpaceDetailPage,
      props: true,
    },
    {
      path: '/search_picture',
      name: '图片搜索',
      component: SearchPicturePage,
    },
    {
      path: '/space_analyze',
      name: '空间分析',
      component: SpaceAnalyzePage,
    }
  ],
});

export default router;
