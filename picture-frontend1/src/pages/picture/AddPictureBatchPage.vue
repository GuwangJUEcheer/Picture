<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  listPictureTagCategoryUsingGet, scrapePicturesUsingPost
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {type Options } from '@/const/PictureConstant'
const picture = ref<API.PictureVO>()
const router = useRouter()
const loading = ref<boolean>(false)

const formData = reactive<API.PictureUploadBatchRequest>({ count: 1, searchText: '', namePrefix:''})

const handleSubmit = async (values: any) => {
  loading.value = true;
  await scrapePicturesUsingPost(formData).then((res) => {
    if (res.data.code === 0 && res.data.data) {
      message.success('批量抓取成功')
      router.push({
        path: `/admin/pictureManage`,
      })
    } else {
      message.error('批量抓取失败' + res.data.message)
    }
  })
  loading.value = false;
}

onMounted(() => {
  getTagCategoryOptions()
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
  <div id="AddPictureBatchPage">
    <h2 style="text-align: center; margin-bottom: 16px">
      批量创建
    </h2>
    <!--图片上传表单-->
    <a-form
      name="formData"
      :model="formData"
      @finish="handleSubmit"
      layout="vertical"
    >
      <a-form-item name="searchText" label="关键词">
        <a-input v-model:value="formData.searchText" placeholder="请输入关键词" />
      </a-form-item>
      <a-form-item name="count" label="抓取数量">
        <a-input-number
          v-model:value="formData.count"
          :min="1"
          :max="30"
          placeholder="请输入抓取数量"
        />
      </a-form-item>
      <a-form-item name="namePrefix" label="名称前缀">
        <a-auto-complete
          v-model:value="formData.namePrefix"
          placeholder="请输入名称前缀"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button style="width: 100%" html-type="submit" type="primary" :loading="loading">执行抓取任务</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<style scoped>
</style>
