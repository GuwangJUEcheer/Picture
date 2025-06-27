<template>
  <div class="picture-upload">
    <a-upload
      :show-upload-list="false"
      list-type="picture-card"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"
    >
      <img v-if="props.picture" :src="props.picture?.url" alt="avatar" />
      <div v-else>
        <loading-outlined v-if="loading"></loading-outlined>
        <plus-outlined v-else></plus-outlined>
        <div class="ant-upload-text">点击或者拖拽上传图片</div>
      </div>
    </a-upload>
  </div>
</template>
<script lang="ts" setup>
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message, type UploadProps } from 'ant-design-vue'
import { defineProps, ref } from 'vue'
import { uploadPictureUsingPost } from '@/api/pictureController'
interface Props {
  picture:API.PictureVO,
  onSuccess?: (newPicture: API.PictureVO) => void
  spaceId?: number
}
const props = defineProps<Props>();

const loading = ref<boolean>(false)

/**
 * 上传前的校验
 * @param file
 */
const beforeUpload = (file:UploadProps['fileList'][number]) => {
  //校验图片格式
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('只支持jpg/png')
  }
  //校验图片大小
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('不能上传超过2MB的图片')
  }
  return isJpgOrPng && isLt2M
}

/**
 *
 * 上传文件
 * @param file
 */
const handleUpload = async ({file}:any) => {
  loading.value = true;
  let id:bigint = 0n;
  if(props.picture?.id){
     id = BigInt(props.picture?.id)
  }
  try{
    const res = await uploadPictureUsingPost({id,spaceId:props.spaceId},{},file)
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
.picture-upload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-height: 152px;
  min-width: 152px;
}

.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}

.picture-upload img{
  max-width: 100%;
  max-height: 480px !important;
}
</style>
