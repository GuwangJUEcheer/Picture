<script setup lang="ts">
import dayjs from 'dayjs'
import { computed, onMounted, reactive, ref, type UnwrapRef } from 'vue'
import {
  deleteBtIdUsingPost,
  listUserVoByPageUsingPost,
  updateUserUsingPost
} from '@/api/userController'
import { message } from 'ant-design-vue'
import { cloneDeep } from 'lodash-es'
const columns = [
  {
    title: 'id',
    key: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    key: 'userAccount',
  },
  {
    title: '用户名',
    key: 'userName',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    key: 'userAvatar',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    key: 'userProfile',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    key: 'userRole',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    key: 'createTime',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'action',
  },
]
//不能修改的列
const unEditableColumns = ['userAvatar', 'userRole', 'createTime']
//数据展示列表
const dataList = ref<API.UserVO[]>([])
//用户总数
const total = ref(0)

const editableData: UnwrapRef<Record<string, API.UserVO>> = reactive({})
const edit = (id: number) => {
  editableData[id] = cloneDeep(dataList.value.filter((item) => id === item.id)[0])
  console.log(editableData)
}
const save = (record) => {
  if (!record.id) {
    return
  }
  updateUserUsingPost(record).then((res) => {
     if(res.data.code === 0 && res.data.data){
       message.success("修改用户信息成功!");
       fetchData();
     }
  }).catch(() => {
    message.error("修改用户信息失败！");
  })
  Object.assign(dataList.value.filter((item) => record.id === item.id)[0], editableData[record.id])
  delete editableData[record.id]
}
const cancel = (id: number) => {
  delete editableData[id]
}

//搜索条件
const userSearchParam = reactive<API.UserQueryRequest>({
  userAccount: '',
  userName: '',
  current: 1,
  pageSize: 5,
  sortField: 'createTime',
  sortOrder: 'descend',
})

//自定义分页器 使用computed动态变化
const pagination = computed(() => {
  return {
    current: userSearchParam.current,
    pageSize: userSearchParam.pageSize,
    total: total.value,
    showSizeChanger: true,
    //模板字符串 动态变量
    showTotal: (total) => `共${total}条`,
    onChange: (page, pageSize) => {
      getPageUser({ current: page, pageSize })
    },
  }
})

const fetchData = async () => {
  await listUserVoByPageUsingPost({ ...userSearchParam })
    .then((res) => {
      if (res.data.code === 0 && res.data.data) {
        dataList.value = res.data.data.records ?? []
        total.value = res.data.data.total ?? 0
      }
    })
    .catch(() => {
      message.error('获取信息失败')
    })
}
const getPageUser = (page) => {
  userSearchParam.current = page.current
  userSearchParam.pageSize = page.pageSize
  fetchData()
}

const doDelete = (id: string) => {
  //这里可以简写 id:id ->id
  const userDeleteRequest: API.UserDeleteRequest = { id }
  deleteBtIdUsingPost(userDeleteRequest)
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
const doSearch = () => {
  //一定要重制页码
  userSearchParam.current = 1
  fetchData()
}
//页面加载时候获得请求数据
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div id="UserManagePage">
    <!--表单-->
    <a-form layout="inline" :model="userSearchParam" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="userSearchParam.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="userSearchParam.userName" placeholder="输入用户名" />
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
      @change="getPageUser"
      bordered
    >
      <!-- ✅ 让 userAccount 这一列的数据正确渲染 -->
      <template #bodyCell="{ column, record }">
        <div>
          <a-input
            v-if="
              !unEditableColumns.includes(column.dataIndex) &&
              editableData[record.id] &&
              column.dataIndex != 'action'
            "
            v-model:value="record[column.dataIndex]"
            style="margin: -5px 0"
          />
          <template v-else-if="column.dataIndex === 'id'">
            <span>{{ record.id }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'userAccount'">
            <span>{{ record.userAccount }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'userName'">
            <span>{{ record.userName }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'userProfile'">
            <span>{{ record.userProfile }}</span>
          </template>
        </div>

        <!-- 头像列 -->
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" width="50px" height="50px" />
        </template>

        <!-- 用户角色 -->
        <template v-else-if="column.dataIndex === 'userRole'">
          <a-tag :color="record.userRole === 'admin' ? 'green' : 'blue'">
            {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
          </a-tag>
        </template>

        <!-- 创建时间 -->
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>

        <!-- 操作列 -->
        <template v-else-if="column.dataIndex === 'action'">
          <div class="editable-row-operations" id="operation-area">
            <span v-if="editableData[record.id]">
              <a-popconfirm title="确定保存么" @confirm="save(record)">
                <a-button type="primary" id="save-btn">保存</a-button>
              </a-popconfirm>
              <a-button type="dashed" danger @click="cancel(record.id)" id="cancel-btn"
                >取消</a-button>
            </span>
            <span v-else>
              <a-button type="primary" @click="edit(record.id)" id="edit-btn">编辑</a-button>
                <a-popconfirm title="真的要删除么" @confirm="doDelete(record.id)">
              <a-button danger id="delete-btn">删除</a-button>
                </a-popconfirm>
            </span>
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped>
#UserManagePage .editable-row-operations a {
  margin-right: 8px;
}
#UserManagePage #save-btn {
  display: inline;
}
#UserManagePage #cancel-btn {
  display: inline;
  margin-left: 4px;
}

#UserManagePage #edit-btn {
  display: inline;
}
#UserManagePage #delete-btn {
  display: inline;
  margin-left: 4px;
}
</style>
