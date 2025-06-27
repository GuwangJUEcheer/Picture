<template>
  <div class="home-view">
    <div class="searchInput">
      <a-input-search
        v-model:value="searchParams.searchText"
        placeholder="请输入搜索条件"
        enter-button="检索"
        size="large"
        @search="onSearch"
      />
    </div>
    <a-tabs v-model:activeKey="selectedCategory" @change="fetchData">
      <a-tab-pane key="all" tab="全部"></a-tab-pane>
      <a-tab-pane v-for="category in categoryList" :key="category" :tab="category"></a-tab-pane>
    </a-tabs>
    <div class="tab-bar">
      <span>标签: </span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectedTagList[index]"
          @change="fetchData()"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>
    <PictureList :loading="loading" :dataList="dataList" />
    <a-pagination
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
      style="text-align: right"
    />
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import PictureList from '@/pages/picture/PictureList.vue'

const loading = ref(false)
const dataList = ref<API.PictureVO[]>([])

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 图片总数
const total = ref(0)

// 自定义分页器 使用computed动态变化
const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  getPagePicture({ current: page, pageSize })
}

// 标签和分类列表
const categoryList = ref<string[]>([])
const tagList = ref<string[]>([])

const selectedCategory = ref<string>('all')
const selectedTagList = ref<boolean[]>([])

const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params: API.PictureQueryRequest = {
    ...searchParams,
    tags: [] as string[],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  // 标签列表
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags?.push(tagList.value[index])
    }
  })

  await listPictureVoByPageUsingPost({ current: 1, pageSize: 10, ...params })
    .then((res) => {
      if (res.data.code === 0 && res.data.data) {
        dataList.value = res.data.data.records ?? []
        total.value = res.data.data.total ?? 0
      }
    })
    .catch(() => {
      message.error('获取信息失败')
    })
  loading.value = false
}

/**
 * 获取标签和分类选项
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    tagList.value = res.data.data.tagList ?? []
    categoryList.value = res.data.data.categoryList ?? []
  } else {
    message.error('获取标签分类列表失败: ' + res.data.message)
  }
}

const getPagePicture = (page: { current: number; pageSize: number }) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const onSearch = () => {
  fetchData()
}

onMounted(() => {
  fetchData()
  getTagCategoryOptions()
})
</script>

<style scoped>
.home-view .searchInput {
  max-width: 480px !important;
  margin: 0 auto 16px;
}

.home-view .tab-bar {
  margin-bottom: 20px;
}
</style>
