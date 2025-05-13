<script setup lang="ts">
import dayjs from 'dayjs'
import { computed, onMounted, reactive, ref } from 'vue'
import { listSpaceByPageAdminUsingPost, listSpaceVoByPageUsingPost } from '@/api/spaceController'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { SPACE_LEVEL_MAP, SPACE_LEVEL_OPTIONS } from '@/const/space'
import { formatSize } from '@/util'

const router = useRouter()
const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUseInfo',
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

//数据展示列表
const dataList = ref<API.SpaceVO[]>([])
//用户总数
const total = ref(0)

//搜索条件
const spaceSearchParam = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

//自定义分页器 使用computed动态变化
const pagination = computed(() => {
  return {
    current: spaceSearchParam.current,
    pageSize: spaceSearchParam.pageSize,
    total: total.value,
    showSizeChanger: true,
    //模板字符串 动态变量
    showTotal: (total) => `共${total}条`,
    onChange: (page, pageSize) => {
      getPageSpace({ current: page, pageSize })
    },
  }
})

const fetchData = async () => {
  await listSpaceByPageAdminUsingPost({ ...spaceSearchParam })
    .then((res) => {
      console.log(res)
      if (res.data.code === 0 && res.data.data) {
        dataList.value = res.data.data.records ?? []
        total.value = res.data.data.total ?? 0
      }
    })
    .catch(() => {
      message.error('获取信息失败111')
    })
}

const getPageSpace = (page) => {
  spaceSearchParam.current = page.current
  spaceSearchParam.pageSize = page.pageSize
  fetchData()
}
const doSearch = () => {
  //一定要重置页码
  spaceSearchParam.current = 1
  fetchData()
}
//页面加载时候获得请求数据
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div id="spaceManagePage">
    <a-flex justify="space-between">
      <h2>空间管理</h2>
      <a-space>
        <a-button type="primary" href="/add_space" target="_blank">+ 创建空间</a-button>
      </a-space>
    </a-flex>
    <!--表单-->
    <a-form layout="inline" :model="spaceSearchParam" @finish="doSearch">
      <a-form-item label="空间名称" name="spaceName">
        <a-input
          v-model:value="spaceSearchParam.spaceName"
          placeholder="请输入空间名称"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="空间级别" name="spaceLevel">
        <a-select
          v-model:value="spaceSearchParam.spaceLevel"
          :options="SPACE_LEVEL_OPTIONS"
          placeholder="请输入空间级别"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="用户 id" name="userId">
        <a-input v-model:value="spaceSearchParam.userId" placeholder="请输入用户 id" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px" />
    <!--表格-->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="getPageSpace"
      bordered
    >
      <template #bodyCell="{ column, record }">
        <!-- 空间级别 -->
        <template v-if="column.dataIndex === 'spaceLevel'">
          <a-tag>{{ SPACE_LEVEL_MAP[record.spaceLevel] }}</a-tag>
        </template>
        <!-- 使用情况 -->
        <template v-if="column.dataIndex === 'spaceUseInfo'">
          <div>大小：{{ formatSize(record.totalSize) }} / {{ formatSize(record.maxSize) }}</div>
          <div>数量：{{ record.totalCount }} / {{ record.maxCount }}</div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button type="link" :href="`/add_space?id=${record.id}`" target="_blank">
              编辑
            </a-button>
            <a-button type="link" danger @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped>
#spaceManagePage .editable-row-operations a {
  margin-right: 8px;
}
#spaceManagePage #save-btn {
  display: inline;
}
#spaceManagePage #cancel-btn {
  display: inline;
  margin-left: 4px;
}

#spaceManagePage #edit-btn {
  display: inline;
}
#spaceManagePage #delete-btn {
  display: inline;
  margin-left: 4px;
}
</style>
