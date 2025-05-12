<script setup lang="ts">
import dayjs from 'dayjs'
import { computed, onMounted, reactive, ref } from 'vue'
import {
  deletePicturesUsingPost,
  doPictureReviewUsingPost,
  listPictureByPageAdminUsingPost
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { PictureFormatOptions } from '@/const/PictureConstant'
import { PIC_REVIEW_STATUS_MAP,PIC_REVIEW_STATUS_ENUM,PIC_REVIEW_STATUS_OPTIONS} from '@/const/picture'
const router = useRouter()
const columns = [
  {
    title: '图片名',
    key: 'name',
    dataIndex: 'name',
  },
  {
    title: '图片详细信息',
    key: 'pictureInfo',
    dataIndex: 'pictureInfo',
  },
  {
    title: '图片一览',
    key: 'url',
    dataIndex: 'url',
  },
  {
    title: '创建用户',
    dataIndex: 'userName',
    key: 'userName',
  },
  {
    title: '分类',
    dataIndex: 'category',
    key: 'category',
  },
  {
    title: '标签',
    dataIndex: 'tags',
    key: 'tags',
  },
  {
    title: '图片格式',
    key: 'picFormat',
    dataIndex: 'picFormat',
  },
  {
    title: '说明',
    key: 'introduction',
    dataIndex: 'introduction',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
    key: 'editTime',
  },
  {
    title: '审核信息',
    dataIndex: 'reviewMessage',
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'action',
    width: 5,
  },
]

//数据展示列表
const dataList = ref<API.PictureVO[]>([])
//用户总数
const total = ref(0)

//审核状态
const reviewStatus = ref()

//搜索条件
const pictureSearchParam = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
  name: '',
})

const handleReview = async (record: API.Picture, reviewStatus: number) => {
  const reviewMessage = reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '管理员操作通过' : '管理员操作拒绝'
  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus,
    reviewMessage,
  })
  if (res.data.code === 0) {
    message.success('审核操作成功')
    // 重新获取列表
    fetchData()
  } else {
    message.error('审核操作失败，' + res.data.message)
  }
}

//自定义分页器 使用computed动态变化
const pagination = computed(() => {
  return {
    current: pictureSearchParam.current,
    pageSize: pictureSearchParam.pageSize,
    total: total.value,
    showSizeChanger: true,
    //模板字符串 动态变量
    showTotal: (total) => `共${total}条`,
    onChange: (page, pageSize) => {
      getPagePicture({ current: page, pageSize })
    },
  }
})

const fetchData = async () => {
  await listPictureByPageAdminUsingPost({ ...pictureSearchParam })
    .then((res) => {
      console.log(res)
      if (res.data.code === 0 && res.data.data) {
        dataList.value = res.data.data.records ?? []
        total.value = res.data.data.total ?? 0
      }
    })
    .catch(() => {
      message.error('获取信息失败')
    })
}

const getPagePicture = (page) => {
  pictureSearchParam.current = page.current
  pictureSearchParam.pageSize = page.pageSize
  fetchData()
}

const changeSelect = ()=>{
  if(reviewStatus.value === PIC_REVIEW_STATUS_ENUM.ALL){
    pictureSearchParam.reviewStatus = -1
    return
  }
  pictureSearchParam.reviewStatus = reviewStatus.value
}

const doDelete = (id: string) => {
  if (id) {
    //这里可以简写 id:id ->id
    deletePicturesUsingPost({ id: BigInt(id) })
      .then((res) => {
        if (res.data.code === 0 && res.data.data) {
          message.success('删除成功!')
          fetchData()
        }
      })
      .catch(() => {
        message.error('删除失败')
      })
  }
}
const doSearch = () => {
  //一定要重置页码
  pictureSearchParam.current = 1
  fetchData()
}

const doEdit = (id: bigint) => {
  router.push('/pic/add?id=' + id)
}

//页面加载时候获得请求数据
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div id="pictureManagePage">
    <a-flex justify="space-between">
      <h2>图片管理</h2>
      <a-space>
        <a-button type="primary" href="/pic/add" target="_blank">+ 创建图片</a-button>
        <a-button type="primary" href="/pic_add/batch" target="_blank" ghost>+ 批量创建图片</a-button>
      </a-space>
    </a-flex>
    <!--表单-->
    <a-form layout="inline" :model="pictureSearchParam" @finish="doSearch">
      <a-form-item label="图片名称">
        <a-input v-model:value="pictureSearchParam.name" placeholder="输入图片名称进行检索" />
      </a-form-item>
      <a-form-item label="图片格式" :style="{ width: '200px' }">
        <a-select v-model:value="pictureSearchParam.picFormat" :options="PictureFormatOptions" />
      </a-form-item>
      <a-form-item>
      <a-select
        ref="select"
        v-model:value="reviewStatus"
        style="width: 120px"
        :options="PIC_REVIEW_STATUS_OPTIONS"
        @change="changeSelect"
      >
      </a-select>
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
      @change="getPagePicture"
      bordered
    >
      <!-- ✅ 让 pictureAccount 这一列的数据正确渲染 -->
      <template #bodyCell="{ column, record }">
        <!-- 图片展示 -->
        <template v-if="column.dataIndex === 'url'">
          <a-image :src="record.url" objectFit="cover" style="max-width: 100px"/>
        </template>

        <template v-if="column.dataIndex === 'pictureInfo'">
          <div>格式: {{ record.picFormat }}</div>
          <div>宽度: {{ record.picWidth }}</div>
          <div>高度: {{ record.picHeight }}</div>
          <div>宽高比: {{ record.picScale }}</div>
          <div>大小: {{ (record.picSize / 1024).toFixed(2) }}KB</div>
        </template>

        <!-- 审核信息 -->
        <template v-if="column.dataIndex === 'reviewMessage'">
          <div>审核状态：{{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}</div>
          <div>审核信息：{{ record.reviewMessage }}</div>
          <div>审核人：{{ record.reviewerId }}</div>
          <div v-if="record.reviewTime">{{dayjs(record.reviewTime)}}</div>
        </template>

        <!-- 用户角色 -->
        <template v-else-if="column.dataIndex === 'userName'">
          <a-tag :color="record.user.pictureRole === 'admin' ? 'green' : 'blue'">
            {{ record.user.userName }}
          </a-tag>
        </template>

        <!-- 用户角色 -->
        <template v-else-if="column.dataIndex === 'tags'">
          <a-space>
            <a-tag v-for="tag in record.tags" :key="tag" color="pink">
              {{ tag }}
            </a-tag>
          </a-space>
        </template>

        <!-- 编辑时间 -->
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <!-- 操作列 -->
        <template v-else-if="column.dataIndex === 'action'">
          <a-space wrap>
            <a-button
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS"
              type="link"
              @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.PASS)"
            >
              通过
            </a-button>

            <a-button
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT"
              type="link"
              danger
              @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.REJECT)"
            >
              拒绝
            </a-button>
              <a-button type="link"  primary @click="doEdit(record.id)" id="edit-btn">编辑</a-button>
              <a-popconfirm title="真的要删除么" @confirm="doDelete(record.id)">
                <a-button danger type="link" id="delete-btn">删除</a-button>
              </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped>
#pictureManagePage .editable-row-operations a {
  margin-right: 8px;
}
#pictureManagePage #save-btn {
  display: inline;
}
#pictureManagePage #cancel-btn {
  display: inline;
  margin-left: 4px;
}

#pictureManagePage #edit-btn {
  display: inline;
}
#pictureManagePage #delete-btn {
  display: inline;
  margin-left: 4px;
}
</style>
