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
    ElMessage.success('上传成功，正在解析并向量化')
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
  <div class="doc-page">
    <div class="doc-page-header">
      <h2>文档上传</h2>
      <p>上传公司内部文档，系统将自动解析并向量化，用于知识库问答</p>
    </div>

    <div class="doc-card" style="max-width: 680px">
      <el-upload
        drag
        :show-file-list="false"
        :http-request="submitUpload"
        :limit="1"
        :on-exceed="handleExceed"
        :disabled="uploading"
        accept=".pdf,.docx,.txt,.md"
      >
        <div class="upload-zone">
          <div class="upload-icon">📁</div>
          <p class="upload-title">{{ uploading ? '上传中，请稍候...' : '拖拽文件到此处，或点击选择' }}</p>
          <p class="upload-tip">支持的格式：</p>
          <div class="upload-formats">
            <span class="format-tag">PDF</span>
            <span class="format-tag">DOCX</span>
            <span class="format-tag">TXT</span>
            <span class="format-tag">MD</span>
          </div>
        </div>
      </el-upload>

      <div v-if="latestUpload" class="upload-result">
        <div class="upload-result-title">✅ 上传成功，正在解析并向量化</div>
        <div class="upload-result-detail">
          文件：{{ latestUpload.fileName }} · 类型：{{ latestUpload.fileType }}
        </div>
      </div>

      <div class="upload-actions">
        <el-button
          type="primary"
          :loading="uploading"
          :disabled="uploading"
        >
          {{ uploading ? '上传中...' : '选择文件上传' }}
        </el-button>
        <el-button v-if="latestUpload" type="success" @click="router.push('/chat')">
          上传后去问答 →
        </el-button>
        <el-button @click="router.push('/documents')">查看文档列表</el-button>
      </div>
    </div>
  </div>
</template>
