<script setup lang="ts">
import PictureUpload from '@/components/icons/PictureUpload.vue'
import { onMounted, reactive, ref } from 'vue'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import {type Options } from '@/const/PictureConstant'
import UrlPictureUpload from '@/components/icons/UrlPictureUpload.vue'

const picture = ref<API.PictureVO>()
const router = useRouter()
const route = useRoute()

//编辑和新建图片是一个图片
const getOldPicture = async () => {
  const id = route.query.id
  if( id && typeof id === 'string' ){
    const res = await getPictureVoByIdUsingGet({ id: BigInt(id) })
    if (res.data.data && res.data.code === 0) {
      message.success('进入编辑模式')
      const data = res.data.data
      picture.value = data
      pictureForm.category = data.category
      pictureForm.tags = data.tags
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
    }
  }
}
/**
 * 图片上传成功
 * @param newPicture
 */
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  console.log(picture)
  pictureForm.name = newPicture.name
}

/**
 * 上传类别
 */
const uploadType = ref<'file' | 'url'>('file');

const pictureForm = reactive<API.PictureEditRequest>({ name: '', introduction: '', tags: [] })

const handleSubmit = async (values: any) => {
  console.log({ ...values })
  const id = picture.value?.id
  if (!id) return
  await editPictureUsingPost({
    id: id,
    ...values,
  }).then((res) => {
    if (res.data.code === 0 && res.data.data) {
      message.success('图片编辑成功')
      router.push({
        path: `/picture/${id}`,
      })
    } else {
      message.error('图片编辑失败' + res.data.message)
    }
  })
}

onMounted(() => {
  getTagCategoryOptions()
  getOldPicture()
})
const categoryOptions = ref<Options[]>([])
const tagOptions = ref<Options[]>([])

/**
 * 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()

  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('创建失败: ' + res.data.message)
  }
}
</script>

<!--一定要加上name 要不然无法识别-->
<template>
  <div id="AddPicturePage">
    <h2 style="text-align: center; margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="图片上传">
        <!--图片上传组件1-->
        <PictureUpload :picture="picture" :on-success="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="url上传" force-render>
        <!--图片上传组件2-->
        <UrlPictureUpload :picture="picture" :on-success="onSuccess" />
      </a-tab-pane>
    </a-tabs>
    <!--图片上传表单-->
    <a-form
      v-if="picture"
      name="pictureForm"
      :model="pictureForm"
      @finish="handleSubmit"
      layout="vertical"
    >
      <a-form-item name="name" label="图片名称">
        <a-input v-model:value="pictureForm.name" placeholder="请输入图片名称" />
      </a-form-item>
      <a-form-item name="introduction" label="图片简介">
        <a-textarea
          v-model:value="pictureForm.introduction"
          :auto-size="{ minRows: 2, maxRows: 5 }"
          placeholder="请输入简介"
          :aria-rowcount="2"
        />
      </a-form-item>
      <a-form-item name="category" label="图片分类">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入图片分类"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="tags" label="图片标签">
        <a-select
          v-model:value="pictureForm.tags"
          placeholder="请输入标签"
          mode="tags"
          :options="tagOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button style="width: 100%" html-type="submit" type="primary">创建</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<style scoped>
</style>
