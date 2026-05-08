<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { parseDocument, uploadDocument } from '../api/document'

const router = useRouter()
const uploading = ref(false)
const latestUpload = ref(null)

async function submitUpload(option) {
  uploading.value = true
  latestUpload.value = null
  try {
    const uploadResult = await uploadDocument(option.file)
    latestUpload.value = uploadResult
    await parseDocument(uploadResult.id)
    ElMessage.success('上传成功，已触发解析')
    option.onSuccess(uploadResult)
  } catch (error) {
    option.onError(error)
  } finally {
    uploading.value = false
  }
}

function handleExceed() {
  ElMessage.warning('一次只能上传一个文件')
}
</script>

<template>
  <div class="page-grid single-column">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>上传知识库文档</span>
        </div>
      </template>

      <el-upload
        drag
        :show-file-list="false"
        :http-request="submitUpload"
        :limit="1"
        :on-exceed="handleExceed"
        class="upload-panel"
      >
        <div class="upload-title">拖拽文件到此处，或点击上传</div>
        <div class="upload-tip">支持 pdf / docx / txt / md，上传后会自动触发解析。</div>
      </el-upload>

      <el-alert
        v-if="latestUpload"
        class="mt-16"
        type="success"
        :closable="false"
        show-icon
        :title="`最近上传：${latestUpload.fileName}`"
        :description="`类型：${latestUpload.fileType}，当前状态：${latestUpload.parseStatus}`"
      />

      <div class="actions mt-16">
        <el-button type="primary" :loading="uploading">{{ uploading ? '上传中...' : '等待上传' }}</el-button>
        <el-button @click="router.push('/documents')">查看文档列表</el-button>
      </div>
    </el-card>
  </div>
</template>
