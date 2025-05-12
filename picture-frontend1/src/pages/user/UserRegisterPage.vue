<script setup lang="ts">
import { ref } from 'vue'

import { reactive, toRaw } from 'vue';
import { Form, message } from 'ant-design-vue'
import { userRegisterUsingPost } from '@/api/userController'
import router from '@/router'

const labelCol = ref({ span: 4 });
const wrapperCol = ref({ span: 20 });
const useForm = Form.useForm;
const modelRef = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
});

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
        min:8,
        max:20,
      },
    ],
    checkPassword: [
      {
        required: true,
        message: '请再次输入密码',
        min:8,
        max:20,
      },
    ],
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
  console.log(modelRef);
  await userRegisterUsingPost(modelRef).then((res) => {
    if(res.data.data && res.data.data>0){
      message.success("注册成功");
      router.push("/user/login");
    }
  });
}
</script>

<template>
  <div id="UserRegisterPage">
    <h2>用户注册</h2>
    <a-form :label-col="labelCol" :wrapper-col="wrapperCol">
      <a-form-item label="用户名" v-bind="validateInfos.userAccount">
        <a-input
          v-model:value="modelRef.userAccount"
          @blur="validate('userAccount', { trigger: 'blur' }).catch(() => {})"
        />
      </a-form-item>
      <a-form-item label="密码" v-bind="validateInfos.userPassword">
        <a-input
          v-model:value="modelRef.userPassword"
          @blur="validate('userPassword', { trigger: 'blur' }).catch(() => {})"
        />
      </a-form-item>
      <a-form-item label="确认密码" v-bind="validateInfos.checkPassword">
        <a-input
          v-model:value="modelRef.checkPassword"
          @blur="validate('checkPassword', { trigger: 'blur' }).catch(() => {})"
        />
      </a-form-item>
      <a-form-item :wrapper-col="{ span: 14, offset: 4 }">
        <a-button type="primary" @click.prevent="onSubmit">注册</a-button>
        <a-button style="margin-left: 10px" @click="reset">取消</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<style scoped>
#UserRegisterPage {
  max-width: 800px;
  margin: 200px auto;
  background: white;
  padding: 20px;
}
</style>
