<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  addSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet
} from '@/api/spaceController'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { SPACE_LEVEL_ENUM, SPACE_LEVEL_OPTIONS } from '@/const/space'
import { formatSize } from '../../util'

const space = ref<API.SpaceVO>()
const router = useRouter()
const route = useRoute()

const spaceLevelList = ref<API.SpaceLevel[]>([])

// 获取空间级别
const fetchSpaceLevelList = async () => {
  const res = await listSpaceLevelUsingGet()
  if (res.data.code === 0 && res.data.data) {
    spaceLevelList.value = res.data.data
  } else {
    message.error('加载空间级别失败，' + res.data.message)
  }
}

onMounted(() => {
  fetchSpaceLevelList()
})

const spaceForm = reactive<API.SpaceAddRequest | API.SpaceEditRequest>({ spaceName: ''})

//编辑和新建空间是一个空间
const getOldSpace = async () => {
  const id = route.query.id
  if( id && typeof id === 'string' ){
    const res = await getSpaceVoByIdUsingGet({ id: BigInt(id) })
    if (res.data.data && res.data.code === 0) {
      message.success('进入编辑模式')
      const data = res.data.data
      space.value = data
      spaceForm.spaceName = data.spaceName
    }
  }
}
/**
 * 空间上传成功
 * @param newSpace
 */
const onSuccess = (newSpace: API.SpaceVO) => {
  space.value = newSpace
  spaceForm.spaceName = newSpace.spaceName
}

const handleSubmit = async (values: any) => {
  loading.value = true
  await addSpaceUsingPost({
    ...values
  }).then((res) => {
    if (res.data.code === 0 && res.data.data) {
      message.success('空间创建成功')
      router.push({
        path: `/space/${res.data.data}`,
      })
    } else {
      message.error('空间创建失败' + res.data.message)
    }
  })
  loading.value = false
}

const formData = reactive<API.SpaceAddRequest | API.SpaceUpdateRequest>({
  spaceName: '',
  spaceLevel: SPACE_LEVEL_ENUM.COMMON,
})
const loading = ref(false)


onMounted(() => {
  getOldSpace()
})
</script>

<!--一定要加上name 要不然无法识别-->
<template>
  <div id="AddSpacePage">
    <h2 style="text-align: center; margin-bottom: 16px">
      {{ route.query?.id ? '修改空间' : '创建空间' }}
    </h2>
    <a-form layout="vertical" :model="formData" @finish="handleSubmit">
      <a-form-item label="空间名称" name="spaceName">
        <a-input v-model:value="formData.spaceName" placeholder="请输入空间名称" allow-clear />
      </a-form-item>
      <a-form-item label="空间级别" name="spaceLevel">
        <a-select
          v-model:value="formData.spaceLevel"
          :options="SPACE_LEVEL_OPTIONS"
          placeholder="请输入空间级别"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">
          提交
        </a-button>
      </a-form-item>
    </a-form>
    <a-card title="空间级别介绍">
      <a-typography-paragraph>
        * 目前仅支持开通普通版，如需升级空间，请联系
        <a href="https://codefather.cn" target="_blank">测试</a>。
      </a-typography-paragraph>
      <a-typography-paragraph v-for="spaceLevel in spaceLevelList">
        {{ spaceLevel.text }}： 大小 {{ formatSize(spaceLevel.maxSize) }}， 数量
        {{ spaceLevel.maxCount }}
      </a-typography-paragraph>
    </a-card>
  </div>
</template>

<style scoped>
</style>
