<template>
  <div class="home-view">
    <div class="searchInput">
      <a-input-search
        v-model:value="queryParams.searchText"
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
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :pagination="pagination"
      :loading="loading"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-card hoverable>
            <template #cover>
              <img
                alt="example"
                :src="item.thumbnailUrl"
                style="height: 180px; object-fit: cover"
                @click="doClickPicture(item.id)"
              />
            </template>
            <a-card-meta :title="item.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ item.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in item.tags" :key="tag">{{ tag }}</a-tag>
                </a-flex>
              </template>
            </a-card-meta>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, computed } from 'vue';
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';

const loading = ref(false);
const dataList = ref<API.PictureVO[]>([]);
const queryParams = ref<API.PictureQueryRequest>({});

// 图片总数
const total = ref(0);

// 自定义分页器 使用computed动态变化
const pagination = computed(() => {
  return {
    current: queryParams.value.current,
    pageSize: queryParams.value.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共${total}条`, // ✅ 修正反引号
    onChange: (page: number, pageSize: number) => {
      getPagePicture({ current: page, pageSize });
    },
  };
});

// 标签和分类列表
const categoryList = ref<string[]>([]);
const tagList = ref<string[]>([]);

const selectedCategory = ref<string>('all');
const selectedTagList = ref<boolean[]>([]); // ✅ 确保是布尔数组

const router = useRouter();

// 跳转到图片详情页
const doClickPicture = (id: bigint) => {
  router.push(`/pic/edit/${id}`); // ✅ 修正反引号
};

const fetchData = async () => {
  loading.value = true;
  // 转换搜索参数
  const params: API.PictureQueryRequest = {
    ...queryParams.value,
    tags: [] as string[],
  };
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value;
  }
  // 标签列表
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags?.push(tagList.value[index]);
    }
  });

  console.log({...params});

  await listPictureVoByPageUsingPost({ current: 1, pageSize: 10, ...params })
    .then((res) => {
      if (res.data.code === 0 && res.data.data) {
        dataList.value = res.data.data.records ?? [];
        total.value = res.data.data.total ?? 0;
      }
    })
    .catch(() => {
      message.error('获取信息失败');
    });
  loading.value = false;
};

/**
 * 获取标签和分类选项
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet();
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    tagList.value = res.data.data.tagList ?? [];
    categoryList.value = res.data.data.categoryList ?? [];
  } else {
    message.error('获取标签分类列表失败: ' + res.data.message);
  }
};

const getPagePicture = (page: { current: number; pageSize: number }) => {
  queryParams.value.current = page.current;
  queryParams.value.pageSize = page.pageSize;
  fetchData();
};

const onSearch = () => {
  fetchData();
};

onMounted(() => {
  fetchData();
  getTagCategoryOptions();
});
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
