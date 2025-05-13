<script setup lang="ts">
import { ref } from 'vue'

import { reactive, toRaw } from 'vue';
import { Form, message } from 'ant-design-vue'
import { userLoginUsingPost } from '@/api/userController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import router from '@/router'

const labelCol = ref({ span: 4 });
const useForm = Form.useForm;
const loginUserStore = useLoginUserStore();
const modelRef = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
});
const loading = ref(false);

const { resetFields,validate,validateInfos} = useForm(
      modelRef,
      reactive({
        userAccount: [
          {
            required: true,
            message: '请输入用户名',
          },
        ],
        userPassword: [
          {
            required: true,
            message: '请输入密码',
          },
        ]
      }),
    );
    const onSubmit = () => {
      validate()
        .then(res => {
          console.log(res, toRaw(modelRef));
          login();
        })
        .catch(err => {
          console.log('error', err);
        });
    };
    const reset = () => {
      resetFields();
    };

    const login = async () => {
      loading.value = true;
      await userLoginUsingPost(modelRef).then((res) => {
        if(res.data.data){
          loginUserStore.setLoginUser(res.data.data);
        }
        message.success("登陆成功");
        router.push("/")
      });
      loading.value = false;
    }
</script>

<template>
  <div id="UserLoginPage">
    <h2 class="title">智能云图库 - 用户登录</h2>
    <div class="desc">企业级智能协同云图库</div>
    <a-form @finish={onSubmit}>
      <a-form-item label="用户名" v-bind="validateInfos.userAccount">
        <a-input
          v-model:value="modelRef.userAccount"
          @blur="validate('userAccount', { trigger: 'blur' }).catch(() => {})"
        />
      </a-form-item>
      <a-form-item label="密码" v-bind="validateInfos.userPassword">
        <a-input-password
          v-model:value="modelRef.userPassword"
          @blur="validate('userPassword', { trigger: 'blur' }).catch(() => {})"
        />
      </a-form-item>
      <div class="tips">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" @click.prevent="onSubmit" html-type="submit" :loading ="loading">登录</a-button>
        <a-button style="margin-left: 10px" @click="reset">取消</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<style scoped>
#UserLoginPage {
  max-width: 800px;
  margin: 50px auto;
  padding: 30px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  font-size: 22px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #777;
  font-size: 14px;
  margin-bottom: 20px;
}

a-form-item {
  margin-bottom: 16px;
}

a-input {
  border-radius: 8px;
  height: 40px;
}

a-input:focus,
a-input:hover {
  border-color: #1890ff;
  box-shadow: 0 0 5px rgba(24, 144, 255, 0.3);
}

.tips {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
}

.tips a {
  color: #0056b3;
  text-decoration: none;
  transition: color 0.3s ease-in-out;
}

.tips a:hover {
  color: #003d80;
}

a-form-item:last-child {
  text-align: center;
}

a-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
}

a-button[type="primary"] {
  background: linear-gradient(135deg, #1890ff, #0066cc);
  border: none;
}

a-button[type="primary"]:hover {
  background: linear-gradient(135deg, #0066cc, #0056b3);
}

a-button[style="margin-left: 10px"] {
  background: #f0f0f0;
  color: #333;
}

a-button[style="margin-left: 10px"]:hover {
  background: #d9d9d9;
}

</style>
