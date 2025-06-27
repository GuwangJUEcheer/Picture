<template>
  <div class="url-picture-upload">
    <a-input-group compact>
      <a-input v-model:value="fileUrl" style="width: calc(100% - 120px)" placeholder="请输入图片地址"/>
      <a-button type="primary" style="width:120px" :loading = "loading" @click="handleUpload">提交</a-button>
    </a-input-group>
    <div class="upload-wrapper">
    <img v-if="props.picture" :src="props.picture?.url" alt="avatar" />
    </div>
  </div>
</template>
<script lang="ts" setup>
import { message } from 'ant-design-vue'
import { defineProps, ref } from 'vue'
import { uploadPictureByUrlUsingPost } from '@/api/pictureController'
interface Props {
  picture:API.PictureVO,
  onSuccess?: (newPicture: API.PictureVO) => void
  spaceId?: number
}
const props = defineProps<Props>();

const fileUrl = ref<string>();

/**
 * 防止重复提交
 */
const loading = ref<boolean>(false)

/**
 *
 * 上传文件
 * @param file
 */
const handleUpload = async () => {
  loading.value = true;
  let id:bigint = 0n;
  if(props.picture?.id){
     id = BigInt(props.picture?.id)
  }
  const params:API.PictureUploadRequest = {url:fileUrl.value,id:id.toString(),spaceId:props.spaceId};
  try{
    const res = await uploadPictureByUrlUsingPost(params);
    if(res.data.code === 0 && res.data.data){
      message.success("上传成功");
       id = BigInt(res.data.data.id);
      props.onSuccess?.(res.data.data);
    }else{
      message.error("图片上传失败" + res.data.message);
    }
  }catch (error) {
    message.error("图片上传失败" +error.message);
  }
  loading.value = false;
}

</script>
<style scoped>
.url-picture-upload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-height: 152px;
  min-width: 152px;
}

.url-picture-upload img{
  max-width: 100%;
  max-height: 480px !important;
}
.url-picture-upload .upload-wrapper {
  text-align: center;
  margin-bottom: 16px;
}
</style>
