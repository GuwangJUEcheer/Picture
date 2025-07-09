<script setup lang="ts">
import VChart from 'vue-echarts'
import 'echarts'
import { watchEffect } from 'vue'
import { message } from 'ant-design-vue'

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

// 图表数据
const dataList = ref<API.SpaceUserAnalyzeResponse[]>([])
const loading = ref(true)

const options = computed(() => {
  const periods = dataList.value.map((item) => item.period) // 时间区间
  const counts = dataList.value.map((item) => item.count) // 上传数量

  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: periods, name: '时间区间' },
    yAxis: { type: 'value', name: '上传数量' },
    series: [
      {
        name: '上传数量',
        type: 'line',
        data: counts,
        smooth: true, // 平滑折线
        emphasis: {
          focus: 'series',
        },
      },
    ],
  }
})

/**
 * 加载数据
 */
const fetchData = async () => {
  loading.value = true
  const res = await getSpaceUserAnalyzeUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId,
    timeDimension: timeDimension.value,
    userId: userId.value,
  })
  if (res.data.code === 0) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}



/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  fetchData()
})
</script>

<template>
  <div class="space-size-analyze">
      <div class="space-user-analyze">
        <a-card title="空间图片用户分析">
          <v-chart :option="options" style="height: 320px; max-width: 100%" :loading="loading" />
        </a-card>
        <a-card title="用户上传分析">
          <v-chart :option="options" style="height: 320px" />
          <template #extra>
            <a-space>
              <a-segmented v-model:value="timeDimension" :options="timeDimensionOptions" />
              <a-input-search placeholder="请输入用户 id" enter-button="搜索用户" @search="doSearch" />
            </a-space>
          </template>
        </a-card>
      </div>
  </div>
</template>

<style scoped></style>
