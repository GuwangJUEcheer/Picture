import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet } from '../api/userController'

/**
 * 存储登录用户的状态Store
 */
export const useLoginUserStore = defineStore('user', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  // 这里应该调用接口获取登录用户信息
  async function fetchLoginUser() {
    const res =  await getLoginUserUsingGet();
   if(res.data.data && res.data.code === 0){
      loginUser.value = res.data.data;
   }
  }

  /**
   * 设置登录用户
   */
  const setLoginUser = (newUser: API.LoginUserVO) =>{
    loginUser.value = newUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
