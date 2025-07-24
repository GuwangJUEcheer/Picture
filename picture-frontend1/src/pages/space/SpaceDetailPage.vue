<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { listPictureVoByPageUsingPost } from '@/api/pictureController'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController'
import { formatSize } from '@/util'
import PictureList from '@/pages/picture/PictureList.vue'
import {TeamOutlined} from "@ant-design/icons-vue";

const props = defineProps<{
  id:  number | undefined
}>()

const space = ref<API.SpaceVO>({})

// 获取空间详情
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取空间详情失败：' + e.message)
  }
}
// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)


onMounted(() => {
  fetchSpaceDetail()
})

// 数据
const dataList = ref<API.PictureVO[] | undefined>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  spaceId: props.id,
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  if (!props.id) {
    loading.value = false
    return
  }
  // 转换搜索参数
  const params: API.PictureQueryRequest = {
    ...searchParams,
  }
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

watch(
  () => props.id,
  (newSpaceId) => {
    fetchSpaceDetail()
    fetchData()
  },
)

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<template>
  <!-- 空间信息 -->
  <a-flex justify="space-between">
    <h2>{{ space.spaceName }}（{{ SPACE_TYPE_MAP[space.spaceType] }}）</h2>
    <a-space size="middle">
      <a-button type="primary" :href="`/add_picture?spaceId=${id}`" target="_blank">
        + 创建图片
      </a-button>
      <a-button
        type="primary"
        ghost
        :icon="h(  TeamOutlined)"
        :href="`/spaceUserManage/${id}`"
        target="_blank"
      >
        成员管理
      </a-button>
      <a-tooltip :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`">
        <a-progress
          type="circle"
          :percent="Number((((space?.totalSize??0)* 100) / (space?.maxSize??0)).toFixed(1))"
          :size="42"
        />
      </a-tooltip>
    </a-space>
  </a-flex>
  <!-- 图片列表 -->
  <PictureList :dataList="dataList" :loading="loading" :show-operation="true" :on-reload="fetchData" />
  <a-pagination
    style="text-align: right"
    v-model:current="searchParams.current"
    v-model:pageSize="searchParams.pageSize"
    :total="total"
    :show-total="() => `图片总数 ${total} / ${space.maxCount}`"
    @change="onPageChange"
  />
  <a-button
    type="primary"
    ghost
    :icon="h(BarChartOutlined)"
    :href="`/space_analyze?spaceId=${id}`"
    target="_blank"
  >
    空间分析
  </a-button>
</template>

<style scoped></style>
